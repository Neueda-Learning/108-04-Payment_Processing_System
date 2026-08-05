import { useEffect, useState, useCallback } from "react";
import axios from "axios";
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
  Tooltip,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  BarChart,
  Bar,
  LineChart,
  Line,
} from "recharts";
import Navbar from "../components/Navbar";

const STATUS_COLORS = {
  CREATED: "#94a3b8",
  VALIDATED: "#38bdf8",
  SENT: "#a78bfa",
  COMPLETED: "#22c55e",
  FAILED: "#ef4444",
};

const CHART_COLORS = ["#6366f1", "#22c55e", "#f97316", "#ef4444", "#0ea5e9", "#a855f7"];

function toIsoDate(date) {
  return date.toISOString().slice(0, 10);
}

function defaultDateRange() {
  const to = new Date();
  const from = new Date();
  from.setDate(from.getDate() - 30);
  return { from: toIsoDate(from), to: toIsoDate(to) };
}

function StatsPage() {
  const [range, setRange] = useState(defaultDateRange);
  const [summary, setSummary] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchStats = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [summaryResponse, dashboardResponse] = await Promise.all([
        axios.get("http://localhost:8080/stats/payments"),
        axios.get("http://localhost:8080/stats/dashboard", {
          params: { from: range.from, to: range.to },
        }),
      ]);
      setSummary(summaryResponse.data);
      setDashboard(dashboardResponse.data);
    } catch (err) {
      setError("Unable to load payment analytics right now.");
      console.error("Failed to load stats", err);
    } finally {
      setLoading(false);
    }
  }, [range.from, range.to]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const stats = [
    {
      title: "Total Payments",
      value: summary ? summary.totalPayments : "—",
      description: "Payments processed",
    },
    {
      title: "Successful Payments",
      value: summary ? summary.successfulPayments : "—",
      description: "Completed transactions",
    },
    {
      title: "Failed Payments",
      value: summary ? summary.failedPayments : "—",
      description: "Requires attention",
    },
    {
      title: "Total Amount",
      value: summary ? `€${Number(summary.totalAmount ?? 0).toLocaleString()}` : "—",
      description: "Processed value",
    },
  ];

  const handleRangeChange = (field) => (event) => {
    setRange((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const isEmpty = (list) => !list || list.length === 0;

  return (

    <div className="min-h-screen relative overflow-hidden">


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
          animate-pulse
        "
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=1600&q=80')"
        }}
      ></div>



      {/* Overlay */}
      <div className="absolute inset-0 bg-white/85"></div>




      <div className="relative z-10 px-4 sm:px-6 py-10 pt-24">



        {/* Header */}
        <div className="max-w-6xl mx-auto mb-8 flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">

          <div>
            <h2 className="text-2xl sm:text-3xl font-bold text-gray-900">
              Payment Statistics
            </h2>

            <p className="text-gray-600 mt-2 max-w-xl">
              Analyze your payment performance, transaction activity,
              success rates, and processed amounts.
            </p>
          </div>

          {/* Date range controls */}
          <div className="flex items-center gap-2 bg-white/95 border border-gray-200 rounded-xl shadow p-3">
            <label className="text-sm text-gray-600">
              From
              <input
                type="date"
                value={range.from}
                onChange={handleRangeChange("from")}
                className="block mt-1 border border-gray-300 rounded-md px-2 py-1 text-sm"
              />
            </label>
            <label className="text-sm text-gray-600">
              To
              <input
                type="date"
                value={range.to}
                onChange={handleRangeChange("to")}
                className="block mt-1 border border-gray-300 rounded-md px-2 py-1 text-sm"
              />
            </label>
          </div>

        </div>

        {error && (
          <div className="max-w-6xl mx-auto mb-6 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl p-4">
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

          {stats.map((stat, index) => (

            <div
              key={index}
              className="
                bg-white/95
                backdrop-blur-sm
                border border-gray-200
                rounded-2xl
                shadow-lg
                p-6
                hover:-translate-y-1
                transition
              "
            >

              <h3 className="text-sm text-gray-500">
                {stat.title}
              </h3>


              <p className="text-3xl font-bold text-gray-900 mt-3">
                {stat.value}
              </p>


              <p className="text-sm text-gray-600 mt-2">
                {stat.description}
              </p>


            </div>

          ))}


        </div>


        {/* Charts Grid */}
        <div className="max-w-6xl mx-auto mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6">

          <ChartCard title="Payments by Status">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.statusDistribution) ? (
              <ChartPlaceholder text="No data for this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie
                    data={dashboard.statusDistribution}
                    dataKey="count"
                    nameKey="status"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={2}
                  >
                    {dashboard.statusDistribution.map((entry) => (
                      <Cell
                        key={entry.status}
                        fill={STATUS_COLORS[entry.status] ?? "#6366f1"}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

          <ChartCard title="Volume Over Time">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.volumeOverTime) ? (
              <ChartPlaceholder text="No data for this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <AreaChart data={dashboard.volumeOverTime}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Area type="monotone" dataKey="count" stroke="#6366f1" fill="#c7d2fe" />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

          <ChartCard title="Failure Reasons">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.failureReasons) ? (
              <ChartPlaceholder text="No failures in this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={dashboard.failureReasons}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="errorCode" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#ef4444" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

          <ChartCard title="Average Time Per Stage (seconds)">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.avgStageDuration) ? (
              <ChartPlaceholder text="No data for this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={dashboard.avgStageDuration}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="stage" tick={{ fontSize: 11 }} />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="avgSeconds" fill="#0ea5e9" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

          <ChartCard title="Success Rate Over Time (%)">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.successRateOverTime) ? (
              <ChartPlaceholder text="No data for this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <LineChart data={dashboard.successRateOverTime}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Line type="monotone" dataKey="successRate" stroke="#22c55e" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

          <ChartCard title="Currency Breakdown">
            {loading ? (
              <ChartPlaceholder text="Loading..." />
            ) : isEmpty(dashboard?.currencyBreakdown) ? (
              <ChartPlaceholder text="No data for this range" />
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={dashboard.currencyBreakdown}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="currency" />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="count" radius={[6, 6, 0, 0]}>
                    {dashboard.currencyBreakdown.map((entry, index) => (
                      <Cell key={entry.currency} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </ChartCard>

        </div>


        {/* Info Banner */}
        <div
          className="
            max-w-6xl
            mx-auto
            mt-6
            bg-white/90
            backdrop-blur-sm
            border border-gray-200
            rounded-xl
            shadow-md
            p-5
          "
        >

          <p className="text-gray-700 text-sm">
            Payment analytics helps you understand transaction
            performance, monitor success rates, and identify trends
            for better financial decisions.
          </p>


        </div>



      </div>


    </div>

  );
}

function ChartCard({ title, children }) {
  return (
    <div
      className="
        bg-white/95
        backdrop-blur-sm
        border border-gray-200
        rounded-2xl
        shadow-lg
        p-6
      "
    >
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{title}</h3>
      {children}
    </div>
  );
}

function ChartPlaceholder({ text }) {
  return (
    <div className="h-[260px] flex items-center justify-center border-2 border-dashed border-gray-200 rounded-xl">
      <p className="text-gray-400 text-center">{text}</p>
    </div>
  );
}

export default StatsPage;