import api from './api';

export const getMyProfile = () => api.get('/users/me');
export const updateProfile = (data) => api.put('/users/me', data);
export const changePassword = (data) => api.put('/users/change-password', data);
export const deactivateAccount = (password) => api.put('/users/deactivate', { password });

export const uploadProfileImage = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/users/profile-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};