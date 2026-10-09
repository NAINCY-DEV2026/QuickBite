import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getMyRestaurants, createRestaurant, updateRestaurant, uploadRestaurantImage,
} from '../services/restaurantService';
import ImageUploadButton from '../components/ImageUploadButton';
import { imageUrl } from '../utils/image';
import { getErrorMessage } from '../utils/errors';

const emptyForm = {
  name: '', description: '', address: '', city: '', phone: '', email: '',
  cuisineType: '', openingTime: '09:00', closingTime: '22:00', deliveryFee: 40, taxPercentage: 5,
};

export default function OwnerDashboard() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [uploadingId, setUploadingId] = useState(null);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4000);
  };

  const load = async () => {
    try {
      const res = await getMyRestaurants();
      setRestaurants(res.data);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not load your restaurants'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowForm(true);
  };

  const openEdit = (r) => {
    setEditingId(r.id);
    setForm({
      name: r.name || '',
      description: r.description || '',
      address: r.address || '',
      city: r.city || '',
      phone: r.phone || '',
      email: r.email || '',
      cuisineType: r.cuisineType || '',
      openingTime: (r.openingTime || '09:00').slice(0, 5),
      closingTime: (r.closingTime || '22:00').slice(0, 5),
      deliveryFee: r.deliveryFee ?? 0,
      taxPercentage: r.taxPercentage ?? 0,
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
      ...form,
      deliveryFee: Number(form.deliveryFee),
      taxPercentage: Number(form.taxPercentage),
    };
    try {
      if (editingId) {
        await updateRestaurant(editingId, payload);
        flash('success', 'Restaurant updated');
      } else {
        await createRestaurant(payload);
        flash('success', 'Restaurant created. Add a photo and your menu next.');
      }
      closeForm();
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not save restaurant'));
    } finally {
      setSaving(false);
    }
  };

  const handleImage = async (restaurantId, file) => {
    setUploadingId(restaurantId);
    try {
      await uploadRestaurantImage(restaurantId, file);
      flash('success', 'Photo updated');
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not upload photo'));
    } finally {
      setUploadingId(null);
    }
  };

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;

  return (
    <div className="container" style={{ maxWidth: '760px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Your restaurants</h2>
        <button className="btn-chili" onClick={showForm ? closeForm : openCreate}>
          {showForm ? 'Cancel' : '+ New restaurant'}
        </button>
      </div>

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="ticket mb-4">
          <h4 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>
            {editingId ? 'Edit restaurant' : 'New restaurant'}
          </h4>
          <div className="row">
            <div className="col-12 field">
              <label className="field-label">Restaurant name</label>
              <input name="name" className="field-input" value={form.name} onChange={handleChange} required />
            </div>
            <div className="col-12 field">
              <label className="field-label">Description</label>
              <input name="description" className="field-input" value={form.description} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">City</label>
              <input name="city" className="field-input" value={form.city} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">Cuisine type</label>
              <input name="cuisineType" className="field-input" placeholder="e.g. Biryani, Indian" value={form.cuisineType} onChange={handleChange} />
            </div>
            <div className="col-12 field">
              <label className="field-label">Address</label>
              <input name="address" className="field-input" value={form.address} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Phone</label>
              <input name="phone" className="field-input" value={form.phone} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Email</label>
              <input type="email" name="email" className="field-input" value={form.email} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Opening time</label>
              <input type="time" name="openingTime" className="field-input" value={form.openingTime} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Closing time</label>
              <input type="time" name="closingTime" className="field-input" value={form.closingTime} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Delivery fee (₹)</label>
              <input type="number" min="0" name="deliveryFee" className="field-input" value={form.deliveryFee} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">Tax (%)</label>
              <input type="number" min="0" max="100" name="taxPercentage" className="field-input" value={form.taxPercentage} onChange={handleChange} required />
            </div>
          </div>
          <button type="submit" className="btn-chili w-100 mt-2" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Create restaurant'}
          </button>
        </form>
      )}

      {restaurants.length === 0 && !showForm && (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>
          You haven't created a restaurant yet.
        </div>
      )}

      {restaurants.map((r) => {
        const photo = imageUrl(r.image);
        return (
          <div key={r.id} className="ticket mb-3">
            <div className="d-flex gap-3 align-items-center">
              {photo ? (
                <img src={photo} alt={r.name} className="card-visual" style={{ objectFit: 'cover' }} />
              ) : (
                <div className="card-visual" style={{ background: '#E63E271A', color: 'var(--chili)' }}>🍽️</div>
              )}
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontWeight: 600, margin: 0, fontSize: '1.05rem' }}>{r.name}</p>
                <p style={{ color: '#8A8075', fontSize: '0.88rem', margin: 0 }}>
                  {r.city} · {r.isActive ? (r.openNow ? 'Open now' : 'Closed right now') : 'Blocked by admin'}
                </p>
              </div>
            </div>
            <div className="d-flex gap-2 flex-wrap mt-3">
              <Link to={`/owner/restaurants/${r.id}/menu`} className="btn-chili" style={{ padding: '0.4rem 1rem', fontSize: '0.85rem' }}>Menu</Link>
              <Link to={`/owner/restaurants/${r.id}/orders`} className="btn-ghost" style={{ padding: '0.4rem 1rem', fontSize: '0.85rem' }}>Orders</Link>
              <button className="btn-ghost" style={{ padding: '0.4rem 1rem', fontSize: '0.85rem' }} onClick={() => openEdit(r)}>Edit</button>
              <ImageUploadButton
                label={photo ? 'Change photo' : 'Add photo'}
                disabled={uploadingId === r.id}
                onUpload={(file) => handleImage(r.id, file)}
                onError={(text) => flash('error', text)}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
}