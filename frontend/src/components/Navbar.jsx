import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import axios from "axios";
import { useIsDarkMode, toggleTheme } from "../utils/theme";

const NAV_LINKS = [
  { label: "Home", path: "/home" },
  { label: "Pay", path: "/payments" },
  { label: "History", path: "/history" },
  { label: "Stats", path: "/stats" },
  { label: "FAQ", path: "/faq" },
];

function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const [profileOpen, setProfileOpen] = useState(false);
  const [accountInfo, setAccountInfo] = useState(null);
  const dark = useIsDarkMode();
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

  const handleToggleTheme = () => toggleTheme();

  return (
    <nav className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 px-4 sm:px-8 py-4 flex items-center justify-between transition-colors">

      {/* Brand */}
      <div
        className="flex items-center gap-2 cursor-pointer"
        onClick={() => navigate("/home")}
      >
        <div className="w-8 h-8 rounded-md bg-red-600"></div>
        <h1 className="text-lg font-semibold text-gray-900 dark:text-gray-100">FlashPay</h1>
      </div>

      {/* Navigation */}
      <div className="flex items-center gap-1 sm:gap-2 text-sm">

        {NAV_LINKS.map((link) => {
          const active = location.pathname === link.path;
          return (
            <button
              key={link.path}
              onClick={() => navigate(link.path)}
              className={`hidden sm:inline-block px-3 py-1.5 rounded-lg transition cursor-pointer font-medium ${
                active
                  ? "text-red-600 bg-red-50 dark:text-red-400 dark:bg-red-500/10"
                  : "text-gray-600 dark:text-gray-300 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10"
              }`}
            >
              {link.label}
            </button>
          );
        })}

        {/* Dark mode toggle */}
        <button
          onClick={handleToggleTheme}
          aria-label="Toggle dark mode"
          className="ml-1 flex h-8 w-8 items-center justify-center rounded-full border border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-300 hover:border-red-300 hover:bg-red-50 dark:hover:bg-gray-800 transition cursor-pointer"
        >
          {dark ? (
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          ) : (
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 1020.354 15.354z" />
            </svg>
          )}
        </button>

        {/* Profile avatar + dropdown */}
        <div className="relative ml-1" ref={dropdownRef}>
          <button
            onClick={() => setProfileOpen(o => !o)}
            className="flex items-center gap-2 rounded-full pl-1 pr-3 py-1 border border-gray-200 dark:border-gray-700 hover:border-red-300 hover:bg-red-50 dark:hover:bg-gray-800 transition cursor-pointer"
          >
            <div className="w-7 h-7 rounded-full bg-red-600 flex items-center justify-center text-white text-xs font-bold">
              {initials}
            </div>
            <span className="hidden sm:inline text-gray-700 dark:text-gray-200 text-sm font-medium max-w-[90px] truncate">
              {accountInfo?.accountHolderName?.split(" ")[0] || accountNumber || "Profile"}
            </span>
            <svg className={`h-3.5 w-3.5 text-gray-400 transition-transform ${profileOpen ? "rotate-180" : ""}`} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
            </svg>
          </button>

          {/* Dropdown */}
          {profileOpen && (
            <div className="absolute right-0 mt-2 w-64 bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-100 dark:border-gray-700 overflow-hidden z-50">
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
                  <span className="text-xs text-gray-500 dark:text-gray-400">Balance</span>
                  <span className="text-sm font-bold text-gray-900 dark:text-gray-100">
                    {accountInfo?.accountCurrencyType || ""} {accountInfo?.balance != null ? Number(accountInfo.balance).toLocaleString(undefined, { minimumFractionDigits: 2 }) : "—"}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500 dark:text-gray-400">Currency</span>
                  <span className="text-sm font-semibold text-gray-900 dark:text-gray-100">{accountInfo?.accountCurrencyType || "—"}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-gray-500 dark:text-gray-400">Status</span>
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${accountInfo?.status === "ACTIVE" ? "bg-green-100 text-green-700 dark:bg-green-500/10 dark:text-green-400" : "bg-gray-100 text-gray-500 dark:bg-gray-700 dark:text-gray-400"}`}>
                    {accountInfo?.status || "—"}
                  </span>
                </div>
              </div>

              <div className="border-t border-gray-100 dark:border-gray-700 px-4 py-3">
                <button
                  onClick={() => { localStorage.removeItem("account"); setProfileOpen(false); navigate("/", { replace: true }); }}
                  className="w-full text-sm text-red-600 dark:text-red-400 font-semibold hover:text-red-700 dark:hover:text-red-300 transition text-left cursor-pointer"
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

  