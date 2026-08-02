import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col relative">

      {/* ✅ FIX: Add z-20 */}
      <div className="relative z-20">
        <Navbar />
      </div>

      {/* Background Image */}
      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1600&q=80')",
        }}
      ></div>

      {/* Overlay */}
      <div className="absolute inset-0 bg-white/80"></div>

      {/* Main Content */}
      <div className="relative flex-1 z-10">

        {/* Hero Section */}
        <div className="max-w-5xl mx-auto px-6 pt-20">

          <div className="max-w-2xl">

            <h1 className="text-4xl font-bold text-gray-900 leading-tight">
              Fast and Secure Payments
            </h1>

            <p className="text-gray-600 mt-4 text-lg">
              Send money instantly with a simple and reliable payment system.
              Track transactions, view history, and manage everything in one place.
            </p>

            <button
              onClick={() => navigate("/payments")}
              className="
                mt-8
                px-6 py-3
                bg-red-600
                text-white
                rounded-md
                text-base
                font-medium
                hover:bg-red-700
                transition
                cursor-pointer
                shadow-sm
              "
            >
              Make Payment
            </button>

          </div>
        </div>

        {/* Cards Section */}
        <div className="max-w-6xl mx-auto px-6 mt-28 pb-20">

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">

            <div className="bg-white border border-gray-200 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2">
              <h3 className="font-semibold text-lg text-gray-900">
                Instant Transfers
              </h3>
              <p className="text-gray-600 mt-3">
                Payments are processed in real-time with minimal delay,
                ensuring fast transactions.
              </p>
            </div>

            <div className="bg-white border border-gray-200 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2 delay-100">
              <h3 className="font-semibold text-lg text-gray-900">
                Secure System
              </h3>
              <p className="text-gray-600 mt-3">
                Built with secure backend processing and transaction validation.
              </p>
            </div>

            <div className="bg-white border border-gray-200 rounded-xl p-8 shadow-md transform transition duration-500 hover:-translate-y-2 delay-200">
              <h3 className="font-semibold text-lg text-gray-900">
                Full Visibility
              </h3>
              <p className="text-gray-600 mt-3">
                Monitor your transactions with complete history and analytics.
              </p>
            </div>

          </div>

        </div>

      </div>

      {/* Footer */}


    </div>
  );
}

export default HomePage;