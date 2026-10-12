import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import { User, Store, Bike, Lock, Mail, ArrowRight, AlertCircle, CheckCircle } from 'lucide-react';

export const Login = () => {
  const [role, setRole] = useState('CUSTOMER'); // 'CUSTOMER' | 'RESTAURANT' | 'DRIVER'
  const [email, setEmail] = useState('an.nguyen@example.com');
  const [password, setPassword] = useState('pass123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleRoleChange = (newRole) => {
    setRole(newRole);
    setError('');
    if (newRole === 'CUSTOMER') {
      setEmail('an.nguyen@example.com');
      setPassword('pass123');
    } else if (newRole === 'RESTAURANT') {
      setEmail('bunbo.hue@example.com');
      setPassword('pass123');
    } else if (newRole === 'DRIVER') {
      setEmail('tuan.driver@example.com');
      setPassword('pass123');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    let endpoint = '/auth/customer/login';
    if (role === 'RESTAURANT') endpoint = '/auth/restaurant/login';
    if (role === 'DRIVER') endpoint = '/auth/driver/login';

    try {
      const data = await axiosClient.post(endpoint, { email, password });
      login(data);
      if (role === 'CUSTOMER') navigate('/customer');
      else if (role === 'RESTAURANT') navigate('/restaurant/orders');
      else if (role === 'DRIVER') navigate('/driver/dashboard');
    } catch (err) {
      console.error(err);
      const msg = err.response?.data?.message || err.response?.data?.error || 'Đăng nhập không thành công. Kiểm tra lại email hoặc mật khẩu!';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-gradient-to-br from-orange-50/50 via-slate-50 to-amber-50/50">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-xl border border-slate-100 p-8">
        <div className="text-center mb-8">
          <div className="inline-flex p-3 rounded-2xl bg-orange-100 text-orange-600 mb-3 shadow-inner">
            {role === 'CUSTOMER' && <User className="w-7 h-7" />}
            {role === 'RESTAURANT' && <Store className="w-7 h-7" />}
            {role === 'DRIVER' && <Bike className="w-7 h-7" />}
          </div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">Đăng nhập hệ thống</h1>
          <p className="text-sm text-slate-600 mt-1">Chọn vai trò của bạn để tiếp tục</p>
        </div>

        {/* Role Selector Tabs */}
        <div className="grid grid-cols-3 gap-1.5 p-1.5 bg-slate-100 rounded-2xl mb-6">
          <button
            type="button"
            onClick={() => handleRoleChange('CUSTOMER')}
            className={`py-2 px-3 text-xs font-bold rounded-xl transition flex flex-col items-center gap-1 ${
              role === 'CUSTOMER'
                ? 'bg-white text-orange-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <User className="w-4 h-4" /> Khách hàng
          </button>
          <button
            type="button"
            onClick={() => handleRoleChange('RESTAURANT')}
            className={`py-2 px-3 text-xs font-bold rounded-xl transition flex flex-col items-center gap-1 ${
              role === 'RESTAURANT'
                ? 'bg-white text-orange-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Store className="w-4 h-4" /> Nhà hàng
          </button>
          <button
            type="button"
            onClick={() => handleRoleChange('DRIVER')}
            className={`py-2 px-3 text-xs font-bold rounded-xl transition flex flex-col items-center gap-1 ${
              role === 'DRIVER'
                ? 'bg-white text-orange-600 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Bike className="w-4 h-4" /> Tài xế
          </button>
        </div>

        {error && (
          <div className="mb-5 p-3.5 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-2.5">
            <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5 text-rose-500" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Email đăng nhập
            </label>
            <div className="relative">
              <Mail className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="example@ptit.edu.vn"
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Mật khẩu
            </label>
            <div className="relative">
              <Lock className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-2 py-3 px-4 rounded-xl font-bold text-white bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 shadow-md shadow-orange-500/25 flex items-center justify-center gap-2 transition disabled:opacity-50"
          >
            {loading ? 'Đang xử lý...' : 'Đăng nhập ngay'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        {/* Demo Fast Fill Pill */}
        <div className="mt-6 pt-5 border-t border-slate-100 text-center">
          <p className="text-xs text-slate-600 mb-2">Tài khoản mẫu từ cơ sở dữ liệu:</p>
          <div className="flex flex-wrap gap-1.5 justify-center">
            <button
              type="button"
              onClick={() => handleRoleChange('CUSTOMER')}
              className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition"
            >
              👤 an.nguyen (Khách)
            </button>
            <button
              type="button"
              onClick={() => handleRoleChange('RESTAURANT')}
              className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition"
            >
              🍲 bunbo.hue (Quán)
            </button>
            <button
              type="button"
              onClick={() => handleRoleChange('DRIVER')}
              className="text-[11px] px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium transition"
            >
              🛵 tuan.driver (Tài xế)
            </button>
          </div>
        </div>

        <div className="mt-4 text-center">
          <span className="text-xs text-slate-600">
            {role === 'CUSTOMER' && 'Chưa có tài khoản khách hàng? '}
            {role === 'RESTAURANT' && 'Chưa có tài khoản nhà hàng? '}
            {role === 'DRIVER' && 'Chưa có tài khoản tài xế? '}
          </span>
          <Link
            to="/register"
            state={{ initialRole: role }}
            className="text-xs font-bold text-orange-600 hover:underline"
          >
            {role === 'CUSTOMER' && 'Đăng ký ngay'}
            {role === 'RESTAURANT' && 'Đăng ký mở quán ngay'}
            {role === 'DRIVER' && 'Đăng ký làm tài xế ngay'}
          </Link>
        </div>
      </div>
    </div>
  );
};
