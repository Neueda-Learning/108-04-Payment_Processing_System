def build_prompt(query: str, chunks: list) -> str:
    context = "\n\n".join(f"Q: {c['question']}\nA: {c['content']}" for c in chunks)
    return f"""You are a support assistant for a payment processing system.
Answer the user's question using ONLY the context below. Be concise and direct.
If the context doesn't fully answer the question, say you don't have that
information and suggest checking with support — do not guess or make up details.

Context:
{context}

User question: {query}

Answer:"""