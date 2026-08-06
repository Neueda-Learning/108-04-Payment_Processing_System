import { useCallback, useEffect, useState } from "react";
import axios from "axios";
import {
  ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend,
  AreaChart, Area, XAxis, YAxis, CartesianGrid,
  BarChart, Bar, LineChart, Line,
} from "recharts";
import Navbar from "../components/Navbar";
import { getStatusChartColor } from "../utils/status";
import { useIsDarkMode } from "../utils/theme";

const CHART_COLORS = ["#ef4444", "#3b82f6", "#22c55e", "#f59e0b", "#6366f1", "#06b6d4", "#ec4899"];

function defaultDateRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - 29);
  const fmt = (d) => d.toISOString().slice(0, 10);
  return { from: fmt(from), to: fmt(to) };
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
  const [range, setRange] = useState(defaultDateRange());
  const [summary, setSummary] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    Promise.all([
      axios.get("http://localhost:8080/stats/payments"),
      axios.get("http://localhost:8080/stats/dashboard", { params: { from: range.from, to: range.to } }),
    ])
      .then(([summaryRes, dashboardRes]) => {
        setSummary(summaryRes.data);
        setDashboard(dashboardRes.data);
      })
      .catch(() => setError("Could not load statistics. Is the backend running?"))
      .finally(() => setLoading(false));
  }, [range.from, range.to]);

  useEffect(() => { load(); }, [load]);

  const cards = summary ? [
    { title: "Total Payments", value: summary.totalPayments, description: "Payments processed" },
    { title: "Successful Payments", value: summary.successfulPayments, description: "Completed transactions", accent: "text-green-600 dark:text-green-400" },
    { title: "Failed Payments", value: summary.failedPayments, description: "Requires attention", accent: "text-red-600 dark:text-red-400" },
    { title: "Total Amount", value: Number(summary.totalAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "Processed value" },
    { title: "Average Amount", value: Number(summary.averageAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "Per payment" },
    { title: "Largest Payment", value: Number(summary.largestAmount || 0).toLocaleString(undefined, { minimumFractionDigits: 2 }), description: "Highest single amount" },
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
        <div className="max-w-6xl mx-auto mb-8 flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">

          <div>
            <h2 className="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-gray-100">
              Payment Statistics
            </h2>

            <p className="text-gray-600 dark:text-gray-400 mt-2 max-w-xl">
              Analyze your payment performance, transaction activity,
              success rates, and processed amounts.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="date"
              aria-label="From date"
              value={range.from}
              max={range.to}
              onChange={(e) => setRange((r) => ({ ...r, from: e.target.value }))}
              className="px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-200"
            />
            <span className="text-gray-400 dark:text-gray-500 text-sm">to</span>
            <input
              type="date"
              aria-label="To date"
              value={range.to}
              min={range.from}
              max={todayStr}
              onChange={(e) => setRange((r) => ({ ...r, to: e.target.value }))}
              className="px-3 py-2 rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-sm text-gray-700 dark:text-gray-200"
            />
          </div>

        </div>


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
                {stat?.title || "—"}
              </h3>


              <p className={`text-3xl font-bold mt-3 ${stat?.accent || "text-gray-900 dark:text-gray-100"}`}>
                {loading ? "…" : (stat?.value ?? "—")}
              </p>


              <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">
                {stat?.description || ""}
              </p>


            </div>

          ))}


        </div>



        {/* Charts */}
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