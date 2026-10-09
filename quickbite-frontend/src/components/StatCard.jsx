export default function StatCard({ label, value, color, hint }) {
  return (
    <div className="ticket" style={{ padding: '1.5rem', textAlign: 'center', height: '100%' }}>
      <p style={{ color: '#8A8075', fontSize: '0.82rem', fontWeight: 600, margin: '0 0 0.4rem' }}>{label}</p>
      <p style={{ fontFamily: 'var(--font-display)', fontSize: '1.9rem', fontWeight: 700, color: color || 'var(--ink)', margin: 0 }}>
        {value}
      </p>
      {hint && <p style={{ color: '#8A8075', fontSize: '0.75rem', margin: '0.35rem 0 0' }}>{hint}</p>}
    </div>
  );
}