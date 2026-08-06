import { useCallback, useEffect, useState } from "react";
import axios from "axios";
import {
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  BarChart, Bar, LineChart, Line,
} from "recharts";
import Navbar from "../components/Navbar";
import PageHeader from "../components/PageHeader";
import Skeleton from "../components/Skeleton";
import { getStatusChartColor } from "../utils/status";
import { useIsDarkMode } from "../utils/theme";

const CHART_COLORS = ["#ef4444", "#3b82f6", "#22c55e", "#f59e0b", "#6366f1", "#06b6d4", "#ec4899"];

const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";
const RANGE_STORAGE_KEY = "flashpay:statsDateRange";

function defaultDateRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - 29);
  const fmt = (d) => d.toISOString().slice(0, 10);
  return { from: fmt(from), to: fmt(to) };
}

// Read a previously-selected range from this browser tab's session so filters
// survive navigating away to another page and back, instead of resetting to
// the default 30-day window every time.
function initialDateRange() {
  try {
    const saved = sessionStorage.getItem(RANGE_STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      if (parsed?.from && parsed?.to) return parsed;
    }
  } catch {
    // ignore malformed/unavailable storage and fall back to the default range
  }
  return defaultDateRange();
}

// The immediately preceding period of equal length, used to compute trend
// deltas (e.g. "+12% vs previous period") for the range-scoped KPI cards.
function previousDateRange(range) {
  const from = new Date(range.from);
  const to = new Date(range.to);
  const spanDays = Math.max(1, Math.round((to - from) / 86400000) + 1);
  const prevTo = new Date(from);
  prevTo.setDate(prevTo.getDate() - 1);
  const prevFrom = new Date(prevTo);
  prevFrom.setDate(prevFrom.getDate() - (spanDays - 1));
  const fmt = (d) => d.toISOString().slice(0, 10);
  return { from: fmt(prevFrom), to: fmt(prevTo) };
}

function countByStatus(dashboardData, status) {
  return dashboardData?.statusDistribution?.find((s) => s.status === status)?.count ?? 0;
}

function totalCount(dashboardData) {
  return dashboardData?.statusDistribution?.reduce((sum, s) => sum + (s.count || 0), 0) ?? 0;
}

// Percentage change vs the previous period, used to render trend arrows on
// the KPI cards. Returns null when there's nothing meaningful to compare.
function trendDelta(current, previous) {
  if (previous === 0) return current > 0 ? { direction: "up", percent: 100 } : null;
  const percent = Math.round(((current - previous) / previous) * 100);
  if (percent === 0) return { direction: "flat", percent: 0 };
  return { direction: percent > 0 ? "up" : "down", percent: Math.abs(percent) };
}

function TrendBadge({ trend, goodDirection = "up" }) {
  if (!trend) return null;
  const isGood = trend.direction === goodDirection;
  const isFlat = trend.direction === "flat";
  const colorClass = isFlat
    ? "text-gray-500 dark:text-gray-400"
    : isGood
    ? "text-green-600 dark:text-green-400"
    : "text-red-500 dark:text-red-400";
  return (
    <span className={`inline-flex items-center gap-0.5 text-xs font-semibold ${colorClass}`}>
      {!isFlat && (
        <svg className="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
          <path strokeLinecap="round" strokeLinejoin="round" d={trend.direction === "up" ? "M5 15l7-7 7 7" : "M19 9l-7 7-7-7"} />
        </svg>
      )}
      {isFlat ? "No change" : `${trend.percent}%`}
    </span>
  );
}

function ChartCard({ title, children }) {
  return (
    <div className="bg-white/95 dark:bg-gray-900/95 backdrop-blur-sm border border-gray-200 dark:border-gray-800 rounded-2xl shadow-lg p-6">
      <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-200 mb-4">{title}</h3>
      {children}
    </div>
  );
}

