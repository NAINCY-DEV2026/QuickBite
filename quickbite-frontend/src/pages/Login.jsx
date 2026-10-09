import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { login as loginApi } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await loginApi({ email, password });
      login(res.data);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '420px', paddingTop: '4rem', paddingBottom: '4rem' }}>
      <div className="ticket">
        <h2 style={{ fontSize: '1.75rem' }}>Welcome back</h2>
        <p style={{ color: '#8A8075', marginBottom: '1.75rem', fontSize: '0.95rem' }}>Log in to continue your order.</p>

        {error && <div className="notice notice-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label className="field-label">Email</label>
            <input type="email" className="field-input" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="field">
            <label className="field-label">Password</label>
            <input type="password" className="field-input" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button type="submit" className="btn-chili w-100" disabled={loading}>
            {loading ? 'Logging in...' : 'Log in'}
          </button>
        </form>

        <div className="d-flex justify-content-between mt-3" style={{ fontSize: '0.9rem' }}>
          <Link to="/forgot-password" style={{ color: '#8A8075' }}>Forgot password?</Link>
          <Link to="/register" style={{ color: 'var(--chili)', fontWeight: 600 }}>Create account</Link>
        </div>
      </div>
    </div>
  );
}