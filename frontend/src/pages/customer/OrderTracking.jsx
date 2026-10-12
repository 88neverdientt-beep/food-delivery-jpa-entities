import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import axiosClient from '../../api/axiosClient';
import {
  Clock, CheckCircle2, XCircle, AlertCircle, RefreshCw,
  QrCode, Star, ArrowLeft, Send, Utensils, Truck, Store, MapPin
} from 'lucide-react';

const ORDER_STEPS = [
  { key: 'PENDING', label: 'Chờ xác nhận', desc: 'Đơn mới tạo, quán chuẩn bị tiếp nhận' },
  { key: 'CONFIRMED', label: 'Đã xác nhận', desc: 'Nhà hàng đã nhận đơn' },
  { key: 'PREPARING', label: 'Đang nấu', desc: 'Bếp đang chuẩn bị món ăn' },
  { key: 'READY_FOR_PICKUP', label: 'Chờ tài xế', desc: 'Món ăn đã xong, chờ tài xế đến lấy' },
  { key: 'DELIVERING', label: 'Đang giao hàng', desc: 'Tài xế đang trên đường giao tới bạn' },
  { key: 'COMPLETED', label: 'Đã hoàn tất', desc: 'Giao hàng thành công! Hãy thưởng thức nhé' },
];

export const OrderTracking = () => {
  const { id } = useParams();
  const { user } = useAuth();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);
  const [simulatingWebhook, setSimulatingWebhook] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  // Đánh giá món ăn
  const [ratings, setRatings] = useState({}); // { [itemId]: { score: 5, comment: '' } }
  const [ratingSubmitting, setRatingSubmitting] = useState({});
  const [ratedItems, setRatedItems] = useState({}); // { [itemId]: true }

  useEffect(() => {
    fetchOrder();
    const interval = setInterval(fetchOrder, 4000);
    return () => clearInterval(interval);
  }, [id]);

  const fetchOrder = async () => {
    try {
      const data = await axiosClient.get(`/orders/${id}`);
      setOrder(data);
    } catch (err) {
      console.error('Lỗi lấy thông tin đơn:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) return;
    try {
      setCancelling(true);
      setError('');
      await axiosClient.patch(`/orders/${id}/cancel`, {
        actorId: user.id,
        role: 'CUSTOMER',
      });
      setMsg('Đã hủy đơn hàng thành công.');
      fetchOrder();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Không thể hủy đơn hàng.');
    } finally {
      setCancelling(false);
    }
  };

  const handleSimulateVietQr = async () => {
    try {
      setSimulatingWebhook(true);
      setError('');
      const txnCode = order?.payment?.transactionCode || `TXN-ORD-${id}`;
      await axiosClient.post('/payments/webhook/vietqr', {
        transactionCode: txnCode,
        orderId: Number(id),
      });
      setMsg('Giả lập webhook ngân hàng thành công! Trạng thái thanh toán đã chuyển thành PAID.');
      fetchOrder();
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Giả lập webhook thất bại.');
    } finally {
      setSimulatingWebhook(false);
    }
  };

  const handleRatingChange = (itemId, field, value) => {
    setRatings((prev) => ({
      ...prev,
      [itemId]: {
        score: prev[itemId]?.score || 5,
        comment: prev[itemId]?.comment || '',
        [field]: value,
      },
    }));
  };

  const handleSubmitRating = async (itemId) => {
    const current = ratings[itemId] || { score: 5, comment: '' };
    try {
      setRatingSubmitting((prev) => ({ ...prev, [itemId]: true }));
      await axiosClient.post(`/order-items/${id}/${itemId}/rating`, {
        customerId: user.id,
        ratingScore: current.score,
        comment: current.comment || 'Món ăn rất ngon, đóng gói cẩn thận!',
      });
      setRatedItems((prev) => ({ ...prev, [itemId]: true }));
      setMsg('Cảm ơn bạn đã đánh giá món ăn!');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Lỗi gửi đánh giá.');
    } finally {
      setRatingSubmitting((prev) => ({ ...prev, [itemId]: false }));
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <div className="w-12 h-12 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
        <p className="text-sm font-semibold text-slate-600">Đang tải trạng thái đơn hàng #{id}...</p>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center">
        <h2 className="text-xl font-bold text-slate-800">Không tìm thấy đơn hàng #{id}</h2>
        <Link to="/customer" className="mt-4 inline-block text-orange-600 font-semibold text-sm">
          Về trang chủ
        </Link>
      </div>
    );
  }

  const isCancelled = order.orderStatus === 'CANCELLED';
  const currentStepIdx = ORDER_STEPS.findIndex((s) => s.key === order.orderStatus);

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top action bar */}
      <div className="flex items-center justify-between mb-6">
        <Link
          to="/customer/orders"
          className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-orange-600 transition"
        >
          <ArrowLeft className="w-4 h-4" /> Danh sách đơn hàng
        </Link>
        <button
          onClick={fetchOrder}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold bg-slate-100 hover:bg-slate-200 text-slate-700 transition"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Làm mới
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

      {/* Main Order Status Header */}
      <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm mb-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-100">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold px-2 py-0.5 rounded-md bg-slate-100 text-slate-700">
                Mã đơn: #{order.orderId}
              </span>
              <span className="text-xs text-slate-600">
                {new Date(order.createdAt).toLocaleString('vi-VN')}
              </span>
            </div>
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
              {order.restaurantName}
            </h1>
          </div>

          <div className="flex items-center gap-3">
            {isCancelled ? (
              <span className="px-3.5 py-1.5 rounded-full text-xs font-bold bg-rose-100 text-rose-700 flex items-center gap-1.5">
                <XCircle className="w-4 h-4" /> Đã hủy đơn
              </span>
            ) : order.orderStatus === 'COMPLETED' ? (
              <span className="px-3.5 py-1.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-700 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4" /> Hoàn tất
              </span>
            ) : (
              <span className="px-3.5 py-1.5 rounded-full text-xs font-bold bg-orange-100 text-orange-800 flex items-center gap-1.5 animate-pulse-subtle">
                <Clock className="w-4 h-4 text-orange-600" />
                {ORDER_STEPS.find((s) => s.key === order.orderStatus)?.label || order.orderStatus}
              </span>
            )}

            {/* Cancel Button: ONLY allowed when PENDING */}
            {order.orderStatus === 'PENDING' && (
              <button
                onClick={handleCancelOrder}
                disabled={cancelling}
                className="px-4 py-1.5 rounded-xl text-xs font-bold text-rose-600 border border-rose-200 hover:bg-rose-50 transition disabled:opacity-50"
              >
                {cancelling ? 'Đang hủy...' : 'Hủy đơn'}
              </button>
            )}
          </div>
        </div>

        {/* Real-time Status Stepper */}
        {!isCancelled && (
          <div className="py-8">
            <div className="relative">
              {/* Progress Line */}
              <div className="hidden sm:block absolute top-5 left-6 right-6 h-1 bg-slate-100 -z-0">
                <div
                  className="h-full bg-gradient-to-r from-orange-500 to-amber-500 transition-all duration-700"
                  style={{
                    width: `${Math.max(0, (currentStepIdx / (ORDER_STEPS.length - 1)) * 100)}%`,
                  }}
                />
              </div>

              {/* Step dots */}
              <div className="grid grid-cols-2 sm:grid-cols-6 gap-4 relative z-10">
                {ORDER_STEPS.map((step, idx) => {
                  const isDone = idx <= currentStepIdx;
                  const isCurrent = idx === currentStepIdx;
                  return (
                    <div key={step.key} className="flex flex-col sm:items-center text-left sm:text-center">
                      <div
                        className={`w-10 h-10 rounded-2xl flex items-center justify-center font-bold text-xs mb-2 transition-all shadow-sm ${
                          isCurrent
                            ? 'bg-orange-500 text-white ring-4 ring-orange-200 scale-110'
                            : isDone
                            ? 'bg-emerald-500 text-white'
                            : 'bg-slate-100 text-slate-600'
                        }`}
                      >
                        {isDone && !isCurrent ? (
                          <CheckCircle2 className="w-5 h-5" />
                        ) : (
                          idx + 1
                        )}
                      </div>
                      <span className={`text-xs font-bold ${isCurrent ? 'text-orange-600' : isDone ? 'text-slate-800' : 'text-slate-600'}`}>
                        {step.label}
                      </span>
                      <span className="text-[10px] text-slate-600 mt-0.5 hidden sm:block">
                        {step.desc}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        )}

        {/* VietQR Section (nếu chọn VietQR) */}
        {order.payment && order.payment.methodCode === 'VIETQR' && (
          <div className="mt-6 p-6 rounded-2xl bg-gradient-to-br from-amber-50/60 to-orange-50/60 border border-orange-200 flex flex-col sm:flex-row items-center gap-6">
            <div className="w-36 h-36 bg-white p-2 rounded-2xl shadow-md border border-slate-200 flex flex-col items-center justify-center flex-shrink-0">
              <QrCode className="w-24 h-24 text-slate-800" />
              <span className="text-[9px] font-bold text-slate-600 mt-1 uppercase">VietQR Mock</span>
            </div>

            <div className="flex-1 text-center sm:text-left">
              <div className="flex items-center justify-center sm:justify-start gap-2 mb-1">
                <span className="font-bold text-sm text-slate-900">Thanh toán chuyển khoản VietQR</span>
                <span
                  className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                    order.payment.paymentStatus === 'PAID'
                      ? 'bg-emerald-100 text-emerald-800'
                      : 'bg-amber-100 text-amber-800'
                  }`}
                >
                  {order.payment.paymentStatus}
                </span>
              </div>
              <p className="text-xs text-slate-600 mb-2 leading-relaxed">
                Số tiền: <span className="font-extrabold text-orange-600">{Number(order.totalAmount).toLocaleString('vi-VN')} đ</span> • Mã giao dịch: <span className="font-mono font-semibold">{order.payment.transactionCode || 'Chưa tạo'}</span>
              </p>

              {order.payment.paymentStatus !== 'PAID' ? (
                <button
                  type="button"
                  onClick={handleSimulateVietQr}
                  disabled={simulatingWebhook}
                  className="px-4 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs shadow-md shadow-orange-500/20 transition flex items-center justify-center sm:justify-start gap-2 disabled:opacity-50 mx-auto sm:mx-0"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${simulatingWebhook ? 'animate-spin' : ''}`} />
                  {simulatingWebhook ? 'Đang gọi webhook...' : '⚡ Giả lập thanh toán VietQR thành công'}
                </button>
              ) : (
                <p className="text-xs font-bold text-emerald-600 flex items-center justify-center sm:justify-start gap-1">
                  <CheckCircle2 className="w-4 h-4" /> Đã nhận thanh toán qua VietQR!
                </p>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Order Details & Delivery Info */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
          <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2 mb-4">
            <Utensils className="w-4 h-4 text-orange-500" /> Các món đã đặt
          </h3>
          <div className="divide-y divide-slate-100">
            {order.items?.map((item) => (
              <div key={item.itemId} className="py-2.5 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-800">{item.itemName}</span>
                  <span className="text-slate-600 ml-1.5">× {item.quantity}</span>
                </div>
                <span className="font-semibold text-slate-700">
                  {Number(item.price * item.quantity).toLocaleString('vi-VN')} đ
                </span>
              </div>
            ))}
          </div>
          <div className="border-t border-slate-100 mt-4 pt-3 space-y-1.5 text-xs text-slate-600">
            <div className="flex justify-between">
              <span>Phí vận chuyển:</span>
              <span>{Number(order.shippingFee).toLocaleString('vi-VN')} đ</span>
            </div>
            <div className="flex justify-between font-bold text-slate-900 text-sm pt-1 border-t border-slate-100">
              <span>Tổng cộng:</span>
              <span className="text-orange-600 font-extrabold">{Number(order.totalAmount).toLocaleString('vi-VN')} đ</span>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
          <h3 className="font-bold text-sm text-slate-900 flex items-center gap-2 mb-4">
            <Truck className="w-4 h-4 text-orange-500" /> Thông tin giao nhận
          </h3>
          <div className="space-y-3 text-xs text-slate-600">
            <div>
              <span className="font-bold text-slate-700 block mb-0.5">Địa chỉ giao hàng:</span>
              <span className="flex items-start gap-1.5">
                <MapPin className="w-4 h-4 text-slate-600 flex-shrink-0 mt-0.5" />
                {order.deliveryAddress}
              </span>
            </div>
            {order.notes && (
              <div>
                <span className="font-bold text-slate-700 block mb-0.5">Ghi chú:</span>
                <span className="italic">{order.notes}</span>
              </div>
            )}
            <div>
              <span className="font-bold text-slate-700 block mb-0.5">Phương thức thanh toán:</span>
              <span className="font-semibold text-slate-900">
                {order.payment?.methodCode || 'COD'} ({order.payment?.paymentStatus || 'PENDING'})
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Đánh giá món ăn sau khi đơn COMPLETED */}
      {order.orderStatus === 'COMPLETED' && (
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 shadow-sm">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 rounded-2xl bg-amber-100 text-amber-600 flex items-center justify-center">
              <Star className="w-5 h-5 fill-amber-500 text-amber-500" />
            </div>
            <div>
              <h3 className="font-bold text-base text-slate-900">Đánh giá món ăn</h3>
              <p className="text-xs text-slate-600">Chia sẻ cảm nhận của bạn để giúp quán phục vụ tốt hơn</p>
            </div>
          </div>

          <div className="space-y-6">
            {order.items?.map((item) => {
              const currentScore = ratings[item.itemId]?.score || 5;
              const currentComment = ratings[item.itemId]?.comment || '';
              const isSubmitting = ratingSubmitting[item.itemId];
              const isRated = ratedItems[item.itemId];

              return (
                <div key={item.itemId} className="p-4 rounded-2xl bg-slate-50 border border-slate-200/60">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
                    <span className="font-bold text-sm text-slate-800">{item.itemName}</span>
                    <div className="flex items-center gap-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          disabled={isRated}
                          onClick={() => handleRatingChange(item.itemId, 'score', star)}
                          className="p-1 hover:scale-110 transition"
                        >
                          <Star
                            className={`w-5 h-5 ${
                              star <= currentScore
                                ? 'fill-amber-400 text-amber-400'
                                : 'text-slate-300'
                            }`}
                          />
                        </button>
                      ))}
                      <span className="text-xs font-bold text-amber-600 ml-1.5">{currentScore} sao</span>
                    </div>
                  </div>

                  {isRated ? (
                    <div className="text-xs text-emerald-600 font-bold flex items-center gap-1.5 py-2">
                      <CheckCircle2 className="w-4 h-4" /> Bạn đã đánh giá món này!
                    </div>
                  ) : (
                    <div className="flex flex-col sm:flex-row gap-2">
                      <input
                        type="text"
                        value={currentComment}
                        onChange={(e) => handleRatingChange(item.itemId, 'comment', e.target.value)}
                        placeholder="Nhập cảm nhận (ví dụ: Món rất ngon, nóng hổi...)"
                        className="flex-1 px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:border-orange-500"
                      />
                      <button
                        type="button"
                        onClick={() => handleSubmitRating(item.itemId)}
                        disabled={isSubmitting}
                        className="px-4 py-2 rounded-xl bg-orange-500 hover:bg-orange-600 text-white font-bold text-xs shadow-sm flex items-center justify-center gap-1.5 transition disabled:opacity-50"
                      >
                        <Send className="w-3.5 h-3.5" />
                        {isSubmitting ? 'Đang gửi...' : 'Gửi đánh giá'}
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
