import { useState } from "react";
import { useNavigate } from "react-router-dom";

function LoginPage() {

  const [account, setAccount] = useState("");
  const navigate = useNavigate();


  const handleLogin = () => {

    if (account) {
      localStorage.setItem("account", account);
      navigate("/home");
    }

  };


  return (

    <div className="min-h-screen relative overflow-hidden flex items-center justify-center px-4 sm:px-6">


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
            "url('https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85"></div>





      {/* Login Card */}
      <div
        className="
          relative
          z-10
          bg-white/95
          backdrop-blur-sm
          w-full
          max-w-md
          rounded-2xl
          shadow-xl
          border border-gray-200
          p-6 sm:p-8
        "
      >



        {/* Logo */}
        <div className="flex justify-center mb-6">

          <div
            className="
              bg-red-50
              border border-red-100
              rounded-full
              p-4
            "
          >
            <span className="text-3xl text-red-600 font-bold">
              FP
            </span>
          </div>

        </div>





        {/* Heading */}
        <div className="text-center mb-8">

          <h1 className="text-3xl font-bold text-gray-900">
            FlashPay
          </h1>


          <p className="text-gray-500 mt-2">
            Fast • Secure • Instant Payments
          </p>


        </div>







        {/* Login Form */}
        <div>


          <h2 className="text-xl font-semibold text-gray-800 mb-4">
            Account Login
          </h2>



          <label className="text-sm text-gray-600">
            Account Number
          </label>



          <input
            type="text"
            placeholder="Enter your account number"
            value={account}
            onChange={(e) => setAccount(e.target.value)}
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
              focus:border-red-500
              transition
            "
          />





          <button
            onClick={handleLogin}
            className="
              w-full
              mt-6
              bg-red-600
              text-white
              py-3
              rounded-lg
              font-medium
              hover:bg-red-700
              transition
              shadow-sm
            "
          >
            Login to FlashPay
          </button>



        </div>







        {/* Footer */}
        <p className="text-center text-sm text-gray-400 mt-6">
          Your payments, delivered securely
        </p>



      </div>


    </div>

  );
}


export default LoginPage;