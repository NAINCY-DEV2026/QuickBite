import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register, registerOwner } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errors';

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', confirmPassword: '' });
  const [isOwner, setIsOwner] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === 'phone' ? value.replace(/\D/g, '') : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (form.password !== form.confirmPassword) {
      setError('Password and confirm password do not match');
      return;
    }
    setLoading(true);
    try {
      const apiCall = isOwner ? registerOwner : register;
      const res = await apiCall(form);
      login(res.data);
      navigate(isOwner ? '/owner' : '/');
    } catch (err) {
      setError(getErrorMessage(err, 'Registration failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '460px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div className="ticket">
        <h2 style={{ fontSize: '1.75rem' }}>Create your account</h2>
        <p style={{ color: '#8A8075', marginBottom: '1.75rem', fontSize: '0.95rem' }}>Order from your favorite places in minutes.</p>

        {error && <div className="notice notice-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label className="field-label">Full name</label>
            <input name="name" className="field-input" value={form.name} onChange={handleChange} maxLength={100} required />
          </div>
          <div className="field">
            <label className="field-label">Email</label>
            <input type="email" name="email" className="field-input" value={form.email} onChange={handleChange} maxLength={150} required />
          </div>
          <div className="field">
            <label className="field-label">Mobile number</label>
            <input name="phone" className="field-input" value={form.phone} onChange={handleChange}
                   inputMode="numeric" maxLength={10} placeholder="10-digit number" required />
          </div>
          <div className="field">
            <label className="field-label">Password</label>
            <input type="password" name="password" className="field-input" value={form.password} onChange={handleChange}
                   minLength={6} maxLength={72} required />
          </div>
          <div className="field">
            <label className="field-label">Confirm password</label>
            <input type="password" name="confirmPassword" className="field-input" value={form.confirmPassword} onChange={handleChange} required />
          </div>

          <label className="d-flex align-items-center gap-2" style={{ fontSize: '0.9rem', marginBottom: '1.5rem', cursor: 'pointer' }}>
            <input type="checkbox" checked={isOwner} onChange={(e) => setIsOwner(e.target.checked)} />
            I'm registering as a restaurant owner
          </label>

          <button type="submit" className="btn-chili w-100" disabled={loading}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <div className="text-center mt-3" style={{ fontSize: '0.9rem' }}>
          Already have an account? <Link to="/login" style={{ color: 'var(--chili)', fontWeight: 600 }}>Log in</Link>
        </div>
      </div>
    </div>
  );
}