export const money = (value) =>
  `₹${Number(value || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

export const formatDateTime = (iso) =>
  iso ? new Date(iso).toLocaleString('en-IN', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' }) : '';

export const formatDate = (iso) =>
  iso ? new Date(iso).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '';

// "2026-10-08" -> "8 Oct"
export const shortDate = (isoDate) =>
  new Date(`${isoDate}T00:00:00`).toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });