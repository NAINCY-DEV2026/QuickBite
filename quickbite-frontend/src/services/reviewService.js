import api from './api';

export const addReview = (data) => api.post('/reviews', data);
export const getReviewsByRestaurant = (restaurantId, params) => api.get(`/restaurants/${restaurantId}/reviews`, { params });