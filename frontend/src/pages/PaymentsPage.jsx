import { useState } from "react";

function PaymentsPage() {

  const [payment, setPayment] = useState({
    amount: "",
    destinationAccount: "",
    summary: ""
  });


  const handleChange = (e) => {
    setPayment({
      ...payment,
      [e.target.name]: e.target.value
    });
  };


  const handleSubmit = () => {
    console.log(payment);
    // API call will come here
  };


  return (
    <div className="min-h-screen relative overflow-hidden bg-gray-50 px-6 py-10">


      {/* Animated Background */}
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
            "url('https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1600&q=80')",
        }}
      ></div>


      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85"></div>



      {/* Content */}
      <div className="relative z-10">


        {/* Header */}
        <div className="max-w-3xl mx-auto mb-8">

          <h2 className="text-3xl font-bold text-gray-900">
            Make Payment
          </h2>

          <p className="text-gray-500 mt-2">
            Send payments securely with FlashPay
          </p>

        </div>




        {/* Payment Card */}
        <div
          className="
            max-w-3xl
            mx-auto
            bg-white/95
            rounded-2xl
            border border-gray-200
            shadow-lg
            p-8
            backdrop-blur-sm
          "
        >


          <div className="space-y-5">



            {/* Amount */}
            <div>

              <label className="text-sm text-gray-600">
                Amount
              </label>

              <input
                type="number"
                name="amount"
                placeholder="Enter payment amount"
                value={payment.amount}
                onChange={handleChange}
                className="
                  w-full
                  mt-2
                  px-4
                  py-3
                  rounded-lg
                  border border-gray-300
                  focus:outline-none
                  focus:ring-2
                  focus:ring-red-500
                "
              />

            </div>




            {/* Destination Account */}
            <div>

              <label className="text-sm text-gray-600">
                Destination Account
              </label>

              <input
                type="text"
                name="destinationAccount"
                placeholder="Enter receiver account number"
                value={payment.destinationAccount}
                onChange={handleChange}
                className="
                  w-full
                  mt-2
                  px-4
                  py-3
                  rounded-lg
                  border border-gray-300
                  focus:outline-none
                  focus:ring-2
                  focus:ring-red-500
                "
              />

            </div>





            {/* Payment Summary */}
            <div>

              <label className="text-sm text-gray-600">
                Payment Summary
              </label>

              <textarea
                name="summary"
                placeholder="Enter payment description"
                value={payment.summary}
                onChange={handleChange}
                rows="4"
                className="
                  w-full
                  mt-2
                  px-4
                  py-3
                  rounded-lg
                  border border-gray-300
                  resize-none
                  focus:outline-none
                  focus:ring-2
                  focus:ring-red-500
                "
              />

            </div>





            {/* Submit Button */}
            <button
              onClick={handleSubmit}
              className="
                w-full
                mt-4
                bg-red-600
                text-white
                py-3
                rounded-lg
                font-medium
                hover:bg-red-700
                transition
                shadow-sm cursor-pointer
              "
            >
              Submit Payment
            </button>


          </div>


        </div>


      </div>


    </div>
  );
}

export default PaymentsPage;