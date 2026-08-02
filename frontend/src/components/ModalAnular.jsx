import { useEffect } from 'react';
import { formatCurrency } from '../utils/format';

export default function ModalAnular({ movimiento, motivo, setMotivo, onConfirm, onClose }) {
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleEsc);
    return () => window.removeEventListener('keydown', handleEsc);
  }, [onClose]);

  if (!movimiento) return null;

  return (
    <div
      className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="bg-white rounded-xl border border-sand-300/60 shadow-warm-md p-6 w-full max-w-md">
        <h3 className="text-lg font-semibold text-deep mb-2 font-display">Anular movimiento</h3>
        <p className="text-sm text-deep-500 mb-4">
          ¿Está seguro de anular este {movimiento.tipo.toLowerCase()} de{' '}
          {formatCurrency(movimiento.monto)}?
        </p>
        <div className="mb-4">
          <label className="label-field">Motivo *</label>
          <textarea
            value={motivo}
            onChange={(e) => setMotivo(e.target.value)}
            rows={3}
            className="w-full px-3 py-2.5 border border-sand-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-clay-500/20 focus:border-clay-400 transition-colors resize-none"
            placeholder="Ingrese el motivo de anulación..."
            autoFocus
          />
        </div>
        <div className="flex gap-3 justify-end">
          <button onClick={onClose} className="btn-secondary">
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            className="px-4 py-2.5 text-sm font-medium text-white bg-clay-500 rounded-lg hover:bg-clay-600 transition-colors"
          >
            Anular
          </button>
        </div>
      </div>
    </div>
  );
}
