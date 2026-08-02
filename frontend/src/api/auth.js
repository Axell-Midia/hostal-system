import api from './api';

export const login = (username, password) =>
  api.post('/api/auth/login', { username, password });

export const getCurrentUser = () => api.get('/api/auth/me');
