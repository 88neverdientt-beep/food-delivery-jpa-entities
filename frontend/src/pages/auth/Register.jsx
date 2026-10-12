import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import {
  UserPlus, User, Store, Bike, Mail, Lock, Phone, MapPin,
  ArrowRight, AlertCircle, Navigation
} from 'lucide-react';

export const Register = () => {
  const location = useLocation();
  const [role, setRole] = useState(location.state?.initialRole || 'CUSTOMER'); // 'CUSTOMER' | 'RESTAURANT' | 'DRIVER'

  // Form states
  const [formData, setFormData] = useState({
    // Chung
    email: '',
    password: '',
    phoneNumber: '',
    // Customer
    fullName: '',
    address: '',
    // Restaurant
    restaurantName: '',
    districtNo: 1,
    // Driver
    licensePlate: '',
  });

  const [districts, setDistricts] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDistricts = async () => {
      try {
        const data = await axiosClient.get('/districts');
        if (Array.isArray(data) && data.length > 0) {
          setDistricts(data);
        } else {
          setDistricts(Array.from({ length: 10 }, (_, i) => ({ districtNo: i + 1, districtName: `Quận ${i + 1}` })));
        }
      } catch (err) {
        setDistricts(Array.from({ length: 10 }, (_, i) => ({ districtNo: i + 1, districtName: `Quận ${i + 1}` })));
      }
    };
    fetchDistricts();
  }, []);

  const handleRoleChange = (newRole) => {
    setRole(newRole);
    setError('');
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    let endpoint = '/auth/customer/register';
    let payload = {};

    if (role === 'CUSTOMER') {
      endpoint = '/auth/customer/register';
      payload = {
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        address: formData.address,
      };
    } else if (role === 'RESTAURANT') {
      endpoint = '/auth/restaurant/register';
      payload = {
        restaurantName: formData.restaurantName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        address: formData.address,
        districtNo: Number(formData.districtNo),
      };
    } else if (role === 'DRIVER') {
      endpoint = '/auth/driver/register';
      payload = {
        fullName: formData.fullName,
        email: formData.email,
        password: formData.password,
        phoneNumber: formData.phoneNumber,
        licensePlate: formData.licensePlate,
      };
    }

    try {
      const data = await axiosClient.post(endpoint, payload);
      login(data);
      if (role === 'CUSTOMER') navigate('/customer');
      else if (role === 'RESTAURANT') navigate('/restaurant/orders');
      else if (role === 'DRIVER') navigate('/driver/dashboard');
    } catch (err) {
      console.error(err);
      const msg = err.response?.data?.message || err.response?.data?.error || 'Đăng ký không thành công. Vui lòng kiểm tra lại thông tin!';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 bg-gradient-to-br from-orange-50/50 via-slate-50 to-amber-50/50">
      <div className="w-full max-w-lg bg-white rounded-3xl shadow-xl border border-slate-100 p-8 my-6">
        <div className="text-center mb-6">
          <div className="inline-flex p-3 rounded-2xl bg-orange-100 text-orange-600 mb-3 shadow-inner">
            {role === 'CUSTOMER' && <UserPlus className="w-7 h-7" />}
            {role === 'RESTAURANT' && <Store className="w-7 h-7" />}
            {role === 'DRIVER' && <Bike className="w-7 h-7" />}
          </div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            {role === 'CUSTOMER' && 'Đăng ký tài khoản Khách hàng'}
            {role === 'RESTAURANT' && 'Đăng ký mở gian hàng Nhà hàng'}
            {role === 'DRIVER' && 'Đăng ký trở thành Đối tác Tài xế'}
          </h1>
          <p className="text-sm text-slate-600 mt-1">Chọn loại tài khoản và điền thông tin để bắt đầu</p>
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
          {/* Tên hiển thị */}
          {role === 'RESTAURANT' ? (
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Tên nhà hàng / quán ăn *
              </label>
              <div className="relative">
                <Store className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  name="restaurantName"
                  value={formData.restaurantName}
                  onChange={handleChange}
                  placeholder="Ví dụ: Cơm Tấm Sài Gòn, Bún Bò Huế..."
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                />
              </div>
            </div>
          ) : (
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Họ và tên *
              </label>
              <div className="relative">
                <User className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  name="fullName"
                  value={formData.fullName}
                  onChange={handleChange}
                  placeholder="Ví dụ: Nguyễn Văn A"
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                />
              </div>
            </div>
          )}

          {/* Email & Số điện thoại */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Email *
              </label>
              <div className="relative">
                <Mail className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="name@example.com"
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Số điện thoại *
              </label>
              <div className="relative">
                <Phone className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="tel"
                  required
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  placeholder="0912345678"
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                />
              </div>
            </div>
          </div>

          {/* Mật khẩu */}
          <div>
            <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
              Mật khẩu *
            </label>
            <div className="relative">
              <Lock className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                required
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Tối thiểu 6 ký tự"
                className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
              />
            </div>
          </div>

          {/* Riêng cho Driver: Biển số xe */}
          {role === 'DRIVER' && (
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Biển số xe *
              </label>
              <div className="relative">
                <Bike className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  name="licensePlate"
                  value={formData.licensePlate}
                  onChange={handleChange}
                  placeholder="Ví dụ: 29A-12345 hoặc 59B-67890"
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                />
              </div>
            </div>
          )}

          {/* Cho Customer & Restaurant: Địa chỉ */}
          {(role === 'CUSTOMER' || role === 'RESTAURANT') && (
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                {role === 'RESTAURANT' ? 'Địa chỉ quán *' : 'Địa chỉ mặc định *'}
              </label>
              <div className="relative">
                <MapPin className="w-5 h-5 text-slate-600 absolute left-3.5 top-3" />
                <textarea
                  rows={2}
                  required
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Số nhà, tên ngõ, đường, phường, quận..."
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition resize-none"
                />
              </div>
            </div>
          )}

          {/* Riêng cho Restaurant: Chọn quận */}
          {role === 'RESTAURANT' && (
            <div>
              <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                Khu vực quận của nhà hàng *
              </label>
              <div className="relative">
                <MapPin className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <select
                  required
                  name="districtNo"
                  value={formData.districtNo}
                  onChange={handleChange}
                  className="w-full pl-11 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 bg-white transition"
                >
                  {districts.map((d) => (
                    <option key={d.districtNo} value={d.districtNo}>
                      {d.districtName || `Quận ${d.districtNo}`}
                    </option>
                  ))}
                </select>
              </div>
              <p className="text-[11px] text-slate-600 mt-1">
                Phí giao hàng được tính tự động dựa trên khoảng cách số quận giữa nhà hàng và khách.
              </p>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-2 py-3 px-4 rounded-xl font-bold text-white bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 shadow-md shadow-orange-500/25 flex items-center justify-center gap-2 transition disabled:opacity-50"
          >
            {loading ? 'Đang đăng ký...' : 'Tạo tài khoản ngay'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <div className="mt-6 text-center">
          <span className="text-xs text-slate-600">Đã có tài khoản? </span>
          <Link to="/login" className="text-xs font-bold text-orange-600 hover:underline">
            Đăng nhập ngay
          </Link>
        </div>
      </div>
    </div>
  );
};
