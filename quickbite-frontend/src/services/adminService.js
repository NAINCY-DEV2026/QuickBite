import api from './api';

export const getStats = () => api.get('/admin/stats');
export const getReports = (days) => api.get('/admin/reports', { params: { days } });

export const getUsers = (params) => api.get('/admin/users', { params });
export const blockUser = (id) => api.put(`/admin/users/${id}/block`);
export const unblockUser = (id) => api.put(`/admin/users/${id}/unblock`);

export const getAllRestaurants = (params) => api.get('/admin/restaurants', { params });
export const blockRestaurant = (id) => api.put(`/admin/restaurants/${id}/block`);
export const unblockRestaurant = (id) => api.put(`/admin/restaurants/${id}/unblock`);

export const getAdminOrders = (params) => api.get('/admin/orders', { params });

export const getAdminCoupons = (params) => api.get('/admin/coupons', { params });
export const createCoupon = (data) => api.post('/admin/coupons', data);
export const updateCoupon = (id, data) => api.put(`/admin/coupons/${id}`, data);
export const setCouponActive = (id, active) => api.put(`/admin/coupons/${id}/active`, null, { params: { active } });
export const deleteCoupon = (id) => api.delete(`/admin/coupons/${id}`);

export const getAdminCategories = () => api.get('/admin/categories');
export const createCategory = (name) => api.post('/admin/categories', { name });
export const deleteCategory = (id) => api.delete(`/admin/categories/${id}`);