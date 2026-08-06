import { useEffect, useState } from "react";
import axios from "axios";
import Navbar from "../components/Navbar";

const getStatusBadgeClasses = (status) => {
  const normalized = String(status || "").toUpperCase();
  if (normalized === "COMPLETED") {
    return "bg-green-50 text-green-700 border-green-200";
  }
  if (normalized === "FAILED") {
    return "bg-red-50 text-red-700 border-red-200";
  }
  return "bg-yellow-50 text-yellow-700 border-yellow-200";
};

function PaymentHistory() {
  const accountNumber = localStorage.getItem("account");

  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [senderCurrency, setSenderCurrency] = useState("USD");

  const [selectedPaymentId, setSelectedPaymentId] = useState(null);
  const [paymentHistory, setPaymentHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;

    if (!accountNumber) {
      setError("No account logged in");
      setLoading(false);
      return () => {
        cancelled = true;
      };
    }

    const load = async () => {
      try {
        setLoading(true);

        const [accountRes, paymentsRes] = await Promise.all([
          axios.get(`${import.meta.env.VITE_API_URL}/accounts/${accountNumber}`),
          axios.get(`${import.meta.env.VITE_API_URL}/payments`)
        ]);

        if (cancelled) return;

        setSenderCurrency(accountRes.data?.accountCurrencyType || "USD");

        const mine = (paymentsRes.data || [])
          .filter((p) => p.sourceAccount === accountNumber || p.destinationAccount === accountNumber)
          .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

        setPayments(mine);
        setError(null);
      } catch (err) {
        if (!cancelled) {
          console.error("Failed to load payment history:", err);
          setPayments([]);
          setError("Failed to load payment history");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    load();

    return () => {
      cancelled = true;
    };
  }, [accountNumber]);

  const formatDate = (value) => {
    if (!value) return "N/A";
    return new Date(value).toLocaleString(undefined, {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };

  const formatDateTime = (value) => {
    if (!value) return "N/A";
    return new Date(value).toLocaleString(undefined, {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    });
  };

  const handleViewHistory = async (paymentId) => {
    setSelectedPaymentId(paymentId);
    setHistoryLoading(true);
    try {
      const response = await axios.get(`${import.meta.env.VITE_API_URL}/payments/${paymentId}/history`);
      setPaymentHistory(response.data || []);
    } catch (err) {
      console.error("Failed to fetch payment history:", err);
      setPaymentHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  const getDirection = (payment) => {
    const outgoing = payment.sourceAccount === accountNumber;
    return {
      type: outgoing ? "Sent" : "Received",
      counterparty: outgoing
        ? `Account ****${(payment.destinationAccount || "").slice(-4) || "0000"}`
        : `From: ****${(payment.sourceAccount || "").slice(-4) || "0000"}`,
      amountClass: outgoing
        ? "text-gray-700 dark:text-gray-200"
        : "text-green-600 dark:text-green-400"
    };
  };

  return (
    <>
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






         {/* Loading State */}
         {loading && (
           <div className="max-w-6xl mx-auto text-center py-12 text-gray-500 dark:text-gray-400 text-sm">
             Loading your payment history...
           </div>
         )}

         {/* Error State */}
         {error && !loading && (
           <div className="max-w-6xl mx-auto text-center py-12 text-red-500 dark:text-red-400 text-sm">
             {error}
           </div>
         )}

         {/* Empty State */}
         {!loading && !error && payments.length === 0 && (
           <div className="max-w-6xl mx-auto text-center py-12">
             <p className="text-gray-600 dark:text-gray-300 text-lg">No payments found</p>
             <p className="text-gray-500 dark:text-gray-400 text-sm mt-2">Your payment history will appear here</p>
           </div>
         )}

         {/* Table Card */}
         {!loading && !error && payments.length > 0 && (
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

             <table className="w-full min-w-[700px] text-left">


               <thead className="bg-gray-50 dark:bg-gray-800/60 border-b border-gray-200 dark:border-gray-800">

                 <tr>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                     Payment ID
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                     Type
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                     Amount
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600 dark:text-gray-300">
                     Account
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
                    const dir = getDirection(payment);
                    return (

                    <tr
                      key={payment.id}
                      className="
                        border-b border-gray-100 dark:border-gray-800
                        hover:bg-gray-50 dark:hover:bg-gray-800/50
                        transition
                        cursor-pointer
                      "
                      onClick={() => handleViewHistory(payment.id)}
                    >

                      <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">
                        #{payment.id}
                      </td>

                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${
                          dir.type === "Sent"
                            ? 'bg-red-50 text-red-700 border-red-200'
                            : 'bg-green-50 text-green-700 border-green-200'
                        }`}>
                          {dir.type}
                        </span>
                      </td>

                      <td className={`px-6 py-4 font-semibold ${dir.amountClass}`}>
                        {senderCurrency} {Number(payment.amount || 0).toFixed(2)}
                      </td>

                      <td className="px-6 py-4 text-gray-600 dark:text-gray-400">
                        {dir.counterparty}
                      </td>

                      <td className="px-6 py-4 text-gray-600 dark:text-gray-400">
                        {payment.description || "-"}
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
                        <br />
                        <button
                          className="text-xs text-blue-600 hover:text-blue-800 font-medium mt-1"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewHistory(payment.id);
                          }}
                        >
                          View History →
                        </button>
                      </td>


                    </tr>
                  );
                })}

               </tbody>


             </table>


           </div>
         )}


       </div>


      </div>

     {/* Payment History Details Modal */}
     {selectedPaymentId && (
       <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
         <div className="bg-white dark:bg-gray-900 rounded-2xl max-w-2xl w-full max-h-[80vh] overflow-y-auto shadow-2xl border border-gray-200 dark:border-gray-800">

           {/* Modal Header */}
           <div className="sticky top-0 bg-gray-50 dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 px-6 py-4 flex justify-between items-center">
             <h3 className="text-lg font-bold text-gray-900 dark:text-gray-100">
               Payment History - PAY{selectedPaymentId}
             </h3>
             <button
               onClick={() => setSelectedPaymentId(null)}
               className="text-gray-500 hover:text-gray-700 dark:text-gray-300 dark:hover:text-white text-2xl font-light"
             >
               ✕
             </button>
           </div>

           {/* Modal Content */}
           <div className="p-6">
             {historyLoading && (
                <div className="text-center py-8">
                 <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-red-600"></div>
                  <p className="mt-3 text-gray-600 dark:text-gray-300 text-sm">Loading history...</p>
               </div>
             )}

             {!historyLoading && paymentHistory.length === 0 && (
               <p className="text-center text-gray-500 dark:text-gray-400 py-8">No history found for this payment</p>
             )}

             {!historyLoading && paymentHistory.length > 0 && (
               <div className="space-y-4">
                 <div className="text-sm text-gray-600 dark:text-gray-300 mb-6">
                   <p><strong>Total Transitions:</strong> {paymentHistory.length}</p>
                 </div>

                 {paymentHistory.map((entry, index) => (
                   <div
                     key={entry.id || index}
                     className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:bg-gray-50 dark:hover:bg-gray-800 transition"
                   >
                     <div className="flex items-start justify-between mb-2">
                       <div className="flex items-center gap-3">
                          <div className="flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300 text-sm font-bold">
                           {index + 1}
                         </div>
                         <div>
                            <p className="font-medium text-gray-900 dark:text-gray-100">
                             {entry.fromStatus ? `${entry.fromStatus} → ${entry.toStatus}` : `Initial: ${entry.toStatus}`}
                           </p>
                            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
                             {formatDateTime(entry.timestamp)}
                           </p>
                         </div>
                       </div>
                        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusBadgeClasses(entry.toStatus)}`}>
                         {entry.toStatus}
                       </span>
                     </div>
                     {entry.notes && (
                       <p className="text-sm text-gray-600 dark:text-gray-300 mt-2 pl-11">
                         <strong>Notes:</strong> {entry.notes}
                       </p>
                     )}
                   </div>
                 ))}
               </div>
             )}
           </div>

           {/* Modal Footer */}
           <div className="bg-gray-50 dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700 px-6 py-4 flex justify-end">
             <button
               onClick={() => setSelectedPaymentId(null)}
               className="px-4 py-2 text-gray-700 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg font-medium text-sm transition"
             >
               Close
             </button>
           </div>
         </div>
        </div>
      )}
    </>
  );
}

export default PaymentHistory;

