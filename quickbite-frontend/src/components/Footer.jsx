import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="qb-footer">
      <div className="container d-flex justify-content-between flex-wrap gap-3">
        <div>
          <p style={{ fontFamily: 'var(--font-display)', fontStyle: 'italic', fontSize: '1.4rem', color: 'var(--paper)', margin: 0 }}>
            QuickBite
          </p>
          <p style={{ margin: '0.25rem 0 0' }}>Food you crave, at your door in minutes.</p>
        </div>
        <div className="d-flex gap-4 align-items-start">
          <Link to="/restaurants">Restaurants</Link>
          <Link to="/register">Sign up</Link>
          <Link to="/login">Login</Link>
        </div>
      </div>
      <div className="container" style={{ marginTop: '1.5rem', fontSize: '0.8rem', opacity: 0.6 }}>
        © {new Date().getFullYear()} QuickBite. Built with Spring Boot and React.
      </div>
    </footer>
  );
}