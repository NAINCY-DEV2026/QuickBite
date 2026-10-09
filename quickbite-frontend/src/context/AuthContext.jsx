import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null; // corrupted value: behave as logged out instead of crashing the app
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const login = (data) => {
    // data = { token, userId, name, role }
    const profile = { userId: data.userId, name: data.name, role: data.role };
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(profile));
    setUser(profile);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  // e.g. keeps the navbar greeting in sync after the name is edited on the profile page
  const updateUser = (patch) => {
    const next = { ...user, ...patch };
    localStorage.setItem('user', JSON.stringify(next));
    setUser(next);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, updateUser, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}