import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addCartItem } from '../services/cartService';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { getErrorMessage } from '../utils/errors';

export default function useAddToCart() {
  const { isAuthenticated, user } = useAuth();
  const { refreshCart } = useCart();
  const navigate = useNavigate();
  const [toast, setToast] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setToast({ type, text });
    setTimeout(() => setToast({ type: '', text: '' }), 3500);
  };

  const addToCart = async (food) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (user.role !== 'CUSTOMER') {
      flash('error', 'Only customers can place orders');
      return;
    }
    try {
      await addCartItem({ foodItemId: food.id, quantity: 1 });
      await refreshCart();
      flash('success', `${food.name} added to cart`);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not add item'));
    }
  };

  return { addToCart, toast };
}