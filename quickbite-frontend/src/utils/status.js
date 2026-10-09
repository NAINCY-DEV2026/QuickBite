const styles = {
  PLACED: ['#FFF6E5', '#9A6A00'],
  CONFIRMED: ['#E8F0FE', '#1A4FA3'],
  PREPARING: ['#E8F0FE', '#1A4FA3'],
  READY_FOR_PICKUP: ['#E8F0FE', '#1A4FA3'],
  OUT_FOR_DELIVERY: ['#EEE8FB', '#5A33A8'],
  DELIVERED: ['#EAF3EA', '#3E7C45'],
  CANCELLED: ['#F1EEEA', '#6B6258'],
  REJECTED: ['#FDEAE6', '#B03A22'],

  ACTIVE: ['#EAF3EA', '#3E7C45'],
  BLOCKED: ['#FDEAE6', '#B03A22'],
  DEACTIVATED: ['#F1EEEA', '#6B6258'],
  DISABLED: ['#F1EEEA', '#6B6258'],
  SCHEDULED: ['#E8F0FE', '#1A4FA3'],
  EXPIRED: ['#FDEAE6', '#B03A22'],
  EXHAUSTED: ['#FFF6E5', '#9A6A00'],
};

export function pillStyle(key) {
  const [background, color] = styles[key] || ['#F1EEEA', '#6B6258'];
  return { background, color };
}

// "OUT_FOR_DELIVERY" -> "Out for delivery"
export function label(value) {
  if (!value) return '';
  const text = value.replaceAll('_', ' ').toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}