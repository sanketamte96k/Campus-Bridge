import axios, { AxiosError } from 'axios';

const envBaseUrl = import.meta.env.VITE_API_BASE_URL;
const baseURL = typeof envBaseUrl === 'string'
  ? envBaseUrl.trim()
  : (import.meta.env.DEV ? 'http://localhost:8080' : '');

export const apiClient = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('cb_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string; error?: string }>) => {
    if (!error.response) {
      return Promise.reject(new Error("Can't reach the server. Is the backend running?"));
    }

    if (error.response.status === 401) {
      localStorage.removeItem('cb_token');
      localStorage.removeItem('cb_session');

      if (typeof window !== 'undefined') {
        const currentPath = window.location.pathname;
        if (currentPath !== '/login' && currentPath !== '/register') {
          window.location.href = '/login';
        }
      }
    }

    const message =
      error.response.data?.message ||
      error.response.data?.error ||
      'Something went wrong. Try again.';

    return Promise.reject(new Error(message));
  }
);

export default apiClient;
