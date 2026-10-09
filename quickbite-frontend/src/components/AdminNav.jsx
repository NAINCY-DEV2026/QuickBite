import { NavLink } from 'react-router-dom';

const links = [
  { to: '/admin', label: 'Overview', end: true },
  { to: '/admin/orders', label: 'Orders' },
  { to: '/admin/restaurants', label: 'Restaurants' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/coupons', label: 'Coupons' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/reports', label: 'Reports' },
];

export default function AdminNav() {
  return (
    <div className="admin-tabs">
      {links.map((l) => (
        <NavLink
          key={l.to}
          to={l.to}
          end={l.end}
          className={({ isActive }) => `admin-tab ${isActive ? 'active' : ''}`}
        >
          {l.label}
        </NavLink>
      ))}
    </div>
  );
}