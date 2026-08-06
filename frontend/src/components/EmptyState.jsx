/**
 * Shared "nothing to show yet" state for data views — keeps empty states
 * visually consistent instead of each page inventing its own copy/markup.
 */
function EmptyState({ title, description, icon = "📭" }) {
  return (
    <div className="text-center py-12">
      <p className="text-3xl" aria-hidden="true">{icon}</p>
      <p className="mt-3 text-gray-700 dark:text-gray-200 text-lg font-medium">{title}</p>
      {description && <p className="text-gray-500 dark:text-gray-400 text-sm mt-1">{description}</p>}
    </div>
  );
}

export default EmptyState;
