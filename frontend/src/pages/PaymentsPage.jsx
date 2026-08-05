import { useState } from "react";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useNavigate } from "react-router-dom";
function PaymentsPage() {
  const navigate = useNavigate();

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



  const handleSubmit = async () => {
    try {
      const accountResponse = await axios.get(
        `http://localhost:8080/accounts/${payment.destinationAccount}`
      );

      const destinationAccount = accountResponse.data;
      const paymentRequest = {
        amount: payment.amount,
        status: "CREATED",
        sourceAccount: localStorage.getItem("account"),
        destinationAccount: payment.destinationAccount,
        idempotencyKey: `idem-${Date.now()}`,
        description: payment.summary,
        currency: destinationAccount.accountCurrencyType,
      };

      const client = new Client({
        webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
        onConnect: async () => {
          console.log("socket connected");

          client.subscribe(`/topic/payment/${paymentRequest.idempotencyKey}`, (message) => {
            const event = JSON.parse(message.body);
            console.log("Payment event:", event);

            if (event.status === "CREATED") {
              const paymentId = event.paymentId ?? event.id ?? paymentRequest.idempotencyKey;

              localStorage.setItem(
                "flashpay-active-payment",
                JSON.stringify({
                  paymentId,
                  paymentTrackingKey: paymentRequest.idempotencyKey,
                  status: event.status,
                  amount: paymentRequest.amount,
                  destinationAccount: paymentRequest.destinationAccount,
                  summary: paymentRequest.description,
                  currency: paymentRequest.currency,
                })
              );

              navigate("/payment-progress", {
                state: {
                  paymentId,
                  paymentTrackingKey: paymentRequest.idempotencyKey,
                  initialStatus: event.status,
                  amount: paymentRequest.amount,
                  destinationAccount: paymentRequest.destinationAccount,
                  summary: paymentRequest.description,
                  currency: paymentRequest.currency,
                },
              });

              client.deactivate();
            }
          });

          try {
            const response = await axios.post("http://localhost:8080/payments/", {
              ...paymentRequest,
            });

            console.log("Payment initiated:", response.data);
          } catch (error) {
            console.error("Payment request failed:", error);
            client.deactivate();

            if (error.response?.status === 404) {
              alert("Destination account does not exist");
            } else {
              alert("Payment failed");
            }
          }
        },
        onStompError: (frame) => {
          console.error("Socket error:", frame);
          client.deactivate();
        },
      });

      client.activate();
    } catch (error) {
      console.error("Setup failed:", error);

      if (error.response?.status === 404) {
        alert("Destination account does not exist");
      } else {
        alert("Payment failed");
      }
    }
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