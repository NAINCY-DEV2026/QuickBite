import { useEffect, useState } from 'react';
import { getAdminCategories, createCategory, deleteCategory } from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import { getErrorMessage } from '../../utils/errors';

export default function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4500);
  };

  const load = async () => {
    try {
      const res = await getAdminCategories();
      setCategories(res.data);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not load categories'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setSaving(true);
    try {
      await createCategory(name.trim());
      setName('');
      flash('success', 'Category added');
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not add category'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (c) => {
    if (!window.confirm(`Delete category "${c.name}"?`)) return;
    try {
      await deleteCategory(c.id);
      flash('success', 'Category deleted');
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not delete category'));
    }
  };

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.9rem', marginBottom: '1.25rem' }}>Admin</h2>
      <AdminNav />

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      <form onSubmit={handleAdd} className="d-flex gap-2 mb-4">
        <input
          className="field-input"
          style={{ maxWidth: '320px' }}
          placeholder="New category name"
          value={name}
          maxLength={100}
          onChange={(e) => setName(e.target.value)}
        />
        <button type="submit" className="btn-chili" disabled={saving} style={{ padding: '0.6rem 1.3rem' }}>
          {saving ? 'Adding...' : 'Add category'}
        </button>
      </form>

      <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
        {loading && <p style={{ padding: '2rem', margin: 0, color: '#8A8075' }}>Loading...</p>}
        {!loading && categories.length === 0 && (
          <p style={{ padding: '2rem', margin: 0, textAlign: 'center', color: '#8A8075' }}>No categories yet.</p>
        )}
        {categories.map((c) => (
          <div key={c.id} className="data-row">
            <div>
              <p style={{ fontWeight: 600, margin: 0 }}>{c.name}</p>
              <p style={{ color: '#8A8075', fontSize: '0.83rem', margin: 0 }}>
                {c.foodCount} {c.foodCount === 1 ? 'dish' : 'dishes'}
              </p>
            </div>
            <button
              className="btn-ghost"
              style={{ padding: '0.35rem 0.9rem', fontSize: '0.82rem', color: 'var(--chili)' }}
              onClick={() => handleDelete(c)}
            >
              Delete
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}