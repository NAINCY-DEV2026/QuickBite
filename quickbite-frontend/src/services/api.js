import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8090/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url || '';
    const isAuthCall = url.startsWith('/auth/');

    // Expired/invalid token on a protected call: log out. A wrong password on /auth/login is
    // also a 401, so auth calls are skipped; otherwise the login error message would vanish on reload.
    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;