import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import { ShoppingBag, Clock, ArrowRight, Store, CheckCircle2, XCircle } from 'lucide-react';

export const MyOrders = () => {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, [user]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const data = await axiosClient.get(`/customers/${user.id}/orders`);
      setOrders(data);
    } catch (err) {
      console.error('Lỗi lấy danh sách đơn:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-800">Chờ xác nhận</span>;
      case 'CONFIRMED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">Đã xác nhận</span>;
      case 'PREPARING':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-purple-100 text-purple-800">Đang chuẩn bị</span>;
      case 'READY_FOR_PICKUP':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-indigo-100 text-indigo-800">Chờ tài xế</span>;
      case 'DELIVERING':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-orange-100 text-orange-800">Đang giao hàng</span>;
      case 'COMPLETED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5" /> Hoàn thành</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-800 flex items-center gap-1"><XCircle className="w-3.5 h-3.5" /> Đã hủy</span>;
      default:
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-800">{status}</span>;
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight flex items-center gap-2">
            <ShoppingBag className="w-6 h-6 text-orange-500" /> Đơn hàng của tôi
          </h1>
          <p className="text-xs text-slate-600 mt-1">Lịch sử và tiến độ tất cả các đơn đã đặt</p>
        </div>
        <Link
          to="/customer"
          className="px-4 py-2 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs shadow-md shadow-orange-500/20 transition"
        >
          Đặt món mới
        </Link>
      </div>

      {loading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((n) => (
            <div key={n} className="h-28 rounded-2xl bg-slate-200 animate-pulse" />
          ))}
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
          <ShoppingBag className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">Bạn chưa có đơn hàng nào</h3>
          <p className="text-xs text-slate-600 mt-1 mb-4">Hãy dạo quanh thực đơn và chọn món ăn ngon nhé!</p>
          <Link
            to="/customer"
            className="inline-block px-5 py-2.5 rounded-xl bg-orange-500 text-white text-xs font-bold shadow-md shadow-orange-500/20 hover:bg-orange-600 transition"
          >
            Khám phá quán ngon
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-sm hover:shadow-md transition flex flex-col sm:flex-row sm:items-center justify-between gap-4"
            >
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-sm text-slate-900">Đơn #{order.orderId}</span>
                  <span className="text-xs text-slate-600">•</span>
                  <span className="text-xs text-slate-600">
                    {new Date(order.createdAt).toLocaleString('vi-VN')}
                  </span>
                  {getStatusBadge(order.orderStatus)}
                </div>

                <div className="flex items-center gap-1.5 text-xs text-slate-700">
                  <Store className="w-4 h-4 text-orange-500 flex-shrink-0" />
                  <span className="font-semibold">{order.restaurantName}</span>
                </div>

                <div className="text-xs text-slate-600 flex flex-wrap gap-2">
                  <span>{order.items?.length || 0} món</span>
                  <span>•</span>
                  <span>Phí ship: {Number(order.shippingFee).toLocaleString('vi-VN')} đ</span>
                  <span>•</span>
                  <span>
                    Tổng: <span className="font-extrabold text-orange-600">{Number(order.totalAmount).toLocaleString('vi-VN')} đ</span>
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3 pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-100">
                <Link
                  to={`/customer/orders/${order.orderId}`}
                  className="w-full sm:w-auto px-4 py-2.5 rounded-xl bg-slate-900 hover:bg-orange-600 text-white font-bold text-xs shadow-sm transition flex items-center justify-center gap-1.5"
                >
                  Chi tiết đơn <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
