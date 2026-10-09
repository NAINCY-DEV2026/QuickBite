import { useEffect, useState } from 'react';
import { getRestaurants } from '../services/restaurantService';
import RestaurantCard from '../components/RestaurantCard';
import Pagination from '../components/Pagination';
import { getErrorMessage } from '../utils/errors';

const cuisineChips = ['All', 'Indian', 'Biryani', 'Chinese', 'Italian', 'Fast Food', 'South Indian', 'Healthy', 'Desserts'];
const defaults = { name: '', city: '', cuisine: '', minRating: '', openNow: false, sort: 'rating,desc' };

export default function Restaurants() {
  const [restaurants, setRestaurants] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState(defaults);
  const [draft, setDraft] = useState({ name: '', city: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const params = { page, size: 6, sort: filters.sort };
        if (filters.name) params.name = filters.name;
        if (filters.city) params.city = filters.city;
        if (filters.cuisine) params.cuisine = filters.cuisine;
        if (filters.minRating) params.minRating = filters.minRating;
        if (filters.openNow) params.openNow = true;

        const res = await getRestaurants(params);
        if (cancelled) return;
        setRestaurants(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      } catch (err) {
        if (cancelled) return;
        setRestaurants([]);
        setError(getErrorMessage(err, 'Could not load restaurants'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, [filters, page]);

  const applyFilters = (patch) => {
    setPage(0);
    setFilters((f) => ({ ...f, ...patch }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    applyFilters({ name: draft.name.trim(), city: draft.city.trim() });
  };

  const clearAll = () => {
    setDraft({ name: '', city: '' });
    setPage(0);
    setFilters(defaults);
  };

  const hasActiveFilters = JSON.stringify(filters) !== JSON.stringify(defaults);

  return (
    <div className="container" style={{ paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '2rem' }}>Restaurants near you</h2>
      <p style={{ color: '#8A8075', marginBottom: '1.5rem' }}>
        {loading ? 'Finding places...' : `${totalElements} ${totalElements === 1 ? 'place' : 'places'} to order from`}
      </p>

      <form onSubmit={handleSearch} className="filter-bar">
        <div className="row g-2 align-items-center">
          <div className="col-md-4">
            <input
              className="field-input"
              placeholder="Search by restaurant name"
              value={draft.name}
              onChange={(e) => setDraft({ ...draft, name: e.target.value })}
            />
          </div>
          <div className="col-md-3">
            <input
              className="field-input"
              placeholder="City"
              value={draft.city}
              onChange={(e) => setDraft({ ...draft, city: e.target.value })}
            />
          </div>
          <div className="col-md-2">
            <select
              className="field-input"
              value={filters.minRating}
              onChange={(e) => applyFilters({ minRating: e.target.value })}
            >
              <option value="">Any rating</option>
              <option value="3">3.0 and above</option>
              <option value="4">4.0 and above</option>
              <option value="4.5">4.5 and above</option>
            </select>
          </div>
          <div className="col-md-3 d-flex gap-2">
            <button type="submit" className="btn-chili flex-grow-1" style={{ padding: '0.65rem 1rem' }}>Search</button>
            {hasActiveFilters && (
              <button type="button" className="btn-ghost" style={{ padding: '0.65rem 1rem' }} onClick={clearAll}>Clear</button>
            )}
          </div>
        </div>
      </form>

      <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
        <div className="d-flex gap-2 flex-wrap">
          {cuisineChips.map((c) => {
            const value = c === 'All' ? '' : c;
            return (
              <button
                key={c}
                type="button"
                className={`chip ${filters.cuisine === value ? 'active' : ''}`}
                onClick={() => applyFilters({ cuisine: value })}
              >
                {c}
              </button>
            );
          })}
        </div>

        <div className="d-flex align-items-center gap-3">
          <label className="filter-toggle">
            <input type="checkbox" checked={filters.openNow} onChange={(e) => applyFilters({ openNow: e.target.checked })} />
            Open now
          </label>
          <select className="sort-select" value={filters.sort} onChange={(e) => applyFilters({ sort: e.target.value })}>
            <option value="rating,desc">Top rated</option>
            <option value="name,asc">Name A–Z</option>
            <option value="createdAt,desc">Newest</option>
            <option value="deliveryFee,asc">Lowest delivery fee</option>
          </select>
        </div>
      </div>

      {error && <div className="notice notice-error">{error}</div>}

      {loading ? (
        <p style={{ color: '#8A8075' }}>Loading restaurants...</p>
      ) : restaurants.length === 0 && !error ? (
        <div className="ticket text-center" style={{ padding: '3rem' }}>
          <p style={{ margin: '0 0 1rem', color: '#8A8075' }}>No restaurants match these filters.</p>
          {hasActiveFilters && <button className="btn-ghost" onClick={clearAll}>Clear filters</button>}
        </div>
      ) : (
        <div className="row">
          {restaurants.map((r, i) => (
            <div className="col-md-6" key={r.id}>
              <RestaurantCard restaurant={r} index={i} />
            </div>
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}