import Navbar from "../components/Navbar";

function PaymentHistory() {

  const payments = [
    {
      id: "PAY001",
      amount: "€250.00",
      receiver: "Account ****4521",
      status: "COMPLETED",
      date: "01 Aug 2026",
      description: "Monthly rent payment"
    },
    {
      id: "PAY002",
      amount: "€120.00",
      receiver: "Account ****7823",
      status: "PENDING",
      date: "30 Jul 2026",
      description: "Online shopping payment"
    },
    {
      id: "PAY003",
      amount: "€75.00",
      receiver: "Account ****9912",
      status: "FAILED",
      date: "28 Jul 2026",
      description: "Utility bill payment"
    }
  ];


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






        {/* Table Card */}
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
                  Amount
                </th>

                <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                  Receiver
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
                  "
                >

                  <td className="px-6 py-4 font-medium text-gray-900">
                    {payment.id}
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
                  </td>


                </tr>

              ))}

            </tbody>


          </table>


        </div>


      </div>


    </div>

  );
}


export default PaymentHistory;