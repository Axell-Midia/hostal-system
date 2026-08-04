import { useState, useEffect, useCallback } from 'react';
import PeriodSelector, { getDateRange } from '../components/PeriodSelector';
import {
  getReporteRango,
  getIngresosPorHabitacion,
  getEgresosPorCategoria,
  exportarCsv,
} from '../api/reportes';
import { formatCurrency } from '../utils/format';
import toast from 'react-hot-toast';

export default function ReportesPage() {
  const [period, setPeriod] = useState('30dias');
  const [resumen, setResumen] = useState(null);
  const [ingresosPorHabitacion, setIngresosPorHabitacion] = useState([]);
  const [egresosPorCategoria, setEgresosPorCategoria] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchReportes = useCallback(
    async (signal) => {
      setLoading(true);
      try {
        const { desde, hasta } = getDateRange(period);
        const params = {};
        if (desde) params.desde = desde;
        if (hasta) params.hasta = hasta;

        const [resumenRes, ingRes, egrRes] = await Promise.all([
          getReporteRango(params, signal),
          getIngresosPorHabitacion(params, signal),
          getEgresosPorCategoria(params, signal),
        ]);

        setResumen(resumenRes.data);
        setIngresosPorHabitacion(ingRes.data || []);
        setEgresosPorCategoria(egrRes.data || []);
      } catch (err) {
        if (err.code === 'ERR_CANCELED') return;
        toast.error('Error al cargar reportes');
      } finally {
        setLoading(false);
      }
    },
    [period]
  );

  useEffect(() => {
    const controller = new AbortController();
    fetchReportes(controller.signal);
    return () => controller.abort();
  }, [fetchReportes]);

  const handleExportCsv = async () => {
    try {
      const { desde, hasta } = getDateRange(period);
      const params = { tipo: 'TODOS' };
      if (desde) params.desde = desde;
      if (hasta) params.hasta = hasta;

      const res = await exportarCsv(params);
      const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte-${period}.csv`;
      link.click();
      URL.revokeObjectURL(url);
      toast.success('CSV descargado');
    } catch (err) {
      toast.error('Error al exportar CSV');
    }
  };

  const handlePrint = () => window.print();

  if (loading) {
    return <div className="flex items-center justify-center h-64 text-sand-500 text-sm">Cargando...</div>;
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-4 no-print">
        <h2 className="text-lg font-semibold text-deep font-display">Reportes</h2>
        <div className="flex gap-2">
          <button onClick={handleExportCsv} className="btn-secondary hidden">
            Exportar CSV
          </button>
          <button onClick={handlePrint} className="btn-secondary hidden">
            Imprimir
          </button>
        </div>
      </div>

      <div className="no-print">
        <PeriodSelector value={period} onChange={setPeriod} />
      </div>

      {/* Print header */}
      <div className="print-only mb-4">
        <h1 className="text-xl font-bold">Reporte Financiero</h1>
        <p className="text-sm text-gray-600">Período: {period}</p>
      </div>

      {/* Resumen */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="card-hover">
          <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Ingresos</p>
          <p className="text-2xl font-bold text-sage-500 font-display">{formatCurrency(resumen?.totalIngresos)}</p>
        </div>
        <div className="card-hover">
          <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Egresos</p>
          <p className="text-2xl font-bold text-clay-500 font-display">{formatCurrency(resumen?.totalEgresos)}</p>
        </div>
        <div className="card-hover">
          <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Ganancia</p>
          <p className="text-2xl font-bold text-terracotta-500 font-display">{formatCurrency(resumen?.ganancia)}</p>
        </div>
      </div>

      {/* Ingresos por tipo de habitación */}
      <div className="bg-white rounded-xl border border-sand-300/60 shadow-warm-sm">
        <div className="px-5 py-4 border-b border-sand-200">
          <h3 className="text-sm font-semibold text-deep font-display">Ingresos por tipo de habitación</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-sand-200">
                <th className="text-left px-5 py-3 text-xs font-medium text-sand-500">Tipo</th>
                <th className="text-right px-5 py-3 text-xs font-medium text-sand-500">Cantidad</th>
                <th className="text-right px-5 py-3 text-xs font-medium text-sand-500">Total</th>
              </tr>
            </thead>
            <tbody>
              {ingresosPorHabitacion.length === 0 ? (
                <tr><td colSpan={3} className="px-5 py-8 text-center text-gray-400 text-sm">Sin datos</td></tr>
              ) : (
                ingresosPorHabitacion.map((r) => (
                  <tr key={r.grupo} className="border-b border-sand-100 last:border-0 hover:bg-sand-50/50 transition-colors">
                    <td className="px-5 py-3 font-medium text-deep">{r.grupo}</td>
                    <td className="px-5 py-3 text-right text-deep-500">{r.cantidad}</td>
                    <td className="px-5 py-3 text-right font-medium text-sage-500">{formatCurrency(r.total)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Egresos por categoría */}
      <div className="bg-white rounded-xl border border-sand-300/60 shadow-warm-sm">
        <div className="px-5 py-4 border-b border-sand-200">
          <h3 className="text-sm font-semibold text-deep font-display">Egresos por categoría</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-sand-200">
                <th className="text-left px-5 py-3 text-xs font-medium text-sand-500">Categoría</th>
                <th className="text-right px-5 py-3 text-xs font-medium text-sand-500">Cantidad</th>
                <th className="text-right px-5 py-3 text-xs font-medium text-sand-500">Total</th>
              </tr>
            </thead>
            <tbody>
              {egresosPorCategoria.length === 0 ? (
                <tr><td colSpan={3} className="px-5 py-8 text-center text-sand-500 text-sm">Sin datos</td></tr>
              ) : (
                egresosPorCategoria.map((r) => (
                  <tr key={r.grupo} className="border-b border-sand-100 last:border-0 hover:bg-sand-50/50 transition-colors">
                    <td className="px-5 py-3 font-medium text-deep">{r.grupo}</td>
                    <td className="px-5 py-3 text-right text-deep-500">{r.cantidad}</td>
                    <td className="px-5 py-3 text-right font-medium text-clay-500">{formatCurrency(r.total)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
