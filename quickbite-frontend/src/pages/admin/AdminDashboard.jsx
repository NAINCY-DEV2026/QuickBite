import { useEffect, useState } from 'react';
import { getStats } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import StatCard from '../../components/StatCard';
import { money } from '../../utils/format';
import { getErrorMessage } from '../../utils/errors';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getStats()
      .then((res) => setStats(res.data))
      .catch((err) => setError(getErrorMessage(err, 'Could not load statistics')));
  }, []);

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      {error && <div className="notice notice-error">{error}</div>}
      {!stats && !error && <p style={{ color: '#8A8075' }}>Loading...</p>}

      {stats && (
        <>
          <div className="row g-3 mb-3">
            <div className="col-md-3 col-6"><StatCard label="Total revenue" value={money(stats.totalRevenue)} color="var(--chili)" hint="Delivered orders" /></div>
            <div className="col-md-3 col-6"><StatCard label="Avg order value" value={money(stats.averageOrderValue)} /></div>
            <div className="col-md-3 col-6"><StatCard label="Total orders" value={stats.totalOrders} /></div>
            <div className="col-md-3 col-6"><StatCard label="In progress" value={stats.ordersInProgress} color="#9A6A00" /></div>
          </div>
          <div className="row g-3">
            <div className="col-md-3 col-6"><StatCard label="Delivered" value={stats.completedOrders} color="var(--basil)" /></div>
            <div className="col-md-3 col-6"><StatCard label="Cancelled" value={stats.cancelledOrders} color="#6B6258" /></div>
            <div className="col-md-3 col-6"><StatCard label="Restaurants" value={`${stats.activeRestaurants} / ${stats.totalRestaurants}`} hint="Active / total" /></div>
            <div className="col-md-3 col-6"><StatCard label="Users" value={stats.totalUsers} /></div>
          </div>
        </>
      )}
    </div>
  );
}