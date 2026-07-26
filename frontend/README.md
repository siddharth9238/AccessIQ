# AccessIQ Frontend

Enterprise Vendor Compliance Management System frontend built with React 19 and TypeScript.

## Tech Stack

- **React 19** - Latest React with concurrent features
- **TypeScript** - Type-safe development
- **Vite** - Fast build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **Redux Toolkit** - State management
- **TanStack Query** - Server state management
- **React Hook Form** - Form handling
- **Zod** - Schema validation
- **Recharts** - Chart library
- **React Router DOM** - Routing
- **React Hot Toast** - Notifications
- **Framer Motion** - Animations

## Getting Started

### Prerequisites

- Node.js 18+
- npm 9+ or yarn 1.22+
- AccessIQ backend running

### Installation

```bash
cd frontend
npm install
```

### Development

```bash
npm run dev
```

The frontend will be available at `http://localhost:3000`

### Build

```bash
npm run build
```

### Preview

```bash
npm run preview
```

### Lint

```bash
npm run lint
```

### Format

```bash
npm run format
```

### Type Check

```bash
npm run type-check
```

### Tests

```bash
npm test
npm run test:coverage
```

## Environment Variables

Create `.env` file in the root:

```bash
VITE_API_URL=http://localhost:8080
```

## Project Structure

```
frontend/
├── public/
├── src/
│   ├── api/           # API client and endpoints
│   ├── assets/        # Static assets
│   ├── auth/          # Authentication components
│   ├── components/    # Reusable UI components
│   │   ├── charts/    # Chart components
│   │   ├── common/    # Common components
│   │   ├── forms/     # Form components
│   │   ├── layout/    # Layout components
│   │   ├── tables/    # Table components
│   │   └── ui/        # UI primitives
│   ├── context/       # React contexts
│   ├── hooks/         # Custom hooks
│   ├── layouts/       # Page layouts
│   ├── pages/         # Page components
│   │   ├── Dashboard/
│   │   ├── Login/
│   │   ├── Vendors/
│   │   ├── Compliance/
│   │   ├── Reports/
│   │   ├── Users/
│   │   ├── Settings/
│   │   ├── Profile/
│   │   └── Error/
│   ├── routes/        # Route configuration
│   ├── services/      # Business logic services
│   ├── store/         # Redux store
│   ├── styles/        # Global styles
│   ├── types/         # TypeScript types
│   ├── utils/         # Utility functions
│   └── main.tsx       # Entry point
├── vite.config.ts
├── tailwind.config.ts
└── tsconfig.json
```

## Features

### Authentication
- JWT-based authentication
- Token refresh
- Role-based access control
- Protected routes

### Dashboard
- KPI cards
- Charts and graphs
- Quick actions
- Recent activity

### Vendor Management
- Vendor listing with pagination
- Search and filtering
- View, create, update, delete

### Compliance
- Compliance checklist
- Approval workflow
- Risk analysis

### Reports
- PDF/Excel/CSV export
- Charts and summaries
- Custom date ranges

### Users & Roles
- User management
- Role assignment
- Permission control

## Integration with Backend

The frontend integrates with the Spring Boot backend via REST APIs:

- Authentication: `/api/v1/auth/*`
- Users: `/api/v1/users`
- Vendors: `/api/v1/vendors`
- Requests: `/api/v1/requests`
- Compliance: `/api/v1/compliance`
- Health: `/actuator/health`

## Design

- **Dark/Light Mode**: Toggle with keyboard shortcut or button
- **Responsive**: Works on desktop, tablet, and mobile
- **Accessibility**: WCAG compliant
- **Animations**: Smooth transitions with Framer Motion

## Styling

Uses Tailwind CSS with custom design tokens:

```css
:root {
  --background: 0 0% 100%;
  --foreground: 222.2 47.4% 11.2%;
  --primary: 221.2 83.3% 53.3%;
  /* ... more tokens */
}
```

## API Client

The API client includes:

- Automatic token injection
- Token refresh on 401
- Error handling
- Request/response interceptors

## State Management

- **Redux Toolkit**: Global state (auth, theme)
- **TanStack Query**: Server state (data fetching, caching)

## Testing

- **Vitest**: Unit testing
- **React Testing Library**: Component testing
- **Jest-like API**: Familiar testing experience

```bash
npm run test          # Run tests
npm run test:ui       # Open test UI
npm run test:coverage # Run with coverage
```

## Deployment

See [AWS-Deployment.md](../docs/AWS-Deployment.md) for production deployment instructions.

## License

MIT