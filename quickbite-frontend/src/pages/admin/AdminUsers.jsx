import { useEffect, useState } from 'react';
import { getUsers, blockUser, unblockUser } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import Pagination from '../../components/Pagination';
import { pillStyle, label } from '../../utils/status';
import { formatDate } from '../../utils/format';
import { getErrorMessage } from '../../utils/errors';

const roleChips = [
  { label: 'All', value: '' },
  { label: 'Customers', value: 'CUSTOMER' },
  { label: 'Owners', value: 'RESTAURANT_OWNER' },
  { label: 'Admins', value: 'ADMIN' },
];

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [total, setTotal] = useState(0);
  const [role, setRole] = useState('');
  const [draft, setDraft] = useState('');
  const [q, setQ] = useState('');
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
        const params = { page, size: 10 };
        if (q) params.q = q;
        if (role) params.role = role;
        const res = await getUsers(params);
        if (cancelled) return;
        setUsers(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotal(res.data.totalElements);
      } catch (err) {
        if (!cancelled) flash('error', getErrorMessage(err, 'Could not load users'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [page, q, role, reloadKey]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    setQ(draft.trim());
  };

  const chooseRole = (value) => {
    setPage(0);
    setRole(value);
  };

  const handleToggle = async (user) => {
    const blocking = user.accountStatus === 'ACTIVE';
    if (!window.confirm(`${blocking ? 'Block' : 'Unblock'} ${user.name}?`)) return;
    try {
      if (blocking) await blockUser(user.id);
      else await unblockUser(user.id);
      flash('success', `${user.name} ${blocking ? 'blocked' : 'unblocked'}`);
      setReloadKey((k) => k + 1);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Action failed'));
    }
  };

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      <form onSubmit={handleSearch} className="d-flex gap-2 mb-3 flex-wrap">
        <input
          className="field-input"
          style={{ maxWidth: '320px' }}
          placeholder="Search by name or email"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
        />
        <button type="submit" className="btn-chili" style={{ padding: '0.6rem 1.2rem' }}>Search</button>
        {q && (
          <button type="button" className="btn-ghost" style={{ padding: '0.6rem 1.2rem' }} onClick={() => { setDraft(''); setQ(''); setPage(0); }}>
            Clear
          </button>
        )}
      </form>

      <div className="d-flex gap-2 flex-wrap mb-3">
        {roleChips.map((c) => (
          <button key={c.label} className={`chip ${role === c.value ? 'active' : ''}`} onClick={() => chooseRole(c.value)}>
            {c.label}
          </button>
        ))}
      </div>

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      <p style={{ color: '#8A8075', fontSize: '0.88rem' }}>{loading ? 'Loading...' : `${total} users`}</p>

      <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
        {!loading && users.length === 0 && (
          <p style={{ padding: '2rem', margin: 0, textAlign: 'center', color: '#8A8075' }}>No users found.</p>
        )}
        {users.map((u) => (
          <div key={u.id} className="data-row">
            <div style={{ minWidth: 0 }}>
              <p style={{ fontWeight: 600, margin: 0 }}>{u.name}</p>
              <p style={{ color: '#8A8075', fontSize: '0.83rem', margin: 0 }}>
                {u.email} · {label(u.role)} · joined {formatDate(u.createdAt)}
              </p>
            </div>
            <div className="d-flex align-items-center gap-3">
              <span className="status-pill" style={pillStyle(u.accountStatus)}>{label(u.accountStatus)}</span>
              {u.role !== 'ADMIN' && u.accountStatus !== 'DEACTIVATED' && (
                <button className="btn-ghost" style={{ padding: '0.35rem 0.9rem', fontSize: '0.82rem' }} onClick={() => handleToggle(u)}>
                  {u.accountStatus === 'BLOCKED' ? 'Unblock' : 'Block'}
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}