"""
Analytics Q&A agent. Handles questions about live payment statistics
(e.g. "what's the most used currency?", "when's the best time to pay?")
that FAQ retrieval (retrieval.py) can never answer, since that data
doesn't exist in the static FAQ corpus.

Flow:
  1. detect_stats_intent(message) - cheap keyword match, no embeddings.
  2. compute_fact(intent, stats)  - deterministic Python computation over
     the JSON returned by stats_client, so numbers are never hallucinated.
  3. build_stats_prompt(...)      - grounds the LLM's phrasing in the
     already-computed fact (LLM only phrases it, never computes it).
  4. template_answer(...)         - plain-string fallback used when the
     LLM is unavailable (mirrors server.py's existing FAQ fallback).
"""
import re
from typing import Optional

STATS_INTENT_PATTERNS = {
    "MOST_USED_CURRENCY": re.compile(r"most used currency|popular currency|top currency", re.I),
    "BEST_TIME_TO_PAY": re.compile(r"best time to pay|quietest time|least busy|off.?peak|best hour", re.I),
    "BUSIEST_TIME": re.compile(r"busiest|peak hour|peak time|most busy", re.I),
    "SUCCESS_RATE": re.compile(r"success rate|failure rate|how many.*(fail|succeed)", re.I),
    "TOP_SENDER": re.compile(r"top sender|who sends the most|most active sender", re.I),
    "TOP_RECEIVER": re.compile(r"top receiver|who receives the most|most active receiver", re.I),
    "FAILURE_REASON": re.compile(r"why.*(fail|declined)|(common|top).*(failure|error)", re.I),
    "AVG_PROCESSING_TIME": re.compile(r"how long.*(take|process)|average.*(processing|time)", re.I),
}


def detect_stats_intent(message: str) -> Optional[str]:
    """Returns the matched intent key, or None if the message isn't a stats question."""
    for intent, pattern in STATS_INTENT_PATTERNS.items():
        if pattern.search(message):
            return intent
    return None


def _empty(stats: dict, key: str) -> bool:
    return not stats.get(key)


def compute_fact(intent: str, stats: dict) -> Optional[dict]:
    """
    Deterministically derives the answer to `intent` from the raw
    /stats/dashboard JSON. Returns None if there isn't enough data.
    """
    if intent == "MOST_USED_CURRENCY":
        if _empty(stats, "currencyBreakdown"):
            return None
        top = max(stats["currencyBreakdown"], key=lambda c: c["count"])
        return {"currency": top["currency"], "count": top["count"], "totalAmount": top["totalAmount"]}

    if intent == "BEST_TIME_TO_PAY":
        if _empty(stats, "volumeByHour"):
            return None
        quietest = min(stats["volumeByHour"], key=lambda h: h["count"])
        return {"hour": quietest["hour"], "count": quietest["count"]}

    if intent == "BUSIEST_TIME":
        if _empty(stats, "volumeByHour"):
            return None
        busiest = max(stats["volumeByHour"], key=lambda h: h["count"])
        return {"hour": busiest["hour"], "count": busiest["count"]}

    if intent == "SUCCESS_RATE":
        rates = stats.get("successRateOverTime") or []
        if not rates:
            return None
        avg_rate = sum(r["successRate"] for r in rates) / len(rates)
        return {"avgSuccessRate": round(avg_rate, 1), "days": len(rates)}

    if intent == "TOP_SENDER":
        if _empty(stats, "topSenders"):
            return None
        top = stats["topSenders"][0]
        return {"account": top["accountNumber"], "count": top["count"], "totalAmount": top["totalAmount"]}

    if intent == "TOP_RECEIVER":
        if _empty(stats, "topReceivers"):
            return None
        top = stats["topReceivers"][0]
        return {"account": top["accountNumber"], "count": top["count"], "totalAmount": top["totalAmount"]}

    if intent == "FAILURE_REASON":
        if _empty(stats, "failureReasons"):
            return None
        top = stats["failureReasons"][0]
        return {"errorCode": top["errorCode"], "count": top["count"]}

    if intent == "AVG_PROCESSING_TIME":
        seconds = stats.get("avgTotalProcessingSeconds")
        if seconds is None:
            return None
        return {"avgSeconds": round(seconds, 1)}

    return None


def template_answer(intent: str, facts: dict) -> str:
    """Plain-string answer used when the LLM is unavailable (no phrasing needed)."""
    if intent == "MOST_USED_CURRENCY":
        return (f"The most used currency is {facts['currency']}, used in {facts['count']} "
                f"payments totalling {facts['totalAmount']}.")
    if intent == "BEST_TIME_TO_PAY":
        return (f"The quietest hour is {facts['hour']}:00, with only {facts['count']} payments "
                f"historically — likely the fastest time to process a payment.")
    if intent == "BUSIEST_TIME":
        return f"The busiest hour is {facts['hour']}:00, with {facts['count']} payments historically."
    if intent == "SUCCESS_RATE":
        return f"The average success rate over the last {facts['days']} day(s) is {facts['avgSuccessRate']}%."
    if intent == "TOP_SENDER":
        return (f"The top sending account is {facts['account']}, with {facts['count']} payments "
                f"totalling {facts['totalAmount']}.")
    if intent == "TOP_RECEIVER":
        return (f"The top receiving account is {facts['account']}, with {facts['count']} payments "
                f"totalling {facts['totalAmount']}.")
    if intent == "FAILURE_REASON":
        return f"The most common failure reason is {facts['errorCode']}, accounting for {facts['count']} payment(s)."
    if intent == "AVG_PROCESSING_TIME":
        return f"On average, a payment takes {facts['avgSeconds']} seconds to fully process."
    return "I don't have enough data to answer that yet."


def build_stats_prompt(query: str, intent: str, facts: dict) -> str:
    return f"""You are a support assistant for a payment processing system.
Answer the user's question using ONLY the computed statistic below. Be concise
and direct. Do not invent or adjust any numbers — use them exactly as given.

Computed statistic ({intent}): {facts}

User question: {query}

Answer:"""
