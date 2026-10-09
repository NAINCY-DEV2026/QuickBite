import { useEffect, useState } from 'react';
import {
  getAdminCoupons, createCoupon, updateCoupon, setCouponActive, deleteCoupon,
} from '../../services/adminService';
import AdminNav from '../../components/AdminNav';
import Pagination from '../../components/Pagination';
import { pillStyle, label } from '../../utils/status';
import { formatDate, money } from '../../utils/format';
import { getErrorMessage } from '../../utils/errors';

const pad = (n) => String(n).padStart(2, '0');
const toLocalInput = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;

const blankForm = () => ({
  code: '',
  description: '',
  discountType: 'PERCENTAGE',
  discountValue: 10,
  minOrderAmount: 0,
  maxDiscount: '',
  startDate: toLocalInput(new Date()),
  expiryDate: toLocalInput(new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)),
  usageLimit: 100,
  isActive: true,
  notifyCustomers: false,
});

const describe = (c) => {
  const off = c.discountType === 'PERCENTAGE' ? `${Number(c.discountValue)}% off` : `${money(c.discountValue)} off`;
  const cap = c.discountType === 'PERCENTAGE' && c.maxDiscount ? ` up to ${money(c.maxDiscount)}` : '';
  const min = Number(c.minOrderAmount) > 0 ? ` on orders above ${money(c.minOrderAmount)}` : '';
  return `${off}${cap}${min}`;
};

