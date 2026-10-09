import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { getAddresses } from '../services/addressService';
import { previewOrder, placeOrder } from '../services/orderService';
import { useCart } from '../context/CartContext';

const paymentOptions = [
  { value: 'COD', label: 'Cash on delivery', hint: 'Pay when your food arrives' },
  { value: 'UPI_SIMULATION', label: 'UPI (simulated)', hint: 'Instant demo payment' },
  { value: 'CARD_SIMULATION', label: 'Card (simulated)', hint: 'No real card details are used' },
];

const money = (value) => `₹${Number(value).toFixed(2)}`;

export default function Checkout() {
  const [summary, setSummary] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [addressId, setAddressId] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [couponInput, setCouponInput] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState('');
  const [couponError, setCouponError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const navigate = useNavigate();
  const { refreshCart } = useCart();

  useEffect(() => {
    const load = async () => {
      try {
        const [aRes, pRes] = await Promise.all([getAddresses(), previewOrder({})]);
        setAddresses(aRes.data);
        if (aRes.data.length) setAddressId(aRes.data[0].id);
        setSummary(pRes.data);
      } catch (err) {
        setLoadError(err.response?.data?.message || 'Could not load checkout');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const applyCoupon = async () => {
    const code = couponInput.trim();
    if (!code) return;
    setCouponError('');
    try {
      const res = await previewOrder({ couponCode: code });
      setSummary(res.data);
      setAppliedCoupon(res.data.couponCode);
    } catch (err) {
      setCouponError(err.response?.data?.message || 'Could not apply coupon');
    }
  };

  const removeCoupon = async () => {
    setCouponInput('');
    setAppliedCoupon('');
    setCouponError('');
    try {
      const res = await previewOrder({});
      setSummary(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not refresh totals');
    }
  };

  const handlePlaceOrder = async () => {
    if (!addressId) {
      setError('Please add or select a delivery address first');
      return;
    }
    setPlacing(true);
    setError('');
    try {
      const res = await placeOrder({
        addressId,
        couponCode: appliedCoupon || undefined,
        paymentMethod,
      });
      await refreshCart();
      navigate(`/orders/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not place order');
    } finally {
      setPlacing(false);
    }
  };

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading checkout...</div>;

  if (loadError) {
    return (
      <div className="container" style={{ maxWidth: '520px', paddingTop: '3rem' }}>
        <div className="ticket text-center" style={{ padding: '2.5rem' }}>
          <p style={{ color: '#8A8075', marginBottom: '1.5rem' }}>{loadError}</p>
          <Link to="/restaurants" className="btn-chili">Browse restaurants</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="container" style={{ maxWidth: '560px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '0.3rem' }}>Checkout</h2>
      <p style={{ color: '#8A8075', marginBottom: '1.75rem' }}>
        {summary.itemCount} {summary.itemCount === 1 ? 'item' : 'items'} from {summary.restaurantName}
      </p>

      {error && <div className="notice notice-error">{error}</div>}

      <div className="ticket mb-3">
        <h4 style={{ fontSize: '1.05rem' }}>Delivery address</h4>
        {addresses.length === 0 ? (
          <p style={{ color: '#8A8075', margin: 0 }}>
            No saved address. <Link to="/addresses" style={{ color: 'var(--chili)', fontWeight: 600 }}>Add one</Link>
          </p>
        ) : (
          addresses.map((a) => (
            <label key={a.id} className="d-flex align-items-start gap-2" style={{ padding: '0.5rem 0', cursor: 'pointer' }}>
              <input
                type="radio"
                name="address"
                checked={String(addressId) === String(a.id)}
                onChange={() => setAddressId(a.id)}
                style={{ marginTop: '0.3rem' }}
              />
              <span style={{ fontSize: '0.92rem' }}>
                <strong>{a.addressType}</strong> · {a.houseFlat}, {a.street}, {a.city} {a.pincode}
              </span>
            </label>
          ))
        )}
      </div>

      <div className="ticket mb-3">
        <h4 style={{ fontSize: '1.05rem' }}>Coupon</h4>
        {appliedCoupon ? (
          <div className="d-flex justify-content-between align-items-center">
            <span style={{ color: 'var(--basil)', fontWeight: 600 }}>
              {appliedCoupon} applied · you save {money(summary.discount)}
            </span>
            <button className="qb-link-btn" onClick={removeCoupon}>Remove</button>
          </div>
        ) : (
          <>
            <div className="d-flex gap-2">
              <input
                className="field-input"
                placeholder="Enter coupon code"
                value={couponInput}
                onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
              />
              <button className="btn-ghost" onClick={applyCoupon}>Apply</button>
            </div>
            {couponError && <p style={{ color: 'var(--chili)', fontSize: '0.85rem', margin: '0.6rem 0 0' }}>{couponError}</p>}
          </>
        )}
      </div>

      <div className="ticket mb-3">
        <h4 style={{ fontSize: '1.05rem' }}>Payment method</h4>
        {paymentOptions.map((m) => (
          <label key={m.value} className="d-flex align-items-start gap-2" style={{ padding: '0.5rem 0', cursor: 'pointer' }}>
            <input
              type="radio"
              name="payment"
              checked={paymentMethod === m.value}
              onChange={() => setPaymentMethod(m.value)}
              style={{ marginTop: '0.3rem' }}
            />
            <span>
              <span style={{ fontSize: '0.92rem', fontWeight: 600 }}>{m.label}</span>
              <br />
              <span style={{ fontSize: '0.8rem', color: '#8A8075' }}>{m.hint}</span>
            </span>
          </label>
        ))}
      </div>

      <div className="ticket">
        <h4 style={{ fontSize: '1.05rem', marginBottom: '1rem' }}>Order summary</h4>
        <div className="d-flex justify-content-between" style={{ fontSize: '0.92rem', marginBottom: '0.45rem' }}>
          <span>Subtotal</span><span>{money(summary.subtotal)}</span>
        </div>
        <div className="d-flex justify-content-between" style={{ fontSize: '0.92rem', marginBottom: '0.45rem' }}>
          <span>Delivery fee</span><span>{money(summary.deliveryFee)}</span>
        </div>
        <div className="d-flex justify-content-between" style={{ fontSize: '0.92rem', marginBottom: '0.45rem' }}>
          <span>Tax</span><span>{money(summary.tax)}</span>
        </div>
        {Number(summary.discount) > 0 && (
          <div className="d-flex justify-content-between" style={{ fontSize: '0.92rem', marginBottom: '0.45rem', color: 'var(--basil)' }}>
            <span>Discount ({summary.couponCode})</span><span>−{money(summary.discount)}</span>
          </div>
        )}
        <div
          className="d-flex justify-content-between"
          style={{ borderTop: '1px dashed var(--line)', marginTop: '0.75rem', paddingTop: '0.9rem', fontWeight: 700, fontSize: '1.2rem' }}
        >
          <span>Total</span><span>{money(summary.totalAmount)}</span>
        </div>
      </div>

      <button className="btn-chili w-100 mt-3" disabled={placing} onClick={handlePlaceOrder}>
        {placing ? 'Placing order...' : `Place order · ${money(summary.totalAmount)}`}
      </button>
    </div>
  );
}