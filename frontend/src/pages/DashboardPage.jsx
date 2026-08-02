import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import PeriodSelector, { getDateRange } from '../components/PeriodSelector';
import { getDashboard, getIngresosPorHabitacion, getEgresosPorCategoria } from '../api/reportes';
import { formatCurrency } from '../utils/format';
import ReactECharts from 'echarts-for-react';

const CATEGORY_COLORS = {
  ELECTRICIDAD: '#2563EB',
  AGUA: '#0891B2',
  INTERNET: '#7C3AED',
  OTROS: '#D97706',
};

const CATEGORY_LABELS = {
  ELECTRICIDAD: 'Electricidad',
  AGUA: 'Agua',
  INTERNET: 'Internet',
  OTROS: 'Otros',
};

const HABITACION_COLORS = ['#059669', '#2563EB', '#D97706', '#7C3AED', '#E11D48'];

const TOOLTIP_STYLE = {
  backgroundColor: '#1E1E1E',
  borderColor: 'rgba(255,255,255,0.08)',
  textStyle: { color: '#fff', fontSize: 12 },
};

export default function DashboardPage() {
  const navigate = useNavigate();
  const [period, setPeriod] = useState('hoy');
  const [resumen, setResumen] = useState(null);
  const [ingresosPorHabitacion, setIngresosPorHabitacion] = useState([]);
  const [egresosPorCategoria, setEgresosPorCategoria] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(
    async (signal) => {
      setLoading(true);
      try {
        const { desde, hasta } = getDateRange(period);
        const params = {};
        if (desde) params.desde = desde;
        if (hasta) params.hasta = hasta;

        const [dashboardRes, ingRes, egRes] = await Promise.all([
          getDashboard(params, signal),
          getIngresosPorHabitacion(params, signal),
          getEgresosPorCategoria(params, signal),
        ]);

        setResumen(dashboardRes.data);
        setIngresosPorHabitacion(ingRes.data || []);
        setEgresosPorCategoria(egRes.data || []);
      } catch (err) {
        if (err.code === 'ERR_CANCELED') return;
        console.error('Error fetching dashboard:', err);
      } finally {
        setLoading(false);
      }
    },
    [period]
  );

  useEffect(() => {
    const controller = new AbortController();
    fetchData(controller.signal);
    return () => controller.abort();
  }, [fetchData]);

  const getIngresosEgresosOption = () => {
    const totalIng = Number(resumen?.totalIngresos || 0);
    const totalEgr = Number(resumen?.totalEgresos || 0);
    return {
      tooltip: {
        trigger: 'axis',
        ...TOOLTIP_STYLE,
        formatter: (params) => {
          let result = params[0].axisValue + '<br/>';
          params.forEach((p) => {
            result += `${p.marker} ${p.seriesName}: <b>S/. ${Number(p.value).toFixed(2)}</b><br/>`;
          });
          return result;
        },
      },
      legend: {
        bottom: 0,
        textStyle: { fontSize: 12, color: '#6d6d6d' },
      },
      grid: { top: 20, right: 20, bottom: 40, left: 60 },
      xAxis: {
        type: 'category',
        data: ['Período'],
        axisLine: { show: false },
        axisTick: { show: false },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: (v) => `S/. ${v >= 1000 ? (v / 1000).toFixed(1) + 'k' : v}`,
          fontSize: 11,
          color: '#9CA3AF',
        },
        splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      },
      series: [
        {
          name: 'Ingresos',
          type: 'bar',
          data: [totalIng],
          barWidth: 50,
          itemStyle: { color: '#10b981', borderRadius: [6, 6, 0, 0] },
        },
        {
          name: 'Egresos',
          type: 'bar',
          data: [totalEgr],
          barWidth: 50,
          itemStyle: { color: '#ef4444', borderRadius: [6, 6, 0, 0] },
        },
      ],
    };
  };

  const getEgresosDonutOption = () => {
    const data = egresosPorCategoria.map((d) => ({
      name: CATEGORY_LABELS[d.grupo] || d.grupo,
      value: Number(d.total),
      itemStyle: { color: CATEGORY_COLORS[d.grupo] || '#B8A590' },
    }));
    return {
      tooltip: {
        trigger: 'item',
        ...TOOLTIP_STYLE,
        formatter: (p) =>
          `${p.marker} ${p.name}<br/><b>S/. ${Number(p.value).toFixed(2)}</b> (${p.percent}%)`,
      },
      legend: {
        bottom: 0,
        textStyle: { fontSize: 11, color: '#6d6d6d' },
      },
      series: [
        {
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 13, fontWeight: 'bold' },
          },
          data,
        },
      ],
    };
  };

  const getIngresosHabitacionOption = () => {
    const data = ingresosPorHabitacion.map((d) => ({
      name: d.grupo,
      value: Number(d.total),
    }));
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        ...TOOLTIP_STYLE,
        formatter: (params) => {
          const p = params[0];
          return `${p.name}<br/><b>S/. ${Number(p.value).toFixed(2)}</b>`;
        },
      },
      grid: { top: 10, right: 20, bottom: 10, left: 100 },
      xAxis: {
        type: 'value',
        axisLabel: {
          formatter: (v) => `S/. ${v >= 1000 ? (v / 1000).toFixed(1) + 'k' : v}`,
          fontSize: 11,
          color: '#B8A590',
        },
        splitLine: { lineStyle: { color: '#F3F4F6', type: 'dashed' } },
      },
      yAxis: {
        type: 'category',
        data: data.map((d) => d.name),
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { fontSize: 12, color: '#374151', fontWeight: 500 },
      },
      series: [
        {
          type: 'bar',
          data: data.map((d, i) => ({
            ...d,
            itemStyle: {
              color: HABITACION_COLORS[i % HABITACION_COLORS.length],
              borderRadius: [0, 6, 6, 0],
            },
          })),
          barWidth: 24,
        },
      ],
    };
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-sand-500 text-sm">Cargando...</div>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="no-print">
        <PeriodSelector value={period} onChange={setPeriod} />
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="card-hover slide-up relative overflow-hidden" style={{ animationDelay: '0ms' }}>
          <div className="absolute left-0 top-0 bottom-0 w-1 bg-sage-500 rounded-l-xl" />
          <div className="flex items-start justify-between pl-3">
            <div>
              <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Ingresos</p>
              <p className="text-2xl font-bold text-sage-600 font-display">
                {formatCurrency(resumen?.totalIngresos)}
              </p>
            </div>
            <div className="w-10 h-10 rounded-lg bg-sage-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-sage-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
            </div>
          </div>
        </div>

        <div className="card-hover slide-up relative overflow-hidden" style={{ animationDelay: '50ms' }}>
          <div className="absolute left-0 top-0 bottom-0 w-1 bg-clay-500 rounded-l-xl" />
          <div className="flex items-start justify-between pl-3">
            <div>
              <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Egresos</p>
              <p className="text-2xl font-bold text-clay-500 font-display">
                {formatCurrency(resumen?.totalEgresos)}
              </p>
            </div>
            <div className="w-10 h-10 rounded-lg bg-clay-50 flex items-center justify-center flex-shrink-0">
              <svg className="w-5 h-5 text-clay-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
              </svg>
            </div>
          </div>
        </div>

        <div className="card-hover slide-up relative overflow-hidden" style={{ animationDelay: '100ms' }}>
          <div className={`absolute left-0 top-0 bottom-0 w-1 rounded-l-xl ${(resumen?.ganancia || 0) >= 0 ? 'bg-terracotta-500' : 'bg-clay-500'}`} />
          <div className="flex items-start justify-between pl-3">
            <div>
              <p className="text-xs font-medium text-sand-500 uppercase tracking-wider mb-1">Ganancia</p>
              <p className={`text-2xl font-bold font-display ${(resumen?.ganancia || 0) >= 0 ? 'text-terracotta-500' : 'text-clay-500'}`}>
                {formatCurrency(resumen?.ganancia)}
              </p>
            </div>
            <div className={`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${(resumen?.ganancia || 0) >= 0 ? 'bg-terracotta-50' : 'bg-clay-50'}`}>
              <svg className={`w-5 h-5 ${(resumen?.ganancia || 0) >= 0 ? 'text-terracotta-500' : 'text-clay-500'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      {/* Quick actions */}
      <div className="flex gap-3 no-print">
        <button
          onClick={() => navigate('/nuevo-ingreso')}
          className="flex items-center gap-2 px-4 py-2.5 bg-sage-500 text-white text-sm font-medium rounded-lg hover:bg-sage-600 transition-colors shadow-sm shadow-sage-500/20"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Nuevo Ingreso
        </button>
        <button
          onClick={() => navigate('/nuevo-egreso')}
          className="flex items-center gap-2 px-4 py-2.5 bg-clay-500 text-white text-sm font-medium rounded-lg hover:bg-clay-600 transition-colors shadow-sm shadow-clay-500/20"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
          </svg>
          Nuevo Egreso
        </button>
      </div>

      {/* Charts row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="card slide-up" style={{ animationDelay: '150ms' }}>
          <h3 className="text-sm font-semibold text-deep mb-3 font-display">Ingresos vs Egresos</h3>
          {Number(resumen?.totalIngresos || 0) === 0 && Number(resumen?.totalEgresos || 0) === 0 ? (
            <div className="flex items-center justify-center h-56 text-sand-500 text-sm">
              Sin datos en este período
            </div>
          ) : (
            <ReactECharts option={getIngresosEgresosOption()} style={{ height: 250 }} />
          )}
        </div>

        <div className="card slide-up" style={{ animationDelay: '200ms' }}>
          <h3 className="text-sm font-semibold text-deep mb-3 font-display">Egresos por categoría</h3>
          {egresosPorCategoria.length === 0 ? (
            <div className="flex items-center justify-center h-56 text-sand-500 text-sm">
              Sin datos en este período
            </div>
          ) : (
            <ReactECharts option={getEgresosDonutOption()} style={{ height: 250 }} />
          )}
        </div>
      </div>

      {/* Chart row 2 */}
      <div className="card slide-up" style={{ animationDelay: '250ms' }}>
        <h3 className="text-sm font-semibold text-deep mb-3 font-display">Ingresos por tipo de habitación</h3>
        {ingresosPorHabitacion.length === 0 ? (
          <div className="flex items-center justify-center h-48 text-sand-500 text-sm">
            Sin datos en este período
          </div>
        ) : (
          <ReactECharts option={getIngresosHabitacionOption()} style={{ height: 200 }} />
        )}
      </div>
    </div>
  );
}
