import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="container" style={{ maxWidth: '480px', paddingTop: '4rem' }}>
      <div className="ticket text-center" style={{ padding: '3rem 2rem' }}>
        <p style={{ fontFamily: 'var(--font-display)', fontStyle: 'italic', fontSize: '4rem', margin: 0, color: 'var(--chili)' }}>404</p>
        <h2 style={{ fontSize: '1.4rem' }}>This page is off the menu</h2>
        <p style={{ color: '#8A8075', marginBottom: '1.5rem' }}>The page you're looking for doesn't exist or has moved.</p>
        <Link to="/" className="btn-chili">Back to home</Link>
      </div>
    </div>
  );
}