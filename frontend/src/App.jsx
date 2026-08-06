import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import PaymentsPage from "./pages/PaymentsPage";
import PaymentProgressPage from "./pages/PaymentProgressPage";
import PaymentHistory from "./pages/PaymentHistory";
import StatsPage from "./pages/StatsPage";
import FAQPage from "./pages/FaqPage";
import ChatWidget from "./components/ChatWidget";

function AppRoutes() {
  const location = useLocation();
  const isLoggedOut = location.pathname === "/";

  return (
    <>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route path="/payment-progress" element={<PaymentProgressPage />} />
        <Route path="/history" element={<PaymentHistory />} />
        <Route path="/stats" element={<StatsPage />} />
        <Route path="/faq" element={<FAQPage />} />
      </Routes>
      {/* Chatbot launcher: reachable from every page except the logged-out login screen */}
      {!isLoggedOut && <ChatWidget />}
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}

export default App;