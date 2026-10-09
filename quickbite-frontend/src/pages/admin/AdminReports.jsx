import { useEffect, useState } from 'react';
import { getReports } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import { label } from '../../utils/status';
import { money, shortDate } from '../../utils/format';
import { getErrorMessage } from '../../utils/errors';

const ranges = [7, 14, 30];

export default function AdminReports() {
  const [days, setDays] = useState(7);
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setError('');
    getReports(days)
      .then((res) => { if (!cancelled) setReport(res.data); })
      .catch((err) => { if (!cancelled) setError(getErrorMessage(err, 'Could not load reports')); });
    return () => { cancelled = true; };
  }, [days]);

  const maxRevenue = report ? Math.max(...report.dailyRevenue.map((d) => Number(d.revenue)), 1) : 1;
  const maxStatus = report ? Math.max(...Object.values(report.ordersByStatus), 1) : 1;
  const rangeRevenue = report ? report.dailyRevenue.reduce((sum, d) => sum + Number(d.revenue), 0) : 0;

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      {error && <div className="notice notice-error">{error}</div>}
      {!report && !error && <p style={{ color: '#8A8075' }}>Loading...</p>}

      {report && (
        <>
          <div className="ticket mb-4">
            <div className="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
              <div>
                <h4 style={{ fontSize: '1.1rem', margin: 0 }}>Revenue by day</h4>
                <p style={{ color: '#8A8075', fontSize: '0.85rem', margin: 0 }}>
                  {money(rangeRevenue)} from delivered orders in the last {report.rangeDays} days
                </p>
              </div>
              <div className="d-flex gap-2">
                {ranges.map((r) => (
                  <button key={r} className={`chip ${days === r ? 'active' : ''}`} onClick={() => setDays(r)}>
                    {r} days
                  </button>
                ))}
              </div>
            </div>

            <div className="col-chart">
              {report.dailyRevenue.map((d) => (
                <div className="col" key={d.date} title={`${shortDate(d.date)}: ${money(d.revenue)} (${d.orders} orders)`}>
                  <div className="col-bar" style={{ height: `${(Number(d.revenue) / maxRevenue) * 100}%` }} />
                </div>
              ))}
            </div>
            <div className="col-labels">
              {report.dailyRevenue.map((d, i) => (
                <span key={d.date}>{report.rangeDays <= 14 || i % 5 === 0 ? shortDate(d.date) : ''}</span>
              ))}
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-lg-6">
              <div className="ticket h-100">
                <h4 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>Orders by status</h4>
                {Object.entries(report.ordersByStatus).map(([status, count]) => (
                  <div className="bar-row" key={status}>
                    <span className="bar-label">{label(status)}</span>
                    <div className="bar-track">
                      <div className="bar-fill" style={{ width: `${(count / maxStatus) * 100}%`, opacity: count === 0 ? 0.25 : 1 }} />
                    </div>
                    <span className="bar-value">{count}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="col-lg-6">
              <div className="ticket h-100" style={{ padding: 0, overflow: 'hidden' }}>
                <h4 style={{ fontSize: '1.1rem', margin: 0, padding: '1.5rem 1.5rem 0.75rem' }}>Top restaurants</h4>
                {report.topRestaurants.length === 0 && (
                  <p style={{ padding: '0 1.5rem 1.5rem', color: '#8A8075' }}>No delivered orders yet.</p>
                )}
                {report.topRestaurants.map((r, i) => (
                  <div className="data-row" key={r.restaurantId}>
                    <span>
                      <strong>{i + 1}.</strong> {r.name}
                      <span style={{ color: '#8A8075', fontSize: '0.8rem' }}> · {r.orders} orders</span>
                    </span>
                    <strong>{money(r.revenue)}</strong>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
            <h4 style={{ fontSize: '1.1rem', margin: 0, padding: '1.5rem 1.5rem 0.75rem' }}>Most ordered dishes</h4>
            {report.popularFoods.length === 0 && (
              <p style={{ padding: '0 1.5rem 1.5rem', color: '#8A8075' }}>No delivered orders yet.</p>
            )}
            {report.popularFoods.map((f, i) => (
              <div className="data-row" key={f.foodId}>
                <span>
                  <strong>{i + 1}.</strong> {f.name}
                  <span style={{ color: '#8A8075', fontSize: '0.8rem' }}> · {f.restaurantName}</span>
                </span>
                <span>
                  <strong>{f.quantitySold}</strong> sold
                  <span style={{ color: '#8A8075', fontSize: '0.8rem' }}> · {money(f.revenue)}</span>
                </span>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}