import { Link } from 'react-router-dom';
import { imageUrl } from '../utils/image';

export default function FoodCard({ food, onAdd, showRestaurant = false }) {
  const photo = imageUrl(food.image);
  const color = food.isVeg ? 'var(--basil)' : 'var(--chili)';

  return (
    <div
      className="ticket d-flex justify-content-between align-items-center gap-3"
      style={{ marginBottom: '1.25rem', padding: '1.25rem 1.5rem' }}
    >
      <div className="d-flex gap-3 align-items-center" style={{ minWidth: 0 }}>
        {photo && <img src={photo} alt={food.name} className="food-photo" />}

        <div style={{ minWidth: 0 }}>
          <div className="d-flex align-items-center gap-2 mb-1">
            <span
              title={food.isVeg ? 'Vegetarian' : 'Non-vegetarian'}
              style={{
                width: '14px', height: '14px', border: `2px solid ${color}`, borderRadius: '2px', flexShrink: 0,
                display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              }}
            >
              <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: color }} />
            </span>
            <h4 style={{ fontSize: '1.05rem', margin: 0 }}>{food.name}</h4>
          </div>

          {food.description && (
            <p style={{ color: '#8A8075', fontSize: '0.88rem', margin: '0.25rem 0' }}>{food.description}</p>
          )}

          <p style={{ fontWeight: 700, color: 'var(--chili)', margin: '0.4rem 0 0' }}>
            ₹{food.price}
            {showRestaurant && food.restaurantName && (
              <Link
                to={`/restaurants/${food.restaurantId}`}
                style={{ fontWeight: 500, color: '#8A8075', fontSize: '0.85rem', marginLeft: '0.75rem' }}
              >
                from {food.restaurantName}
              </Link>
            )}
          </p>
        </div>
      </div>

      <button
        className="btn-chili"
        style={{ padding: '0.55rem 1.2rem', fontSize: '0.9rem', flexShrink: 0 }}
        disabled={!food.isAvailable}
        onClick={() => onAdd(food)}
      >
        {food.isAvailable ? 'Add' : 'Unavailable'}
      </button>
    </div>
  );
}