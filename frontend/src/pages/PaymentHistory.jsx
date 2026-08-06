import { useEffect, useState } from "react";
import axios from "axios";
import Navbar from "../components/Navbar";
import PageHeader from "../components/PageHeader";
import Skeleton from "../components/Skeleton";
import EmptyState from "../components/EmptyState";
import { getStatusBadgeClasses } from "../utils/status";

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
          axios.get(`http://localhost:8080/accounts/${accountNumber}`),
          axios.get("http://localhost:8080/payments")
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
      const response = await axios.get(`http://localhost:8080/payments/${paymentId}/history`);
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
        <PageHeader
          title="Payment History"
          subtitle="View all your previous transactions, payment status, receiver details, and descriptions in one secure place."
        />






         {/* Loading State - skeleton rows so the table shape is visible immediately */}
         {loading && (
           <div className="max-w-6xl mx-auto bg-white/95 dark:bg-gray-900/95 rounded-2xl border border-gray-200 dark:border-gray-800 shadow-lg overflow-hidden">
             {Array.from({ length: 6 }).map((_, i) => (
               <div key={i} className="flex items-center gap-6 px-6 py-4 border-b border-gray-100 dark:border-gray-800 last:border-0">
                 <Skeleton className="h-4 w-14" />
                 <Skeleton className="h-5 w-16 rounded-full" />
                 <Skeleton className="h-4 w-20" />
                 <Skeleton className="h-4 w-28" />
                 <Skeleton className="h-4 w-32" />
                 <Skeleton className="h-5 w-20 rounded-full" />
                 <Skeleton className="h-4 w-24 ml-auto" />
               </div>
             ))}
           </div>
         )}

         {/* Error State */}
         {error && !loading && (
           <div className="max-w-6xl mx-auto rounded-xl border border-red-200 dark:border-red-500/30 bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 text-sm px-4 py-3 text-center">
             {error}
           </div>
         )}

         {/* Empty State */}
         {!loading && !error && payments.length === 0 && (
           <div className="max-w-6xl mx-auto">
             <EmptyState
               icon="\uD83D\uDCB3"
               title="No payments found"
               description="Your payment history will appear here once you send or receive a payment."
             />
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

                   <th className="px-6 py-4 w-8" aria-hidden="true"></th>

                 </tr>

               </thead>




               <tbody>

                 {payments.map((payment) => {
                    const dir = getDirection(payment);
                    return (

                    <tr
                      key={payment.id}
                      tabIndex={0}
                      className="
                        group
                        border-b border-gray-100 dark:border-gray-800
                        hover:bg-gray-50 dark:hover:bg-gray-800/50
                        transition
                        cursor-pointer
                        focus-visible:outline focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-red-500
                      "
                      onClick={() => handleViewHistory(payment.id)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleViewHistory(payment.id);
                      }}
                    >

                      <td className="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">
                        #{payment.id}
                      </td>

                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${
                          dir.type === "Sent"
                            ? 'bg-gray-100 text-gray-700 border-gray-200 dark:bg-gray-700/40 dark:text-gray-300 dark:border-gray-600'
                            : 'bg-green-50 text-green-700 border-green-200 dark:bg-green-500/10 dark:text-green-400 dark:border-green-500/30'
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
                          className="text-xs text-red-600 dark:text-red-400 hover:text-red-800 dark:hover:text-red-300 font-medium mt-1 cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500 rounded"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewHistory(payment.id);
                          }}
                        >
                          View History →
                        </button>
                      </td>

                      <td className="px-6 py-4 text-gray-300 dark:text-gray-700 group-hover:text-red-400 dark:group-hover:text-red-500 transition">
                        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                        </svg>
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
               aria-label="Close payment history"
               className="text-gray-500 hover:text-gray-700 dark:text-gray-300 dark:hover:text-white text-2xl font-light cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500 rounded"
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
               className="px-4 py-2 text-gray-700 dark:text-gray-100 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg font-medium text-sm transition cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500"
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

