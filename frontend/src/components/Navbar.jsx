import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { ShoppingBag, LogOut, User, Store, Bike, UtensilsCrossed } from 'lucide-react';

export const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { totalQuantity } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadge = (role) => {
    switch (role) {
      case 'CUSTOMER':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800">Khách hàng</span>;
      case 'RESTAURANT':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-orange-100 text-orange-800">Nhà hàng</span>;
      case 'DRIVER':
        return <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800">Tài xế</span>;
      default:
        return null;
    }
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur border-b border-slate-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-orange-500 to-amber-400 flex items-center justify-center text-white shadow-md shadow-orange-500/20 group-hover:scale-105 transition">
            <UtensilsCrossed className="w-5 h-5" />
          </div>
          <div className="flex flex-col">
            <span className="font-extrabold text-lg tracking-tight text-slate-900 group-hover:text-orange-600 transition">
              PTIT <span className="text-orange-500">FoodDelivery</span>
            </span>
            <span className="text-[10px] text-slate-600 font-medium -mt-1">Dịch vụ giao đồ ăn trực tuyến</span>
          </div>
        </Link>

        {/* Center navigation */}
        <nav className="hidden md:flex items-center gap-6">
          {isAuthenticated && user?.role === 'CUSTOMER' && (
            <>
              <Link to="/customer" className="text-sm font-semibold text-slate-600 hover:text-orange-600 transition">
                Nhà hàng & Thực đơn
              </Link>
              <Link to="/customer/orders" className="text-sm font-semibold text-slate-600 hover:text-orange-600 transition">
                Đơn hàng của tôi
              </Link>
            </>
          )}

          {isAuthenticated && user?.role === 'RESTAURANT' && (
            <Link to="/restaurant/orders" className="text-sm font-semibold text-orange-600 flex items-center gap-1.5">
              <Store className="w-4 h-4" /> Đơn hàng của quán
            </Link>
          )}

          {isAuthenticated && user?.role === 'DRIVER' && (
            <Link to="/driver/dashboard" className="text-sm font-semibold text-blue-600 flex items-center gap-1.5">
              <Bike className="w-4 h-4" /> Bảng điều khiển tài xế
            </Link>
          )}
        </nav>

        {/* Right side controls */}
        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <>
              {user.role === 'CUSTOMER' && (
                <Link
                  to="/customer/checkout"
                  className="relative p-2 rounded-xl text-slate-700 hover:bg-slate-100 transition"
                  title="Giỏ hàng"
                >
                  <ShoppingBag className="w-5 h-5" />
                  {totalQuantity > 0 && (
                    <span className="absolute -top-1 -right-1 bg-orange-500 text-white text-[11px] font-bold w-5 h-5 rounded-full flex items-center justify-center shadow-md animate-pulse">
                      {totalQuantity}
                    </span>
                  )}
                </Link>
              )}

              <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
                <div className="flex flex-col text-right">
                  <div className="flex items-center gap-1.5 justify-end">
                    <span className="text-sm font-bold text-slate-800">{user.name}</span>
                    {getRoleBadge(user.role)}
                  </div>
                  <span className="text-xs text-slate-600">{user.email}</span>
                </div>

                <button
                  onClick={handleLogout}
                  className="p-2 text-slate-600 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition"
                  title="Đăng xuất"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-semibold text-slate-700 hover:text-orange-600 transition"
              >
                Đăng nhập
              </Link>
              <Link
                to="/register"
                className="px-4 py-2 text-sm font-semibold text-white bg-orange-500 hover:bg-orange-600 rounded-xl shadow-sm transition shadow-orange-500/20"
              >
                Đăng ký
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
