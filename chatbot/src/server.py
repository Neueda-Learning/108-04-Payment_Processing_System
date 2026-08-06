from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
from retrieval import retrieve
from prompt import build_prompt
from llm import ask_gemini, LLMUnavailableError
from stats_agent import detect_stats_intent, compute_fact, template_answer, build_stats_prompt
from stats_client import get_dashboard_stats, StatsUnavailableError

app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str


class ChatResponse(BaseModel):
    reply: str
    source: str  # "llm" or "fallback" or "no_match" — useful for debugging/demo


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest):
    # Stats questions ("most used currency", "best time to pay", ...) are
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