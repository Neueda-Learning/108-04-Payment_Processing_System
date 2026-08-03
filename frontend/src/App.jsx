import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import HomePage from "./pages/HomePage";
import PaymentsPage from "./pages/PaymentsPage";
import PaymentHistory from "./pages/PaymentHistory";
import StatsPage from "./pages/StatsPage";
import FAQPage from "./pages/FaqPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route path="/history" element={<PaymentHistory />} />
        <Route path="/stats" element={<StatsPage />} />
        <Route path="/faq" element={<FAQPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;