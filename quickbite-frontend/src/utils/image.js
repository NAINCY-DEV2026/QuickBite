const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8090/api';
const ORIGIN = API_URL.replace(/\/api\/?$/, '');

// Backend stores paths like /uploads/foods/abc.jpg; this turns them into full URLs
export function imageUrl(path) {
  if (!path) return null;
  return path.startsWith('http') ? path : `${ORIGIN}${path}`;
}

const ALLOWED = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

// Returns an error message, or null when the file is fine
export function validateImage(file) {
  if (!file) return 'Please choose an image';
  if (!ALLOWED.includes(file.type)) return 'Use a JPG, PNG, WEBP or GIF image';
  if (file.size > 5 * 1024 * 1024) return 'Image must be smaller than 5 MB';
  return null;
}