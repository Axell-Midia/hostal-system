import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Sidebar from './Sidebar';

export default function Layout({ children }) {
  const { user } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const today = new Date().toLocaleDateString('es-PE', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-sand">
      {/* Mobile top bar */}
      <div className="md:hidden flex items-center justify-between px-4 py-3 bg-deep border-b border-white/8 no-print">
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="p-2 rounded-lg text-white/50 hover:bg-white/5 hover:text-white/80"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <span className="text-sm font-bold text-white tracking-wide font-display">HOSTAL</span>
        <span className="text-xs text-white/40">{user?.sede}</span>
      </div>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="md:hidden fixed inset-0 bg-black/40 backdrop-blur-sm z-40 transition-opacity"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div className={`
        fixed md:sticky top-0 left-0 z-50 h-screen
        transform transition-transform duration-200 ease-in-out
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
        md:translate-x-0
      `}>
        <Sidebar onClose={() => setSidebarOpen(false)} />
      </div>

      {/* Main content */}
      <main className="flex-1 min-h-screen md:ml-0">
        {/* Header */}
        <header className="bg-white/70 backdrop-blur-md border-b border-sand-300/60 px-6 py-4 no-print">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-1">
            <h1 className="text-lg font-semibold text-deep font-display">{user?.sede}</h1>
            <p className="text-sm text-sand-500 capitalize">{today}</p>
          </div>
        </header>

        {/* Page content */}
        <div className="p-4 md:p-6 fade-in">
          {children}
        </div>
      </main>
    </div>
  );
}
