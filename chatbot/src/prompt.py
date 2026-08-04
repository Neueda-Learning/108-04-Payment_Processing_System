def build_prompt(query: str, chunks: list) -> str:
    context = "\n\n".join(f"Q: {c['question']}\nA: {c['content']}" for c in chunks)
    return f"""You are a support assistant for a payment processing system.
Answer the user's question using ONLY the context below. You are allowed to answer generic questions and return greetings. Be concise and direct.
If the context doesn't fully answer the question, say you don't have that
information and suggest checking with support — do not guess or make up details.

Context:
{context}

User question: {query}

Answer:"""


def build_general_prompt(query: str) -> str:
    """
    Used when no FAQ content matches the query. Lets the assistant handle
    greetings, small talk, and generic conversation naturally, while still
    refusing to invent specific facts about the payment system.
    """
    return f"""You are a friendly support assistant for a payment processing system called FlashPay.
The user's message did not match any specific FAQ content on file.

- If it's a greeting, small talk, or general conversation, reply warmly and briefly, like a helpful human would.
- If it's a question that requires specific knowledge about the payment system that you don't have, say you don't have that information and suggest contacting support — do not guess or make up details.
- Keep replies short and conversational.

User message: {query}

Answer:"""