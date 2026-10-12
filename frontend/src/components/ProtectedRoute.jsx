import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // Chuyển hướng về trang phù hợp với role
    if (user.role === 'CUSTOMER') return <Navigate to="/customer" replace />;
    if (user.role === 'RESTAURANT') return <Navigate to="/restaurant/orders" replace />;
    if (user.role === 'DRIVER') return <Navigate to="/driver/dashboard" replace />;
    return <Navigate to="/login" replace />;
  }

  return children;
};
