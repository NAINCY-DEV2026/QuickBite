import api from './api';

export const getRestaurants = (params) => api.get('/restaurants', { params });
export const getRestaurantById = (id) => api.get(`/restaurants/${id}`);
export const getMyRestaurants = () => api.get('/restaurants/mine');
export const createRestaurant = (data) => api.post('/restaurants', data);
export const updateRestaurant = (id, data) => api.put(`/restaurants/${id}`, data);
export const deleteRestaurant = (id) => api.delete(`/restaurants/${id}`);

export const uploadRestaurantImage = (id, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/restaurants/${id}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};