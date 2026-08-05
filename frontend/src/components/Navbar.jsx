import { useNavigate } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import axios from "axios";

function Navbar() {
  const navigate = useNavigate();
  const [profileOpen, setProfileOpen] = useState(false);
  const [accountInfo, setAccountInfo] = useState(null);
  const dropdownRef = useRef(null);

  const accountNumber = localStorage.getItem("account");

  useEffect(() => {
    if (!accountNumber) return;
    axios.get(`http://localhost:8080/accounts/${accountNumber}`)
      .then(res => setAccountInfo(res.data))
      .catch(() => {});
  }, [accountNumber]);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setProfileOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const initials = accountInfo?.accountHolderName
    ? accountInfo.accountHolderName.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase()
    : accountNumber?.slice(0, 2).toUpperCase() || "?";

  return (
    <nav className="bg-white border-b border-gray-200 px-8 py-4 flex items-center justify-between">

      {/* Brand */}
      <div
        className="flex items-center gap-2 cursor-pointer"
        onClick={() => navigate("/home")}
      >
        <div className="w-8 h-8 rounded-md bg-red-600"></div>
        <h1 className="text-lg font-semibold text-gray-900">FlashPay</h1>
      </div>

      {/* Navigation */}
      <div className="flex items-center gap-6 text-sm">

        <button onClick={() => navigate("/history")} className="text-gray-600 hover:text-red-600 transition cursor-pointer">History</button>
        <button onClick={() => navigate("/stats")} className="text-gray-600 hover:text-red-600 transition cursor-pointer">Stats</button>
        <button onClick={() => navigate("/faq")} className="text-gray-600 hover:text-red-600 transition cursor-pointer">FAQ</button>

        {/* Profile avatar + dropdown */}
        <div className="relative ml-2" ref={dropdownRef}>
          <button
            onClick={() => setProfileOpen(o => !o)}
            className="flex items-center gap-2 rounded-full pl-1 pr-3 py-1 border border-gray-200 hover:border-red-300 hover:bg-red-50 transition"
          >
            <div className="w-7 h-7 rounded-full bg-red-600 flex items-center justify-center text-white text-xs font-bold">
              {initials}
            </div>
            <span className="text-gray-700 text-sm font-medium max-w-[90px] truncate">
              {accountInfo?.accountHolderName?.split(" ")[0] || accountNumber || "Profile"}
            </span>
            <svg className={`h-3.5 w-3.5 text-gray-400 transition-transform ${profileOpen ? "rotate-180" : ""}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {/* Dropdown */}
          {profileOpen && (
            <div className="absolute right-0 mt-2 w-64 bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden z-50">
              {/* Top gradient strip */}
              <div className="bg-red-600 px-4 py-4">
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 rounded-full bg-white/20 flex items-center justify-center text-white font-bold text-base">
                    {initials}
                  </div>
                  <div>
                    <p className="text-white font-semibold text-sm leading-tight">
                      {accountInfo?.accountHolderName || "—"}
                    </p>
                    <p className="text-red-200 text-xs mt-0.5">{accountNumber}</p>
                  </div>
                </div>
              </div>

              {/* Info rows */}
              <div className="px-4 py-3 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500">Balance</span>
                  <span className="text-sm font-bold text-gray-900">
                    {accountInfo?.accountCurrencyType || ""} {accountInfo?.balance != null ? Number(accountInfo.balance).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "—"}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500">Currency</span>
                  <span className="text-sm font-semibold text-gray-900">{accountInfo?.accountCurrencyType || "—"}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500">Status</span>
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${accountInfo?.status === "ACTIVE" ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}>
                    {accountInfo?.status || "—"}
                  </span>
                </div>
              </div>

              <div className="border-t border-gray-100 px-4 py-3">
                <button
                  onClick={() => { setProfileOpen(false); navigate("/"); }}
                  className="w-full text-sm text-red-600 font-semibold hover:text-red-700 transition text-left"
                >
                  Logout →
                </button>
              </div>
            </div>
          )}
        </div>

      </div>
    </nav>
  );
}

export default Navbar;

  