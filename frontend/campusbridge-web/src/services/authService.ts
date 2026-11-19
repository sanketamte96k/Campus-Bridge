import type { LoginInput, RegisterInput, Role, User } from '../types';

interface StoredUser extends User {
  password: string;
}

const USERS_STORAGE_KEY = 'cb_users';
const SESSION_STORAGE_KEY = 'cb_session';

const delay = (ms = 400): Promise<void> =>
  new Promise((resolve) => {
    setTimeout(resolve, ms);
  });

function getStoredUsers(): StoredUser[] {
  try {
    const raw = localStorage.getItem(USERS_STORAGE_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as StoredUser[]) : [];
  } catch {
    return [];
  }
}

function saveStoredUsers(users: StoredUser[]): void {
  localStorage.setItem(USERS_STORAGE_KEY, JSON.stringify(users));
}

// Note: These mock methods will later be replaced by Axios calls to the Spring Boot backend with the same signatures.

export async function register(input: RegisterInput): Promise<User> {
  await delay();

  const users = getStoredUsers();
  const normalizedEmail = input.email.trim().toLowerCase();

  const exists = users.some(
    (user) => user.email.toLowerCase() === normalizedEmail
  );

  if (exists) {
    throw new Error('An account with this email already exists.');
  }

  const user: User = {
    id: crypto.randomUUID(),
    name: input.name.trim(),
    email: normalizedEmail,
    role: input.role,
  };

  const storedUser: StoredUser = {
    ...user,
    password: input.password,
  };

  users.push(storedUser);
  saveStoredUsers(users);

  localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
  return user;
}

export async function login(input: LoginInput): Promise<User> {
  await delay();

  const users = getStoredUsers();
  const normalizedEmail = input.email.trim().toLowerCase();

  const matchingUser = users.find(
    (user) =>
      user.email.toLowerCase() === normalizedEmail &&
      user.password === input.password
  );

  if (!matchingUser) {
    throw new Error('Email or password is incorrect.');
  }

  const user: User = {
    id: matchingUser.id,
    name: matchingUser.name,
    email: matchingUser.email,
    role: matchingUser.role as Role,
  };

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
    if (parsed && typeof parsed === 'object' && parsed.id && parsed.email && parsed.role) {
      return parsed;
    }
    return null;
  } catch {
    return null;
  }
}

export function logout(): void {
  localStorage.removeItem(SESSION_STORAGE_KEY);
}
