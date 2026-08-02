import { useNavigate } from "react-router-dom";

function Navbar() {
  const navigate = useNavigate();

  return (
    <nav className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between">

      {/* Brand */}
      <div
        className="flex items-center gap-2 cursor-pointer"
        onClick={() => navigate("/home")}
      >
        <div className="w-8 h-8 rounded-md bg-red-600"></div>

        <h1 className="text-lg font-semibold text-gray-900">
          FlashPay
        </h1>
      </div>


      {/* Navigation */}
      <div className="flex items-center gap-6 text-sm ">

        <button
          onClick={() => navigate("/history")}
          className="text-gray-600 hover:text-red-600 transition cursor-pointer"
        >
          History
        </button>

        <button
          onClick={() => navigate("/stats")}
          className="text-gray-600 hover:text-red-600 transition cursor-pointer"
        >
          Stats
        </button>

        <button
          onClick={() => navigate("/faq")}
          className="text-gray-600 hover:text-red-600 transition cursor-pointer"
        >
          FAQ
        </button>

        <button
          onClick={() => navigate("/")}
          className="
            ml-4
            px-4 py-2
            bg-red-600
            text-white
            rounded-md
            text-sm
            hover:bg-red-700 cursor-pointer
          "
        >
          Logout
        </button>

      </div>

    </nav>
  );
}

export default Navbar;