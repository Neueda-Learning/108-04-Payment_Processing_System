/**
 * Simple pulsing placeholder block used while data is loading. Compose with
 * utility classes to match the shape of the content it's standing in for
 * (e.g. `<Skeleton className="h-4 w-24" />` for a text line).
 */
function Skeleton({ className = "" }) {
  return (
    <div
      aria-hidden="true"
      className={`animate-pulse rounded-md bg-gray-200 dark:bg-gray-800 ${className}`}
    />
  );
}

export default Skeleton;
