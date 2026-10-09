import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getRestaurantById } from '../services/restaurantService';
import { getFoodsByRestaurant } from '../services/foodService';
import { getReviewsByRestaurant } from '../services/reviewService';
import FoodCard from '../components/FoodCard';
import ReviewList from '../components/ReviewList';
import useAddToCart from '../hooks/useAddToCart';
import { imageUrl } from '../utils/image';

export default function RestaurantDetails() {
  const { id } = useParams();
  const [restaurant, setRestaurant] = useState(null);
  const [foods, setFoods] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewCount, setReviewCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [vegOnly, setVegOnly] = useState(false);
  const [activeCategory, setActiveCategory] = useState('All');
  const { addToCart, toast } = useAddToCart();

  useEffect(() => {
    const load = async () => {
      try {
        const [rRes, fRes, revRes] = await Promise.all([
          getRestaurantById(id),
          getFoodsByRestaurant(id),
          getReviewsByRestaurant(id, { page: 0, size: 10, sort: 'createdAt,desc' }),
        ]);
        setRestaurant(rRes.data);
        setFoods(fRes.data);
        setReviews(revRes.data.content);
        setReviewCount(revRes.data.totalElements);
      } catch {
        setRestaurant(null);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const categoryNames = useMemo(
    () => ['All', ...Array.from(new Set(foods.map((f) => f.categoryName)))],
    [foods]
  );

  const grouped = useMemo(() => {
    const filtered = foods.filter((f) => {
      if (vegOnly && !f.isVeg) return false;
      if (activeCategory !== 'All' && f.categoryName !== activeCategory) return false;
      if (search && !f.name.toLowerCase().includes(search.toLowerCase())) return false;
      return true;
    });
    return filtered.reduce((acc, f) => {
      (acc[f.categoryName] = acc[f.categoryName] || []).push(f);
      return acc;
    }, {});
  }, [foods, vegOnly, activeCategory, search]);

  if (loading) return <div className="container" style={{ paddingTop: '3rem' }}>Loading...</div>;
  if (!restaurant) return <div className="container" style={{ paddingTop: '3rem' }}>Restaurant not found.</div>;

  const photo = imageUrl(restaurant.image);
  const bannerStyle = photo
    ? { background: `linear-gradient(rgba(27,20,16,0.6), rgba(27,20,16,0.88)), url(${photo}) center/cover` }
    : { background: 'var(--ink)' };
  const rating = Number(restaurant.rating) > 0 ? Number(restaurant.rating).toFixed(1) : 'New';

  return (
    <div className="container" style={{ paddingTop: '3rem', paddingBottom: '2rem' }}>
      <div style={{ ...bannerStyle, color: 'var(--paper)', borderRadius: '4px 4px 24px 24px', padding: '2.5rem 2rem', marginBottom: '2rem' }}>
        <div className="d-flex justify-content-between align-items-start flex-wrap gap-3">
          <div>
            <h1 style={{ color: 'var(--paper)', fontSize: '2.1rem' }}>{restaurant.name}</h1>
            <p style={{ color: 'rgba(253,246,236,0.8)', margin: 0 }}>
              {restaurant.cuisineType || 'Multi-cuisine'} · {restaurant.city}
            </p>
            {restaurant.description && (
              <p style={{ color: 'rgba(253,246,236,0.7)', fontSize: '0.9rem', margin: '0.6rem 0 0', maxWidth: '480px' }}>
                {restaurant.description}
              </p>
            )}
            <p style={{ color: 'rgba(253,246,236,0.7)', fontSize: '0.85rem', marginTop: '0.75rem', marginBottom: 0 }}>
              Delivery ₹{restaurant.deliveryFee} · Tax {restaurant.taxPercentage}%
              {restaurant.openingTime && ` · ${restaurant.openingTime.slice(0, 5)} to ${restaurant.closingTime.slice(0, 5)}`}
            </p>
          </div>
          <div className="text-end">
            <span className="rating-summary">★ {rating}</span>
            <p style={{ color: 'rgba(253,246,236,0.7)', fontSize: '0.8rem', margin: '0.4rem 0 0' }}>
              {reviewCount} {reviewCount === 1 ? 'review' : 'reviews'}
            </p>
            <p style={{ fontSize: '0.85rem', fontWeight: 700, margin: '0.4rem 0 0', color: restaurant.openNow ? '#8FD39A' : '#FF9C8A' }}>
              {restaurant.openNow ? 'Open now' : 'Closed right now'}
            </p>
          </div>
        </div>
      </div>

      {!restaurant.isActive && (
        <div className="notice notice-error">This restaurant is not accepting orders at the moment.</div>
      )}
      {toast.text && <div className={`notice notice-${toast.type}`}>{toast.text}</div>}

      <div className="row">
        <div className="col-lg-7">
          <div className="d-flex gap-2 mb-3 flex-wrap align-items-center">
            <input
              className="field-input"
              placeholder="Search this menu"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ maxWidth: '240px' }}
            />
            <label className="filter-toggle">
              <input type="checkbox" checked={vegOnly} onChange={(e) => setVegOnly(e.target.checked)} />
              <span className="tag-veg">Veg only</span>
            </label>
          </div>

          <div className="d-flex gap-2 flex-wrap mb-2">
            {categoryNames.map((c) => (
              <button key={c} className={`chip ${activeCategory === c ? 'active' : ''}`} onClick={() => setActiveCategory(c)}>
                {c}
              </button>
            ))}
          </div>

          {Object.keys(grouped).length === 0 ? (
            <p style={{ color: '#8A8075', marginTop: '2rem' }}>No items match your filters.</p>
          ) : (
            Object.entries(grouped).map(([category, items]) => (
              <section key={category}>
                <h3 className="menu-section-title">
                  {category}
                  <span>{items.length} {items.length === 1 ? 'item' : 'items'}</span>
                </h3>
                {items.map((food) => (
                  <FoodCard
                    key={food.id}
                    food={restaurant.isActive ? food : { ...food, isAvailable: false }}
                    onAdd={addToCart}
                  />
                ))}
              </section>
            ))
          )}
        </div>

        <div className="col-lg-5 mt-5 mt-lg-0">
          <h3 className="menu-section-title" style={{ marginTop: 0 }}>
            Reviews
            <span>{reviewCount} total</span>
          </h3>
          <ReviewList reviews={reviews} />
          <p style={{ color: '#8A8075', fontSize: '0.82rem' }}>
            Only customers with a delivered order can leave a review, so every rating here is from a real meal.
          </p>
        </div>
      </div>
    </div>
  );
}