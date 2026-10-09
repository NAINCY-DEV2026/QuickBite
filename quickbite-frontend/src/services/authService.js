import api from './api';

export const register = (data) => api.post('/auth/register', data);
export const registerOwner = (data) => api.post('/auth/register-owner', data);
export const login = (data) => api.post('/auth/login', data);
export const forgotPassword = (email) => api.post('/auth/forgot-password', { email });
export const resetPassword = (data) => api.post('/auth/reset-password', data);