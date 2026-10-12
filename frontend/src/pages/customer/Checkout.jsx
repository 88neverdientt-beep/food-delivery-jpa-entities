import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import axiosClient from '../../api/axiosClient';
import {
  MapPin, CreditCard, ShoppingBag, Plus, Minus, Trash2,
  ArrowLeft, CheckCircle, AlertCircle, QrCode, Navigation, Truck
} from 'lucide-react';

export const Checkout = () => {
  const { user } = useAuth();
  const { cart, updateQuantity, removeItem, clearCart, subtotal } = useCart();
  const navigate = useNavigate();

  const [deliveryAddress, setDeliveryAddress] = useState('Km10 Nguyễn Trãi, Hà Đông, Hà Nội');
  const [deliveryDistrictNo, setDeliveryDistrictNo] = useState(1);
  const [districts, setDistricts] = useState([]);
  const [notes, setNotes] = useState('');

  const [paymentMethods, setPaymentMethods] = useState([]);
  const [selectedMethod, setSelectedMethod] = useState('COD'); // 'COD' | 'VIETQR'

  const [preview, setPreview] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchPaymentMethods();
    fetchDistricts();
  }, []);

  useEffect(() => {
    if (cart.items.length > 0 && cart.restaurantId) {
      calculatePreview();
    }
  }, [deliveryDistrictNo, cart.items, cart.restaurantId]);

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

  const fetchPaymentMethods = async () => {
    try {
      const data = await axiosClient.get('/payment-methods');
      setPaymentMethods(data);
      if (data.length > 0) {
        setSelectedMethod(data[0].methodCode);
      }
    } catch (err) {
      console.error('Lỗi tải phương thức thanh toán:', err);
    }
  };

  const calculatePreview = async () => {
    try {
      setPreviewLoading(true);
      const req = {
        restaurantId: cart.restaurantId,
        customerId: user?.id || 1,
        deliveryAddress: deliveryAddress || 'Địa chỉ giao hàng',
        deliveryDistrictNo: Number(deliveryDistrictNo),
        notes: notes,
        items: cart.items.map((i) => ({ itemId: i.itemId, quantity: i.quantity })),
      };
      const res = await axiosClient.post('/orders/preview', req);
      setPreview(res);
    } catch (err) {
      console.error('Lỗi tính phí ship xem trước:', err);
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleSubmitOrder = async (e) => {
    e.preventDefault();
    if (cart.items.length === 0) {
      setError('Giỏ hàng trống!');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      // 1. Tạo đơn hàng POST /api/orders
      const orderReq = {
        restaurantId: cart.restaurantId,
        customerId: user.id,
        deliveryAddress,
        deliveryDistrictNo: Number(deliveryDistrictNo),
        notes,
        items: cart.items.map((i) => ({ itemId: i.itemId, quantity: i.quantity })),
      };
      const createdOrder = await axiosClient.post('/orders', orderReq);

      // 2. Tạo bản ghi thanh toán POST /api/payments
      try {
        await axiosClient.post('/payments', {
          orderId: createdOrder.orderId,
          methodCode: selectedMethod,
        });
      } catch (payErr) {
        console.warn('Lỗi tạo thanh toán (hoặc đã tạo tự động):', payErr);
      }

      // Xoá giỏ hàng và điều hướng tới trang theo dõi đơn
      clearCart();
      navigate(`/customer/orders/${createdOrder.orderId}`);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Đặt đơn hàng không thành công.');
    } finally {
      setSubmitting(false);
    }
  };

  if (cart.items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <div className="w-20 h-20 rounded-3xl bg-orange-100 text-orange-600 flex items-center justify-center mx-auto mb-4">
          <ShoppingBag className="w-10 h-10" />
        </div>
        <h2 className="text-2xl font-bold text-slate-900 mb-2">Giỏ hàng của bạn đang trống</h2>
        <p className="text-sm text-slate-600 mb-6">Hãy chọn các món ngon yêu thích để bắt đầu đặt hàng nhé!</p>
        <Link
          to="/customer"
          className="inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-orange-500 hover:bg-orange-600 font-bold text-sm text-white shadow-lg shadow-orange-500/25 transition"
        >
          <ArrowLeft className="w-4 h-4" /> Khám phá nhà hàng
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <Link
        to={cart.restaurantId ? `/customer/restaurant/${cart.restaurantId}` : '/customer'}
        className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-orange-600 mb-6 transition"
      >
        <ArrowLeft className="w-4 h-4" /> Quay lại thực đơn ({cart.restaurantName})
      </Link>

      <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-6">
        Xác nhận & Đặt đơn hàng
      </h1>

      {error && (
        <div className="mb-6 p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-start gap-3">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-500 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmitOrder} className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Columns: Delivery & Payment Details */}
        <div className="lg:col-span-2 space-y-6">
          {/* Section: Địa chỉ nhận hàng */}
          <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-4">
              <MapPin className="w-5 h-5 text-orange-500" /> Thông tin giao hàng
            </h2>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Địa chỉ chi tiết *
                </label>
                <input
                  type="text"
                  required
                  value={deliveryAddress}
                  onChange={(e) => setDeliveryAddress(e.target.value)}
                  placeholder="Số nhà, tên ngõ, đường, quận..."
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500"
                />
              </div>

              {/* Chọn Quận Giao Hàng */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Quận giao hàng *
                </label>
                <div className="relative">
                  <MapPin className="w-5 h-5 text-slate-600 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <select
                    value={deliveryDistrictNo}
                    onChange={(e) => setDeliveryDistrictNo(Number(e.target.value))}
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
                  Phí giao hàng được tính theo số quận chênh lệch (cùng quận: 15.000 đ, mỗi quận chênh lệch: 15.000 đ).
                </p>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Ghi chú cho quán / tài xế
                </label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Ví dụ: Ít cay, giao gọi trước..."
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-orange-500"
                />
              </div>
            </div>
          </div>

          {/* Section: Phương thức thanh toán */}
          <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-4">
              <CreditCard className="w-5 h-5 text-orange-500" /> Phương thức thanh toán
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <label
                className={`p-4 rounded-2xl border-2 cursor-pointer transition flex items-start gap-3.5 ${
                  selectedMethod === 'COD'
                    ? 'border-orange-500 bg-orange-50/50'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <input
                  type="radio"
                  name="paymentMethod"
                  value="COD"
                  checked={selectedMethod === 'COD'}
                  onChange={() => setSelectedMethod('COD')}
                  className="mt-1 text-orange-600 focus:ring-orange-500"
                />
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-sm text-slate-900">Tiền mặt (COD)</span>
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-slate-200 text-slate-700 font-semibold">
                      Phổ biến
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 mt-1">
                    Thanh toán trực tiếp cho tài xế khi nhận món ăn thành công.
                  </p>
                </div>
              </label>

              <label
                className={`p-4 rounded-2xl border-2 cursor-pointer transition flex items-start gap-3.5 ${
                  selectedMethod === 'VIETQR'
                    ? 'border-orange-500 bg-orange-50/50'
                    : 'border-slate-200 hover:border-slate-300'
                }`}
              >
                <input
                  type="radio"
                  name="paymentMethod"
                  value="VIETQR"
                  checked={selectedMethod === 'VIETQR'}
                  onChange={() => setSelectedMethod('VIETQR')}
                  className="mt-1 text-orange-600 focus:ring-orange-500"
                />
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-sm text-slate-900">Chuyển khoản VietQR</span>
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-100 text-emerald-800 font-semibold flex items-center gap-0.5">
                      <QrCode className="w-3 h-3" /> Nhanh
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 mt-1">
                    Quét mã QR tự động xác nhận qua webhook ngân hàng.
                  </p>
                </div>
              </label>
            </div>
          </div>
        </div>

        {/* Right 1 Column: Order Summary */}
        <div className="space-y-6">
          <div className="bg-white rounded-3xl border border-slate-200/80 p-6 shadow-sm">
            <h2 className="text-base font-bold text-slate-900 flex items-center gap-2 mb-4">
              <ShoppingBag className="w-5 h-5 text-orange-500" /> Đơn hàng ({cart.restaurantName})
            </h2>

            {/* Items list */}
            <div className="divide-y divide-slate-100 max-h-64 overflow-y-auto pr-1 mb-4">
              {cart.items.map((item) => (
                <div key={item.itemId} className="py-3 flex items-center justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="font-bold text-xs text-slate-900 truncate">{item.itemName}</p>
                    <p className="text-[11px] text-slate-600">
                      {Number(item.price).toLocaleString('vi-VN')} đ × {item.quantity}
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    <div className="flex items-center border border-slate-200 rounded-lg">
                      <button
                        type="button"
                        onClick={() => updateQuantity(item.itemId, -1)}
                        className="p-1 hover:bg-slate-100 text-slate-600"
                      >
                        <Minus className="w-3 h-3" />
                      </button>
                      <span className="px-2 text-xs font-bold text-slate-800">{item.quantity}</span>
                      <button
                        type="button"
                        onClick={() => updateQuantity(item.itemId, 1)}
                        className="p-1 hover:bg-slate-100 text-slate-600"
                      >
                        <Plus className="w-3 h-3" />
                      </button>
                    </div>

                    <button
                      type="button"
                      onClick={() => removeItem(item.itemId)}
                      className="p-1 text-slate-600 hover:text-rose-600 transition"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Calculations Breakdown */}
            <div className="border-t border-slate-100 pt-4 space-y-2.5 text-xs text-slate-600">
              <div className="flex justify-between">
                <span>Tiền món:</span>
                <span className="font-semibold text-slate-800">
                  {subtotal.toLocaleString('vi-VN')} đ
                </span>
              </div>

              <div className="flex justify-between items-center">
                <span className="flex items-center gap-1">
                  <Truck className="w-3.5 h-3.5 text-orange-500" />
                  Phí giao hàng:
                  {preview?.restaurantDistrictNo && preview?.deliveryDistrictNo && (
                    <span className="text-[10px] text-slate-600">
                      (Q{preview.restaurantDistrictNo} → Q{preview.deliveryDistrictNo}
                      {preview.gap === 0 ? ' • cùng quận' : ` • cách ${preview.gap} quận`})
                    </span>
                  )}
                </span>
                <span className="font-semibold text-slate-800">
                  {previewLoading
                    ? 'Đang tính...'
                    : preview?.shippingFee != null
                    ? `${Number(preview.shippingFee).toLocaleString('vi-VN')} đ`
                    : '15.000 đ'}
                </span>
              </div>

              <div className="border-t border-slate-200 pt-3 flex justify-between items-baseline">
                <span className="text-sm font-bold text-slate-900">Tổng thanh toán:</span>
                <span className="text-lg font-extrabold text-orange-600">
                  {preview?.totalAmount != null
                    ? Number(preview.totalAmount).toLocaleString('vi-VN')
                    : (subtotal + 15000).toLocaleString('vi-VN')}{' '}
                  đ
                </span>
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full mt-6 py-3 px-4 rounded-xl font-bold text-white bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 shadow-lg shadow-orange-500/25 flex items-center justify-center gap-2 transition disabled:opacity-50"
            >
              {submitting ? 'Đang tạo đơn hàng...' : 'Đặt đơn ngay'}
              <CheckCircle className="w-4 h-4" />
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};
