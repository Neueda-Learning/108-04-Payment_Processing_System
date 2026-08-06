import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

function PaymentsPage() {
  const navigate = useNavigate();

  const [payment, setPayment] = useState({
    amount: "",
    destinationAccount: "",
    summary: ""
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

          {/* ALL YOUR EXISTING INPUT CODE REMAINS SAME */}

          {/* Pay button */}
          <button
            onClick={handleSubmit}
            disabled={
              !payment.amount ||
              !payment.destinationAccount ||
              isSubmitting
            }
            className="w-full mt-2 bg-red-600 text-white py-3.5 rounded-xl font-semibold text-sm tracking-wide hover:bg-red-700 active:scale-95 transition disabled:opacity-40 disabled:cursor-not-allowed shadow-md shadow-red-200"
          >
            {isSubmitting
              ? "Processing..."
              : payment.amount && receiverInfo
                ? `Pay ${senderCurrency} ${payment.amount} to ${receiverInfo.name.split(" ")[0]}`
                : "Send Payment"}
          </button>

        </div>
      </div>
    </div>
  );
}

export default PaymentsPage;