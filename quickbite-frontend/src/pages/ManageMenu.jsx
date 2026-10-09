import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  getFoodsByRestaurant, createFood, updateFood, deleteFood, setFoodAvailability, uploadFoodImage,
} from '../services/foodService';
import { getCategories } from '../services/categoryService';
import ImageUploadButton from '../components/ImageUploadButton';
import { imageUrl } from '../utils/image';
import { getErrorMessage } from '../utils/errors';

const emptyForm = { name: '', description: '', price: '', categoryId: '', isVeg: true, isAvailable: true };

export default function ManageMenu() {
  const { id } = useParams();
  const [foods, setFoods] = useState([]);
  const [categories, setCategories] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [uploadingId, setUploadingId] = useState(null);
  const [message, setMessage] = useState({ type: '', text: '' });

  const flash = (type, text) => {
    setMessage({ type, text });
    setTimeout(() => setMessage({ type: '', text: '' }), 4500);
  };

  const load = async () => {
    try {
      const [fRes, cRes] = await Promise.all([getFoodsByRestaurant(id), getCategories()]);
      setFoods(fRes.data);
      setCategories(cRes.data);
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not load the menu'));
    }
  };

  useEffect(() => { load(); }, [id]); // eslint-disable-line

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value });
  };

  const openCreate = () => {
    setEditingId(null);
    setForm({ ...emptyForm, categoryId: categories[0]?.id || '' });
    setShowForm(true);
  };

  const openEdit = (food) => {
    setEditingId(food.id);
    setForm({
      name: food.name,
      description: food.description || '',
      price: food.price,
      categoryId: food.categoryId,
      isVeg: !!food.isVeg,
      isAvailable: !!food.isAvailable,
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
    const payload = { ...form, price: Number(form.price), categoryId: Number(form.categoryId) };
    try {
      if (editingId) {
        await updateFood(editingId, payload);
        flash('success', 'Dish updated');
      } else {
        await createFood(id, payload);
        flash('success', 'Dish added. You can add a photo from the list below.');
      }
      closeForm();
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not save dish'));
    } finally {
      setSaving(false);
    }
  };

  const toggleAvailability = async (food) => {
    try {
      await setFoodAvailability(food.id, !food.isAvailable);
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not update availability'));
    }
  };

  const handleDelete = async (food) => {
    if (!window.confirm(`Delete "${food.name}"?`)) return;
    try {
      const res = await deleteFood(food.id);
      flash(res.data.deleted ? 'success' : 'error', res.data.message);
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not delete dish'));
    }
  };

  const handleImage = async (foodId, file) => {
    setUploadingId(foodId);
    try {
      await uploadFoodImage(foodId, file);
      flash('success', 'Photo updated');
      load();
    } catch (err) {
      flash('error', getErrorMessage(err, 'Could not upload photo'));
    } finally {
      setUploadingId(null);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '740px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <Link to="/owner" style={{ color: '#8A8075', fontSize: '0.88rem', textDecoration: 'none' }}>← Back to restaurants</Link>
      <div className="d-flex justify-content-between align-items-center mb-4 mt-2">
        <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Manage menu</h2>
        <button className="btn-chili" onClick={showForm ? closeForm : openCreate}>
          {showForm ? 'Cancel' : '+ Add dish'}
        </button>
      </div>

      {message.text && <div className={`notice notice-${message.type}`}>{message.text}</div>}

      {showForm && (
        <form onSubmit={handleSubmit} className="ticket mb-4">
          <h4 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>{editingId ? 'Edit dish' : 'New dish'}</h4>
          <div className="field">
            <label className="field-label">Name</label>
            <input name="name" className="field-input" value={form.name} onChange={handleChange} required />
          </div>
          <div className="field">
            <label className="field-label">Description</label>
            <input name="description" className="field-input" value={form.description} onChange={handleChange} />
          </div>
          <div className="row">
            <div className="col-6 field">
              <label className="field-label">Price (₹)</label>
              <input type="number" min="1" step="0.01" name="price" className="field-input" value={form.price} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">Category</label>
              <select name="categoryId" className="field-input" value={form.categoryId} onChange={handleChange} required>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          </div>
          <div className="d-flex gap-4 mb-3">
            <label className="filter-toggle">
              <input type="checkbox" name="isVeg" checked={form.isVeg} onChange={handleChange} /> Vegetarian
            </label>
            <label className="filter-toggle">
              <input type="checkbox" name="isAvailable" checked={form.isAvailable} onChange={handleChange} /> Available
            </label>
          </div>
          <button type="submit" className="btn-chili w-100" disabled={saving}>
            {saving ? 'Saving...' : editingId ? 'Save changes' : 'Add dish'}
          </button>
        </form>
      )}

      {foods.length === 0 && !showForm && (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>No dishes yet.</div>
      )}

      {foods.map((food) => {
        const photo = imageUrl(food.image);
        return (
          <div key={food.id} className="ticket mb-3" style={{ padding: '1.25rem 1.5rem' }}>
            <div className="d-flex gap-3 align-items-center">
              {photo && <img src={photo} alt={food.name} className="food-photo" />}
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ fontWeight: 600, margin: 0 }}>
                  {food.name} {food.isVeg && <span className="tag-veg" style={{ marginLeft: '0.4rem' }}>Veg</span>}
                </p>
                <p style={{ color: '#8A8075', fontSize: '0.85rem', margin: 0 }}>₹{food.price} · {food.categoryName}</p>
              </div>
              <button
                className="btn-ghost"
                style={{ padding: '0.35rem 0.8rem', fontSize: '0.8rem', color: food.isAvailable ? 'var(--basil)' : 'var(--chili)' }}
                onClick={() => toggleAvailability(food)}
              >
                {food.isAvailable ? 'Available' : 'Unavailable'}
              </button>
            </div>
            <div className="d-flex gap-2 flex-wrap mt-3">
              <button className="btn-ghost" style={{ padding: '0.4rem 0.9rem', fontSize: '0.82rem' }} onClick={() => openEdit(food)}>Edit</button>
              <ImageUploadButton
                label={photo ? 'Change photo' : 'Add photo'}
                disabled={uploadingId === food.id}
                onUpload={(file) => handleImage(food.id, file)}
                onError={(text) => flash('error', text)}
              />
              <button
                className="btn-ghost"
                style={{ padding: '0.4rem 0.9rem', fontSize: '0.82rem', color: 'var(--chili)' }}
                onClick={() => handleDelete(food)}
              >
                Delete
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
}