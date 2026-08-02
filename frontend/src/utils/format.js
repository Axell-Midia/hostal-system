export const formatCurrency = (v) => `S/. ${Number(v || 0).toFixed(2)}`;

export const formatDate = (d) =>
  d
    ? new Date(d).toLocaleDateString('es-PE', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      })
    : '-';
