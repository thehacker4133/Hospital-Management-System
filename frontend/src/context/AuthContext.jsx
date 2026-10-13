import { createContext, useContext, useEffect, useState } from 'react';
import { api, getStoredUser, STORAGE_KEY } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getStoredUser);

  function save(result) {
    setUser(result);
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(result)); // includes the JWT, cleared when the tab closes
    return result;
  }
  async function login(email, password) { return save(await api.login({ email, password })); }
  async function register(name, email, password, phone) { return save(await api.register({ name, email, password, phone })); }
  function logout() { setUser(null); sessionStorage.removeItem(STORAGE_KEY); }

  // api.js fires this when the server answers 401 (token expired or account removed).
  useEffect(() => {
    const onExpired = () => logout();
    window.addEventListener('auth:expired', onExpired);
    return () => window.removeEventListener('auth:expired', onExpired);
  }, []);

  return <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
