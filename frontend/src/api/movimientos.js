import api from './api';

export const getMovimientos = (params, signal) =>
  api.get('/api/movimientos', { params, signal });

export const crearIngreso = (data) => api.post('/api/ingresos', data);

export const crearEgreso = (data) => api.post('/api/egresos', data);

export const anularIngreso = (id, motivo) =>
  api.post(`/api/ingresos/${id}/anular`, { motivo });

export const anularEgreso = (id, motivo) =>
  api.post(`/api/egresos/${id}/anular`, { motivo });
