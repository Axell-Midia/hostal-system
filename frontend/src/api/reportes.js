import api from './api';

export const getDashboard = (params, signal) =>
  api.get('/api/dashboard', { params, signal });

export const getReporteRango = (params, signal) =>
  api.get('/api/reportes/rango', { params, signal });

export const getIngresosPorHabitacion = (params, signal) =>
  api.get('/api/reportes/ingresos-por-habitacion', { params, signal });

export const getEgresosPorCategoria = (params, signal) =>
  api.get('/api/reportes/egresos-por-categoria', { params, signal });

export const exportarCsv = (params) =>
  api.get('/api/reportes/exportar-csv', { params });