function ChartPlaceholder({ message }) {
  return (
    <div className="h-56 flex items-center justify-center border-2 border-dashed border-gray-200 dark:border-gray-700 rounded-xl">
      <p className="text-gray-400 dark:text-gray-500 text-sm text-center px-4">{message}</p>
    </div>
  );
}

function StatsPage() {
  const dark = useIsDarkMode();
  const gridColor = dark ? "#374151" : "#e5e7eb";
  const axisColor = dark ? "#9ca3af" : "#6b7280";
  const tooltipStyle = dark
    ? { backgroundColor: "#1f2937", border: "1px solid #374151", borderRadius: 8, color: "#f3f4f6" }
    : { backgroundColor: "#ffffff", border: "1px solid #e5e7eb", borderRadius: 8, color: "#111827" };

  const todayStr = new Date().toISOString().slice(0, 10);
  const [range, setRange] = useState(initialDateRange);
  const [summary, setSummary] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [prevDashboard, setPrevDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Persist the selected range for this browser tab so it survives navigating
  // to another page and back instead of resetting to the default window.
  useEffect(() => {
    try {
      sessionStorage.setItem(RANGE_STORAGE_KEY, JSON.stringify(range));
    } catch {
      // sessionStorage may be unavailable (e.g. private browsing); non-fatal
    }
  }, [range]);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    const prevRange = previousDateRange(range);
    Promise.all([
      axios.get(`${API_BASE}/stats/payments`),
      axios.get(`${API_BASE}/stats/dashboard`, { params: { from: range.from, to: range.to } }),
      axios.get(`${API_BASE}/stats/dashboard`, { params: { from: prevRange.from, to: prevRange.to } }),
    ])
      .then(([summaryRes, dashboardRes, prevDashboardRes]) => {
        setSummary(summaryRes.data);
        setDashboard(dashboardRes.data);
        setPrevDashboard(prevDashboardRes.data);
      })
      .catch(() => setError("Could not load statistics. Is the backend running?"))
      .finally(() => setLoading(false));
  }, [range.from, range.to]);

  useEffect(() => { load(); }, [load]);

  const totalPaymentsCount = totalCount(dashboard);
  const successfulCount = countByStatus(dashboard, "COMPLETED");
  const failedCount = countByStatus(dashboard, "FAILED");
  const prevTotalCount = totalCount(prevDashboard);
  const prevSuccessfulCount = countByStatus(prevDashboard, "COMPLETED");
  const prevFailedCount = countByStatus(prevDashboard, "FAILED");

  const cards = summary ? [
    {
      title: "Total Payments",
      value: totalPaymentsCount,
      description: "In selected range",
      trend: trendDelta(totalPaymentsCount, prevTotalCount),
    },
    {
      title: "Successful Payments",
      value: successfulCount,
      description: "Completed transactions",
      accent: "text-green-600 dark:text-green-400",
      trend: trendDelta(successfulCount, prevSuccessfulCount),
    },
    {
      title: "Failed Payments",
      value: failedCount,
      description: "Requires attention",
      accent: "text-red-600 dark:text-red-400",
      trend: trendDelta(failedCount, prevFailedCount),
      goodDirection: "down",
    },
    { title: "Total Amount", value: Number(summary.totalAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "All time" },
    { title: "Average Amount", value: Number(summary.averageAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "Per payment, all time" },
    { title: "Largest Payment", value: Number(summary.largestAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "Highest single amount, all time" },
    {
      title: "Avg Processing Time",
      value: dashboard ? `${Number(dashboard.avgTotalProcessingSeconds || 0).toFixed(1)}s` : "—",
      description: "Created to completed/failed",
    },
  ] : Array.from({ length: 4 });

  return (

    <div className="min-h-screen relative overflow-hidden bg-gray-50 dark:bg-gray-950">


      {/* Navbar */}
      <div className="relative z-20">
        <Navbar />
      </div>



      {/* Background Image */}
      <div
        className="
          absolute
          inset-0
          bg-cover
          bg-center
          dark:opacity-20
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85 dark:bg-gray-950/90"></div>




      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">



        {/* Header + date range */}
        <PageHeader
          title="Payment Statistics"
          subtitle="Analyze your payment performance, transaction activity, success rates, and processed amounts."
          actions={
            <>
              <input
                type="date"
                aria-label="From date"
                value={range.from}
                max={range.to}
                onChange={(e) => setRange((r) => ({ ...r, from: e.target.value }))}
                className="px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-200 focus-visible:ring-2 focus-visible:ring-red-500 outline-none"
              />
              <span className="text-gray-400 dark:text-gray-500 text-sm">to</span>
              <input
                type="date"
                aria-label="To date"
                value={range.to}
                min={range.from}
                max={todayStr}
                onChange={(e) => setRange((r) => ({ ...r, to: e.target.value }))}
                className="px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-200 focus-visible:ring-2 focus-visible:ring-red-500 outline-none"
              />
            </>
          }
        />


        {error && (
          <div className="max-w-6xl mx-auto mb-6 rounded-xl border border-red-200 dark:border-red-500/30 bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400 text-sm px-4 py-3">
            {error}
          </div>
        )}


        {/* Stats Cards */}
        <div
          className="
            max-w-6xl
            mx-auto
            grid
            grid-cols-1
            sm:grid-cols-2
            lg:grid-cols-4
            gap-6
          "
        >

          {cards.map((stat, index) => (

            <div
              key={index}
              className="
                bg-white/95
                dark:bg-gray-900/95
                backdrop-blur-sm
                border border-gray-200 dark:border-gray-800
                rounded-2xl
                shadow-lg
                p-6
                hover:-translate-y-1
                transition
              "
            >

              <h3 className="text-sm text-gray-500 dark:text-gray-400">
                {loading ? <Skeleton className="h-4 w-24" /> : (stat?.title || "—")}
              </h3>


              <div className="flex items-baseline gap-2 mt-3">
                <p className={`text-3xl font-bold ${stat?.accent || "text-gray-900 dark:text-gray-100"}`}>
                  {loading ? <Skeleton className="h-8 w-20" /> : (stat?.value ?? "—")}
                </p>
                {!loading && <TrendBadge trend={stat?.trend} goodDirection={stat?.goodDirection || "up"} />}
              </div>


              <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">
                {loading ? <Skeleton className="h-4 w-32" /> : (stat?.description || "")}
              </p>


            </div>

          ))}


        </div>



        {/* Charts */}
        {loading ? (
          <div className="max-w-6xl mx-auto mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="bg-white/95 dark:bg-gray-900/95 backdrop-blur-sm border border-gray-200 dark:border-gray-800 rounded-2xl shadow-lg p-6">
                <Skeleton className="h-4 w-32 mb-4" />
                <Skeleton className="h-56 w-full" />
              </div>
            ))}
          </div>
        ) : (
        <div className="max-w-6xl mx-auto mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6">

          <ChartCard title="Status Distribution">
            {dashboard?.statusDistribution?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={dashboard.statusDistribution} dataKey="count" nameKey="status" innerRadius={55} outerRadius={85} paddingAngle={2}>
                    {dashboard.statusDistribution.map((entry) => (
                      <Cell key={entry.status} fill={getStatusChartColor(entry.status)} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={tooltipStyle} />
                  <Legend wrapperStyle={{ fontSize: 12, color: axisColor }} />
                </PieChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No payments in this date range yet." />}
          </ChartCard>

          <ChartCard title="Volume Over Time">
            {dashboard?.volumeOverTime?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <AreaChart data={dashboard.volumeOverTime}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11, fill: axisColor }} />
                  <YAxis tick={{ fontSize: 11, fill: axisColor }} allowDecimals={false} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Area type="monotone" dataKey="count" stroke="#3b82f6" fill="#3b82f6" fillOpacity={0.25} name="Payments" />
                </AreaChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No volume data in this date range yet." />}
          </ChartCard>

          <ChartCard title="Failure Reasons">
            {dashboard?.failureReasons?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.failureReasons}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="errorCode" tick={{ fontSize: 10, fill: axisColor }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: axisColor }} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="count" fill="#ef4444" radius={[4, 4, 0, 0]} name="Occurrences" />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No failed payments in this date range." />}
          </ChartCard>

          <ChartCard title="Average Time per Stage">
            {dashboard?.avgStageDuration?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.avgStageDuration}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="stage" tick={{ fontSize: 10, fill: axisColor }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis tick={{ fontSize: 11, fill: axisColor }} label={{ value: "sec", angle: -90, position: "insideLeft", fill: axisColor, fontSize: 11 }} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="avgSeconds" fill="#6366f1" radius={[4, 4, 0, 0]} name="Avg seconds" />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="Not enough completed stage transitions yet." />}
          </ChartCard>

          <ChartCard title="Success Rate Over Time">
            {dashboard?.successRateOverTime?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <LineChart data={dashboard.successRateOverTime}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11, fill: axisColor }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: axisColor }} unit="%" />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Line type="monotone" dataKey="successRate" stroke="#22c55e" strokeWidth={2} dot={false} name="Success rate" />
                </LineChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No terminal payments in this date range yet." />}
          </ChartCard>

          <ChartCard title="Currency Breakdown">
            {dashboard?.currencyBreakdown?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.currencyBreakdown}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="currency" tick={{ fontSize: 11, fill: axisColor }} />
                  <YAxis tick={{ fontSize: 11, fill: axisColor }} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="totalAmount" radius={[4, 4, 0, 0]} name="Total amount">
                    {dashboard.currencyBreakdown.map((entry, index) => (
                      <Cell key={entry.currency} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No currency data in this date range yet." />}
          </ChartCard>

          <ChartCard title="Top Senders">
            {dashboard?.topSenders?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.topSenders}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="accountNumber" tick={{ fontSize: 10, fill: axisColor }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: axisColor }} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} name="Payments sent" />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No sender activity in this date range yet." />}
          </ChartCard>

          <ChartCard title="Top Receivers">
            {dashboard?.topReceivers?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.topReceivers}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="accountNumber" tick={{ fontSize: 10, fill: axisColor }} interval={0} angle={-20} textAnchor="end" height={50} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: axisColor }} />
                  <Tooltip contentStyle={tooltipStyle} />
                  <Bar dataKey="count" fill="#22c55e" radius={[4, 4, 0, 0]} name="Payments received" />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No receiver activity in this date range yet." />}
          </ChartCard>

          <ChartCard title="Volume by Hour of Day">
            {dashboard?.volumeByHour?.length ? (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={dashboard.volumeByHour}>
                  <CartesianGrid stroke={gridColor} strokeDasharray="3 3" />
                  <XAxis dataKey="hour" tick={{ fontSize: 11, fill: axisColor }} tickFormatter={(h) => `${h}:00`} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: axisColor }} />
                  <Tooltip contentStyle={tooltipStyle} labelFormatter={(h) => `${h}:00`} />
                  <Bar dataKey="count" fill="#f59e0b" radius={[4, 4, 0, 0]} name="Payments" />
                </BarChart>
              </ResponsiveContainer>
            ) : <ChartPlaceholder message="No hourly data in this date range yet." />}
          </ChartCard>

        </div>
        )}


        {/* Info Banner */}
        <div
          className="
            max-w-6xl
            mx-auto
            mt-6
            bg-white/90
            dark:bg-gray-900/90
            backdrop-blur-sm
            border border-gray-200 dark:border-gray-800
            rounded-xl
            shadow-md
            p-5
          "
        >

          <p className="text-gray-700 dark:text-gray-300 text-sm">
            Payment analytics helps you understand transaction
            performance, monitor success rates, and identify trends
            for better financial decisions.
          </p>


        </div>



      </div>


    </div>

  );
}

export default StatsPage;