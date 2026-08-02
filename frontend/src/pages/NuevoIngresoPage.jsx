import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { crearIngreso } from '../api/movimientos';
import toast from 'react-hot-toast';

const TIPOS_HABITACION = ['SIMPLE', 'DOBLE', 'MATRIMONIAL', 'TRIPLE', 'CUADRUPLE'];

export default function NuevoIngresoPage() {
  const [tipoHabitacion, setTipoHabitacion] = useState('');
  const [monto, setMonto] = useState('');
  const [observacion, setObservacion] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!tipoHabitacion) {
      toast.error('Seleccione un tipo de habitación');
      return;
    }
    if (!monto || parseFloat(monto) <= 0) {
      toast.error('El monto debe ser mayor que cero');
      return;
    }
    setLoading(true);
    try {
      await crearIngreso({
        tipoHabitacion,
        monto: parseFloat(monto),
        observacion: observacion || null,
      });
      toast.success('Ingreso registrado');
      navigate('/movimientos');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error al registrar ingreso');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg">
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 rounded-lg bg-sage-500 flex items-center justify-center shadow-sm shadow-sage-500/20">
          <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </div>
        <h2 className="text-lg font-semibold text-deep font-display">Nuevo Ingreso</h2>
      </div>
      <form onSubmit={handleSubmit} className="bg-white rounded-xl border border-sand-300/60 shadow-warm-sm overflow-hidden">
        <div className="h-1 bg-sage-500" />
        <div className="p-6 space-y-4">
          <div>
            <label className="label-field">Tipo de habitación *</label>
            <select
              value={tipoHabitacion}
              onChange={(e) => setTipoHabitacion(e.target.value)}
              className="input-field"
            >
              <option value="">Seleccionar...</option>
              {TIPOS_HABITACION.map((t) => (
                <option key={t} value={t}>{t.charAt(0) + t.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>

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
            <label className="label-field">Observación</label>
            <textarea
              value={observacion}
              onChange={(e) => setObservacion(e.target.value)}
              rows={3}
              className="input-field resize-none"
              placeholder="Opcional..."
            />
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => navigate(-1)} className="flex-1 btn-secondary">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2.5 text-sm font-medium text-white bg-sage-500 rounded-lg hover:bg-sage-600 focus:outline-none focus:ring-2 focus:ring-sage-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {loading ? 'Guardando...' : 'Registrar Ingreso'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
