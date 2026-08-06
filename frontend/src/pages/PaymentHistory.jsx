import { useState, useEffect } from "react";
import axios from "axios";
import Navbar from "../components/Navbar";

function PaymentHistory() {

  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPaymentId, setSelectedPaymentId] = useState(null);
  const [paymentHistory, setPaymentHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [currentAccount, setCurrentAccount] = useState(null);
  const [senderCurrency, setSenderCurrency] = useState("USD");

  useEffect(() => {
    const account = localStorage.getItem("account");
    setCurrentAccount(account);
    if (account) {
      // Fetch sender's account details to get currency
      axios.get(`http://localhost:8080/accounts/${account}`)
        .then(res => {
          setSenderCurrency(res.data.accountCurrencyType || "USD");
        })
        .catch(err => {
          console.error("Failed to fetch sender account:", err);
          setSenderCurrency("USD");
        });
      fetchPayments();
    } else {
      setError("No account logged in");
      setLoading(false);
    }
  }, []);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      const response = await axios.get("http://localhost:8080/payments");

      // Filter payments for the current logged-in account
      const account = localStorage.getItem("account");
      const filteredPayments = response.data.filter(payment =>
        payment.sourceAccount === account || payment.destinationAccount === account
      );

      // Transform backend payment data to frontend format
      const transformedPayments = filteredPayments.map(payment => ({
        id: `PAY${payment.id}`,
        amount: `${senderCurrency} ${payment.amount.toFixed(2)}`,
        receiver: payment.sourceAccount === account
          ? `Account ****${payment.destinationAccount?.slice(-4) || "0000"}`
          : `From: ****${payment.sourceAccount?.slice(-4) || "0000"}`,
        status: payment.status,
        date: payment.createdAt ? formatDate(payment.createdAt) : "N/A",
        description: payment.description || "No description",
        type: payment.sourceAccount === account ? "Sent" : "Received"
      }));

      setPayments(transformedPayments);
      setError(null);
    } catch (err) {
      console.error("Failed to fetch payments:", err);
      setError("Failed to load payment history");
      setPayments([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      day: "2-digit",
      month: "short",
      year: "numeric"
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleString("en-US", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    });
  };

  const fetchPaymentHistory = async (paymentId) => {
    try {
      setHistoryLoading(true);
      const response = await axios.get(`http://localhost:8080/payments/${paymentId}/history`);
      setPaymentHistory(response.data || []);
    } catch (err) {
      console.error("Failed to fetch payment history:", err);
      setPaymentHistory([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleViewHistory = (paymentId) => {
    setSelectedPaymentId(paymentId);
    // Extract numeric ID from "PAY123" format
    const numericId = paymentId.replace("PAY", "");
    fetchPaymentHistory(numericId);
  };

  const statusStyle = (status) => {

    if (status === "COMPLETED") {
      return "bg-green-50 text-green-700 border-green-200";
    }

    if (status === "FAILED") {
      return "bg-red-50 text-red-700 border-red-200";
    }

    return "bg-yellow-50 text-yellow-700 border-yellow-200";

  };


  return (
    <>
    <div className="min-h-screen relative overflow-hidden">


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
          animate-pulse
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85"></div>




      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">



        {/* Header */}
        <div className="max-w-6xl mx-auto mb-8">

          <h2 className="text-2xl sm:text-3xl font-bold text-gray-900">
            Payment History
          </h2>

          <p className="text-gray-600 mt-2 max-w-xl text-sm sm:text-base">
            View all your previous transactions, payment status,
            receiver details, and descriptions in one secure place.
          </p>

        </div>






         {/* Loading State */}
         {loading && (
           <div className="max-w-6xl mx-auto text-center py-12">
             <div className="inline-block">
               <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
             </div>
             <p className="mt-4 text-gray-600">Loading payment history...</p>
           </div>
         )}

         {/* Error State */}
         {error && !loading && (
           <div className="max-w-6xl mx-auto bg-red-50 border border-red-200 rounded-2xl p-6">
             <p className="text-red-700">{error}</p>
             <button
               onClick={fetchPayments}
               className="mt-3 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-medium"
             >
               Retry
             </button>
           </div>
         )}

         {/* Empty State */}
         {!loading && !error && payments.length === 0 && (
           <div className="max-w-6xl mx-auto text-center py-12">
             <p className="text-gray-600 text-lg">No payments found</p>
             <p className="text-gray-500 text-sm mt-2">Your payment history will appear here</p>
           </div>
         )}

         {/* Table Card */}
         {!loading && !error && payments.length > 0 && (
           <div
             className="
               max-w-6xl
               mx-auto
               bg-white/95
               backdrop-blur-sm
               rounded-2xl
               border border-gray-200
               shadow-lg
               overflow-x-auto
             "
           >

             <table className="w-full min-w-[700px] text-left">


               <thead className="bg-gray-50 border-b">

                 <tr>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Payment ID
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Type
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Amount
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Account
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Description
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Status
                   </th>

                   <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                     Date
                   </th>

                 </tr>

               </thead>




               <tbody>

                 {payments.map((payment) => (

                    <tr
                      key={payment.id}
                      className="
                        border-b
                        hover:bg-gray-50
                        transition
                        cursor-pointer
                      "
                      onClick={() => handleViewHistory(payment.id)}
                    >

                      <td className="px-6 py-4 font-medium text-gray-900">
                        {payment.id}
                      </td>

                      <td className="px-6 py-4">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${
                          payment.type === 'Sent'
                            ? 'bg-red-50 text-red-700 border-red-200'
                            : 'bg-green-50 text-green-700 border-green-200'
                        }`}>
                          {payment.type}
                        </span>
                      </td>

                      <td className="px-6 py-4 text-gray-700">
                        {payment.amount}
                      </td>

                      <td className="px-6 py-4 text-gray-600">
                        {payment.receiver}
                      </td>

                      <td className="px-6 py-4 text-gray-600">
                        {payment.description}
                      </td>

                      <td className="px-6 py-4">

                        <span
                          className={`
                            px-3 py-1
                            rounded-full
                            text-xs
                            font-medium
                            border
                            ${statusStyle(payment.status)}
                          `}
                        >
                          {payment.status}
                        </span>

                      </td>


                      <td className="px-6 py-4 text-gray-500">
                        {payment.date}
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

                 ))}

               </tbody>


             </table>


           </div>
         )}


       </div>


     </div>

     {/* Payment History Details Modal */}
     {selectedPaymentId && (
       <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
         <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[80vh] overflow-y-auto shadow-2xl">

           {/* Modal Header */}
           <div className="sticky top-0 bg-gray-50 border-b px-6 py-4 flex justify-between items-center">
             <h3 className="text-lg font-bold text-gray-900">
               Payment History - {selectedPaymentId}
             </h3>
             <button
               onClick={() => setSelectedPaymentId(null)}
               className="text-gray-500 hover:text-gray-700 text-2xl font-light"
             >
               ✕
             </button>
           </div>

           {/* Modal Content */}
           <div className="p-6">
             {historyLoading && (
               <div className="text-center py-8">
                 <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-red-600"></div>
                 <p className="mt-3 text-gray-600 text-sm">Loading history...</p>
               </div>
             )}

             {!historyLoading && paymentHistory.length === 0 && (
               <p className="text-center text-gray-500 py-8">No history found for this payment</p>
             )}

             {!historyLoading && paymentHistory.length > 0 && (
               <div className="space-y-4">
                 <div className="text-sm text-gray-600 mb-6">
                   <p><strong>Total Transitions:</strong> {paymentHistory.length}</p>
                 </div>

                 {paymentHistory.map((entry, index) => (
                   <div
                     key={entry.id || index}
                     className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition"
                   >
                     <div className="flex items-start justify-between mb-2">
                       <div className="flex items-center gap-3">
                         <div className="flex items-center justify-center w-8 h-8 rounded-full bg-blue-100 text-blue-700 text-sm font-bold">
                           {index + 1}
                         </div>
                         <div>
                           <p className="font-medium text-gray-900">
                             {entry.fromStatus ? `${entry.fromStatus} → ${entry.toStatus}` : `Initial: ${entry.toStatus}`}
                           </p>
                           <p className="text-xs text-gray-500 mt-1">
                             {formatDateTime(entry.timestamp)}
                           </p>
                         </div>
                       </div>
                       <span className={`px-3 py-1 rounded-full text-xs font-medium border ${
                         entry.toStatus === 'COMPLETED' ? 'bg-green-50 text-green-700 border-green-200' :
                         entry.toStatus === 'FAILED' ? 'bg-red-50 text-red-700 border-red-200' :
                         'bg-yellow-50 text-yellow-700 border-yellow-200'
                       }`}>
                         {entry.toStatus}
                       </span>
                     </div>
                     {entry.notes && (
                       <p className="text-sm text-gray-600 mt-2 pl-11">
                         <strong>Notes:</strong> {entry.notes}
                       </p>
                     )}
                   </div>
                 ))}
               </div>
             )}
           </div>

           {/* Modal Footer */}
           <div className="bg-gray-50 border-t px-6 py-4 flex justify-end">
             <button
               onClick={() => setSelectedPaymentId(null)}
               className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg font-medium text-sm transition"
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

