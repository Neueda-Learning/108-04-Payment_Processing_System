import re

from fastapi import FastAPI
from pydantic import BaseModel


from fastapi.middleware.cors import CORSMiddleware
from retrieval import retrieve
from prompt import build_prompt, build_general_prompt
from llm import ask_gemini, LLMUnavailableError

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
    source: str  # "small_talk" or "llm" or "llm_general" or "fallback" or "no_match"


# Fast, deterministic replies for common greetings/small talk so the bot
# feels conversational even without calling the LLM. Anything that doesn't
# match one of these patterns falls through to FAQ retrieval / the LLM.
_SMALL_TALK_PATTERNS = [
    (re.compile(r"^(hi|hello|hey|yo|sup|hiya|howdy|good morning|good afternoon|good evening)[\s!.,]*$"),
     "Hello! I'm the FlashPay support assistant. Ask me about payments, statuses, or how the system works, and I'll do my best to help."),
    (re.compile(r"^(bye|goodbye|see you|see ya|later|take care)[\s!.,]*$"),
     "Goodbye! Feel free to come back anytime you have a question about payments."),
    (re.compile(r"^(thanks|thank you|thx|appreciate it|cheers)[\s!.,]*$"),
     "You're welcome! Let me know if there's anything else I can help with."),
    (re.compile(r"^(how are you|how'?s it going|how are things|what'?s up|whats up)[\s?!.,]*$"),
     "I'm doing well, thanks for asking! How can I help you with payments today?"),
    (re.compile(r"^(what can you do|who are you|what do you do|help)[\s?!.,]*$"),
     "I'm a support assistant for the payment processing system. I can answer questions about payment statuses, the payment lifecycle, idempotency, errors, and more. What would you like to know?"),
]


def _match_small_talk(message: str) -> str | None:
    normalized = message.strip().lower()
    for pattern, reply in _SMALL_TALK_PATTERNS:
        if pattern.match(normalized):
            return reply
    return None


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest):
    small_talk_reply = _match_small_talk(req.message)
    if small_talk_reply:
        return ChatResponse(reply=small_talk_reply, source="small_talk")

    chunks = retrieve(req.message)

    if chunks:
        prompt = build_prompt(req.message, chunks)
        try:
            reply = ask_gemini(prompt)
            return ChatResponse(reply=reply, source="llm")
        except LLMUnavailableError:
            # Fallback: return the best-matched FAQ's raw content directly.
            # Less conversational, but still a correct, grounded answer.
            fallback_reply = chunks[0]["content"]
            return ChatResponse(reply=fallback_reply, source="fallback")

    # No FAQ match — still try to be helpful for generic/small-talk style
    # messages instead of immediately giving up.
    general_prompt = build_general_prompt(req.message)
    try:
        reply = ask_gemini(general_prompt)
        return ChatResponse(reply=reply, source="llm_general")
    except LLMUnavailableError:
        return ChatResponse(
            reply="I don't have information on that. Please contact support for help.",
            source="no_match",
        )