import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { crearEgreso } from '../api/movimientos';
import toast from 'react-hot-toast';

const CATEGORIAS = ['ELECTRICIDAD', 'AGUA', 'INTERNET', 'OTROS'];

export default function NuevoEgresoPage() {
  const [monto, setMonto] = useState('');
  const [categoria, setCategoria] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!monto || parseFloat(monto) <= 0) {
      toast.error('El monto debe ser mayor que cero');
      return;
    }
    if (!categoria) {
      toast.error('Seleccione una categoría');
      return;
    }
    if (categoria === 'OTROS' && !descripcion.trim()) {
      toast.error('La categoría Otros requiere una descripción');
      return;
    }
    setLoading(true);
    try {
      await crearEgreso({
        monto: parseFloat(monto),
        categoria,
        descripcion: descripcion || null,
      });
      toast.success('Egreso registrado');
      navigate('/movimientos');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al registrar egreso');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg">
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 rounded-lg bg-clay-500 flex items-center justify-center shadow-sm shadow-clay-500/20">
          <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
          </svg>
        </div>
        <h2 className="text-lg font-semibold text-deep font-display">Nuevo Egreso</h2>
      </div>
      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-sand-300/60 shadow-warm-sm overflow-hidden">
        <div className="h-1 bg-clay-500" />
        <div className="p-6 space-y-4">
          <div>
            <label className="label-field">Monto (S/.) *</label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={monto}
              onChange={(e) => setMonto(e.target.value)}
              className="input-field"
              placeholder="0.00"
            />
          </div>

          <div>
            <label className="label-field">Categoría *</label>
            <select
              value={categoria}
              onChange={(e) => setCategoria(e.target.value)}
              className="input-field"
            >
              <option value="">Seleccionar...</option>
              {CATEGORIAS.map((c) => (
                <option key={c} value={c}>{c.charAt(0) + c.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>

          {categoria === 'OTROS' && (
            <div>
              <label className="label-field">Descripción *</label>
              <textarea
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                rows={3}
                className="input-field resize-none"
                placeholder="Describa el gasto..."
              />
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => navigate(-1)} className="flex-1 btn-secondary">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2.5 text-sm font-medium text-white bg-clay-500 rounded-lg hover:bg-clay-600 focus:outline-none focus:ring-2 focus:ring-clay-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Guardando...' : 'Registrar Egreso'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
