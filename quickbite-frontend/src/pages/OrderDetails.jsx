import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrderById, cancelOrder } from '../services/orderService';
import OrderStatusTracker from '../components/OrderStatusTracker';
import ReviewForm from '../components/ReviewForm';
import { useAuth } from '../context/AuthContext';
import { money, formatDateTime } from '../utils/format';
import { getErrorMessage } from '../utils/errors';

export default function OrderDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [loadError, setLoadError] = useState('');
  const [reviewSubmitted, setReviewSubmitted] = useState(false);

  const load = async () => {
    try {
      const res = await getOrderById(id);
      setOrder(res.data);
      setLoadError('');
    } catch (err) {
      setLoadError(getErrorMessage(err, 'Could not load this order'));
    }
  };

  // Refresh every 20 seconds so status changes show up without reloading the page
  useEffect(() => {
    load();
    const timer = setInterval(load, 20000);
    return () => clearInterval(timer);
  }, [id]); // eslint-disable-line

  const handleCancel = async () => {
    if (!window.confirm('Cancel this order?')) return;
    try {
      await cancelOrder(id);
      setError('');
      load();
    } catch (err) {
      setError(getErrorMessage(err, 'Could not cancel order'));
    }
  };

  if (loadError && !order) {
    return (
      <div className="container" style={{ maxWidth: '560px', paddingTop: '3rem' }}>
        <div className="notice notice-error">{loadError}</div>
      </div>
    );
  }
  if (!order) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  const isCustomer = user.role === 'CUSTOMER';
  const canCancel = isCustomer && ['PLACED', 'CONFIRMED'].includes(order.orderStatus);
  const canReview = isCustomer && order.orderStatus === 'DELIVERED' && !reviewSubmitted;

  const backLink = {
    CUSTOMER: { to: '/orders', text: '← Back to orders' },
    RESTAURANT_OWNER: { to: `/owner/restaurants/${order.restaurantId}/orders`, text: '← Back to incoming orders' },
    ADMIN: { to: '/admin/orders', text: '← Back to all orders' },
  }[user.role];

  return (
    <div className="container" style={{ maxWidth: '560px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      {backLink && <Link to={backLink.to} style={{ color: '#8A8075', fontSize: '0.88rem', textDecoration: 'none' }}>{backLink.text}</Link>}

      <h2 style={{ fontSize: '1.8rem', marginTop: '0.5rem' }}>Order #{order.id}</h2>
      <p style={{ color: '#8A8075', marginBottom: '2rem' }}>
        {order.restaurantName} · {formatDateTime(order.createdAt)}
      </p>

      {error && <div className="notice notice-error">{error}</div>}

      <div className="ticket mb-3">
        <OrderStatusTracker status={order.orderStatus} />
      </div>

      <div className="ticket mb-3">
        <h4 style={{ fontSize: '1rem' }}>{isCustomer ? 'Delivering to' : 'Customer'}</h4>
        {!isCustomer && (
          <p style={{ margin: 0, fontWeight: 600 }}>
            {order.customerName} · {order.customerPhone}
          </p>
        )}
        <p style={{ margin: 0, fontSize: '0.9rem', color: '#5C5248' }}>{order.deliveryAddress}</p>
      </div>

      <div className="ticket mb-3">
        {order.items.map((item) => (
          <div key={item.id} className="d-flex justify-content-between" style={{ padding: '0.4rem 0', fontSize: '0.92rem' }}>
            <span>{item.foodName} × {item.quantity}</span>
            <span>{money(item.totalPrice)}</span>
          </div>
        ))}

        <div style={{ borderTop: '1px dashed var(--line)', marginTop: '0.6rem', paddingTop: '0.6rem', fontSize: '0.88rem', color: '#5C5248' }}>
          <div className="d-flex justify-content-between"><span>Subtotal</span><span>{money(order.subtotal)}</span></div>
          <div className="d-flex justify-content-between"><span>Delivery fee</span><span>{money(order.deliveryFee)}</span></div>
          <div className="d-flex justify-content-between"><span>Tax</span><span>{money(order.tax)}</span></div>
          {Number(order.discount) > 0 && (
            <div className="d-flex justify-content-between" style={{ color: 'var(--basil)' }}>
              <span>Discount{order.couponCode ? ` (${order.couponCode})` : ''}</span>
              <span>−{money(order.discount)}</span>
            </div>
          )}
        </div>

        <div className="d-flex justify-content-between pt-2 mt-2" style={{ borderTop: '1px solid var(--line)', fontWeight: 700 }}>
          <span>Total</span><span>{money(order.totalAmount)}</span>
        </div>
        <p style={{ margin: '0.5rem 0 0', fontSize: '0.8rem', color: '#8A8075' }}>
          Payment: {order.paymentStatus.toLowerCase()}
        </p>
      </div>

      {canCancel && (
        <button className="btn-ghost w-100" onClick={handleCancel}>Cancel order</button>
      )}

      {canReview && <ReviewForm orderId={Number(id)} onSubmitted={() => setReviewSubmitted(true)} />}

      {reviewSubmitted && <div className="notice notice-success mt-3">Thanks for your review!</div>}
    </div>
  );
}