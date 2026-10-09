import { useState } from 'react';
import { forgotPassword } from '../services/authService';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await forgotPassword(email);
      setMessage(res.data.message);
    } catch {
      setMessage('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '420px', paddingTop: '4rem', paddingBottom: '4rem' }}>
      <div className="ticket">
        <h2 style={{ fontSize: '1.6rem' }}>Reset your password</h2>
        <p style={{ color: '#8A8075', marginBottom: '1.5rem', fontSize: '0.95rem' }}>We'll email you a link to get back in.</p>

        {message && <div className="notice notice-success">{message}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label className="field-label">Registered email</label>
            <input type="email" className="field-input" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <button type="submit" className="btn-chili w-100" disabled={loading}>
            {loading ? 'Sending...' : 'Send reset link'}
          </button>
        </form>
      </div>
    </div>
  );
}