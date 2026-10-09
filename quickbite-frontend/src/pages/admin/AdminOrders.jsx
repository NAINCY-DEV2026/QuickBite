import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAdminOrders } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import Pagination from '../../components/Pagination';
import { pillStyle, label } from '../../utils/status';
import { money, formatDateTime } from '../../utils/format';
import { getErrorMessage } from '../../utils/errors';

const statuses = ['', 'PLACED', 'CONFIRMED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED', 'REJECTED'];

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const params = { page, size: 10, sort: 'createdAt,desc' };
        if (status) params.status = status;
        const res = await getAdminOrders(params);
        if (cancelled) return;
        setOrders(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotal(res.data.totalElements);
      } catch (err) {
        if (!cancelled) setError(getErrorMessage(err, 'Could not load orders'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [page, status]);

  const chooseStatus = (value) => {
    setPage(0);
    setStatus(value);
  };

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      <div className="d-flex gap-2 flex-wrap mb-3">
        {statuses.map((s) => (
          <button key={s || 'all'} className={`chip ${status === s ? 'active' : ''}`} onClick={() => chooseStatus(s)}>
            {s ? label(s) : 'All'}
          </button>
        ))}
      </div>

      {error && <div className="notice notice-error">{error}</div>}
      <p style={{ color: '#8A8075', fontSize: '0.88rem' }}>{loading ? 'Loading...' : `${total} orders`}</p>

      <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
        {!loading && orders.length === 0 && (
          <p style={{ padding: '2rem', margin: 0, textAlign: 'center', color: '#8A8075' }}>No orders found.</p>
        )}
        {orders.map((o) => (
          <Link key={o.id} to={`/orders/${o.id}`} className="data-row text-decoration-none" style={{ color: 'var(--ink)' }}>
            <div style={{ minWidth: 0 }}>
              <p style={{ fontWeight: 600, margin: 0 }}>#{o.id} · {o.restaurantName}</p>
              <p style={{ color: '#8A8075', fontSize: '0.83rem', margin: 0 }}>
                {o.customerName} · {formatDateTime(o.createdAt)} · {o.items.length} {o.items.length === 1 ? 'item' : 'items'}
              </p>
            </div>
            <div className="d-flex align-items-center gap-3">
              <span style={{ fontWeight: 700 }}>{money(o.totalAmount)}</span>
              <span className="status-pill" style={pillStyle(o.orderStatus)}>{label(o.orderStatus)}</span>
            </div>
          </Link>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}