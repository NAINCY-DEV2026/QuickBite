import { Link, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import NotificationBell from './NotificationBell';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { itemCount, refreshCart } = useCart();
  const navigate = useNavigate();

  useEffect(() => { refreshCart(); }, [isAuthenticated]); // eslint-disable-line

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="qb-navbar">
      <div className="container d-flex justify-content-between align-items-center">
        <Link to="/" className="qb-brand">QuickBite</Link>

        <div className="d-flex align-items-center gap-4">
          <Link to="/restaurants" className="qb-nav-link">Restaurants</Link>
          <Link to="/search" className="qb-nav-link">Dishes</Link>

          {isAuthenticated && user.role === 'RESTAURANT_OWNER' && (
            <Link to="/owner" className="qb-nav-link">My Restaurant</Link>
          )}

          {isAuthenticated && user.role === 'ADMIN' && (
            <Link to="/admin" className="qb-nav-link">Admin</Link>
          )}

          {isAuthenticated && user.role === 'CUSTOMER' && (
            <>
              <Link to="/orders" className="qb-nav-link">Orders</Link>
              <Link to="/cart" className="qb-nav-link" style={{ position: 'relative' }}>
                Cart
                {itemCount > 0 && (
                  <span style={{
                    position: 'absolute', top: '-10px', right: '-14px',
                    background: 'var(--mango)', color: 'var(--ink)',
                    borderRadius: '50%', width: '18px', height: '18px',
                    fontSize: '0.7rem', fontWeight: 700,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    {itemCount}
                  </span>
                )}
              </Link>
              <Link to="/addresses" className="qb-nav-link">Addresses</Link>
            </>
          )}

          {isAuthenticated && (
            <>
              <NotificationBell />
              <Link to="/profile" className="qb-nav-link">Hi, {user.name.split(' ')[0]}</Link>
              <button onClick={handleLogout} className="qb-logout">Logout</button>
            </>
          )}

          {!isAuthenticated && (
            <>
              <Link to="/login" className="qb-nav-link">Login</Link>
              <Link to="/register" className="btn-chili" style={{ padding: '0.5rem 1.25rem', fontSize: '0.92rem' }}>
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}