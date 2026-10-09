import api from './api';

export const validateCoupon = (data) => api.post('/coupons/validate', data);