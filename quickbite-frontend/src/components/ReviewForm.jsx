import { useState } from 'react';
import { addReview } from '../services/reviewService';

export default function ReviewForm({ orderId, onSubmitted }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await addReview({ orderId, rating, comment });
      onSubmitted();
    } catch (err) {
      setError(err.response?.data?.message || 'Could not submit review');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="ticket mt-3">
      <h4 style={{ fontSize: '1rem' }}>Rate your order</h4>
      {error && <div className="notice notice-error">{error}</div>}
      <div className="d-flex gap-2 mb-3">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            onClick={() => setRating(n)}
            style={{
              width: '38px', height: '38px', borderRadius: '8px',
              border: `1.5px solid ${n <= rating ? 'var(--mango)' : 'var(--line)'}`,
              background: n <= rating ? 'var(--mango)' : 'transparent',
              color: n <= rating ? '#fff' : 'var(--ink)',
              fontWeight: 700, cursor: 'pointer',
            }}
          >
            ★
          </button>
        ))}
      </div>
      <div className="field">
        <label className="field-label">Comment (optional)</label>
        <textarea className="field-input" rows={3} value={comment} onChange={(e) => setComment(e.target.value)} />
      </div>
      <button type="submit" className="btn-chili w-100" disabled={loading}>
        {loading ? 'Submitting...' : 'Submit review'}
      </button>
    </form>
  );
}