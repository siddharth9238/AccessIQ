import React, { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAppSelector } from '@/store/hooks';
import { Toaster } from 'react-hot-toast';
import MainLayout from '@/layouts/MainLayout';
import LoginPage from '@/pages/Login';
import Dashboard from '@/pages/Dashboard';
import Vendors from '@/pages/Vendors';
import Compliance from '@/pages/Compliance';
import Reports from '@/pages/Reports';
import Users from '@/pages/Users';
import Settings from '@/pages/Settings';
import Profile from '@/pages/Profile';
import Error404 from '@/pages/Error/404';
import Error403 from '@/pages/Error/403';
import Error401 from '@/pages/Error/401';
import ProtectedRoute from '@/components/common/ProtectedRoute';
import { ThemeProvider } from '@/context/ThemeContext';

function App() {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  return (
    <ThemeProvider>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/login" element={!isAuthenticated ? <LoginPage /> : <Navigate to="/" />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="vendors" element={<Vendors />} />
            <Route path="compliance" element={<Compliance />} />
            <Route path="reports" element={<Reports />} />
            <Route path="users" element={<Users />} />
            <Route path="settings" element={<Settings />} />
            <Route path="profile" element={<Profile />} />
          </Route>
          <Route path="/401" element={<Error401 />} />
          <Route path="/403" element={<Error403 />} />
          <Route path="/*" element={<Error404 />} />
        </Routes>
        <Toaster position="top-right" />
      </div>
    </ThemeProvider>
  );
}

export default App;