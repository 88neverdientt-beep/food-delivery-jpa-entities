import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import {
  Bike, Power, MapPin, Store, Navigation, CheckCircle2,
  AlertCircle, RefreshCw, Package, ArrowRight, ShieldAlert, Phone
} from 'lucide-react';

export const DriverDashboard = () => {
  const { user } = useAuth();

  const [isOnline, setIsOnline] = useState(true);
  const [activeDeliveries, setActiveDeliveries] = useState([]);
  const [availableOrders, setAvailableOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDriverStatusAndOrders();
    const interval = setInterval(fetchDriverStatusAndOrders, 4000);
    return () => clearInterval(interval);
  }, [user]);

  const fetchDriverStatusAndOrders = async () => {
    try {
      // 1. Kiểm tra đơn đang giao
      const actives = await axiosClient.get(`/drivers/${user.id}/active-delivery`);
      setActiveDeliveries(actives);

      // 2. Lấy danh sách đơn chờ tài xế nhận
      const pool = await axiosClient.get('/drivers/deliveries/available');
      setAvailableOrders(pool);
    } catch (err) {
      console.error('Lỗi cập nhật dữ liệu tài xế:', err);
    } finally {
      setLoading(false);
    }
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
      fetchDriverStatusAndOrders();
    } catch (err) {
      console.error(err);
      if (err.response?.status === 409) {
        setError(`⚠️ Rất tiếc! Đơn #${orderId} vừa được một tài xế khác nhận trước.`);
      } else {
        setError(err.response?.data?.message || err.response?.data?.error || 'Không thể nhận đơn hàng này.');
      }
      fetchDriverStatusAndOrders();
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
      fetchDriverStatusAndOrders();
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
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">{user.name}</h1>
            <p className="text-xs text-slate-600">{user.email}</p>
          </div>
        </div>

        {/* Online Toggle Button */}
        <div className="flex items-center gap-3">
          <button
            onClick={fetchDriverStatusAndOrders}
            className="p-3 rounded-2xl border border-slate-200 hover:bg-slate-100 text-slate-600 transition"
            title="Làm mới dữ liệu"
          >
            <RefreshCw className="w-4 h-4" />
          </button>

          <button
            onClick={handleToggleOnline}
            disabled={actionLoading}
            className={`px-5 py-3 rounded-2xl font-bold text-xs flex items-center gap-2 shadow-lg transition ${
              isOnline
                ? 'bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 shadow-rose-500/10'
                : 'bg-emerald-600 text-white hover:bg-emerald-700 shadow-emerald-600/20'
            }`}
          >
            <Power className="w-4 h-4" />
            {isOnline ? 'Chuyển sang Offline' : 'Bật Online nhận đơn'}
          </button>
        </div>
      </div>

      {msg && (
        <div className="mb-6 p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <span>{msg}</span>
          <button onClick={() => setMsg('')} className="text-emerald-600 font-bold text-xs">Đóng</button>
        </div>
      )}

      {error && (
        <div className="mb-6 p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-2.5">
          <ShieldAlert className="w-5 h-5 flex-shrink-0 text-rose-500 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* ─── PHẦN 1: ĐƠN ĐANG GIAO CỦA TÀI XẾ ────────────────────────────────── */}
      {activeDeliveries.length > 0 && (
        <div className="mb-10">
          <div className="flex items-center gap-2 mb-4">
            <Package className="w-5 h-5 text-orange-500" />
            <h2 className="text-lg font-bold text-slate-900">Đơn hàng đang thực hiện ({activeDeliveries.length})</h2>
          </div>

          <div className="space-y-4">
            {activeDeliveries.map((delivery) => (
              <div
                key={delivery.deliveryId}
                className="bg-white rounded-3xl border-2 border-orange-300 p-6 shadow-lg shadow-orange-500/5 relative overflow-hidden"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
                  <div>
                    <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-orange-100 text-orange-800">
                      Chuyến giao #{delivery.deliveryId} • Đơn #{delivery.orderId}
                    </span>
                    <h3 className="text-xl font-extrabold text-slate-900 mt-2">
                      Trạng thái giao: <span className="text-orange-600">{delivery.deliveryStatus}</span>
                    </h3>
                  </div>

                  {/* Delivery Action Buttons */}
                  <div className="flex items-center gap-2">
                    {delivery.deliveryStatus === 'ASSIGNED' && (
                      <button
                        onClick={() => handleUpdateDeliveryStatus(delivery.deliveryId, 'PICKED_UP')}
                        disabled={actionLoading}
                        className="px-5 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs shadow-md shadow-orange-500/20 transition flex items-center gap-1.5"
                      >
                        <Store className="w-4 h-4" /> Đã lấy hàng tại quán (PICKED_UP)
                      </button>
                    )}

                    {delivery.deliveryStatus === 'PICKED_UP' && (
                      <button
                        onClick={() => handleUpdateDeliveryStatus(delivery.deliveryId, 'DELIVERED')}
                        disabled={actionLoading}
                        className="px-5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-md shadow-emerald-600/20 transition flex items-center gap-1.5"
                      >
                        <CheckCircle2 className="w-4 h-4" /> Đã giao thành công (DELIVERED)
                      </button>
                    )}
                  </div>
                </div>

                <div className="pt-4 grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                  <div className="bg-slate-50 p-3.5 rounded-2xl">
                    <span className="font-bold text-slate-700 block mb-1">Gợi ý lộ trình:</span>
                    <p className="text-slate-600">
                      1. Di chuyển tới nhà hàng để nhận món ăn đã đóng gói.
                    </p>
                    <p className="text-slate-600 mt-1">
                      2. Sau khi nhận, bấm "Đã lấy hàng" để thông báo cho khách.
                    </p>
                    <p className="text-slate-600 mt-1">
                      3. Giao tới tận tay khách hàng và bấm "Đã giao thành công".
                    </p>
                  </div>

                  <div className="bg-slate-50 p-3.5 rounded-2xl flex flex-col justify-center">
                    <span className="font-bold text-slate-700 block mb-1">Thời gian nhận chuyến:</span>
                    <p className="text-slate-600">
                      {delivery.pickupTime ? new Date(delivery.pickupTime).toLocaleString('vi-VN') : 'Đang tới quán'}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ─── PHẦN 2: DANH SÁCH ĐƠN CHỜ TÀI XẾ NHẬN ────────────────────────── */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Navigation className="w-5 h-5 text-blue-500" />
            <h2 className="text-lg font-bold text-slate-900">
              Đơn hàng chờ tài xế (READY_FOR_PICKUP)
            </h2>
          </div>
          <span className="text-xs font-semibold px-3 py-1 rounded-full bg-slate-100 text-slate-600">
            {availableOrders.length} đơn có thể nhận
          </span>
        </div>

        {availableOrders.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
            <Bike className="w-12 h-12 text-slate-600 mx-auto mb-3" />
            <h3 className="text-base font-bold text-slate-800">Hiện không có đơn nào đang chờ</h3>
            <p className="text-xs text-slate-600 mt-1">
              Hệ thống sẽ tự động cập nhật ngay khi quán chuẩn bị xong món ăn!
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
                    <div className="flex items-start gap-1.5">
                      <MapPin className="w-3.5 h-3.5 text-slate-600 flex-shrink-0 mt-0.5" />
                      <span className="truncate">Giao đến: {order.deliveryAddress}</span>
                    </div>
                    <div className="flex justify-between text-[11px] text-slate-600">
                      <span>Phí giao hàng:</span>
                      <span className="font-bold text-slate-800">
                        {Number(order.shippingFee).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                    <div className="flex justify-between text-[11px] text-slate-600">
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
      </div>
    </div>
  );
};
