import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import {
  Store, Clock, CheckCircle2, AlertCircle, RefreshCw, XCircle,
  ChefHat, Bike, Filter, MapPin
} from 'lucide-react';

export const RestaurantOrders = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    fetchOrders();
    const interval = setInterval(fetchOrders, 5000);
    return () => clearInterval(interval);
  }, [user]);

  const fetchOrders = async () => {
    try {
      const data = await axiosClient.get(`/restaurants/${user.id}/orders`);
      setOrders(data);
    } catch (err) {
      console.error('Lỗi tải danh sách đơn của quán:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAdvanceStatus = async (orderId, newStatus) => {
    try {
      setActionLoading(true);
      setError('');
      await axiosClient.patch(`/orders/${orderId}/status?restaurantId=${user.id}`, {
        newStatus,
      });
      setMsg(`Đã chuyển đơn #${orderId} sang trạng thái ${newStatus}!`);
      fetchOrders();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Lỗi cập nhật trạng thái đơn.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm(`Bạn có chắc chắn muốn hủy đơn hàng #${orderId}?`)) return;
    try {
      setActionLoading(true);
      setError('');
      await axiosClient.patch(`/orders/${orderId}/cancel`, {
        actorId: user.id,
        role: 'RESTAURANT',
      });
      setMsg(`Đã hủy đơn hàng #${orderId} thành công.`);
      fetchOrders();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Lỗi khi hủy đơn hàng.');
    } finally {
      setActionLoading(false);
    }
  };

  const filteredOrders = orders.filter((o) => {
    if (filter === 'ALL') return true;
    if (filter === 'PENDING') return o.orderStatus === 'PENDING';
    if (filter === 'IN_PROGRESS') return o.orderStatus === 'CONFIRMED' || o.orderStatus === 'PREPARING';
    if (filter === 'READY_FOR_PICKUP') return o.orderStatus === 'READY_FOR_PICKUP';
    if (filter === 'DONE') return o.orderStatus === 'DELIVERING' || o.orderStatus === 'COMPLETED' || o.orderStatus === 'CANCELLED';
    return true;
  });

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <Store className="w-6 h-6 text-orange-500" /> Quản lý đơn hàng quán ({user.name})
          </h1>
          <p className="text-xs text-slate-600 mt-1">Xác nhận, nấu món và bàn giao cho tài xế</p>
        </div>

        <button
          onClick={fetchOrders}
          className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 shadow-sm transition"
        >
          <RefreshCw className="w-3.5 h-3.5 text-orange-500" /> Tự động cập nhật
        </button>
      </div>

      {msg && (
        <div className="mb-6 p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-sm flex items-center justify-between">
          <span>{msg}</span>
          <button onClick={() => setMsg('')} className="text-emerald-600 font-bold text-xs">Đóng</button>
        </div>
      )}

      {error && (
        <div className="mb-6 p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-2">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-500 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex flex-wrap gap-2 mb-6">
        {[
          { key: 'ALL', label: 'Tất cả đơn' },
          { key: 'PENDING', label: 'Chờ xác nhận (PENDING)' },
          { key: 'IN_PROGRESS', label: 'Đang nấu (CONFIRMED / PREPARING)' },
          { key: 'READY_FOR_PICKUP', label: 'Sẵn sàng giao (READY)' },
          { key: 'DONE', label: 'Lịch sử / Đã giao' },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`px-3.5 py-2 rounded-xl text-xs font-bold transition ${
              filter === tab.key
                ? 'bg-orange-500 text-white shadow-md shadow-orange-500/20'
                : 'bg-white text-slate-600 hover:bg-slate-100 border border-slate-200'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Orders List */}
      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-40 rounded-2xl bg-slate-200 animate-pulse" />
          ))}
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
          <ChefHat className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-base font-bold text-slate-800">Không có đơn hàng nào trong mục này</h3>
          <p className="text-xs text-slate-600 mt-1">Đơn mới từ khách hàng sẽ xuất hiện tự động tại đây.</p>
        </div>
      ) : (
        <div className="space-y-5">
          {filteredOrders.map((order) => {
            // Can cancel: rule specifies can cancel BEFORE READY_FOR_PICKUP
            const canCancel =
              order.orderStatus === 'PENDING' ||
              order.orderStatus === 'CONFIRMED' ||
              order.orderStatus === 'PREPARING';

            return (
              <div
                key={order.orderId}
                className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm hover:shadow-md transition"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-extrabold text-base text-slate-900">
                        Đơn #{order.orderId}
                      </span>
                      <span className="text-xs text-slate-600">
                        ({new Date(order.createdAt).toLocaleTimeString('vi-VN')} {new Date(order.createdAt).toLocaleDateString('vi-VN')})
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-xs text-slate-600">
                      <MapPin className="w-3.5 h-3.5 text-slate-600" />
                      <span>Giao đến: {order.deliveryAddress}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold px-3 py-1 rounded-full bg-slate-100 text-slate-800">
                      Trạng thái: {order.orderStatus}
                    </span>
                    <span className="text-xs font-extrabold text-orange-600 px-3 py-1 rounded-full bg-orange-50">
                      {Number(order.totalAmount).toLocaleString('vi-VN')} đ
                    </span>
                  </div>
                </div>

                {/* Items in order */}
                <div className="py-4 grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                  <div className="space-y-1.5 bg-slate-50 p-3.5 rounded-2xl">
                    <span className="font-bold text-slate-700 block mb-1">Món cần làm:</span>
                    {order.items?.map((item) => (
                      <div key={item.itemId} className="flex justify-between text-slate-800">
                        <span>
                          <strong className="text-orange-600 font-extrabold">{item.quantity}×</strong> {item.itemName}
                        </span>
                        <span className="font-medium">
                          {Number(item.price * item.quantity).toLocaleString('vi-VN')} đ
                        </span>
                      </div>
                    ))}
                    {order.notes && (
                      <p className="text-[11px] text-amber-700 font-medium pt-1 italic">
                        Ghi chú: {order.notes}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2 bg-slate-50 p-3.5 rounded-2xl flex flex-col justify-between">
                    <div>
                      <span className="font-bold text-slate-700 block mb-1">Thanh toán:</span>
                      <p className="text-slate-600">
                        Hình thức: <strong className="text-slate-800">{order.payment?.methodCode || 'COD'}</strong>
                      </p>
                      <p className="text-slate-600">
                        Trạng thái thanh toán: <strong className="text-slate-800">{order.payment?.paymentStatus || 'PENDING'}</strong>
                      </p>
                    </div>

                    {/* Operational Status Action Buttons */}
                    <div className="flex flex-wrap items-center gap-2 pt-2">
                      {order.orderStatus === 'PENDING' && (
                        <button
                          onClick={() => handleAdvanceStatus(order.orderId, 'CONFIRMED')}
                          disabled={actionLoading}
                          className="px-4 py-2 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs shadow-sm transition flex items-center gap-1.5"
                        >
                          <CheckCircle2 className="w-3.5 h-3.5" /> Xác nhận đơn (CONFIRMED)
                        </button>
                      )}

                      {order.orderStatus === 'CONFIRMED' && (
                        <button
                          onClick={() => handleAdvanceStatus(order.orderId, 'PREPARING')}
                          disabled={actionLoading}
                          className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs shadow-sm transition flex items-center gap-1.5"
                        >
                          <ChefHat className="w-3.5 h-3.5" /> Bắt đầu làm món (PREPARING)
                        </button>
                      )}

                      {order.orderStatus === 'PREPARING' && (
                        <button
                          onClick={() => handleAdvanceStatus(order.orderId, 'READY_FOR_PICKUP')}
                          disabled={actionLoading}
                          className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-sm transition flex items-center gap-1.5"
                        >
                          <Bike className="w-3.5 h-3.5" /> Món xong, gọi tài xế (READY_FOR_PICKUP)
                        </button>
                      )}

                      {order.orderStatus === 'READY_FOR_PICKUP' && (
                        <span className="text-xs text-indigo-700 font-bold bg-indigo-50 px-3 py-1.5 rounded-xl flex items-center gap-1">
                          <Bike className="w-4 h-4 animate-bounce" /> Đang chờ tài xế nhận đơn...
                        </span>
                      )}

                      {canCancel && (
                        <button
                          onClick={() => handleCancelOrder(order.orderId)}
                          disabled={actionLoading}
                          className="px-3 py-2 rounded-xl text-xs font-bold text-rose-600 border border-rose-200 hover:bg-rose-50 transition"
                        >
                          Hủy đơn
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
