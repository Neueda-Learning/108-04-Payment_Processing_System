import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useLocation, useNavigate } from "react-router-dom";

const STAGES = ["CREATED", "VALIDATED", "SENT", "COMPLETED"];

const normalizeStatus = (s) => String(s || "").toUpperCase();

function PaymentProgressPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const paymentRequest = location.state?.paymentRequest;

  const [paymentStatus, setPaymentStatus] = useState("CONNECTING");
  const [paymentId, setPaymentId] = useState(null);
  const [socketMessage, setSocketMessage] = useState("Connecting to server...");
  const [done, setDone] = useState(false);
  const [success, setSuccess] = useState(false);
  const clientRef = useRef(null);
  const terminalRef = useRef(false);

  useEffect(() => {
    if (!paymentRequest) {
      navigate("/payments", { replace: true });
      return;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws/payments"),
      reconnectDelay: 0,
      onConnect: async () => {
        setSocketMessage("Connected. Sending payment...");

        // Subscribe BEFORE posting so we catch every event
        client.subscribe(`/topic/payment/${paymentRequest.idempotencyKey}`, (message) => {
          const event = JSON.parse(message.body);
          const status = normalizeStatus(event.status);

          if (terminalRef.current) return;

          if (status === "CREATED") {
            setPaymentId(event.paymentId ?? null);
          }

          setPaymentStatus(status);
          setSocketMessage(`Payment ${status.toLowerCase()}.`);

          if (status === "COMPLETED") {
            terminalRef.current = true;
            setSuccess(true);
            setDone(true);
            client.deactivate();
          } else if (status === "FAILED") {
            terminalRef.current = true;
            setSuccess(false);
            setDone(true);
            client.deactivate();
          }
        });

        // Now POST — all events will be caught above
        try {
          await axios.post("http://localhost:8080/payments", paymentRequest);
        } catch (error) {
          if (!terminalRef.current) {
            terminalRef.current = true;
            setSocketMessage("Payment request failed.");
            setSuccess(false);
            setDone(true);
            client.deactivate();
          }
        }
      },
      onStompError: () => {
        setSocketMessage("Connection error.");
        setSuccess(false);
        setDone(true);
      },
    });

    clientRef.current = client;
    client.activate();

    return () => clientRef.current?.deactivate();
  }, []);

  // Auto-navigate back after done
  useEffect(() => {
    if (!done) return;
    const t = setTimeout(() => navigate("/payments", { replace: true }), 3000);
    return () => clearTimeout(t);
  }, [done, navigate]);

  const currentIndex = Math.max(STAGES.indexOf(paymentStatus), 0);
  const progressPercent = paymentStatus === "FAILED"
    ? 100
    : (currentIndex / (STAGES.length - 1)) * 100;

  const isFailed = paymentStatus === "FAILED";
  const isCompleted = paymentStatus === "COMPLETED";

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Success / Failure overlay */}
      {done && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className={`mx-4 w-full max-w-sm rounded-2xl p-8 text-center shadow-2xl bg-white ${success ? "border-t-4 border-green-500" : "border-t-4 border-red-600"}`}>
            <div className={`mx-auto flex h-16 w-16 items-center justify-center rounded-full text-3xl font-bold ${success ? "bg-green-100 text-green-600" : "bg-red-100 text-red-600"}`}>
              {success ? "✓" : "✗"}
            </div>
            <h2 className={`mt-5 text-xl font-bold ${success ? "text-green-700" : "text-red-700"}`}>
              {success ? "Payment Successful!" : "Payment Failed"}
            </h2>
            <p className="mt-1 text-sm text-gray-500">Returning to payments in 3 seconds...</p>
            <button
              onClick={() => navigate("/payments", { replace: true })}
              className={`mt-5 w-full rounded-xl py-3 font-semibold text-white transition ${success ? "bg-green-500 hover:bg-green-600" : "bg-red-600 hover:bg-red-700"}`}
            >
              Go Now
            </button>
          </div>
        </div>
      )}

      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-5 shadow-sm">
        <div className="mx-auto max-w-4xl flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold uppercase tracking-widest text-red-600">FlashPay</p>
            <h1 className="text-xl font-bold text-gray-900 mt-0.5">Payment in Progress</h1>
          </div>
          <span className={`rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-wide ${
            isFailed ? "bg-red-100 text-red-700"
            : isCompleted ? "bg-green-100 text-green-700"
            : "bg-amber-100 text-amber-700"
          }`}>
            {paymentStatus}
          </span>
        </div>
      </div>

      <div className="mx-auto max-w-4xl px-6 py-8 space-y-6">

        {/* Wide Razorpay-style stepper */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-8">

          {/* Steps row */}
          <div className="relative flex items-start justify-between">
            {/* Background track */}
            <div className="absolute top-5 left-[2.5rem] right-[2.5rem] h-1 bg-gray-200" />
            {/* Animated fill */}
            <div
              className={`absolute top-5 left-[2.5rem] h-1 transition-all duration-700 ${isFailed ? "bg-red-500" : "bg-red-600"}`}
              style={{ width: `calc(${progressPercent}% * (100% - 5rem) / 100)` }}
            />

            {STAGES.map((stage, index) => {
              const isComplete = index < currentIndex && !isFailed;
              const isActive = index === currentIndex;
              return (
                <div key={stage} className="relative z-10 flex flex-col items-center flex-1">
                  <div className={`flex h-10 w-10 items-center justify-center rounded-full border-2 text-sm font-bold transition-all duration-500 ${
                    isFailed && isActive
                      ? "border-red-600 bg-red-600 text-white scale-110 shadow-md"
                      : isComplete
                      ? "border-red-600 bg-red-600 text-white"
                      : isActive
                      ? "border-red-600 bg-white text-red-600 scale-110 shadow-md ring-4 ring-red-100"
                      : "border-gray-300 bg-white text-gray-400"
                  }`}>
                    {isComplete ? (
                      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                      </svg>
                    ) : index + 1}
                  </div>
                  <p className={`mt-3 text-xs font-bold uppercase tracking-wide ${
                    isFailed && isActive ? "text-red-600"
                    : isComplete || isActive ? "text-gray-900"
                    : "text-gray-400"
                  }`}>{stage}</p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {["Submitted","Verified","Transferred","Confirmed"][index]}
                  </p>
                </div>
              );
            })}
          </div>

          {/* Status bar */}
          <div className={`mt-8 flex items-center gap-2 rounded-xl px-4 py-3 text-sm font-medium ${
            isFailed ? "bg-red-50 text-red-700 border border-red-200"
            : isCompleted ? "bg-green-50 text-green-700 border border-green-200"
            : "bg-red-50 text-red-600 border border-red-100"
          }`}>
            <span className={`inline-block h-2 w-2 rounded-full flex-shrink-0 ${isFailed ? "bg-red-500" : isCompleted ? "bg-green-500" : "bg-red-500 animate-pulse"}`} />
            {socketMessage}
          </div>
        </div>

        {/* Payment details */}
        <div className="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
          <h2 className="text-xs font-bold uppercase tracking-widest text-red-600 mb-5">Payment Details</h2>
          <div className="grid grid-cols-3 gap-6 pb-6 border-b border-gray-100">
            <div>
              <p className="text-xs text-gray-500">Amount</p>
              <p className="mt-1 text-2xl font-bold text-gray-900">
                {paymentRequest?.currency || ""} {paymentRequest?.amount || "0"}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500">Destination</p>
              <p className="mt-1 text-sm font-semibold text-gray-900 break-all">{paymentRequest?.destinationAccount || "N/A"}</p>
            </div>
            <div>
              <p className="text-xs text-gray-500">Summary</p>
              <p className="mt-1 text-sm text-gray-700">{paymentRequest?.description || "No summary"}</p>
            </div>
          </div>
          <button
            type="button"
            onClick={() => navigate("/payments")}
            className="mt-5 w-full rounded-xl border border-red-600 py-3 text-sm font-semibold text-red-600 transition hover:bg-red-600 hover:text-white"
          >
            ← Back to Payments
          </button>
        </div>

      </div>
    </div>
  );
}

export default PaymentProgressPage;
