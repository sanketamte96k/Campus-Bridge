import apiClient from './apiClient';
import type { LoginInput, RegisterInput, Role, User } from '../types';

interface AuthResponse {
  token: string;
  type: string;
  id: string;
  name: string;
  email: string;
  role: Role;
}

const SESSION_STORAGE_KEY = 'cb_session';
const TOKEN_STORAGE_KEY = 'cb_token';

export async function register(input: RegisterInput): Promise<User> {
  const res = await apiClient.post<AuthResponse>('/api/auth/register', {
    name: input.name.trim(),
    email: input.email.trim().toLowerCase(),
    password: input.password,
    role: input.role,
  });

  const { token, id, name, email, role } = res.data;
  const user: User = { id, name, email, role };

  localStorage.setItem(TOKEN_STORAGE_KEY, token);
  localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));

  return user;
}

export async function login(input: LoginInput): Promise<User> {
  const res = await apiClient.post<AuthResponse>('/api/auth/login', {
    email: input.email.trim().toLowerCase(),
    password: input.password,
  });

  const { token, id, name, email, role } = res.data;
  const user: User = { id, name, email, role };

  localStorage.setItem(TOKEN_STORAGE_KEY, token);
  localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));

  return user;
}

export function getSession(): User | null {
  try {
    const raw = localStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) {
      return null;
    }
    const parsed = JSON.parse(raw) as User;
    if (
      parsed &&
      typeof parsed === 'object' &&
      typeof parsed.id === 'string' &&
      typeof parsed.email === 'string' &&
      typeof parsed.name === 'string' &&
      typeof parsed.role === 'string'
    ) {
      return parsed;
    }
    return null;
  } catch {
    return null;
  }
}

export function logout(): void {
  localStorage.removeItem(TOKEN_STORAGE_KEY);
  localStorage.removeItem(SESSION_STORAGE_KEY);
}
