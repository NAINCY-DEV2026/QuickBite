import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getCart, updateCartItem, removeCartItem, clearCart } from '../services/cartService';
import { useCart } from '../context/CartContext';

export default function Cart() {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { refreshCart } = useCart();

  const load = async () => {
    setLoading(true);
    try {
      const res = await getCart();
      setCart(res.data);
    } catch {
      setCart(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleQty = async (itemId, qty) => {
    await updateCartItem(itemId, qty);
    load();
    refreshCart();
  };

  const handleRemove = async (itemId) => {
    await removeCartItem(itemId);
    load();
    refreshCart();
  };

  const handleClear = async () => {
    await clearCart();
    load();
    refreshCart();
  };

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading cart...</div>;

  const items = cart?.items || [];

  return (
    <div className="container" style={{ maxWidth: '680px', paddingTop: '3rem', paddingBottom: '4rem' }}>
      <h2 style={{ fontSize: '2rem', marginBottom: '0.3rem' }}>Your cart</h2>
      <p style={{ color: '#8A8075', marginBottom: '2rem' }}>
        {cart?.restaurantName ? `From ${cart.restaurantName}` : 'No restaurant selected yet'}
      </p>

      {items.length === 0 ? (
        <div className="ticket text-center" style={{ padding: '3rem' }}>
          <p style={{ color: '#8A8075', marginBottom: '1.5rem' }}>Your cart is empty.</p>
          <Link to="/restaurants" className="btn-chili">Browse restaurants</Link>
        </div>
      ) : (
        <>
          <div className="ticket">
            {items.map((item) => (
              <div key={item.id} className="d-flex justify-content-between align-items-center" style={{ padding: '0.9rem 0', borderBottom: '1px solid var(--line)' }}>
                <div>
                  <p style={{ fontWeight: 600, margin: 0 }}>{item.foodName}</p>
                  <p style={{ color: '#8A8075', fontSize: '0.85rem', margin: 0 }}>₹{item.unitPrice} each</p>
                </div>
                <div className="d-flex align-items-center gap-3">
                  <div className="d-flex align-items-center gap-2">
                    <button className="btn-ghost" style={{ padding: '0.2rem 0.6rem' }} onClick={() => handleQty(item.id, item.quantity - 1)}>−</button>
                    <span style={{ minWidth: '20px', textAlign: 'center' }}>{item.quantity}</span>
                    <button className="btn-ghost" style={{ padding: '0.2rem 0.6rem' }} onClick={() => handleQty(item.id, item.quantity + 1)}>+</button>
                  </div>
                  <span style={{ fontWeight: 700, minWidth: '60px', textAlign: 'right' }}>₹{item.totalPrice}</span>
                  <button onClick={() => handleRemove(item.id)} style={{ background: 'none', border: 'none', color: 'var(--chili)', cursor: 'pointer' }}>✕</button>
                </div>
              </div>
            ))}
            <div className="d-flex justify-content-between align-items-center pt-3">
              <span style={{ fontWeight: 600 }}>Subtotal</span>
              <span style={{ fontWeight: 700, fontSize: '1.2rem' }}>₹{cart.subtotal}</span>
            </div>
          </div>

          <div className="d-flex justify-content-between mt-3">
            <button className="btn-ghost" onClick={handleClear}>Clear cart</button>
            <button className="btn-chili" onClick={() => navigate('/checkout')}>Proceed to checkout</button>
          </div>
        </>
      )}
    </div>
  );
}