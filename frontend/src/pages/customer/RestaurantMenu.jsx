import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axiosClient from '../../api/axiosClient';
import { useCart } from '../../context/CartContext';
import {
  Store, MapPin, Phone, Plus, Minus, ShoppingBag, ArrowLeft,
  AlertTriangle, Check, Utensils
} from 'lucide-react';

export const RestaurantMenu = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { cart, addItem, forceAddItem, updateQuantity, totalQuantity, subtotal } = useCart();

  const [restaurant, setRestaurant] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modal đổi giỏ hàng
  const [confirmModal, setConfirmModal] = useState({
    isOpen: false,
    pendingItem: null,
    currentRestaurant: '',
    newRestaurant: '',
  });

  useEffect(() => {
    fetchRestaurantAndMenu();
  }, [id]);

  const fetchRestaurantAndMenu = async () => {
    try {
      setLoading(true);
      // Lấy thông tin chi tiết nhà hàng
      try {
        const rest = await axiosClient.get(`/restaurants/${id}`);
        setRestaurant(rest);
      } catch (err) {
        const data = await axiosClient.get('/restaurants');
        const list = Array.isArray(data) ? data : (data?.content || []);
        const found = list.find((r) => r.restaurantId.toString() === id.toString());
        setRestaurant(found || { restaurantId: id, restaurantName: `Nhà hàng #${id}`, address: '' });
      }

      // Lấy món còn bán của quán
      const menuItems = await axiosClient.get(`/restaurants/${id}/items`);
      setItems(menuItems);
    } catch (err) {
      console.error('Lỗi tải thực đơn:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = (item) => {
    if (!restaurant) return;
    const res = addItem(item, restaurant);
    if (res.needsConfirm) {
      setConfirmModal({
        isOpen: true,
        pendingItem: item,
        currentRestaurant: res.currentRestaurant,
        newRestaurant: res.newRestaurant,
      });
    }
  };

  const confirmSwitchRestaurant = () => {
    if (confirmModal.pendingItem && restaurant) {
      forceAddItem(confirmModal.pendingItem, restaurant);
    }
    setConfirmModal({ isOpen: false, pendingItem: null, currentRestaurant: '', newRestaurant: '' });
  };

  const getItemQuantityInCart = (itemId) => {
    if (cart.restaurantId?.toString() !== id.toString()) return 0;
    const found = cart.items.find((i) => i.itemId === itemId);
    return found ? found.quantity : 0;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back button */}
      <Link
        to="/customer"
        className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-600 hover:text-orange-600 mb-6 transition"
      >
        <ArrowLeft className="w-4 h-4" /> Quay lại danh sách nhà hàng
      </Link>

      {/* Restaurant Header Banner */}
      {restaurant && (
        <div className="bg-white rounded-3xl border border-slate-200/80 p-6 sm:p-8 mb-8 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="flex items-start gap-5">
            <div className="w-16 h-16 rounded-2xl bg-orange-100 text-orange-600 flex items-center justify-center font-bold text-2xl shadow-inner flex-shrink-0">
              <Store className="w-8 h-8" />
            </div>
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-bold px-2 py-0.5 rounded-md bg-orange-100 text-orange-700">
                  Đối tác chính thức
                </span>
                <span className="text-xs text-emerald-600 font-semibold">• Đang nhận đơn</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
                {restaurant.restaurantName}
              </h1>
              <div className="mt-2 flex flex-wrap gap-4 text-xs text-slate-600">
                <div className="flex items-center gap-1.5">
                  <MapPin className="w-4 h-4 text-slate-600" />
                  <span>{restaurant.address}</span>
                </div>
                {restaurant.phoneNumber && (
                  <div className="flex items-center gap-1.5">
                    <Phone className="w-4 h-4 text-slate-600" />
                    <span>{restaurant.phoneNumber}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Cart preview badge if cart has items from this restaurant */}
          {cart.restaurantId?.toString() === id.toString() && totalQuantity > 0 && (
            <div className="bg-orange-50 border border-orange-200 rounded-2xl p-4 flex items-center gap-4">
              <div>
                <span className="text-xs text-orange-700 font-medium">Giỏ hàng của bạn:</span>
                <p className="text-base font-bold text-orange-950">
                  {totalQuantity} món • {subtotal.toLocaleString('vi-VN')} đ
                </p>
              </div>
              <Link
                to="/customer/checkout"
                className="px-4 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold shadow-md shadow-orange-500/20 transition flex items-center gap-1.5"
              >
                <ShoppingBag className="w-4 h-4" /> Thanh toán
              </Link>
            </div>
          )}
        </div>
      )}

      {/* Menu Section */}
      <div className="mb-6">
        <h2 className="text-xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <Utensils className="w-5 h-5 text-orange-500" /> Thực đơn món ngon đang bán
        </h2>
        <p className="text-xs text-slate-600 mt-0.5">Chọn món vào giỏ để tiếp tục đặt hàng</p>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <div key={n} className="h-44 rounded-2xl bg-slate-200 animate-pulse" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-3xl border border-slate-200 p-8">
          <p className="text-slate-600 font-medium">Hiện tại quán chưa có món nào đang bán.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {items.map((item) => {
            const qty = getItemQuantityInCart(item.itemId);
            return (
              <div
                key={item.itemId}
                className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <h3 className="font-bold text-slate-900 text-base">{item.itemName}</h3>
                    <span className="text-sm font-extrabold text-orange-600 whitespace-nowrap">
                      {Number(item.price).toLocaleString('vi-VN')} đ
                    </span>
                  </div>
                  {item.description && (
                    <p className="text-xs text-slate-600 line-clamp-2 mb-4 leading-relaxed">
                      {item.description}
                    </p>
                  )}
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between">
                  <span className="text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
                    Sẵn sàng phục vụ
                  </span>

                  {qty > 0 ? (
                    <div className="flex items-center gap-2 bg-orange-50 border border-orange-200 rounded-xl p-1">
                      <button
                        onClick={() => updateQuantity(item.itemId, -1)}
                        className="w-7 h-7 rounded-lg bg-white text-orange-600 hover:bg-orange-100 flex items-center justify-center font-bold shadow-sm transition"
                      >
                        <Minus className="w-3.5 h-3.5" />
                      </button>
                      <span className="font-bold text-xs text-orange-950 px-1">{qty}</span>
                      <button
                        onClick={() => updateQuantity(item.itemId, 1)}
                        className="w-7 h-7 rounded-lg bg-orange-500 text-white hover:bg-orange-600 flex items-center justify-center font-bold shadow-sm transition"
                      >
                        <Plus className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <button
                      onClick={() => handleAddToCart(item)}
                      className="px-3.5 py-2 rounded-xl text-xs font-bold text-white bg-slate-900 hover:bg-orange-600 shadow-sm transition flex items-center gap-1.5"
                    >
                      <Plus className="w-3.5 h-3.5" /> Thêm vào giỏ
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Floating Bottom Cart Bar for easy checkout */}
      {cart.items.length > 0 && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-30 w-full max-w-xl px-4 animate-bounce-short">
          <div className="bg-slate-900 text-white rounded-2xl p-4 shadow-2xl flex items-center justify-between border border-slate-700 backdrop-blur">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-orange-500 text-white flex items-center justify-center font-bold shadow-md">
                <ShoppingBag className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-slate-600 font-medium">Giỏ hàng ({cart.restaurantName})</p>
                <p className="text-sm font-extrabold text-white">
                  {totalQuantity} món • {subtotal.toLocaleString('vi-VN')} đ
                </p>
              </div>
            </div>
            <Link
              to="/customer/checkout"
              className="px-5 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 font-bold text-xs text-white shadow-md shadow-orange-500/25 transition"
            >
              Xem giỏ & Đặt đơn
            </Link>
          </div>
        </div>
      )}

      {/* Confirmation Modal when switching restaurant */}
      {confirmModal.isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-100">
            <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-600 flex items-center justify-center mb-4">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <h3 className="text-lg font-bold text-slate-900 mb-2">Tạo giỏ hàng mới?</h3>
            <p className="text-sm text-slate-600 mb-6 leading-relaxed">
              Bạn đang có món trong giỏ hàng từ quán{' '}
              <span className="font-bold text-slate-800">"{confirmModal.currentRestaurant}"</span>. Nếu
              thêm món từ quán{' '}
              <span className="font-bold text-orange-600">"{confirmModal.newRestaurant}"</span>, giỏ
              hàng cũ sẽ bị xoá. Bạn có muốn tiếp tục?
            </p>
            <div className="flex items-center justify-end gap-3">
              <button
                onClick={() => setConfirmModal({ isOpen: false, pendingItem: null, currentRestaurant: '', newRestaurant: '' })}
                className="px-4 py-2.5 text-xs font-bold text-slate-600 hover:bg-slate-100 rounded-xl transition"
              >
                Hủy bỏ
              </button>
              <button
                onClick={confirmSwitchRestaurant}
                className="px-5 py-2.5 text-xs font-bold text-white bg-orange-500 hover:bg-orange-600 rounded-xl shadow-md shadow-orange-500/20 transition"
              >
                Đồng ý, tạo giỏ mới
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
