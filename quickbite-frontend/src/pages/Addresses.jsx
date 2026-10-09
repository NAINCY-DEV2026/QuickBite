import { useEffect, useState } from 'react';
import { getAddresses, addAddress, deleteAddress } from '../services/addressService';

export default function Addresses() {
  const [addresses, setAddresses] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    houseFlat: '', street: '', area: '', city: '', state: '', pincode: '', landmark: '', addressType: 'HOME',
  });

  const load = async () => {
    const res = await getAddresses();
    setAddresses(res.data);
  };

  useEffect(() => { load(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    await addAddress(form);
    setForm({ houseFlat: '', street: '', area: '', city: '', state: '', pincode: '', landmark: '', addressType: 'HOME' });
    setShowForm(false);
    load();
  };

  const handleDelete = async (id) => {
    await deleteAddress(id);
    load();
  };

  return (
    <div className="container" style={{ maxWidth: '640px', paddingTop: '3rem', paddingBottom: '4rem' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 style={{ fontSize: '1.9rem', margin: 0 }}>Your addresses</h2>
        <button className="btn-chili" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : '+ Add address'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="ticket mb-4">
          <div className="row">
            <div className="col-6 field">
              <label className="field-label">House / Flat</label>
              <input name="houseFlat" className="field-input" value={form.houseFlat} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">Street</label>
              <input name="street" className="field-input" value={form.street} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Area</label>
              <input name="area" className="field-input" value={form.area} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">City</label>
              <input name="city" className="field-input" value={form.city} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">State</label>
              <input name="state" className="field-input" value={form.state} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Pincode</label>
              <input name="pincode" className="field-input" value={form.pincode} onChange={handleChange} required />
            </div>
            <div className="col-6 field">
              <label className="field-label">Landmark</label>
              <input name="landmark" className="field-input" value={form.landmark} onChange={handleChange} />
            </div>
            <div className="col-6 field">
              <label className="field-label">Type</label>
              <select name="addressType" className="field-input" value={form.addressType} onChange={handleChange}>
                <option value="HOME">Home</option>
                <option value="WORK">Work</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
          <button type="submit" className="btn-chili w-100 mt-2">Save address</button>
        </form>
      )}

      {addresses.length === 0 && !showForm && (
        <div className="ticket text-center" style={{ padding: '2.5rem', color: '#8A8075' }}>No saved addresses yet.</div>
      )}

      {addresses.map((a) => (
        <div key={a.id} className="ticket d-flex justify-content-between align-items-start mb-3">
          <div>
            <span className="tag-veg" style={{ color: 'var(--chili)', background: '#FDEAE6' }}>{a.addressType}</span>
            <p style={{ margin: '0.5rem 0 0' }}>{a.houseFlat}, {a.street}, {a.area}</p>
            <p style={{ color: '#8A8075', fontSize: '0.9rem', margin: 0 }}>{a.city}, {a.state} — {a.pincode}</p>
          </div>
          <button onClick={() => handleDelete(a.id)} style={{ background: 'none', border: 'none', color: 'var(--chili)', cursor: 'pointer' }}>Remove</button>
        </div>
      ))}
    </div>
  );
}