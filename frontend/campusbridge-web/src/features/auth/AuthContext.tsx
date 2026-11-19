import { createContext, useContext, useState } from 'react';
import type { ReactNode } from 'react';
import { getSession, login as apiLogin, logout as apiLogout, register as apiRegister } from '../../services/authService';
import type { LoginInput, RegisterInput, User } from '../../types';

interface AuthContextValue {
  user: User | null;
  login: (input: LoginInput) => Promise<User>;
  register: (input: RegisterInput) => Promise<User>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(() => getSession());

  const login = async (input: LoginInput): Promise<User> => {
    const loggedInUser = await apiLogin(input);
    setUser(loggedInUser);
    return loggedInUser;
  };

  const register = async (input: RegisterInput): Promise<User> => {
    const registeredUser = await apiRegister(input);
    setUser(registeredUser);
    return registeredUser;
  };

  const logout = (): void => {
    apiLogout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// oxlint-disable-next-line react/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
