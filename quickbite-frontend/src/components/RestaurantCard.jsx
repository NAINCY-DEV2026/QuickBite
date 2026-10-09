import { Link } from 'react-router-dom';
import { imageUrl } from '../utils/image';

const palette = ['#E63E27', '#FFB400', '#3E7C45', '#8B5E3C'];
const emojis = ['🍛', '🍕', '🍔', '🍰', '🍜', '🍟'];

export default function RestaurantCard({ restaurant, index = 0 }) {
  const accent = palette[index % palette.length];
  const emoji = emojis[index % emojis.length];
  const photo = imageUrl(restaurant.image);
  const rating = Number(restaurant.rating) > 0 ? Number(restaurant.rating).toFixed(1) : 'New';
  const isOpen = restaurant.openNow;

  return (
    <Link to={`/restaurants/${restaurant.id}`} className="ticket d-block text-decoration-none" style={{ marginBottom: '1.5rem' }}>
      <div className="d-flex gap-3">
        {photo ? (
          <img src={photo} alt={restaurant.name} className="card-visual" style={{ objectFit: 'cover' }} />
        ) : (
          <div className="card-visual" style={{ background: `${accent}1A`, color: accent }}>{emoji}</div>
        )}

        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="d-flex justify-content-between align-items-start gap-2">
            <div style={{ minWidth: 0 }}>
              <h3 style={{ fontSize: '1.25rem', marginBottom: '0.3rem', color: 'var(--ink)' }}>{restaurant.name}</h3>
              <span className="chip" style={{ cursor: 'default', fontSize: '0.75rem', padding: '0.15rem 0.6rem' }}>
                {restaurant.cuisineType || 'Multi-cuisine'}
              </span>
            </div>
            <div
              style={{
                background: accent,
                color: accent === '#FFB400' ? 'var(--ink)' : '#fff',
                borderRadius: '8px',
                padding: '0.3rem 0.6rem',
                fontWeight: 700,
                fontSize: '0.85rem',
                whiteSpace: 'nowrap',
              }}
            >
              ★ {rating}
            </div>
          </div>

          <div className="d-flex flex-wrap gap-2 mt-3" style={{ fontSize: '0.85rem', color: '#8A8075' }}>
            <span>📍 {restaurant.city}</span>
            <span>·</span>
            <span>Delivery ₹{restaurant.deliveryFee}</span>
            <span>·</span>
            <span style={{ color: isOpen ? 'var(--basil)' : 'var(--chili)', fontWeight: 600 }}>
              {isOpen ? 'Open now' : 'Closed'}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}