export default function AdminCoupons() {
  const [coupons, setCoupons] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(blankForm());
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 5000);
  };
  const reload = () => setReloadKey((k) => k + 1);

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const res = await getAdminCoupons({ page, size: 8 });
        if (cancelled) return;
        setCoupons(res.data.content);
        setTotalPages(res.data.totalPages);
      } catch (err) {
        if (!cancelled) flash('error', getErrorMessage(err, 'Could not load coupons'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [page, reloadKey]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(blankForm());
    setShowForm(true);
  };

  const openEdit = (c) => {
    setEditingId(c.id);
    setForm({
      code: c.code,
      description: c.description || '',
      discountType: c.discountType,
      discountValue: c.discountValue,
      minOrderAmount: c.minOrderAmount,
      maxDiscount: c.maxDiscount ?? '',
      startDate: c.startDate.slice(0, 16),
      expiryDate: c.expiryDate.slice(0, 16),
      usageLimit: c.usageLimit,
      isActive: !!c.isActive,
      notifyCustomers: false,
    });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const payload = {
      code: form.code.trim().toUpperCase(),
      description: form.description.trim(),
      discountType: form.discountType,
      discountValue: Number(form.discountValue),
      minOrderAmount: Number(form.minOrderAmount || 0),
      maxDiscount: form.discountType === 'PERCENTAGE' && form.maxDiscount !== '' ? Number(form.maxDiscount) : null,
      startDate: form.startDate,
      expiryDate: form.expiryDate,
      usageLimit: Number(form.usageLimit),
      isActive: form.isActive,
      notifyCustomers: !editingId && form.notifyCustomers,
    };
    try {
      if (editingId) {
        await updateCoupon(editingId, payload);
        flash('success', 'Coupon updated');
      } else {
        await createCoupon(payload);
        flash('success', `Coupon ${payload.code} created`);
      }
      closeForm();
      setPage(0);
      reload();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not save coupon'));
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (c) => {
    try {
      await setCouponActive(c.id, !c.isActive);
      reload();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not update coupon'));
    }
  };

  const handleDelete = async (c) => {
    if (!window.confirm(`Delete coupon ${c.code}?`)) return;
    try {
      const res = await deleteCoupon(c.id);
      flash(res.data.deleted ? 'success' : 'error', res.data.message);
      reload();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not delete coupon'));
    }
  };

  return (
    <div className="container" style={{ maxWidth: '960px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Admin</h2>
        <button className="btn-chili" onClick={showForm ? closeForm : openCreate}>
          {showForm ? 'Cancel' : '+ New coupon'}
        </button>
      </div>
      <AdminNav />

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="ticket mb-4">
          <h4 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>{editingId ? 'Edit coupon' : 'New coupon'}</h4>
          <div className="row">
            <div className="col-md-4 field">
              <label className="field-label">Code</label>
              <input name="code" className="field-input" value={form.code} onChange={handleChange}
                     placeholder="QUICK20" maxLength={30} required style={{ textTransform: 'uppercase' }} />
            </div>
            <div className="col-md-8 field">
              <label className="field-label">Description</label>
              <input name="description" className="field-input" value={form.description} onChange={handleChange} maxLength={255} />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Discount type</label>
              <select name="discountType" className="field-input" value={form.discountType} onChange={handleChange}>
                <option value="PERCENTAGE">Percentage (%)</option>
                <option value="FIXED">Fixed amount (₹)</option>
              </select>
            </div>
            <div className="col-md-4 field">
              <label className="field-label">{form.discountType === 'PERCENTAGE' ? 'Percent off' : 'Amount off (₹)'}</label>
              <input type="number" min="0.01" step="0.01" max={form.discountType === 'PERCENTAGE' ? 100 : undefined}
                     name="discountValue" className="field-input" value={form.discountValue} onChange={handleChange} required />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Max discount (₹)</label>
              <input type="number" min="0" step="0.01" name="maxDiscount" className="field-input"
                     value={form.maxDiscount} onChange={handleChange}
                     disabled={form.discountType !== 'PERCENTAGE'} placeholder="No cap" />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Minimum order (₹)</label>
              <input type="number" min="0" step="0.01" name="minOrderAmount" className="field-input"
                     value={form.minOrderAmount} onChange={handleChange} required />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Starts</label>
              <input type="datetime-local" name="startDate" className="field-input" value={form.startDate} onChange={handleChange} required />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Expires</label>
              <input type="datetime-local" name="expiryDate" className="field-input" value={form.expiryDate} onChange={handleChange} required />
            </div>
            <div className="col-md-4 field">
              <label className="field-label">Total uses allowed</label>
              <input type="number" min="1" name="usageLimit" className="field-input" value={form.usageLimit} onChange={handleChange} required />
            </div>
          </div>

          <div className="d-flex gap-4 flex-wrap mb-3">
            <label className="filter-toggle">
              <input type="checkbox" name="isActive" checked={form.isActive} onChange={handleChange} /> Active
            </label>
            {!editingId && (
              <label className="filter-toggle">
                <input type="checkbox" name="notifyCustomers" checked={form.notifyCustomers} onChange={handleChange} />
                Notify all customers about this offer
              </label>
            )}
          </div>

          <button type="submit" className="btn-chili w-100" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Create coupon'}
          </button>
        </form>
      )}

      <div className="ticket" style={{ padding: 0, overflow: 'hidden' }}>
        {loading && <p style={{ padding: '2rem', margin: 0, color: '#8A8075' }}>Loading...</p>}
        {!loading && coupons.length === 0 && (
          <p style={{ padding: '2rem', margin: 0, textAlign: 'center', color: '#8A8075' }}>No coupons yet.</p>
        )}
        {coupons.map((c) => (
          <div key={c.id} className="data-row" style={{ alignItems: 'flex-start', flexWrap: 'wrap' }}>
            <div style={{ minWidth: 0, flex: 1 }}>
              <p style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: '1.1rem', margin: 0 }}>{c.code}</p>
              <p style={{ color: '#5C5248', fontSize: '0.88rem', margin: '0.15rem 0' }}>{describe(c)}</p>
              <p style={{ color: '#8A8075', fontSize: '0.8rem', margin: 0 }}>
                {formatDate(c.startDate)} to {formatDate(c.expiryDate)} · used {c.usedCount} of {c.usageLimit}
              </p>
            </div>
            <div className="d-flex align-items-center gap-2 flex-wrap">
              <span className="status-pill" style={pillStyle(c.status)}>{label(c.status)}</span>
              <button className="btn-ghost" style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem' }} onClick={() => openEdit(c)}>Edit</button>
              <button className="btn-ghost" style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem' }} onClick={() => handleToggle(c)}>
                {c.isActive ? 'Disable' : 'Enable'}
              </button>
              <button className="btn-ghost" style={{ padding: '0.3rem 0.8rem', fontSize: '0.8rem', color: 'var(--chili)' }} onClick={() => handleDelete(c)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}