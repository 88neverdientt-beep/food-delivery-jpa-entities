import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axiosClient from '../../api/axiosClient';
import { Search, Store, MapPin, Phone, ArrowRight, Utensils, Star, Clock } from 'lucide-react';

export const CustomerHome = () => {
  const [restaurants, setRestaurants] = useState([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRestaurants();
  }, []);

  const fetchRestaurants = async () => {
    try {
      const data = await axiosClient.get('/restaurants');
      setRestaurants(data);
    } catch (err) {
      console.error('Lỗi tải danh sách quán:', err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = restaurants.filter(
    (r) =>
      r.restaurantName.toLowerCase().includes(search.toLowerCase()) ||
      r.address.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Hero Header */}
      <div className="relative rounded-3xl bg-gradient-to-r from-orange-600 via-orange-500 to-amber-500 text-white p-8 sm:p-12 mb-10 overflow-hidden shadow-xl shadow-orange-500/15">
        <div className="relative z-10 max-w-2xl">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/20 backdrop-blur text-xs font-semibold uppercase tracking-wider mb-3">
            ✨ Đặt món ăn chuẩn vị • Giao siêu tốc
          </span>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight mb-3">
            Hôm nay bạn muốn thưởng thức món gì?
          </h1>
          <p className="text-orange-100 text-sm sm:text-base leading-relaxed mb-6">
            Khám phá hàng chục nhà hàng uy tín, thực đơn phong phú cùng dịch vụ giao hàng tận nơi nhanh chóng.
          </p>

          {/* Search bar inside hero */}
          <div className="relative max-w-md">
            <Search className="w-5 h-5 text-slate-600 absolute left-4 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Tìm theo tên quán, địa chỉ..."
              className="w-full pl-12 pr-4 py-3 rounded-2xl bg-white text-slate-800 placeholder-slate-600 text-sm focus:outline-none shadow-lg focus:ring-4 focus:ring-white/30 transition"
            />
          </div>
        </div>

        {/* Decorative background circle */}
        <div className="absolute right-0 top-0 -mr-16 -mt-16 w-80 h-80 rounded-full bg-white/10 blur-2xl pointer-events-none" />
      </div>

      {/* Restaurant List Section */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <Store className="w-6 h-6 text-orange-500" /> Danh sách nhà hàng
          </h2>
          <p className="text-sm text-slate-600 mt-0.5">Các quán đang hoạt động nhận đơn</p>
        </div>
        <span className="text-xs font-semibold px-3 py-1 rounded-full bg-slate-100 text-slate-600">
          {filtered.length} nhà hàng
        </span>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-48 rounded-2xl bg-slate-200 animate-pulse" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
          <Utensils className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-700">Không tìm thấy quán nào</h3>
          <p className="text-sm text-slate-600 mt-1">Hãy thử tìm với từ khóa khác</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.map((restaurant) => (
            <Link
              key={restaurant.restaurantId}
              to={`/customer/restaurant/${restaurant.restaurantId}`}
              className="group bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-xl hover:border-orange-200 transition-all duration-300 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div className="w-12 h-12 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center font-bold text-xl group-hover:bg-orange-500 group-hover:text-white transition">
                    <Store className="w-6 h-6" />
                  </div>
                  <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200/60">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" /> Đang mở cửa
                  </span>
                </div>

                <h3 className="text-lg font-bold text-slate-900 group-hover:text-orange-600 transition tracking-tight">
                  {restaurant.restaurantName}
                </h3>

                <div className="mt-3 space-y-1.5 text-xs text-slate-600">
                  <div className="flex items-center gap-2">
                    <MapPin className="w-3.5 h-3.5 text-slate-600 flex-shrink-0" />
                    <span className="truncate">{restaurant.address}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Phone className="w-3.5 h-3.5 text-slate-600 flex-shrink-0" />
                    <span>{restaurant.phoneNumber}</span>
                  </div>
                </div>
              </div>

              <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between">
                <span className="text-xs font-bold text-orange-600 group-hover:translate-x-0.5 transition flex items-center gap-1">
                  Xem thực đơn
                </span>
                <div className="w-8 h-8 rounded-full bg-slate-50 group-hover:bg-orange-50 text-slate-600 group-hover:text-orange-600 flex items-center justify-center transition">
                  <ArrowRight className="w-4 h-4" />
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};
