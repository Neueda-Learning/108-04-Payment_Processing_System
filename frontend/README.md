# FlashPay Frontend

React + Vite frontend for the Payment Processing System.

## Tech Stack

- React
- React Router
- Axios
- Tailwind CSS
- Vite

## Dependencies

Dependency declarations live in `package.json`.

### Runtime dependencies

- `react`
- `react-dom`
- `react-router-dom`
- `axios`

### Development dependencies

- `vite`
- `@vitejs/plugin-react`
- `tailwindcss`
- `@tailwindcss/vite`
- `oxlint`

Install/update dependencies from `frontend/`:

```bash
npm install
```

After dependency changes, run:

```bash
npm run build
```

to validate the production bundle.

## Run Locally

From the `frontend/` folder:

```bash
npm install
npm run dev
```

App URL:

```text
http://localhost:5173
```

## Scripts

- `npm run dev` - start development server
- `npm run build` - production build
- `npm run preview` - preview production build
- `npm run lint` - run linting

## Routes

- `/` - login page
- `/home` - landing dashboard page
- `/payments` - payment creation form
- `/history` - payment history page
- `/stats` - payment statistics page
- `/faq` - FAQ page

## Current Behavior

- Navigation and UI screens are implemented.
- Payment form currently logs data to console and does not submit to backend yet.
- History and stats currently use static placeholder data.

## Planned Backend Integration

Expected backend base URL:

```text
http://localhost:8080
```

Planned first integration points:

- create payment from `/payments`
- fetch payment by id for status/details
- fetch payment list/history for `/history`
- derive stats from backend payment data

## Folder Guide

- `src/components/` - shared UI components
- `src/pages/` - route-level pages
- `src/App.jsx` - router setup
- `src/index.css` - Tailwind import and global styles
