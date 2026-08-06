import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

function LoginPage() {

  const [account, setAccount] = useState("");
  const [showCreate, setShowCreate] = useState(false);

  const [newAccount, setNewAccount] = useState({
    accountNumber: "",
    accountHolderName: "",
    balance: "",
    accountCurrencyType: "USD",
    status: "ACTIVE"
  });

  const navigate = useNavigate();

  useEffect(() => {
    if (localStorage.getItem("account")) {
      navigate("/home", { replace: true });
    }
  }, [navigate]);


 const handleLogin = async () => {

  if (!account) {
    alert("Please enter account number");
    return;
  }

  try {

    await axios.get(`${import.meta.env.VITE_API_URL}/accounts/${account}`);

    localStorage.setItem("account", account);
    navigate("/home");

  } catch (error) {

    if (error.response && error.response.status === 404) {
      alert("Account not found");
    } else {
      alert("Something went wrong");
    }

  }

};


  const handleCreateAccount = async () => {

    try {
//console.log(newAccount);

      const response = await axios.post(
        `${import.meta.env.VITE_API_URL}/accounts/`,
        newAccount
      );

      //console.log("Account created:", response.data);

      alert("Account created successfully");

      setShowCreate(false);

    } catch (error) {

      console.error(error);
      alert("Account creation failed");

    }

  };


  return (
    <div className="min-h-screen relative flex items-center justify-center px-4 bg-gray-50 dark:bg-gray-950">

      {/* Background image */}
      <div
        className="absolute inset-0 bg-cover bg-center dark:opacity-20"
        style={{ backgroundImage: "url('https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1600&q=80')" }}
      />
      <div className="absolute inset-0 bg-white/80 dark:bg-gray-950/90 backdrop-blur-sm" />

      <div className="relative z-10 w-full max-w-sm">

        {/* Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-red-600 mb-4 shadow-lg shadow-red-200">
            <svg className="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">FlashPay</h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Fast • Secure • Instant Payments</p>
        </div>

        {/* Card */}
        <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 p-6">

          <h2 className="text-base font-semibold text-gray-800 dark:text-gray-200 mb-4">Login to your account</h2>

          <input
            type="text"
            placeholder="Enter account number"
            value={account}
            onChange={(e) => setAccount(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleLogin()}
            className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
          />

          <button
            onClick={handleLogin}
            className="w-full mt-3 bg-red-600 text-white py-3 rounded-xl text-sm font-semibold hover:bg-red-700 active:scale-95 transition shadow-sm shadow-red-200"
          >
            Login
          </button>

          <div className="flex items-center gap-3 my-4">
            <div className="flex-1 h-px bg-gray-100 dark:bg-gray-800" />
            <span className="text-xs text-gray-400 dark:text-gray-500">or</span>
            <div className="flex-1 h-px bg-gray-100 dark:bg-gray-800" />
          </div>

          <button
            onClick={() => setShowCreate(true)}
            className="w-full border border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-300 py-3 rounded-xl text-sm font-medium hover:bg-gray-50 dark:hover:bg-gray-800 active:scale-95 transition"
          >
            Create New Account
          </button>
        </div>

        <p className="text-center text-xs text-gray-400 dark:text-gray-500 mt-5">
          By continuing you agree to FlashPay's Terms & Privacy Policy
        </p>
      </div>

      {/* Create Account Popup */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50 px-4 pb-4 sm:pb-0">
          <div className="bg-white dark:bg-gray-900 rounded-2xl w-full max-w-sm p-6 shadow-2xl">

            <div className="flex items-center justify-between mb-5">
              <h2 className="text-base font-bold text-gray-900 dark:text-gray-100">Create Account</h2>
              <button onClick={() => setShowCreate(false)} className="w-8 h-8 flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 hover:bg-gray-200 dark:hover:bg-gray-700 text-sm transition">✕</button>
            </div>

            <div className="space-y-3">
              {[
                ["accountNumber", "Account Number"],
                ["accountHolderName", "Full Name"],
                ["balance", "Initial Balance"],
              ].map(([key, label]) => (
                <input
                  key={key}
                  placeholder={label}
                  value={newAccount[key]}
                  onChange={(e) => setNewAccount({ ...newAccount, [key]: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
                />
              ))}

              <select
                value={newAccount.accountCurrencyType}
                onChange={(e) => setNewAccount({ ...newAccount, accountCurrencyType: e.target.value })}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-gray-100 text-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:bg-white dark:focus:bg-gray-800 transition"
              >
                <option value="USD">USD – US Dollar</option>
                <option value="EUR">EUR – Euro</option>
                <option value="GBP">GBP – British Pound</option>
                <option value="INR">INR – Indian Rupee</option>
                <option value="JPY">JPY – Japanese Yen</option>
                <option value="AUD">AUD – Australian Dollar</option>
                <option value="CAD">CAD – Canadian Dollar</option>
                <option value="CHF">CHF – Swiss Franc</option>
                <option value="CNY">CNY – Chinese Yuan</option>
                <option value="MXN">MXN – Mexican Peso</option>
              </select>
            </div>

            <button
              onClick={handleCreateAccount}
              className="w-full mt-4 bg-red-600 text-white py-3 rounded-xl text-sm font-semibold hover:bg-red-700 active:scale-95 transition shadow-sm shadow-red-200"
            >
              Create Account
            </button>
            <button
              onClick={() => setShowCreate(false)}
              className="w-full mt-2 py-3 text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

    </div>
  );
}

export default LoginPage;
