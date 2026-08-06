import { useNavigate } from "react-router-dom";

/**
 * Shared page header: back button, title, subtitle, and an optional right-hand
 * slot for page-specific controls (e.g. date-range filters). Gives every
 * sub-page a consistent "where am I / how do I go back" affordance instead of
 * relying on the browser back button.
 */
function PageHeader({ title, subtitle, backTo = "/home", backLabel = "Back", actions }) {
  const navigate = useNavigate();

  return (
    <div className="max-w-6xl mx-auto mb-8 flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
      <div>
        <button
          onClick={() => navigate(backTo)}
          className="mb-3 inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-red-600 dark:text-gray-400 dark:hover:text-red-400 transition cursor-pointer focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-500 rounded"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
          </svg>
          {backLabel}
        </button>
        <h2 className="text-2xl sm:text-3xl font-bold text-gray-900 dark:text-gray-100">{title}</h2>
        {subtitle && <p className="text-gray-600 dark:text-gray-400 mt-2 max-w-xl text-sm sm:text-base">{subtitle}</p>}
      </div>

      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  );
}

export default PageHeader;
