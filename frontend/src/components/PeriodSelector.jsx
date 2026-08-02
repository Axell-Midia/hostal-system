const periods = [
  { label: 'Hoy', value: 'hoy' },
  { label: 'Últimos 7 días', value: '7dias' },
  { label: 'Últimos 30 días', value: '30dias' },
  { label: 'Todo', value: 'todo' },
];

export function getDateRange(period) {
  const now = new Date();
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  switch (period) {
    case 'hoy':
      return {
        desde: todayStart.toISOString(),
        hasta: now.toISOString(),
      };
    case '7dias': {
      const desde = new Date(now);
      desde.setDate(desde.getDate() - 7);
      return { desde: desde.toISOString(), hasta: now.toISOString() };
    }
    case '30dias': {
      const desde = new Date(now);
      desde.setDate(desde.getDate() - 30);
      return { desde: desde.toISOString(), hasta: now.toISOString() };
    }
    case 'todo':
    default:
      return { desde: null, hasta: null };
  }
}

export default function PeriodSelector({ value, onChange }) {
  return (
    <div className="flex flex-wrap gap-2">
      {periods.map((period) => (
        <button
          key={period.value}
          onClick={() => onChange(period.value)}
          className={`px-4 py-2 text-sm font-medium rounded-lg transition-all duration-150 ${
            value === period.value
              ? 'bg-terracotta-500 text-white shadow-sm shadow-terracotta-500/20'
              : 'bg-white text-deep-600 border border-sand-300 hover:bg-sand-100 hover:border-sand-400'
          }`}
        >
          {period.label}
        </button>
      ))}
    </div>
  );
}
