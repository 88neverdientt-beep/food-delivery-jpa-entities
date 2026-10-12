import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';

// Pages
import { Login } from './pages/auth/Login';
import { Register } from './pages/auth/Register';
import { CustomerHome } from './pages/customer/CustomerHome';
import { RestaurantMenu } from './pages/customer/RestaurantMenu';
import { Checkout } from './pages/customer/Checkout';
import { OrderTracking } from './pages/customer/OrderTracking';
import { MyOrders } from './pages/customer/MyOrders';
import { RestaurantOrders } from './pages/restaurant/RestaurantOrders';
import { DriverDashboard } from './pages/driver/DriverDashboard';

const HomeRedirect = () => {
  const { user, isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user.role === 'CUSTOMER') return <Navigate to="/customer" replace />;
  if (user.role === 'RESTAURANT') return <Navigate to="/restaurant/orders" replace />;
  if (user.role === 'DRIVER') return <Navigate to="/driver/dashboard" replace />;
  return <Navigate to="/login" replace />;
};

export const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <div className="min-h-screen bg-slate-50 flex flex-col font-['Plus_Jakarta_Sans',sans-serif]">
            <Navbar />
            <main className="flex-1">
              <Routes>
                {/* Public Auth Routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/" element={<HomeRedirect />} />

                {/* Customer Routes (Protected) */}
                <Route
                  path="/customer"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <CustomerHome />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/customer/restaurant/:id"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <RestaurantMenu />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/customer/checkout"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <Checkout />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/customer/orders"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <MyOrders />
                    </ProtectedRoute>
                  }
                />
                <Route
                  path="/customer/orders/:id"
                  element={
                    <ProtectedRoute allowedRoles={['CUSTOMER']}>
                      <OrderTracking />
                    </ProtectedRoute>
                  }
                />

                {/* Restaurant Routes (Protected) */}
                <Route
                  path="/restaurant/orders"
                  element={
                    <ProtectedRoute allowedRoles={['RESTAURANT']}>
                      <RestaurantOrders />
                    </ProtectedRoute>
                  }
                />

                {/* Driver Routes (Protected) */}
                <Route
                  path="/driver/dashboard"
                  element={
                    <ProtectedRoute allowedRoles={['DRIVER']}>
                      <DriverDashboard />
                    </ProtectedRoute>
                  }
                />

                {/* Fallback */}
                <Route path="*" element={<HomeRedirect />} />
              </Routes>
            </main>
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
