export default function ReviewList({ reviews }) {
  if (reviews.length === 0) {
    return <p style={{ color: '#8A8075' }}>No reviews yet.</p>;
  }

  return (
    <div>
      {reviews.map((r) => (
        <div key={r.id} className="ticket mb-3" style={{ padding: '1.25rem 1.5rem' }}>
          <div className="d-flex justify-content-between align-items-center">
            <p style={{ fontWeight: 600, margin: 0 }}>{r.customerName}</p>
            <span style={{ color: 'var(--mango)', fontWeight: 700 }}>{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</span>
          </div>
          {r.comment && <p style={{ color: '#5C5248', fontSize: '0.9rem', margin: '0.5rem 0 0' }}>{r.comment}</p>}
        </div>
      ))}
    </div>
  );
}