import { useEffect } from "react";

const VARIANT_CLASSES = {
  error: "bg-red-600 text-white",
  success: "bg-green-600 text-white",
  info: "bg-gray-900 text-white dark:bg-gray-800",
};

const VARIANT_ICON = {
  error: "✕",
  success: "✓",
  info: "ℹ",
};

/**
 * Lightweight in-app toast/alert, used in place of native `alert()` /
 * `confirm()` dialogs so error and success feedback matches the rest of the
 * product instead of a jarring, unstyled browser popup. Non-blocking and
 * auto-dismisses after `duration` ms (default 5s); can also be dismissed
 * manually.
 */
function Toast({ message, type = "info", onClose, duration = 5000 }) {
  useEffect(() => {
    if (!message || !duration) return;
    const t = setTimeout(onClose, duration);
    return () => clearTimeout(t);
  }, [message, duration, onClose]);

  if (!message) return null;

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`fixed top-5 left-1/2 -translate-x-1/2 z-[100] flex items-start gap-3 max-w-sm w-[calc(100vw-2rem)] rounded-xl px-4 py-3 shadow-2xl ${VARIANT_CLASSES[type] || VARIANT_CLASSES.info}`}
    >
      <span className="flex h-5 w-5 flex-shrink-0 items-center justify-center rounded-full bg-white/20 text-xs font-bold">
        {VARIANT_ICON[type] || VARIANT_ICON.info}
      </span>
      <p className="text-sm font-medium flex-1">{message}</p>
      <button
        onClick={onClose}
        aria-label="Dismiss notification"
        className="text-white/80 hover:text-white cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white rounded"
      >
        ✕
      </button>
    </div>
  );
}

export default Toast;
