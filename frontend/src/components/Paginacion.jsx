export default function Paginacion({ page, totalPages, totalElements, size, onChange }) {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;
    let start = Math.max(0, page - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages - 1, start + maxVisible - 1);
    if (end - start < maxVisible - 1) {
      start = Math.max(0, end - maxVisible + 1);
    }
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <div className="px-5 py-3 border-t border-sand-200 flex items-center justify-between text-sm">
      <span className="text-sand-500 text-xs">
        Mostrando {page * size + 1}-{Math.min((page + 1) * size, totalElements)} de {totalElements}
      </span>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onChange(0)}
          disabled={page === 0}
          className="px-2 py-1.5 rounded-lg text-sm disabled:opacity-30 hover:bg-sand-100 transition-colors"
          title="Primera página"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>
        <button
          onClick={() => onChange(Math.max(0, page - 1))}
          disabled={page === 0}
          className="px-2 py-1.5 rounded-lg text-sm disabled:opacity-30 hover:bg-sand-100 transition-colors"
          title="Página anterior"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        {getPageNumbers().map((pageNum) => (
          <button
            key={pageNum}
            onClick={() => onChange(pageNum)}
            className={`w-8 h-8 rounded-lg text-sm font-medium transition-colors ${
              page === pageNum ? 'bg-terracotta-500 text-white' : 'text-deep-500 hover:bg-sand-100'
            }`}
          >
            {pageNum + 1}
          </button>
        ))}

        <button
          onClick={() => onChange(Math.min(totalPages - 1, page + 1))}
          disabled={page >= totalPages - 1}
          className="px-2 py-1.5 rounded-lg text-sm disabled:opacity-30 hover:bg-sand-100 transition-colors"
          title="Página siguiente"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
        <button
          onClick={() => onChange(totalPages - 1)}
          disabled={page >= totalPages - 1}
          className="px-2 py-1.5 rounded-lg text-sm disabled:opacity-30 hover:bg-sand-100 transition-colors"
          title="Última página"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  );
}
