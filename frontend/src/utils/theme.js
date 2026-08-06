// Small shared dark-mode helper. Theme is applied as a `.dark` class on <html>
// (see the inline bootstrap script in index.html, which runs before React mounts
// to avoid a flash of the wrong theme) and persisted to localStorage.
import { useEffect, useState } from "react";

const STORAGE_KEY = "theme";

export function isDarkMode() {
  return document.documentElement.classList.contains("dark");
}

export function applyTheme(theme) {
  document.documentElement.classList.toggle("dark", theme === "dark");
}

export function toggleTheme() {
  const next = isDarkMode() ? "light" : "dark";
  localStorage.setItem(STORAGE_KEY, next);
  applyTheme(next);
  return next;
}

/**
 * React hook that stays in sync with the current theme, even when toggled
 * from a different component (e.g. the Navbar), by observing the `.dark`
 * class on <html>. Useful for anything that can't rely on Tailwind's
 * `dark:` variant, like inline SVG colors passed to chart libraries.
 */
export function useIsDarkMode() {
  const [dark, setDark] = useState(() => isDarkMode());

  useEffect(() => {
    const observer = new MutationObserver(() => setDark(isDarkMode()));
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ["class"] });
    return () => observer.disconnect();
  }, []);

  return dark;
}
