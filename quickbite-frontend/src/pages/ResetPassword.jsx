import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { resetPassword } from '../services/authService';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await resetPassword({ token, newPassword });
      setMessage(res.data.message);
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '420px', paddingTop: '4rem', paddingBottom: '4rem' }}>
      <div className="ticket">
        <h2 style={{ fontSize: '1.6rem' }}>Set a new password</h2>

        {message && <div className="notice notice-success">{message}</div>}
        {error && <div className="notice notice-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label className="field-label">New password</label>
            <input type="password" className="field-input" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={6} />
          </div>
          <button type="submit" className="btn-chili w-100" disabled={loading}>
            {loading ? 'Resetting...' : 'Reset password'}
          </button>
        </form>
      </div>
    </div>
  );
}