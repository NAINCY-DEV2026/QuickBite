import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getRestaurantOrders, updateOrderStatus } from '../services/orderService';
import { pillStyle, label } from '../utils/status';
import { money, formatDateTime } from '../utils/format';
import { getErrorMessage } from '../utils/errors';

const nextActions = {
  PLACED: [{ status: 'CONFIRMED', text: 'Accept order' }, { status: 'REJECTED', text: 'Reject', danger: true }],
  CONFIRMED: [{ status: 'PREPARING', text: 'Start preparing' }],
  PREPARING: [{ status: 'READY_FOR_PICKUP', text: 'Mark ready' }],
  READY_FOR_PICKUP: [{ status: 'OUT_FOR_DELIVERY', text: 'Out for delivery' }],
  OUT_FOR_DELIVERY: [{ status: 'DELIVERED', text: 'Mark delivered' }],
};

const finalStatuses = ['DELIVERED', 'CANCELLED', 'REJECTED'];

export default function RestaurantOrders() {
  const { id } = useParams();
  const [orders, setOrders] = useState([]);
  const [filter, setFilter] = useState('ACTIVE');
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4000);
  };

  const load = async () => {
    try {
      const res = await getRestaurantOrders(id, { page: 0, size: 50, sort: 'createdAt,desc' });
      setOrders(res.data.content);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not load orders'));
    } finally {
      setLoading(false);
    }
  };

  // New orders show up on their own: poll every 30 seconds
  useEffect(() => {
    load();
    const timer = setInterval(load, 30000);
    return () => clearInterval(timer);
  }, [id]); // eslint-disable-line

  const handleStatus = async (order, action) => {
    if (action.danger && !window.confirm(`Reject order #${order.id}? The customer will be refunded if they paid.`)) return;
    setBusyId(order.id);
    try {
      await updateOrderStatus(order.id, action.status);
      await load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not update order'));
    } finally {
      setBusyId(null);
    }
  };

  const visible = filter === 'ACTIVE' ? orders.filter((o) => !finalStatuses.includes(o.orderStatus)) : orders;

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  return (
    <div className="container" style={{ maxWidth: '720px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <Link to="/owner" style={{ color: '#8A8075', fontSize: '0.88rem', textDecoration: 'none' }}>← Back to restaurants</Link>
      <div className="d-flex justify-content-between align-items-center flex-wrap gap-2 mt-2 mb-3">
        <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Incoming orders</h2>
        <div className="d-flex gap-2">
          <button className={`chip ${filter === 'ACTIVE' ? 'active' : ''}`} onClick={() => setFilter('ACTIVE')}>Active</button>
          <button className={`chip ${filter === 'ALL' ? 'active' : ''}`} onClick={() => setFilter('ALL')}>All</button>
        </div>
      </div>

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      {visible.length === 0 ? (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>
          {filter === 'ACTIVE' ? 'No active orders right now. This page refreshes automatically.' : 'No orders yet.'}
        </div>
      ) : (
        visible.map((o) => (
          <div key={o.id} className="ticket mb-3">
            <div className="d-flex justify-content-between align-items-start">
              <div>
                <p style={{ fontWeight: 700, margin: 0 }}>
                  <Link to={`/orders/${o.id}`} style={{ color: 'var(--ink)', textDecoration: 'none' }}>Order #{o.id}</Link>
                </p>
                <p style={{ color: '#8A8075', fontSize: '0.83rem', margin: 0 }}>{formatDateTime(o.createdAt)}</p>
              </div>
              <span className="status-pill" style={pillStyle(o.orderStatus)}>{label(o.orderStatus)}</span>
            </div>

            <div style={{ margin: '0.9rem 0', fontSize: '0.88rem' }}>
              <p style={{ margin: 0, fontWeight: 600 }}>{o.customerName} · {o.customerPhone}</p>
              <p style={{ margin: 0, color: '#5C5248' }}>{o.deliveryAddress}</p>
            </div>

            <div style={{ borderTop: '1px dashed var(--line)', paddingTop: '0.6rem' }}>
              {o.items.map((item) => (
                <p key={item.id} style={{ fontSize: '0.88rem', margin: '0.15rem 0', color: '#5C5248' }}>
                  {item.quantity} × {item.foodName}
                </p>
              ))}
            </div>

            <div className="d-flex justify-content-between align-items-center mt-3 pt-2" style={{ borderTop: '1px solid var(--line)' }}>
              <div>
                <strong>{money(o.totalAmount)}</strong>
                <span style={{ fontSize: '0.8rem', marginLeft: '0.5rem', color: o.paymentStatus === 'PENDING' ? '#9A6A00' : '#8A8075' }}>
                  {o.paymentStatus === 'PENDING' ? 'Collect cash on delivery' : `Payment ${o.paymentStatus.toLowerCase()}`}
                </span>
              </div>
              <div className="d-flex gap-2">
                {(nextActions[o.orderStatus] || []).map((action) => (
                  <button
                    key={action.status}
                    className={action.danger ? 'btn-ghost' : 'btn-chili'}
                    style={{
                      padding: '0.4rem 0.95rem', fontSize: '0.82rem',
                      ...(action.danger ? { color: 'var(--chili)', borderColor: 'var(--chili)' } : {}),
                    }}
                    disabled={busyId === o.id}
                    onClick={() => handleStatus(o, action)}
                  >
                    {action.text}
                  </button>
                ))}
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}