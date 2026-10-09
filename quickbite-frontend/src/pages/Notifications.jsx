import { useEffect, useState } from 'react';
import { getNotifications, markAsRead, markAllAsRead } from '../services/notificationService';
import { timeAgo, typeColor, typeLabel } from '../utils/notifications';

export default function Notifications() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    try {
      const res = await getNotifications();
      setItems(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleRead = async (n) => {
    if (n.isRead) return;
    await markAsRead(n.id);
    setItems((prev) => prev.map((i) => (i.id === n.id ? { ...i, isRead: true } : i)));
  };

  const handleReadAll = async () => {
    await markAllAsRead();
    setItems((prev) => prev.map((i) => ({ ...i, isRead: true })));
  };

  const unread = items.filter((i) => !i.isRead).length;

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  return (
    <div className="container" style={{ maxWidth: '680px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div className="d-flex justify-content-between align-items-end mb-4">
        <div>
          <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Notifications</h2>
          <p style={{ color: '#8A8075', margin: 0 }}>{unread > 0 ? `${unread} unread` : 'All caught up'}</p>
        </div>
        {unread > 0 && (
          <button className="btn-ghost" style={{ padding: '0.5rem 1.1rem', fontSize: '0.85rem' }} onClick={handleReadAll}>
            Mark all as read
          </button>
        )}
      </div>

      {items.length === 0 ? (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>
          No notifications yet. Updates about your orders will show up here.
        </div>
      ) : (
        items.map((n) => (
          <button
            key={n.id}
            onClick={() => handleRead(n)}
            className="ticket d-block w-100 text-start mb-3"
            style={{
              padding: '1.1rem 1.4rem',
              background: n.isRead ? 'var(--paper-raised)' : '#FFF6E5',
              cursor: n.isRead ? 'default' : 'pointer',
            }}
          >
            <div className="d-flex justify-content-between align-items-center mb-1">
              <span style={{
                fontSize: '0.72rem', fontWeight: 700, padding: '0.15rem 0.55rem', borderRadius: '6px',
                background: `${typeColor[n.type] || 'var(--chili)'}22`,
                color: typeColor[n.type] || 'var(--chili)',
              }}>
                {typeLabel[n.type] || n.type}
              </span>
              <span style={{ fontSize: '0.78rem', color: '#8A8075' }}>{timeAgo(n.createdAt)}</span>
            </div>
            <p style={{ margin: 0, fontSize: '0.95rem', fontWeight: n.isRead ? 400 : 600 }}>{n.message}</p>
          </button>
        ))
      )}
    </div>
  );
}