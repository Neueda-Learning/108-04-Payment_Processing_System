import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

const QUICK_LINKS = [
  {
    title: "Payment History",
    description: "Review past transactions, statuses, and receiver details.",
    path: "/history",
  },
  {
    title: "Analytics",
    description: "Track success rates, volume trends, and currency breakdowns.",
    path: "/stats",
  },
  {
    title: "FAQ & Assistant",
    description: "Get quick answers about payments and account activity.",
    path: "/faq",
  },
];

function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex flex-col relative">

      {/* Navbar */}
      <div className="relative z-20">
        <Navbar />
      </div>

      {/* Background Image */}
      <div
        className="absolute inset-0 bg-cover bg-center dark:opacity-20"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1600&q=80')",
        }}
      ></div>

      {/* Overlay */}
      <div className="absolute inset-0 bg-white/80 dark:bg-gray-950/90"></div>

      {/* Main Content */}
      <div className="relative flex-1 z-10">

        {/* Hero */}
        <div className="max-w-5xl mx-auto px-6 pt-20">
          <div className="max-w-2xl">
            <h1 className="text-4xl font-bold text-gray-900 dark:text-gray-100 leading-tight">
              Fast and Secure Payments
            </h1>

            <p className="text-gray-600 dark:text-gray-400 mt-4 text-lg">
              Send money instantly with a simple and reliable payment system.
              Track transactions, view history, and manage everything in one place.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <button
                onClick={() => navigate("/payments")}
                className="px-6 py-3 bg-red-600 text-white rounded-md font-medium hover:bg-red-700 active:scale-95 transition cursor-pointer shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500"
              >
                Make Payment
              </button>
              <button
                onClick={() => navigate("/history")}
                className="px-6 py-3 bg-white dark:bg-gray-900 text-gray-700 dark:text-gray-200 border border-gray-200 dark:border-gray-700 rounded-md font-medium hover:border-red-300 hover:text-red-600 dark:hover:text-red-400 active:scale-95 transition cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500"
              >
                View History
              </button>
            </div>
          </div>
        </div>

        {/* Quick-access cards */}
        <div className="max-w-6xl mx-auto px-6 mt-20 pb-20">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {QUICK_LINKS.map((card) => (
              <button
                key={card.path}
                onClick={() => navigate(card.path)}
                className="text-left bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-8 shadow-md transform transition duration-300 hover:-translate-y-2 hover:border-red-200 dark:hover:border-red-500/30 hover:shadow-lg cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500"
              >
                <h3 className="font-semibold text-lg text-gray-900 dark:text-gray-100">
                  {card.title}
                </h3>
                <p className="text-gray-600 dark:text-gray-400 mt-3">
                  {card.description}
                </p>
                <span className="mt-4 inline-flex items-center gap-1 text-sm font-medium text-red-600 dark:text-red-400">
                  Open
                  <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
                  </svg>
                </span>
              </button>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}

export default HomePage;