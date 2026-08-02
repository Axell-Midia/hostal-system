import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { getMovimientos, anularIngreso, anularEgreso } from '../api/movimientos';
import { formatCurrency, formatDate } from '../utils/format';
import ModalAnular from '../components/ModalAnular';
import Paginacion from '../components/Paginacion';

const SIZE = 10;

export default function MovimientosPage() {
  const navigate = useNavigate();
  const [movimientos, setMovimientos] = useState([]);
  const [tipo, setTipo] = useState('TODOS');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [search, setSearch] = useState('');
  const [searchApplied, setSearchApplied] = useState('');
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [totals, setTotals] = useState({ totalIngresos: 0, totalEgresos: 0, ganancia: 0 });
  const [anulando, setAnulando] = useState(null);
  const [motivo, setMotivo] = useState('');

  const fetchMovimientos = useCallback(
    async (signal) => {
      setLoading(true);
      try {
        const params = { tipo, page, size: SIZE };
        if (desde) params.desde = new Date(desde).toISOString();
        if (hasta) {
          const h = new Date(hasta);
          h.setHours(23, 59, 59, 999);
          params.hasta = h.toISOString();
        }
        if (searchApplied) params.search = searchApplied;

        const res = await getMovimientos(params, signal);
        setMovimientos(res.data.movimientos || []);
        setTotalElements(res.data.totalElements || 0);
        setTotalPages(res.data.totalPages || 0);
        setTotals({
          totalIngresos: res.data.totalIngresos || 0,
          totalEgresos: res.data.totalEgresos || 0,
          ganancia: res.data.ganancia || 0,
        });
      } catch (err) {
        if (err.code === 'ERR_CANCELED') return;
        toast.error('Error al cargar movimientos');
      } finally {
        setLoading(false);
      }
    },
    [tipo, desde, hasta, page, searchApplied]
  );

  useEffect(() => {
    const controller = new AbortController();
    fetchMovimientos(controller.signal);
    return () => controller.abort();
  }, [fetchMovimientos]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearchApplied(search);
    setPage(0);
  };

  const closeModal = useCallback(() => {
    setAnulando(null);
    setMotivo('');
  }, []);

  const handleAnular = async () => {
    if (!motivo.trim()) {
      toast.error('El motivo es obligatorio');
      return;
    }
    try {
      if (anulando.tipo === 'INGRESO') {
        await anularIngreso(anulando.id, motivo.trim());
      } else {
        await anularEgreso(anulando.id, motivo.trim());
      }
      toast.success('Movimiento anulado');
      closeModal();
      fetchMovimientos();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al anular');
    }
  };

  return (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold text-deep font-display">Movimientos</h2>

      {/* Filters */}
      <div className="card no-print">
        <form onSubmit={handleSearch} className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="label-field">Tipo</label>
            <select
              value={tipo}
              onChange={(e) => { setTipo(e.target.value); setPage(0); }}
              className="input-field"
            >
              <option value="TODOS">Todos</option>
              <option value="INGRESO">Ingresos</option>
              <option value="EGRESO">Egresos</option>
            </select>
          </div>
          <div>
            <label className="label-field">Desde</label>
            <input
              type="date"
              value={desde}
              onChange={(e) => { setDesde(e.target.value); setPage(0); }}
              className="input-field"
            />
          </div>
          <div>
            <label className="label-field">Hasta</label>
            <input
              type="date"
              value={hasta}
              onChange={(e) => { setHasta(e.target.value); setPage(0); }}
              className="input-field"
            />
          </div>
          <div>
            <label className="label-field">Buscar</label>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar..."
              className="input-field"
            />
          </div>
          <button type="submit" className="btn-primary">
            Buscar
          </button>
        </form>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-sand-300/60 shadow-warm-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-sand-200">
                <th className="text-left px-5 py-3 text-xs font-medium text-sand-500">Fecha</th>
                <th className="text-left px-5 py-3 text-xs font-medium text-sand-500">Tipo</th>
                <th className="text-left px-5 py-3 text-xs font-medium text-sand-500">Descripción</th>
                <th className="text-right px-5 py-3 text-xs font-medium text-sand-500">Monto</th>
                <th className="text-center px-5 py-3 text-xs font-medium text-sand-500">Estado</th>
                <th className="hidden text-center px-5 py-3 text-xs font-medium text-sand-500">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="px-5 py-10 text-center text-sand-500 text-sm">Cargando...</td></tr>
              ) : movimientos.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-5 py-12">
                    <div className="flex flex-col items-center gap-3">
                      <div className="w-12 h-12 rounded-full bg-sand-100 flex items-center justify-center">
                        <svg className="w-6 h-6 text-sand-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                      </div>
                      <div className="text-center">
                        <p className="text-sm font-medium text-deep-600">No hay movimientos</p>
                        <p className="text-xs text-sand-500 mt-0.5">Comienza registrando un ingreso o egreso</p>
                      </div>
                      <div className="flex gap-3 mt-2">
                        <button
                          onClick={() => navigate('/nuevo-ingreso')}
                          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-sage-500 rounded-lg hover:bg-sage-600 transition-colors shadow-sm shadow-sage-500/20"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                          </svg>
                          + Ingreso
                        </button>
                        <button
                          onClick={() => navigate('/nuevo-egreso')}
                          className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-clay-500 rounded-lg hover:bg-clay-600 transition-colors shadow-sm shadow-clay-500/20"
                        >
                          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                          </svg>
                          + Egreso
                        </button>
                      </div>
                    </div>
                  </td>
                </tr>
              ) : (
                movimientos.map((m) => (
                  <tr key={`${m.tipo}-${m.id}`} className="border-b border-sand-100 last:border-0 hover:bg-sand-50/50 transition-colors">
                    <td className="px-5 py-3 text-deep-500">{formatDate(m.fecha)}</td>
                    <td className="px-5 py-3">
                      <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        m.tipo === 'INGRESO' ? 'bg-sage-50 text-sage-600' : 'bg-clay-50 text-clay-600'
                      }`}>
                        {m.tipo}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-deep-600">
                      {m.tipo === 'INGRESO' ? m.tipoHabitacion : (m.descripcion || m.categoria)}
                    </td>
                    <td className={`px-5 py-3 text-right font-medium ${m.tipo === 'INGRESO' ? 'text-sage-500' : 'text-clay-500'}`}>
                      {m.tipo === 'INGRESO' ? '+' : '-'}{formatCurrency(m.monto)}
                    </td>
                    <td className="px-5 py-3 text-center">
                      {m.anulado ? (
                        <span className="text-xs text-sand-400 line-through">Anulado</span>
                      ) : (
                        <span className="text-xs text-sage-500">Activo</span>
                      )}
                    </td>
                    <td className="px-5 py-3 text-center">
                      {!m.anulado && (
                        <button
                          onClick={() => setAnulando(m)}
                          className="hidden inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-clay-600 bg-clay-50 rounded-full hover:bg-clay-100 transition-colors"
                        >
                          <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                          </svg>
                          Anular
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Totals */}
        {movimientos.length > 0 && (
          <div className="px-5 py-3 bg-sand-50 border-t border-sand-200 flex flex-wrap gap-5 text-sm">
            <span className="text-deep-500">Ingresos: <strong className="text-sage-500">{formatCurrency(totals.totalIngresos)}</strong></span>
            <span className="text-deep-500">Egresos: <strong className="text-clay-500">{formatCurrency(totals.totalEgresos)}</strong></span>
            <span className="text-deep-500">Ganancia: <strong className="text-terracotta-500">{formatCurrency(totals.ganancia)}</strong></span>
          </div>
        )}

        {/* Pagination */}
        <Paginacion
          page={page}
          totalPages={totalPages}
          totalElements={totalElements}
          size={SIZE}
          onChange={setPage}
        />
      </div>

      {/* Anular modal */}
      <ModalAnular
        movimiento={anulando}
        motivo={motivo}
        setMotivo={setMotivo}
        onConfirm={handleAnular}
        onClose={closeModal}
      />
    </div>
  );
}
