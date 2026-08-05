import { useEffect, useMemo, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useLocation, useNavigate } from "react-router-dom";

const STAGES = ["CREATED", "VALIDATED", "SENT", "COMPLETED"];
const STREAM_STAGES = ["VALIDATED", "SENT", "COMPLETED"];

const normalizeStatus = (status) => String(status || "").toUpperCase();

function PaymentProgressPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [paymentInfo, setPaymentInfo] = useState(() => {
    const stored = localStorage.getItem("flashpay-active-payment");
    return stored ? JSON.parse(stored) : null;
  });
  const [paymentStatus, setPaymentStatus] = useState(() => {
    const stateStatus = normalizeStatus(location.state?.initialStatus);
    const storedStatus = normalizeStatus(paymentInfo?.status);
    return stateStatus || storedStatus || "CREATED";
  });
  const [socketMessage, setSocketMessage] = useState("Waiting for payment updates...");
  const clientRef = useRef(null);
  const terminalStatusRef = useRef(false);
  const lastStatusRef = useRef("");

  const activePayment = useMemo(() => {
    return location.state || paymentInfo;
  }, [location.state, paymentInfo]);

  useEffect(() => {
    if (!activePayment?.paymentId) {
      navigate("/payments", { replace: true });
      return;
    }

    const paymentId = activePayment.paymentId;
    const paymentTrackingKey = activePayment.paymentTrackingKey;
    lastStatusRef.current = normalizeStatus(activePayment.status) || paymentStatus;
    const client = new Client({
      webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
      reconnectDelay: 3000,
      onConnect: () => {
        setSocketMessage("Connected. Waiting for VALIDATED, SENT and completion events...");

        const topics = Array.from(
          new Set(
            [
              `/topic/payment/${paymentId}`,
              paymentTrackingKey ? `/topic/payment/${paymentTrackingKey}` : null,
            ].filter(Boolean)
          )
        );

        topics.forEach((topic) => {
          client.subscribe(topic, (message) => {
            const event = JSON.parse(message.body);
            const nextStatus = normalizeStatus(event.status);

            if (!nextStatus || nextStatus === lastStatusRef.current) {
              return;
            }

            if (!STREAM_STAGES.includes(nextStatus) && nextStatus !== "FAILED") {
              return;
            }

            lastStatusRef.current = nextStatus;

            setPaymentInfo((current) => ({
              ...(current || activePayment),
              paymentId,
              paymentTrackingKey,
              status: nextStatus,
            }));
            localStorage.setItem(
              "flashpay-active-payment",
              JSON.stringify({
                ...(activePayment || {}),
                paymentId,
                paymentTrackingKey,
                status: nextStatus,
              })
            );

            if (nextStatus === "FAILED") {
              if (!terminalStatusRef.current) {
                terminalStatusRef.current = true;
                setPaymentStatus(nextStatus);
                setSocketMessage("Payment failed. Returning to payments...");
                client.deactivate();
                window.setTimeout(() => {
                  navigate("/payments", {
                    replace: true,
                    state: { error: "Payment failed" },
                  });
                }, 1500);
              }
              return;
            }

            if (!STAGES.includes(nextStatus) || terminalStatusRef.current) {
              return;
            }

            setPaymentStatus(nextStatus);
            if (nextStatus === "COMPLETED") {
              terminalStatusRef.current = true;
              setSocketMessage("Payment completed successfully.");
              client.deactivate();
            } else {
              setSocketMessage(`Payment moved to ${nextStatus}.`);
            }
          });
        });
      },
      onStompError: (frame) => {
        console.error("Socket error:", frame);
        setSocketMessage("Connection error. Returning to payments...");
        client.deactivate();
        window.setTimeout(() => {
          navigate("/payments", { replace: true, state: { error: "Connection error" } });
        }, 1200);
      },
      onWebSocketClose: () => {
        if (!terminalStatusRef.current) {
          setSocketMessage("Connection closed. Returning to payments...");
        }
      },
    });

    clientRef.current = client;
    client.activate();

    return () => {
      clientRef.current?.deactivate();
    };
  }, [activePayment, navigate]);

  const currentIndex = Math.max(STAGES.indexOf(paymentStatus), 0);
  const progressPercent = paymentStatus === "FAILED" ? 100 : (currentIndex / (STAGES.length - 1)) * 100;

  return (
    <div className="min-h-screen bg-slate-950 text-white px-4 py-8 sm:px-6 lg:px-10">
      <div className="mx-auto max-w-5xl">
        <div className="mb-8 rounded-3xl border border-white/10 bg-white/5 p-6 shadow-2xl shadow-black/20 backdrop-blur">
          <p className="text-sm uppercase tracking-[0.3em] text-red-200">FlashPay Progress</p>
          <h1 className="mt-3 text-3xl font-semibold sm:text-4xl">Tracking payment state in real time</h1>
          <p className="mt-3 max-w-2xl text-sm text-slate-300 sm:text-base">The backend pushes each status update here. The bar advances from CREATED to VALIDATED, SENT, and COMPLETED. If the payment fails, you are sent back to the payment screen.</p>
        </div>

        <div className="grid gap-6 lg:grid-cols-[1.25fr_0.75fr]">
          <div className="rounded-3xl border border-white/10 bg-slate-900/80 p-6 shadow-xl shadow-black/20">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-sm text-slate-400">Payment ID</p>
                <p className="mt-1 text-lg font-medium text-white break-all">{activePayment?.paymentId || "Waiting..."}</p>
              </div>
              <div className={`rounded-full px-4 py-2 text-sm font-semibold ${paymentStatus === "FAILED" ? "bg-red-500/15 text-red-300" : paymentStatus === "COMPLETED" ? "bg-emerald-500/15 text-emerald-300" : "bg-amber-500/15 text-amber-200"}`}>
                {paymentStatus}
              </div>
            </div>

            <div className="mt-10">
              <div className="relative h-3 rounded-full bg-slate-800">
                <div
                  className={`h-3 rounded-full transition-all duration-500 ${paymentStatus === "FAILED" ? "bg-red-500" : "bg-gradient-to-r from-amber-400 via-orange-500 to-emerald-500"}`}
                  style={{ width: `${progressPercent}%` }}
                />
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-4">
                {STAGES.map((stage, index) => {
                  const isComplete = index <= currentIndex && paymentStatus !== "FAILED";
                  const isActive = stage === paymentStatus;
                  return (
                    <div key={stage} className="flex flex-col items-center gap-2 text-center">
                      <div
                        className={`flex h-11 w-11 items-center justify-center rounded-full border text-sm font-semibold transition-all ${
                          isComplete
                            ? "border-emerald-400 bg-emerald-400 text-slate-950"
                            : isActive
                              ? "border-amber-300 bg-amber-300 text-slate-950"
                              : "border-white/15 bg-white/5 text-slate-400"
                        }`}
                      >
                        {index + 1}
                      </div>
                      <span className={`text-xs font-medium tracking-wide ${isComplete || isActive ? "text-white" : "text-slate-500"}`}>{stage}</span>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="mt-8 rounded-2xl border border-white/10 bg-white/5 p-4 text-sm text-slate-300">
              {socketMessage}
            </div>
          </div>

          <div className="space-y-6 rounded-3xl border border-white/10 bg-white/5 p-6 shadow-xl shadow-black/20 backdrop-blur">
            <div>
              <p className="text-sm text-slate-400">Amount</p>
              <p className="mt-1 text-2xl font-semibold text-white">{activePayment?.currency || ""} {activePayment?.amount || "0"}</p>
            </div>
            <div>
              <p className="text-sm text-slate-400">Destination</p>
              <p className="mt-1 break-all text-white">{activePayment?.destinationAccount || "N/A"}</p>
            </div>
            <div>
              <p className="text-sm text-slate-400">Summary</p>
              <p className="mt-1 text-white">{activePayment?.summary || "No summary provided"}</p>
            </div>
            <button
              type="button"
              onClick={() => navigate("/payments")}
              className="w-full rounded-xl bg-white px-4 py-3 font-semibold text-slate-950 transition hover:bg-slate-100"
            >
              Back to Payments
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentProgressPage;
