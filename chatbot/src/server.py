from fastapi import FastAPI
from pydantic import BaseModel

from retrieval import retrieve
from prompt import build_prompt
from llm import ask_gemini, LLMUnavailableError

app = FastAPI()


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