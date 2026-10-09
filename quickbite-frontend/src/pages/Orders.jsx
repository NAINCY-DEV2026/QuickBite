import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getMyOrders } from '../services/orderService';

export default function Orders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMyOrders({ page: 0, size: 20, sort: 'createdAt,desc' })
      .then((res) => setOrders(res.data.content))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  return (
    <div className="container" style={{ maxWidth: '640px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '2rem' }}>Your orders</h2>

      {orders.length === 0 ? (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>
          No orders yet. <Link to="/restaurants" style={{ color: 'var(--chili)', fontWeight: 600 }}>Find something tasty</Link>
        </div>
      ) : (
        orders.map((o) => {
          const delivered = o.orderStatus === 'DELIVERED';
          const cancelled = o.orderStatus === 'CANCELLED' || o.orderStatus === 'REJECTED';
          return (
            <Link key={o.id} to={`/orders/${o.id}`} className="ticket d-block text-decoration-none mb-3">
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <p style={{ fontWeight: 600, margin: 0, color: 'var(--ink)' }}>{o.restaurantName}</p>
                  <p style={{ color: '#8A8075', fontSize: '0.85rem', margin: 0 }}>
                    {new Date(o.createdAt).toLocaleString()}
                  </p>
                </div>
                <div className="text-end">
                  <p style={{ fontWeight: 700, margin: 0, color: 'var(--ink)' }}>₹{o.totalAmount}</p>
                  <span
                    style={{
                      fontSize: '0.78rem', fontWeight: 600,
                      color: delivered ? 'var(--basil)' : cancelled ? '#8A8075' : 'var(--chili)',
                    }}
                  >
                    {o.orderStatus.replaceAll('_', ' ')}
                  </span>
                </div>
              </div>
              {delivered && (
                <div style={{ marginTop: '0.75rem', paddingTop: '0.75rem', borderTop: '1px dashed var(--line)' }}>
                  <span className="rating-summary" style={{ fontSize: '0.82rem', padding: '0.25rem 0.7rem' }}>
                    ★ How was it? Rate this order
                  </span>
                </div>
              )}
            </Link>
          );
        })
      )}
    </div>
  );
}