"""
Thin HTTP client for the Spring Boot backend's payment endpoints, used to
answer a customer's questions about their OWN transactions.

There is no per-account backend endpoint (GET /payments/{account}), so —
matching the same pattern already used by the frontend
(PaymentHistory.jsx: fetch GET /payments, filter client-side by account) —
this fetches all payments and filters down to the given account number.
"""
import os
import requests

BACKEND_API_URL = os.environ.get("BACKEND_API_URL", "http://localhost:8080")
REQUEST_TIMEOUT_SECONDS = 5


class TransactionsUnavailableError(Exception):
    """Raised when the backend payments endpoint can't be reached or returns an error."""
    pass


def get_payments_for_account(account_number: str) -> list[dict]:
    """
    Returns the payments where `account_number` is either the source or
    destination account, most-recent first.
    """
    try:
        response = requests.get(
            f"{BACKEND_API_URL}/payments",
            timeout=REQUEST_TIMEOUT_SECONDS,
        )
        response.raise_for_status()
        payments = response.json()
    except requests.exceptions.RequestException as e:
        raise TransactionsUnavailableError(f"Could not fetch payments: {e}") from e

    matches = [
        p for p in payments
        if p.get("sourceAccount") == account_number or p.get("destinationAccount") == account_number
    ]
    matches.sort(key=lambda p: p.get("createdAt") or "", reverse=True)
    return matches
