import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { login as loginRequest, getCurrentUser } from '../api/auth';
import { setUnauthorizedHandler } from '../api/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    try {
      const response = await loginRequest(username, password);
      const { token, username: u, nombreCompleto, sede } = response.data;
      localStorage.setItem('token', token);
      const userData = { username: u, nombreCompleto, sede };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  const refreshUserInfo = useCallback(async () => {
    const token = localStorage.getItem('token');
    if (!token) return;
    try {
      const response = await getCurrentUser();
      const { username: u, nombreCompleto, sede, sedeId } = response.data;
      const userData = { username: u, nombreCompleto, sede, sedeId };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
    } catch {
      logout();
    }
  }, [logout]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token && !user) {
      refreshUserInfo().finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [user, refreshUserInfo]);

  useEffect(() => {
    setUnauthorizedHandler(() => logout());
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  useEffect(() => {
    const interval = setInterval(() => {
      const token = localStorage.getItem('token');
      if (!token) return;
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const expiresIn = payload.exp * 1000 - Date.now();
        if (expiresIn < 6 * 60 * 60 * 1000) {
          refreshUserInfo();
        }
      } catch {
        // token inválido, el interceptor de API se encargará del 401
      }
    }, 5 * 60 * 1000);
    return () => clearInterval(interval);
  }, [refreshUserInfo]);

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, refreshUserInfo }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
