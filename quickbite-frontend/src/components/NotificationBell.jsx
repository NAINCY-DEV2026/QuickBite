import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '../services/notificationService';
import { timeAgo, typeColor } from '../utils/notifications';

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState([]);
  const [count, setCount] = useState(0);
  const wrapRef = useRef(null);
  const navigate = useNavigate();

  const loadCount = useCallback(async () => {
    try {
      const res = await getUnreadCount();
      setCount(res.data.count);
    } catch {
      /* ignore: the bell should never break the navbar */
    }
  }, []);

  // Poll for new notifications every 30 seconds
  useEffect(() => {
    loadCount();
    const timer = setInterval(loadCount, 30000);
    return () => clearInterval(timer);
  }, [loadCount]);

  // Close the dropdown when clicking outside of it
  useEffect(() => {
    const onClickOutside = (e) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  const toggle = async () => {
    const next = !open;
    setOpen(next);
    if (next) {
      try {
        const res = await getNotifications();
        setItems(res.data.slice(0, 6));
        loadCount();
      } catch {
        setItems([]);
      }
    }
  };

  const handleRead = async (n) => {
    if (n.isRead) return;
    try {
      await markAsRead(n.id);
      setItems((prev) => prev.map((i) => (i.id === n.id ? { ...i, isRead: true } : i)));
      setCount((c) => Math.max(0, c - 1));
    } catch {
      /* ignore */
    }
  };

  const handleReadAll = async () => {
    try {
      await markAllAsRead();
      setItems((prev) => prev.map((i) => ({ ...i, isRead: true })));
      setCount(0);
    } catch {
      /* ignore */
    }
  };

  const goToAll = () => {
    setOpen(false);
    navigate('/notifications');
  };

  return (
    <div className="qb-bell-wrap" ref={wrapRef}>
      <button className="qb-bell" onClick={toggle} aria-label="Notifications">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.7 21a2 2 0 0 1-3.4 0" />
        </svg>
        {count > 0 && <span className="qb-bell-badge">{count > 9 ? '9+' : count}</span>}
      </button>

      {open && (
        <div className="qb-dropdown">
          <div className="qb-dropdown-header">
            <span>Notifications</span>
            {count > 0 && <button className="qb-link-btn" onClick={handleReadAll}>Mark all read</button>}
          </div>

          {items.length === 0 ? (
            <p style={{ padding: '1.5rem', margin: 0, color: '#8A8075', fontSize: '0.9rem', textAlign: 'center' }}>
              You're all caught up.
            </p>
          ) : (
            items.map((n) => (
              <button key={n.id} className={`notif-item ${n.isRead ? '' : 'unread'}`} onClick={() => handleRead(n)}>
                <span className="notif-dot" style={{ background: n.isRead ? 'transparent' : typeColor[n.type] || 'var(--chili)' }} />
                <span>
                  <p className="notif-message">{n.message}</p>
                  <p className="notif-time">{timeAgo(n.createdAt)}</p>
                </span>
              </button>
            ))
          )}

          <div style={{ padding: '0.7rem', textAlign: 'center' }}>
            <button className="qb-link-btn" onClick={goToAll}>View all notifications</button>
          </div>
        </div>
      )}
    </div>
  );
}