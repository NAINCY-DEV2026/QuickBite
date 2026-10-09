import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAllRestaurants, blockRestaurant, unblockRestaurant } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import Pagination from '../../components/Pagination';
import { pillStyle } from '../../utils/status';
import { getErrorMessage } from '../../utils/errors';

export default function AdminRestaurants() {
  const [restaurants, setRestaurants] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4000);
  };

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const res = await getAllRestaurants({ page, size: 10 });
        if (cancelled) return;
        setRestaurants(res.data.content);
        setTotalPages(res.data.totalPages);
      } catch (err) {
        if (!cancelled) flash('error', getErrorMessage(err, 'Could not load restaurants'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [page, reloadKey]);

  const handleToggle = async (r) => {
    if (!window.confirm(`${r.isActive ? 'Block' : 'Unblock'} ${r.name}?`)) return;
    try {
      if (r.isActive) await blockRestaurant(r.id);
      else await unblockRestaurant(r.id);
      flash('success', `${r.name} ${r.isActive ? 'blocked' : 'is live again'}`);
      setReloadKey((k) => k + 1);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Action failed'));
    }
  };

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
        {loading && <p style={{ padding: '2rem', margin: 0, color: '#8A8075' }}>Loading...</p>}
        {!loading && restaurants.length === 0 && (
          <p style={{ padding: '2rem', margin: 0, textAlign: 'center', color: '#8A8075' }}>No restaurants yet.</p>
        )}
        {restaurants.map((r) => (
          <div key={r.id} className="data-row">
            <div style={{ minWidth: 0 }}>
              <p style={{ fontWeight: 600, margin: 0 }}>{r.name}</p>
              <p style={{ color: '#8A8075', fontSize: '0.83rem', margin: 0 }}>
                {r.city} · {r.cuisineType || 'Multi-cuisine'} · ★ {Number(r.rating) > 0 ? Number(r.rating).toFixed(1) : 'New'}
              </p>
            </div>
            <div className="d-flex align-items-center gap-3">
              <span className="status-pill" style={pillStyle(r.isActive ? 'ACTIVE' : 'BLOCKED')}>
                {r.isActive ? 'Active' : 'Blocked'}
              </span>
              <Link to={`/restaurants/${r.id}`} className="btn-ghost" style={{ padding: '0.35rem 0.9rem', fontSize: '0.82rem' }}>View</Link>
              <button className="btn-ghost" style={{ padding: '0.35rem 0.9rem', fontSize: '0.82rem' }} onClick={() => handleToggle(r)}>
                {r.isActive ? 'Block' : 'Unblock'}
              </button>
            </div>
          </div>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}