import { useEffect, useState } from "react";
import axios from "axios";
import Navbar from "../components/Navbar";
import { getStatusBadgeClasses } from "../utils/status";

function PaymentHistory() {

  const accountNumber = localStorage.getItem("account");

  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    axios.get("http://localhost:8080/payments")
      .then((res) => {
        if (cancelled) return;
        const mine = res.data
          .filter((p) => p.sourceAccount === accountNumber || p.destinationAccount === accountNumber)
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        setPayments(mine);
      })
      .catch(() => {
        if (!cancelled) setError("Could not load payment history. Is the backend running?");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => { cancelled = true; };
  }, [accountNumber]);

  const formatDate = (value) => {
    if (!value) return "—";
    return new Date(value).toLocaleString(undefined, {
      day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
    });
  };

  const direction = (payment) => {
    const outgoing = payment.sourceAccount === accountNumber;
    return {
      label: outgoing ? "Sent to" : "Received from",
      counterparty: outgoing ? payment.destinationAccount : payment.sourceAccount,
      sign: outgoing ? "−" : "+",
      colorClass: outgoing ? "text-gray-900 dark:text-gray-100" : "text-green-600 dark:text-green-400",
    };
  };


  return (

    <div className="min-h-screen relative overflow-hidden bg-gray-50 dark:bg-gray-950">


      {/* Navbar */}
      <div className="relative z-20">
        <Navbar />
      </div>



      {/* Background Image */}
      <div
        className="
          absolute
          inset-0
          bg-cover
          bg-center
          dark:opacity-20
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85 dark:bg-gray-950/90"></div>




      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">



        {/* Header */}
        <div className="max-w-6xl mx-auto mb-8">

          <h2 className="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-gray-100">
            Payment History
          </h2>

          <p className="text-gray-600 dark:text-gray-400 mt-2 max-w-xl text-sm sm:text-base">
            View all your previous transactions, payment status,
            receiver details, and descriptions in one secure place.
          </p>

        </div>






        {/* Table Card */}
        <div
          className="
            max-w-6xl
            mx-auto
            bg-white/95
            dark:bg-gray-900/95
            backdrop-blur-sm
            rounded-2xl
            border border-gray-200 dark:border-gray-800
            shadow-lg
            overflow-x-auto
          "
        >

          {loading ? (
            <div className="py-16 text-center text-gray-400 dark:text-gray-500 text-sm">
              Loading your payment history…
            </div>
          ) : error ? (
            <div className="py-16 text-center text-red-500 dark:text-red-400 text-sm">
              {error}
            </div>
          ) : payments.length === 0 ? (
            <div className="py-16 text-center text-gray-400 dark:text-gray-500 text-sm">
              No payments yet. Once you send or receive money, it'll show up here.
            </div>
          ) : (

          <table className="w-full min-w-[700px] text-left">


            <thead className="bg-gray-50 dark:bg-gray-800/60 border-b border-gray-200 dark:border-gray-800">

              <tr>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Payment ID
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Amount
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Counterparty
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Description
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Status
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                  Date
                </th>

              </tr>

            </thead>




            <tbody>

              {payments.map((payment) => {
                const dir = direction(payment);
                return (

                <tr
                  key={payment.id}
                  className="
                    border-b border-gray-100 dark:border-gray-800
                    hover:bg-gray-50 dark:hover:bg-gray-800/50
                    transition
                  "
                >

                  <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">
                    #{payment.id}
                  </td>


                  <td className={`px-6 py-4 font-semibold ${dir.colorClass}`}>
                    {dir.sign}{payment.currency} {Number(payment.amount).toFixed(2)}
                  </td>


                  <td className="px-6 py-4 text-gray-600 dark:text-gray-400">
                    <span className="text-xs text-gray-400 dark:text-gray-500">{dir.label}</span>
                    <br />
                    {dir.counterparty}
                  </td>


                  <td className="px-6 py-4 text-gray-600 dark:text-gray-400">
                    {payment.description || "—"}
                  </td>


                  <td className="px-6 py-4">

                    <span
                      className={`
                        px-3 py-1
                        rounded-full
                        text-xs
                        font-medium
                        border
                        ${getStatusBadgeClasses(payment.status)}
                      `}
                    >
                      {payment.status}
                    </span>

                  </td>


                  <td className="px-6 py-4 text-gray-500 dark:text-gray-400 whitespace-nowrap">
                    {formatDate(payment.createdAt)}
                  </td>


                </tr>

                );
              })}

            </tbody>


          </table>

          )}


        </div>


      </div>


    </div>

  );
}


export default PaymentHistory;