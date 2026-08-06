import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

function PaymentsPage() {
  const navigate = useNavigate();

  const [payment, setPayment] = useState({
    amount: "",
    destinationAccount: "",
    summary: "",
    paymentType: "IMMEDIATE"
  });

  const [senderCurrency, setSenderCurrency] = useState("USD"); 
  const [receiverInfo, setReceiverInfo] = useState(null); 
  const [receiverStatus, setReceiverStatus] = useState("idle"); 
  const debounceRef = useRef(null);

  // NEW: generate once when payment page opens
  const [idempotencyKey] = useState(() => `idem-${crypto.randomUUID()}`);
  const [isSubmitting, setIsSubmitting] = useState(false);


  useEffect(() => {
    const sourceAccount = localStorage.getItem("account");
    if (sourceAccount) {
      axios.get(`${API_BASE}/accounts/${sourceAccount}`)
        .then(res => {
          setSenderCurrency(res.data.accountCurrencyType || "USD");
        })
        .catch(err => {
          console.error("Failed to fetch sender account:", err);
          setSenderCurrency("USD");
        });
    }
  }, []);


  useEffect(() => {
    const acc = payment.destinationAccount.trim();

    if (!acc) {
      setReceiverInfo(null);
      setReceiverStatus("idle");
      return;
    }

    setReceiverStatus("loading");
    clearTimeout(debounceRef.current);

    debounceRef.current = setTimeout(async () => {
      try {
        const res = await axios.get(`${API_BASE}/accounts/${acc}`);
        setReceiverInfo({
          name: res.data.accountHolderName,
          currency: res.data.accountCurrencyType
        });
        setReceiverStatus("found");
      } catch {
        setReceiverInfo(null);
        setReceiverStatus("not_found");
      }
    }, 600);

    return () => clearTimeout(debounceRef.current);
  }, [payment.destinationAccount]);


  const handleChange = (e) => {
    setPayment({ ...payment, [e.target.name]: e.target.value });
  };


  const handleSubmit = async () => {

    if (isSubmitting) return;

    setIsSubmitting(true);

    try {
      await axios.get(`${API_BASE}/accounts/${payment.destinationAccount}`);

      navigate("/payment-progress", {
        state: {
          paymentRequest: {
            amount: payment.amount,
            status: "CREATED",
            sourceAccount: localStorage.getItem("account"),
            destinationAccount: payment.destinationAccount,

            // CHANGED: same key generated when page opened
            idempotencyKey: idempotencyKey,

            description: payment.summary,
            currency: senderCurrency,
            paymentType: payment.paymentType,
            scheduledDelaySeconds: payment.paymentType === "SCHEDULED" ? 60 : 0,
          },
        },
      });

    } catch (error) {

      setIsSubmitting(false);

      if (error.response?.status === 404) {
        alert("Destination account does not exist");
      } else if (!error.response) {
        alert("Cannot connect to server. Is the backend running?");
      } else {
        alert(
          "Error " +
          error.response.status +
          ": " +
          (error.response.data?.message || error.message)
        );
      }
    }
  };


  const sourceAccount = localStorage.getItem("account") || "-";


  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex flex-col items-center justify-center px-4 py-10">

      <div className="w-full max-w-md bg-white dark:bg-gray-900 rounded-3xl shadow-xl overflow-hidden">

        <div className="bg-red-600 px-6 pt-7 pb-10">
          <p className="text-xs font-bold uppercase tracking-widest text-red-200">
            FlashPay
          </p>
          <h1 className="mt-1 text-2xl font-bold text-white">
            Send Money
          </h1>
          <p className="mt-0.5 text-sm text-red-200">
            From: <span className="text-white font-semibold">{sourceAccount}</span>
          </p>
        </div>


        <div className="-mt-5 bg-white dark:bg-gray-900 rounded-t-3xl px-6 pt-6 pb-8 space-y-5">

          {/* Destination account + receiver name */}
          <div>
            <label className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">To Account</label>
            <div className="relative mt-2">
              <input
                type="text"
                name="destinationAccount"
                placeholder="Enter account number"
                value={payment.destinationAccount}
                onChange={handleChange}
                className="w-full pl-4 pr-10 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
              />
              {/* Status indicator */}
              <div className="absolute right-3 top-3.5">
                {receiverStatus === "loading" && (
                  <div className="h-4 w-4 rounded-full border-2 border-red-400 border-t-transparent animate-spin" />
                )}
                {receiverStatus === "found" && (
                  <svg className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                  </svg>
                )}
                {receiverStatus === "not_found" && payment.destinationAccount && (
                  <svg className="h-4 w-4 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                )}
              </div>
            </div>

            {/* Receiver card - shown when found */}
            {receiverStatus === "found" && receiverInfo && (
              <div className="mt-2 flex items-center gap-3 rounded-xl bg-green-50 dark:bg-green-500/10 border border-green-200 dark:border-green-500/30 px-4 py-3">
                <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-green-600 text-white text-sm font-bold">
                  {receiverInfo.name.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-900 dark:text-gray-100">{receiverInfo.name}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Account verified - {receiverInfo.currency}</p>
                </div>
                <svg className="ml-auto h-5 w-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                </svg>
              </div>
            )}

            {receiverStatus === "not_found" && payment.destinationAccount && (
              <p className="mt-1.5 text-xs text-red-500 dark:text-red-400">Account not found</p>
            )}
          </div>

          {/* Amount */}
          <div>
            <label className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Amount</label>
            <div className="relative mt-2">
              <span className="absolute left-4 top-3 text-gray-400 dark:text-gray-500 text-sm font-semibold">
                {senderCurrency || "USD"}
              </span>
              <input
                type="number"
                name="amount"
                placeholder="0.00"
                value={payment.amount}
                onChange={handleChange}
                className="w-full pl-10 pr-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
              />
            </div>
          </div>

          {/* Summary / Note */}
          <div>
            <label className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Add a note</label>
            <input
              type="text"
              name="summary"
              placeholder="e.g. Rent, Groceries..."
              value={payment.summary}
              onChange={handleChange}
              className="w-full mt-2 px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
            />
          </div>

          {/* Payment type */}
          <div>
            <label className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">Payment Type</label>
            <select
              name="paymentType"
              value={payment.paymentType}
              onChange={handleChange}
              className="w-full mt-2 px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              <option value="IMMEDIATE">Immediate</option>
              <option value="SCHEDULED">Scheduled (after 1 minute)</option>
            </select>
            {payment.paymentType === "SCHEDULED" && (
              <p className="mt-1.5 text-xs text-amber-600 dark:text-amber-400">
                This payment will start processing after 60 seconds.
              </p>
            )}
          </div>

           {/* Pay button */}
           <button
             onClick={handleSubmit}
             disabled={!payment.amount || !payment.destinationAccount}
             className="w-full mt-2 bg-red-600 text-white py-3.5 rounded-xl font-semibold text-sm tracking-wide hover:bg-red-700 active:scale-95 transition disabled:opacity-40 disabled:cursor-not-allowed shadow-md shadow-red-200"
           >
             {payment.amount && receiverInfo
               ? (payment.paymentType === "SCHEDULED"
                 ? `Schedule ${senderCurrency} ${payment.amount} to ${receiverInfo.name.split(" ")[0]}`
                 : `Pay ${senderCurrency} ${payment.amount} to ${receiverInfo.name.split(" ")[0]}`)
               : payment.paymentType === "SCHEDULED" ? "Schedule Payment" : "Send Payment"}
           </button>

        </div>
      </div>
    </div>
  );
}

export default PaymentsPage;