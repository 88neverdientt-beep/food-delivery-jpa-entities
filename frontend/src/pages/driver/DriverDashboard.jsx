import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import {
  Bike, Power, MapPin, Store, Navigation, CheckCircle2,
  AlertCircle, RefreshCw, Package, ArrowRight, ShieldAlert, Phone,
  Filter, ChevronLeft, ChevronRight
} from 'lucide-react';

const TOTAL_DISTRICTS = 10;
const districts = Array.from({ length: TOTAL_DISTRICTS }, (_, i) => ({
  districtNo: i + 1,
  districtName: `Quận ${i + 1}`,
}));

export const DriverDashboard = () => {
  const { user } = useAuth();

  const [isOnline, setIsOnline] = useState(true);
  const [activeDeliveries, setActiveDeliveries] = useState([]);
  const [availableOrders, setAvailableOrders] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [page, setPage] = useState(0);
  const [selectedDistrict, setSelectedDistrict] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchActiveDeliveries();
    const interval = setInterval(fetchActiveDeliveries, 4000);
    return () => clearInterval(interval);
  }, [user]);

  useEffect(() => {
    fetchAvailableOrders(page, selectedDistrict);
  }, [page, selectedDistrict]);

  const fetchActiveDeliveries = async () => {
    try {
      const actives = await axiosClient.get(`/drivers/${user.id}/active-delivery`);
      setActiveDeliveries(Array.isArray(actives) ? actives : []);
    } catch (err) {
      console.error('Lỗi tải đơn đang giao:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableOrders = async (currentPage, currentDistrict) => {
    try {
      const params = { page: currentPage, size: 6 };
      if (currentDistrict) params.district = currentDistrict;
      const data = await axiosClient.get('/drivers/deliveries/available', { params });
      if (data && data.content) {
        setAvailableOrders(data.content);
        setTotalPages(data.totalPages || 1);
      } else if (Array.isArray(data)) {
        setAvailableOrders(data);
        setTotalPages(1);
      } else {
        setAvailableOrders([]);
      }
    } catch (err) {
      console.error('Lỗi tải đơn chờ tài xế:', err);
    }
  };

  const handleDistrictChange = (e) => {
    setSelectedDistrict(e.target.value);
    setPage(0);
  };

  const handleToggleOnline = async () => {
    try {
      setActionLoading(true);
      setError('');
      const newStatus = !isOnline;
      await axiosClient.patch(`/drivers/${user.id}/online`, { online: newStatus });
      setIsOnline(newStatus);
      setMsg(`Đã chuyển sang trạng thái: ${newStatus ? 'Sẵn sàng nhận đơn (Online)' : 'Nghỉ ngơi (Offline)'}`);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Lỗi thay đổi trạng thái online.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAcceptOrder = async (orderId) => {
    if (!isOnline) {
      setError('Vui lòng bật Online trước khi nhận đơn!');
      return;
    }
    try {
      setActionLoading(true);
      setError('');
      setMsg('');
      await axiosClient.post(`/deliveries/${orderId}/accept`, {
        driverId: user.id,
      });
      setMsg(`🎉 Nhận đơn #${orderId} thành công! Hãy đến quán để nhận hàng.`);
      fetchActiveDeliveries();
      fetchAvailableOrders(page, selectedDistrict);
    } catch (err) {
      console.error(err);
      if (err.response?.status === 409) {
        setError(`⚠️ Rất tiếc! Đơn #${orderId} vừa được một tài xế khác nhận trước.`);
      } else {
        setError(err.response?.data?.message || err.response?.data?.error || 'Không thể nhận đơn hàng này.');
      }
      fetchAvailableOrders(page, selectedDistrict);
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdateDeliveryStatus = async (deliveryId, newStatus) => {
    try {
      setActionLoading(true);
      setError('');
      await axiosClient.patch(`/deliveries/${deliveryId}/status`, { newStatus });
      setMsg(`Đã cập nhật trạng thái chuyến giao thành: ${newStatus}!`);
      fetchActiveDeliveries();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Lỗi cập nhật trạng thái chuyến giao.');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top Driver Control Card */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className={`w-16 h-16 rounded-2xl flex items-center justify-center font-bold text-white shadow-lg transition ${
            isOnline ? 'bg-gradient-to-tr from-emerald-500 to-teal-400 shadow-emerald-500/25' : 'bg-slate-400'
          }`}>
            <Bike className="w-8 h-8" />
          </div>
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold px-2 py-0.5 rounded-md bg-blue-100 text-blue-800">
                Tài xế đối tác
              </span>
              <span className={`text-xs font-bold flex items-center gap-1 ${isOnline ? 'text-emerald-600' : 'text-slate-600'}`}>
                <span className={`w-2 h-2 rounded-full ${isOnline ? 'bg-emerald-500 animate-ping' : 'bg-slate-400'}`} />
                {isOnline ? 'Đang hoạt động (Online)' : 'Đang nghỉ ngơi (Offline)'}
              </span>
            </div>
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">{user?.name}</h1>
            <p className="text-xs text-slate-500 mt-0.5">
              {isOnline ? 'Bạn đang nhận đơn. Hệ thống cập nhật mỗi 4 giây.' : 'Bật Online để nhận đơn giao hàng.'}
            </p>
          </div>
        </div>

        <button
          onClick={handleToggleOnline}
          disabled={actionLoading}
          className={`flex items-center gap-2.5 px-6 py-3 rounded-2xl font-bold text-sm shadow-lg transition disabled:opacity-50 ${
            isOnline
              ? 'bg-slate-100 hover:bg-red-50 text-slate-700 hover:text-red-700 border border-slate-200 hover:border-red-200'
              : 'bg-gradient-to-r from-emerald-500 to-teal-400 text-white hover:from-emerald-600 hover:to-teal-500 shadow-emerald-500/25'
          }`}
        >
          <Power className="w-5 h-5" />
          {isOnline ? 'Tạm nghỉ (Offline)' : 'Bật Online & Nhận đơn'}
        </button>
      </div>

      {/* Notifications */}
      {msg && (
        <div className="mb-6 p-4 bg-emerald-50 border border-emerald-200 rounded-2xl text-emerald-700 text-sm font-medium flex items-start gap-2.5">
          <CheckCircle2 className="w-5 h-5 flex-shrink-0 mt-0.5" />
          {msg}
        </div>
      )}
      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl text-red-700 text-sm font-medium flex items-start gap-2.5">
          <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
          {error}
        </div>
      )}

      {/* ─── PHẦN 1: ĐƠN ĐANG GIAO ────────────────────────────────────────────── */}
      {activeDeliveries.length > 0 && (
        <div className="mb-10">
          <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2 mb-4">
            <Package className="w-5 h-5 text-emerald-500" /> Đơn hàng đang giao ({activeDeliveries.length})
          </h2>
          {activeDeliveries.map((delivery) => (
            <div key={delivery.deliveryId} className="bg-white rounded-3xl border border-emerald-200 p-6 shadow-sm mb-4">
              <div className="flex items-start justify-between gap-3 mb-4">
                <div>
                  <span className="text-xs font-bold text-slate-500">Đơn #{delivery.orderId}</span>
                  <h3 className="text-base font-bold text-slate-900 mt-0.5">{delivery.restaurantName}</h3>
                </div>
                <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                  delivery.deliveryStatus === 'PICKED_UP'
                    ? 'bg-blue-50 text-blue-700 border border-blue-200'
                    : 'bg-amber-50 text-amber-700 border border-amber-200'
                }`}>
                  {delivery.deliveryStatus === 'PICKED_UP' ? '🚴 Đang giao' : '⏳ Đến lấy hàng'}
                </span>
              </div>

              <div className="bg-slate-50 rounded-2xl p-3 mb-4 text-xs text-slate-600 space-y-1.5">
                <div className="flex items-start gap-2">
                  <Store className="w-3.5 h-3.5 text-orange-500 flex-shrink-0 mt-0.5" />
                  <span>Nhà hàng: <strong>Q{delivery.restaurantDistrictNo || '?'}</strong></span>
                </div>
                <div className="flex items-start gap-2">
                  <MapPin className="w-3.5 h-3.5 text-blue-500 flex-shrink-0 mt-0.5" />
                  <span>Giao đến: <strong>Q{delivery.deliveryDistrictNo || '?'}</strong> – {delivery.deliveryAddress}</span>
                </div>
                <div className="flex justify-between">
                  <span>Phí giao hàng:</span>
                  <span className="font-bold text-slate-800">{Number(delivery.shippingFee || 0).toLocaleString('vi-VN')} đ</span>
                </div>
              </div>

              <div className="flex gap-3">
                {delivery.deliveryStatus === 'ASSIGNED' && (
                  <button
                    onClick={() => handleUpdateDeliveryStatus(delivery.deliveryId, 'PICKED_UP')}
                    disabled={actionLoading}
                    className="flex-1 py-2.5 px-4 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-bold text-xs shadow-md shadow-amber-500/20 flex items-center justify-center gap-1.5 transition disabled:opacity-50"
                  >
                    <Package className="w-4 h-4" /> Đã lấy hàng (PICKED_UP)
                  </button>
                )}
                {delivery.deliveryStatus === 'PICKED_UP' && (
                  <button
                    onClick={() => handleUpdateDeliveryStatus(delivery.deliveryId, 'DELIVERED')}
                    disabled={actionLoading}
                    className="flex-1 py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-md shadow-emerald-600/20 flex items-center justify-center gap-1.5 transition disabled:opacity-50"
                  >
                    <CheckCircle2 className="w-4 h-4" /> Đã giao thành công (DELIVERED)
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ─── PHẦN 2: DANH SÁCH ĐƠN CHỜ TÀI XẾ NHẬN ────────────────────────── */}
      <div>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <div className="flex items-center gap-2">
            <Navigation className="w-5 h-5 text-blue-500" />
            <h2 className="text-lg font-bold text-slate-900">
              Đơn hàng chờ tài xế (READY_FOR_PICKUP)
            </h2>
          </div>

          {/* District Filter */}
          <div className="flex items-center gap-3">
            <div className="relative">
              <Filter className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <select
                value={selectedDistrict}
                onChange={handleDistrictChange}
                className="pl-9 pr-8 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-700 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition cursor-pointer shadow-sm"
              >
                <option value="">Tất cả quận</option>
                {districts.map((d) => (
                  <option key={d.districtNo} value={d.districtNo}>
                    {d.districtName}
                  </option>
                ))}
              </select>
            </div>
            <span className="text-xs font-semibold px-3 py-1.5 rounded-xl bg-slate-100 text-slate-600">
              {availableOrders.length} đơn
            </span>
          </div>
        </div>

        {availableOrders.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
            <Bike className="w-12 h-12 text-slate-600 mx-auto mb-3" />
            <h3 className="text-base font-bold text-slate-800">Hiện không có đơn nào đang chờ</h3>
            <p className="text-xs text-slate-600 mt-1">
              {selectedDistrict ? `Không có đơn nào tại Quận ${selectedDistrict}` : 'Hệ thống sẽ tự động cập nhật ngay khi quán chuẩn bị xong món ăn!'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {availableOrders.map((order) => (
              <div
                key={order.orderId}
                className="bg-white rounded-3xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-3">
                    <div>
                      <span className="text-xs font-bold text-slate-600">Đơn #{order.orderId}</span>
                      <h4 className="font-bold text-base text-slate-900 mt-0.5">{order.restaurantName}</h4>
                    </div>
                    <span className="text-xs font-extrabold text-orange-600 px-2.5 py-1 rounded-full bg-orange-50">
                      {Number(order.totalAmount).toLocaleString('vi-VN')} đ
                    </span>
                  </div>

                  <div className="space-y-2 text-xs text-slate-600 bg-slate-50 p-3 rounded-2xl mb-4">
                    {/* District info */}
                    <div className="flex items-center justify-between">
                      <span className="flex items-center gap-1.5">
                        <Store className="w-3.5 h-3.5 text-orange-500" />
                        Nhà hàng:
                      </span>
                      <span className="font-bold text-slate-800">
                        {order.restaurantDistrictNo ? `Quận ${order.restaurantDistrictNo}` : 'Chưa phân quận'}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="flex items-center gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-blue-500" />
                        Giao đến:
                      </span>
                      <span className="font-bold text-slate-800">
                        {order.deliveryDistrictNo ? `Quận ${order.deliveryDistrictNo}` : 'Chưa phân quận'}
                      </span>
                    </div>
                    <div className="flex items-start gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-slate-600 flex-shrink-0 mt-0.5" />
                      <span className="truncate">{order.deliveryAddress}</span>
                    </div>
                    <div className="flex justify-between text-[11px] border-t border-slate-200 pt-2">
                      <span>Phí giao hàng:</span>
                      <span className="font-bold text-slate-800">
                        {Number(order.shippingFee).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                    <div className="flex justify-between text-[11px]">
                      <span>Số lượng món:</span>
                      <span className="font-bold text-slate-800">{order.items?.length || 0} món</span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => handleAcceptOrder(order.orderId)}
                  disabled={actionLoading || !isOnline}
                  className="w-full py-2.5 px-4 rounded-xl font-bold text-xs text-white bg-blue-600 hover:bg-blue-700 shadow-md shadow-blue-600/20 flex items-center justify-center gap-2 transition disabled:opacity-50"
                >
                  <Bike className="w-4 h-4" />
                  {!isOnline ? 'Bật online để nhận' : 'Nhận đơn ngay (First-come)'}
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="mt-6 flex items-center justify-center gap-3">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 disabled:opacity-40 flex items-center gap-1 shadow-sm transition"
            >
              <ChevronLeft className="w-4 h-4" /> Trước
            </button>
            <span className="text-xs font-semibold text-slate-600 px-3">
              Trang {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 disabled:opacity-40 flex items-center gap-1 shadow-sm transition"
            >
              Sau <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
