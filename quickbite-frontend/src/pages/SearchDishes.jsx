import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { searchFoods } from '../services/foodService';
import { getCategories } from '../services/categoryService';
import FoodCard from '../components/FoodCard';
import Pagination from '../components/Pagination';
import useAddToCart from '../hooks/useAddToCart';
import { getErrorMessage } from '../utils/errors';

const sortOptions = [
  { value: 'name,asc', label: 'Name A–Z' },
  { value: 'price,asc', label: 'Price: low to high' },
  { value: 'price,desc', label: 'Price: high to low' },
  { value: 'restaurant.rating,desc', label: 'Top rated restaurants' },
];

export default function SearchDishes() {
  const [searchParams] = useSearchParams();
  const urlCategory = searchParams.get('category') || '';

  const [categories, setCategories] = useState([]);
  const [draft, setDraft] = useState({ q: '', minPrice: '', maxPrice: '' });
  const [applied, setApplied] = useState({ q: '', minPrice: '', maxPrice: '', category: urlCategory, vegOnly: false, sort: 'name,asc' });
  const [foods, setFoods] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { addToCart, toast } = useAddToCart();

  useEffect(() => {
    getCategories().then((res) => setCategories(res.data)).catch(() => setCategories([]));
  }, []);

  // Home page tiles link here with ?category=Pizza
  useEffect(() => {
    setPage(0);
    setApplied((a) => (a.category === urlCategory ? a : { ...a, category: urlCategory }));
  }, [urlCategory]);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const params = { page, size: 8, sort: applied.sort };
        if (applied.q) params.q = applied.q;
        if (applied.category) params.category = applied.category;
        if (applied.minPrice) params.minPrice = applied.minPrice;
        if (applied.maxPrice) params.maxPrice = applied.maxPrice;
        if (applied.vegOnly) params.vegetarian = true;

        const res = await searchFoods(params);
        if (cancelled) return;
        setFoods(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      } catch (err) {
        if (cancelled) return;
        setFoods([]);
        setError(getErrorMessage(err, 'Could not search dishes'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => { cancelled = true; };
  }, [applied, page]);

  const apply = (patch) => {
    setPage(0);
    setApplied((a) => ({ ...a, ...patch }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    apply({ q: draft.q.trim(), minPrice: draft.minPrice, maxPrice: draft.maxPrice });
  };

  const clearAll = () => {
    setDraft({ q: '', minPrice: '', maxPrice: '' });
    setPage(0);
    setApplied({ q: '', minPrice: '', maxPrice: '', category: '', vegOnly: false, sort: 'name,asc' });
  };

  return (
    <div className="container" style={{ maxWidth: '860px', paddingTop: '3rem', paddingBottom: '2rem' }}>
      <h2 style={{ fontSize: '2rem' }}>Find a dish</h2>
      <p style={{ color: '#8A8075', marginBottom: '1.5rem' }}>
        {loading ? 'Searching...' : `${totalElements} ${totalElements === 1 ? 'dish' : 'dishes'} available`}
      </p>

      <form onSubmit={handleSubmit} className="filter-bar">
        <div className="row g-2 align-items-center">
          <div className="col-md-5">
            <input
              className="field-input"
              placeholder="Search dishes (biryani, paneer, shake...)"
              value={draft.q}
              onChange={(e) => setDraft({ ...draft, q: e.target.value })}
            />
          </div>
          <div className="col-md-3">
            <select className="field-input" value={applied.category} onChange={(e) => apply({ category: e.target.value })}>
              <option value="">All categories</option>
              {categories.map((c) => <option key={c.id} value={c.name}>{c.name}</option>)}
            </select>
          </div>
          <div className="col-6 col-md-2">
            <input
              type="number" min="0" className="field-input" placeholder="Min ₹"
              value={draft.minPrice} onChange={(e) => setDraft({ ...draft, minPrice: e.target.value })}
            />
          </div>
          <div className="col-6 col-md-2">
            <input
              type="number" min="0" className="field-input" placeholder="Max ₹"
              value={draft.maxPrice} onChange={(e) => setDraft({ ...draft, maxPrice: e.target.value })}
            />
          </div>
        </div>

        <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mt-3">
          <div className="d-flex align-items-center gap-3">
            <label className="filter-toggle">
              <input type="checkbox" checked={applied.vegOnly} onChange={(e) => apply({ vegOnly: e.target.checked })} />
              <span className="tag-veg">Veg only</span>
            </label>
            <select className="sort-select" value={applied.sort} onChange={(e) => apply({ sort: e.target.value })}>
              {sortOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>
          <div className="d-flex gap-2">
            <button type="button" className="btn-ghost" style={{ padding: '0.55rem 1.1rem' }} onClick={clearAll}>Clear</button>
            <button type="submit" className="btn-chili" style={{ padding: '0.55rem 1.4rem' }}>Search</button>
          </div>
        </div>
      </form>

      {toast.text && <div className={`notice notice-${toast.type}`}>{toast.text}</div>}
      {error && <div className="notice notice-error">{error}</div>}

      {loading ? (
        <p style={{ color: '#8A8075' }}>Loading dishes...</p>
      ) : foods.length === 0 && !error ? (
        <div className="ticket text-center" style={{ padding: '3rem', color: '#8A8075' }}>
          No dishes match your filters. Try widening the price range or clearing filters.
        </div>
      ) : (
        foods.map((food) => <FoodCard key={food.id} food={food} onAdd={addToCart} showRestaurant />)
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}