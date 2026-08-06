import os
import re
from typing import Optional

from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from retrieval import retrieve
from prompt import build_prompt
from llm import ask_gemini, LLMUnavailableError
from stats_agent import detect_stats_intent, compute_fact, template_answer, build_stats_prompt
from stats_client import get_dashboard_stats, StatsUnavailableError
from transaction_agent import (
    detect_transaction_intent,
    compute_fact as compute_transaction_fact,
    template_answer as template_transaction_answer,
    build_transaction_prompt,
)
from transactions_client import get_payments_for_account, TransactionsUnavailableError

app = FastAPI()

# The deployed frontend's origin is always allowed. Local dev origins
# (`vite`/`vite preview` on localhost) are included by default so the chat
# widget works out of the box when running the frontend locally; extra
# origins (e.g. another dev machine's IP) can be added via the
# CORS_ALLOWED_ORIGINS env var (comma-separated) without editing code.
_DEFAULT_ALLOWED_ORIGINS = [
    "http://10.9.67.247:8081",
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "http://localhost:4173",
    "http://127.0.0.1:4173",
]
_extra_origins = [o.strip() for o in os.environ.get("CORS_ALLOWED_ORIGINS", "").split(",") if o.strip()]

app.add_middleware(
    CORSMiddleware,
    allow_origins=_DEFAULT_ALLOWED_ORIGINS + _extra_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
class ChatRequest(BaseModel):
    message: str
    # Optional: the logged-in customer's account number, so the bot can
    # answer questions about THEIR OWN transactions. Sent by the frontend
    # from the same localStorage("account") value used elsewhere in the
    # app (see PaymentHistory.jsx) — never parsed from the chat text.
    account_number: str | None = None


class ChatResponse(BaseModel):
    reply: str
    source: str  # "llm" or "fallback" or "no_match" — useful for debugging/demo


# Cheap, regex-based greeting/small-talk detection — short-circuits before
# any retrieval/tool call so "hi" never burns an embedding or LLM call.
_SMALL_TALK_PATTERNS = {
    "greeting": re.compile(r"^\s*(hi|hello|hey|good (morning|afternoon|evening))\b", re.I),
    "thanks": re.compile(r"^\s*(thanks|thank you|cheers|ty)\b", re.I),
    "farewell": re.compile(r"^\s*(bye|goodbye|see you|later)\b", re.I),
    "how_are_you": re.compile(r"how are you|how('?s| is) it going", re.I),
    "capabilities": re.compile(r"what can you (do|help)|what do you do|help me with", re.I),
}

_SMALL_TALK_REPLIES = {
    "greeting": "Hi there! I can help with FAQs, your own transactions, or platform statistics — what would you like to know?",
    "thanks": "You're welcome! Let me know if there's anything else I can help with.",
    "farewell": "Goodbye! Feel free to come back if you have more questions.",
    "how_are_you": "I'm doing well, thanks for asking! How can I help you today?",
    "capabilities": "I can answer FAQs, tell you about your own recent transactions, and share platform statistics like success rates or most-used currencies.",
}


def _match_small_talk(message: str) -> Optional[str]:
    for intent, pattern in _SMALL_TALK_PATTERNS.items():
        if pattern.search(message):
            return intent
    return None


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest):
    # 1. Greetings/small talk — answered conversationally, no retrieval needed.
    small_talk_intent = _match_small_talk(req.message)
    if small_talk_intent:
        return ChatResponse(reply=_SMALL_TALK_REPLIES[small_talk_intent], source="small_talk")

    # 2. Personal transaction questions ("my last payment", "did my transfer
    # go through?") — answered ONLY from the asking user's own data. Routed
    # before stats/FAQ since "my" phrasing is unambiguous and this data
    # doesn't exist in either of those sources.
    transaction_intent = detect_transaction_intent(req.message)
    if transaction_intent:
        if not req.account_number:
            return ChatResponse(
                reply="Please log in so I can look up your own transactions.",
                source="transaction_no_identity",
            )

        try:
            payments = get_payments_for_account(req.account_number)
        except TransactionsUnavailableError:
            return ChatResponse(
                reply="I couldn't reach the payments service just now. Please try again shortly.",
                source="transaction_unavailable",
            )

        facts = compute_transaction_fact(transaction_intent, payments)
        if facts is None:
            return ChatResponse(
                reply="I don't see any transactions on your account yet.",
                source="transaction_no_data",
            )

        try:
            reply = ask_gemini(
                build_transaction_prompt(req.message, transaction_intent, facts, req.account_number)
            )
            return ChatResponse(reply=reply, source="transaction_llm")
        except LLMUnavailableError:
            return ChatResponse(
                reply=template_transaction_answer(transaction_intent, facts),
                source="transaction_fallback",
            )

    # 3. Stats questions ("most used currency", "best time to pay", ...) are
    # answered from live payment data, not the static FAQ corpus, so they're
    # routed separately before FAQ retrieval even runs.
    stats_intent = detect_stats_intent(req.message)
    if stats_intent:
        try:
            stats = get_dashboard_stats()
        except StatsUnavailableError:
            return ChatResponse(
                reply="I couldn't reach the payment statistics service just now. Please try again shortly.",
                source="stats_unavailable",
            )

        facts = compute_fact(stats_intent, stats)
        if facts is None:
            return ChatResponse(
                reply="I don't have enough payment data yet to answer that.",
                source="stats_no_data",
            )

        try:
            reply = ask_gemini(build_stats_prompt(req.message, stats_intent, facts))
            return ChatResponse(reply=reply, source="stats_llm")
        except LLMUnavailableError:
            return ChatResponse(reply=template_answer(stats_intent, facts), source="stats_fallback")

    chunks = retrieve(req.message)

    if not chunks:
        return ChatResponse(
            reply="I don't have information on that. Please contact support for help.",
            source="no_match",
        )

    prompt = build_prompt(req.message, chunks)

    try:
        reply = ask_gemini(prompt)
        return ChatResponse(reply=reply, source="llm")
    except LLMUnavailableError:
        # Fallback: return the best-matched FAQ's raw content directly.
        # Less conversational, but still a correct, grounded answer.
        fallback_reply = chunks[0]["content"]
        return ChatResponse(reply=fallback_reply, source="fallback")