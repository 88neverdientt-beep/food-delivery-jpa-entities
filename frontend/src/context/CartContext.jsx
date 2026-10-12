import React, { createContext, useContext, useState, useEffect } from 'react';

const CartContext = createContext(null);

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(() => {
    try {
      const saved = localStorage.getItem('cart');
      return saved ? JSON.parse(saved) : { restaurantId: null, restaurantName: '', items: [] };
    } catch {
      return { restaurantId: null, restaurantName: '', items: [] };
    }
  });

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  // Thêm món vào giỏ. Nếu khác nhà hàng, trả về { needsConfirm: true, targetRestaurant }
  const addItem = (item, restaurant) => {
    if (cart.restaurantId && cart.restaurantId !== restaurant.restaurantId && cart.items.length > 0) {
      return {
        needsConfirm: true,
        currentRestaurant: cart.restaurantName,
        newRestaurant: restaurant.restaurantName,
      };
    }

    setCart((prev) => {
      const existingIndex = prev.items.findIndex((i) => i.itemId === item.itemId);
      let newItems;
      if (existingIndex > -1) {
        newItems = [...prev.items];
        newItems[existingIndex] = {
          ...newItems[existingIndex],
          quantity: newItems[existingIndex].quantity + (item.quantity || 1),
        };
      } else {
        newItems = [...prev.items, { ...item, quantity: item.quantity || 1 }];
      }

      return {
        restaurantId: restaurant.restaurantId,
        restaurantName: restaurant.restaurantName,
        items: newItems,
      };
    });

    return { needsConfirm: false };
  };

  const forceAddItem = (item, restaurant) => {
    setCart({
      restaurantId: restaurant.restaurantId,
      restaurantName: restaurant.restaurantName,
      items: [{ ...item, quantity: item.quantity || 1 }],
    });
  };

  const updateQuantity = (itemId, delta) => {
    setCart((prev) => {
      const newItems = prev.items
        .map((item) => {
          if (item.itemId === itemId) {
            const newQty = item.quantity + delta;
            return newQty > 0 ? { ...item, quantity: newQty } : null;
          }
          return item;
        })
        .filter(Boolean);

      return {
        restaurantId: newItems.length === 0 ? null : prev.restaurantId,
        restaurantName: newItems.length === 0 ? '' : prev.restaurantName,
        items: newItems,
      };
    });
  };

  const removeItem = (itemId) => {
    setCart((prev) => {
      const newItems = prev.items.filter((item) => item.itemId !== itemId);
      return {
        restaurantId: newItems.length === 0 ? null : prev.restaurantId,
        restaurantName: newItems.length === 0 ? '' : prev.restaurantName,
        items: newItems,
      };
    });
  };

  const clearCart = () => {
    setCart({ restaurantId: null, restaurantName: '', items: [] });
  };

  const totalQuantity = cart.items.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = cart.items.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <CartContext.Provider
      value={{
        cart,
        addItem,
        forceAddItem,
        updateQuantity,
        removeItem,
        clearCart,
        totalQuantity,
        subtotal,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
