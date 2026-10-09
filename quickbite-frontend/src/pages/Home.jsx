import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRestaurants } from '../services/restaurantService';
import RestaurantCard from '../components/RestaurantCard';

// category = exact category name used by the dish search
const tiles = [
  { name: 'Biryani', category: 'Biryani', color: '#E63E27' },
  { name: 'Pizza', category: 'Pizza', color: '#FFB400' },
  { name: 'Burgers', category: 'Burger', color: '#3E7C45' },
  { name: 'Desserts', category: 'Desserts', color: '#8B5E3C' },
];

export default function Home() {
  const { isAuthenticated } = useAuth();
  const [featured, setFeatured] = useState([]);

  useEffect(() => {
    getRestaurants({ page: 0, size: 4, sort: 'rating,desc' })
      .then((res) => setFeatured(res.data.content))
      .catch(() => setFeatured([]));
  }, []);

  return (
    <div className="container" style={{ paddingTop: '4rem' }}>
      <div className="row align-items-center">
        <div className="col-lg-6">
          <h1 style={{ fontSize: '3rem', lineHeight: 1.08, maxWidth: '480px' }}>
            Food you crave,<br />at your door<br />in minutes.
          </h1>
          <p style={{ maxWidth: '420px', color: '#5C5248', fontSize: '1.05rem', margin: '1.25rem 0 2rem' }}>
            Browse restaurants near you, build your order, and track it from the kitchen to your doorstep.
          </p>
          <div className="d-flex gap-3 flex-wrap">
            <Link to="/restaurants" className="btn-chili">Browse restaurants</Link>
            <Link to="/search" className="btn-ghost">Find a dish</Link>
            {!isAuthenticated && <Link to="/register" className="btn-ghost">Create account</Link>}
          </div>
        </div>

        <div className="col-lg-6 mt-5 mt-lg-0">
          <div className="d-flex flex-column gap-3" style={{ maxWidth: '360px', marginLeft: 'auto' }}>
            {tiles.map((tile, i) => (
              <Link
                to={`/search?category=${encodeURIComponent(tile.category)}`}
                key={tile.name}
                className="menu-tile text-decoration-none"
                style={{
                  background: tile.color,
                  marginLeft: i % 2 === 0 ? '0' : '2.5rem',
                  transform: `rotate(${i % 2 === 0 ? -1.5 : 1.5}deg)`,
                }}
              >
                {tile.name}
              </Link>
            ))}
          </div>
        </div>
      </div>

      {featured.length > 0 && (
        <div style={{ marginTop: '5rem' }}>
          <div className="d-flex justify-content-between align-items-end mb-4">
            <div>
              <h2 style={{ fontSize: '1.9rem', marginBottom: '0.2rem' }}>Top rated right now</h2>
              <p style={{ color: '#8A8075', margin: 0 }}>Loved by QuickBite customers.</p>
            </div>
            <Link to="/restaurants" style={{ color: 'var(--chili)', fontWeight: 600, textDecoration: 'none' }}>
              See all →
            </Link>
          </div>
          <div className="row">
            {featured.map((r, i) => (
              <div className="col-md-6" key={r.id}>
                <RestaurantCard restaurant={r} index={i} />
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}