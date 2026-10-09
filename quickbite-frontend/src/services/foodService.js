import api from './api';

export const getFoodsByRestaurant = (restaurantId) => api.get(`/restaurants/${restaurantId}/foods`);
export const searchFoods = (params) => api.get('/foods', { params });
export const createFood = (restaurantId, data) => api.post(`/restaurants/${restaurantId}/foods`, data);
export const updateFood = (id, data) => api.put(`/foods/${id}`, data);
export const deleteFood = (id) => api.delete(`/foods/${id}`);
export const setFoodAvailability = (id, available) =>
  api.patch(`/foods/${id}/availability`, null, { params: { available } });

export const uploadFoodImage = (id, file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post(`/foods/${id}/image`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};