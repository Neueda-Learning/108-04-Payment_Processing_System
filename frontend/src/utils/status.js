// Centralized semantic color coding for payment lifecycle statuses, so the
// "polarity" of a payment (good/bad/in-progress) reads consistently across
// the whole app: green = success, red = failure, amber = in-flight,
// blue/indigo = early/neutral stages.
const STATUS_BADGE_CLASSES = {
  CREATED:
    "bg-blue-50 text-blue-700 border-blue-200 dark:bg-blue-500/10 dark:text-blue-300 dark:border-blue-500/30",
  VALIDATED:
    "bg-indigo-50 text-indigo-700 border-indigo-200 dark:bg-indigo-500/10 dark:text-indigo-300 dark:border-indigo-500/30",
  SENT:
    "bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-500/10 dark:text-amber-300 dark:border-amber-500/30",
  COMPLETED:
    "bg-green-50 text-green-700 border-green-200 dark:bg-green-500/10 dark:text-green-300 dark:border-green-500/30",
  FAILED:
    "bg-red-50 text-red-700 border-red-200 dark:bg-red-500/10 dark:text-red-300 dark:border-red-500/30",
};

const DEFAULT_BADGE_CLASSES =
  "bg-gray-50 text-gray-600 border-gray-200 dark:bg-gray-500/10 dark:text-gray-300 dark:border-gray-500/30";

export function getStatusBadgeClasses(status) {
  return STATUS_BADGE_CLASSES[String(status || "").toUpperCase()] || DEFAULT_BADGE_CLASSES;
}

// Hex palette for chart libraries (e.g. recharts) that render outside the DOM
// class system and need literal color values instead of Tailwind classes.
export const STATUS_CHART_COLORS = {
  CREATED: "#3b82f6",
  VALIDATED: "#6366f1",
  SENT: "#f59e0b",
  COMPLETED: "#22c55e",
  FAILED: "#ef4444",
};

export function getStatusChartColor(status) {
  return STATUS_CHART_COLORS[String(status || "").toUpperCase()] || "#9ca3af";
}
