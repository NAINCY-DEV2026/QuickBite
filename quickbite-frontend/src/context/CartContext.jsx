import { createContext, useContext, useState, useCallback } from 'react';
import { getCart } from '../services/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [itemCount, setItemCount] = useState(0);
  const { isAuthenticated } = useAuth();

  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      setItemCount(0);
      return;
    }
    try {
      const res = await getCart();
      const count = (res.data.items || []).reduce((sum, i) => sum + i.quantity, 0);
      setItemCount(count);
    } catch {
      setItemCount(0);
    }
  }, [isAuthenticated]);

  return (
    <CartContext.Provider value={{ itemCount, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}