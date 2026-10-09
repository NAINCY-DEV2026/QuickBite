import api from './api';

export const previewOrder = (data) => api.post('/orders/preview', data || {});
export const placeOrder = (data) => api.post('/orders', data);
export const getMyOrders = (params) => api.get('/orders', { params });
export const getOrderById = (id) => api.get(`/orders/${id}`);
export const cancelOrder = (id) => api.put(`/orders/${id}/cancel`);
export const getRestaurantOrders = (restaurantId, params) => api.get(`/restaurant/${restaurantId}/orders`, { params });
export const updateOrderStatus = (orderId, status) => api.put(`/restaurant/orders/${orderId}/status`, { status });