"""
Personal transaction Q&A agent. Handles a customer asking about THEIR OWN
payments (e.g. "what's the status of my last payment?", "did my transfer
go through?", "how many payments have I made?").

This is intentionally separate from stats_agent.py (platform-wide,
aggregate data) — this module only ever returns data scoped to the single
account number the caller supplies, and never row-level data belonging to
anyone else.

Flow mirrors stats_agent.py:
  1. detect_transaction_intent(message) - cheap keyword match, no embeddings.
     Only matches "my"/"I" phrasing, so it doesn't collide with the
     platform-wide stats intents in stats_agent.py.
  2. compute_fact(intent, payments) - deterministic Python computation over
     the caller's own payment list, so numbers/statuses are never
     hallucinated.
  3. build_transaction_prompt(...) - grounds the LLM's phrasing in the
     already-computed fact (LLM only phrases it, never computes it).
  4. template_answer(...) - plain-string fallback used when the LLM is
     unavailable.

IMPORTANT: the `account_number` used to fetch payments must come from the
caller's own logged-in session (see server.py), never parsed out of the
chat message text itself.
"""
import re
from typing import Optional

TRANSACTION_INTENT_PATTERNS = {
    "LAST_PAYMENT_STATUS": re.compile(
        r"(status of|how('?s| is)) my (last|latest|most recent|recent) (payment|transfer|transaction)"
        r"|did my (payment|transfer|transaction) go through"
        r"|has my (payment|transfer|transaction) (gone through|been sent|completed)",
        re.I,
    ),
    "RECENT_TRANSACTIONS": re.compile(
        r"my (recent|last|latest) (payments|transfers|transactions)"
        r"|my (payment|transfer|transaction) history"
        r"|(show|list) my (payments|transfers|transactions)",
        re.I,
    ),
    "PAYMENT_COUNT": re.compile(
        r"how many (payments|transfers|transactions) (have i|did i) (made|sent|do i have)",
        re.I,
    ),
    "FAILED_PAYMENTS": re.compile(
        r"(my|did any of my) (payments?|transfers?|transactions?).*(fail|failed|declined)"
        r"|why did my (payment|transfer|transaction) fail",
        re.I,
    ),
}


def detect_transaction_intent(message: str) -> Optional[str]:
    """Returns the matched intent key, or None if the message isn't a personal-transaction question."""
    for intent, pattern in TRANSACTION_INTENT_PATTERNS.items():
        if pattern.search(message):
            return intent
    return None


def compute_fact(intent: str, payments: list[dict]) -> Optional[dict]:
    """
    Deterministically derives the answer to `intent` from the caller's own
    (already account-filtered) payment list. Returns None if there isn't
    enough data to answer.
    """
    if not payments and intent != "PAYMENT_COUNT":
        return None

    if intent == "LAST_PAYMENT_STATUS":
        last = payments[0]
        return {
            "status": last.get("status"),
            "amount": last.get("amount"),
            "currency": last.get("currency"),
            "sourceAccount": last.get("sourceAccount"),
            "destinationAccount": last.get("destinationAccount"),
            "createdAt": last.get("createdAt"),
            "errorCode": last.get("errorCode"),
        }

    if intent == "RECENT_TRANSACTIONS":
        recent = payments[:5]
        return {
            "count": len(recent),
            "payments": [
                {
                    "status": p.get("status"),
                    "amount": p.get("amount"),
                    "currency": p.get("currency"),
                    "destinationAccount": p.get("destinationAccount"),
                    "createdAt": p.get("createdAt"),
                }
                for p in recent
            ],
        }

    if intent == "PAYMENT_COUNT":
        return {"count": len(payments)}

    if intent == "FAILED_PAYMENTS":
        failed = [p for p in payments if (p.get("status") or "").upper() == "FAILED"]
        if not failed:
            return {"count": 0}
        latest = failed[0]
        return {
            "count": len(failed),
            "latestErrorCode": latest.get("errorCode"),
            "latestAmount": latest.get("amount"),
            "latestCurrency": latest.get("currency"),
        }

    return None


def template_answer(intent: str, facts: dict) -> str:
    """Plain-string answer used when the LLM is unavailable (no phrasing needed)."""
    if intent == "LAST_PAYMENT_STATUS":
        base = (f"Your last payment ({facts['amount']} {facts['currency']} to "
                f"{facts['destinationAccount']}) is currently {facts['status']}.")
        if facts.get("errorCode"):
            base += f" Error code: {facts['errorCode']}."
        return base
    if intent == "RECENT_TRANSACTIONS":
        if facts["count"] == 0:
            return "You don't have any transactions yet."
        lines = [
            f"- {p['amount']} {p['currency']} to {p['destinationAccount']}: {p['status']}"
            for p in facts["payments"]
        ]
        return "Here are your most recent transactions:\n" + "\n".join(lines)
    if intent == "PAYMENT_COUNT":
        return f"You have made {facts['count']} payment(s) in total."
    if intent == "FAILED_PAYMENTS":
        if facts["count"] == 0:
            return "None of your payments have failed."
        return (f"You have {facts['count']} failed payment(s). The most recent failure "
                f"({facts['latestAmount']} {facts['latestCurrency']}) had error code "
                f"{facts['latestErrorCode']}.")
    return "I don't have enough data to answer that yet."


def build_transaction_prompt(query: str, intent: str, facts: dict, account_number: str) -> str:
    return f"""You are a support assistant for a payment processing system.
Answer the user's question about their OWN transactions using ONLY the data
below, which belongs to account {account_number}. Be concise and direct.
Do not invent or adjust any statuses, amounts, or account numbers — use them
exactly as given. Never mention or imply data belonging to any other account.

Account: {account_number}
Computed data ({intent}): {facts}

User question: {query}

Answer:"""
