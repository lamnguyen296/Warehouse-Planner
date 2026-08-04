import axios from 'axios';
import toast from 'react-hot-toast';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081/wms';
export const SESSION_EXPIRED_EVENT = 'wms:session-expired';

const clientOptions = {
  baseURL,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
  headers: {
    'Content-Type': 'application/json',
  },
};

const axiosClient = axios.create({ ...clientOptions });
const refreshClient = axios.create({ ...clientOptions });
let refreshPromise = null;
let csrfHeader = null;

const notifySessionExpired = () => window.dispatchEvent(new Event(SESSION_EXPIRED_EVENT));
export const setCsrfToken = (csrfToken) => {
  csrfHeader = csrfToken?.headerName && csrfToken?.token
    ? { name: csrfToken.headerName, value: csrfToken.token }
    : null;
};
const csrfHeaders = () => csrfHeader
  ? { [csrfHeader.name]: csrfHeader.value }
  : {};
const rotateSession = () => refreshClient.post('/auth/refresh', null, {
  skipGlobalError: true,
  headers: csrfHeaders(),
});
const requestTokenRefresh = () => {
  if (typeof navigator !== 'undefined' && navigator.locks?.request) {
    return navigator.locks.request('wms-auth-refresh', rotateSession);
  }
  return rotateSession();
};

axiosClient.interceptors.request.use((config) => {
  const method = config.method?.toUpperCase();
  if (csrfHeader && !['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    config.headers[csrfHeader.name] = csrfHeader.value;
  }
  return config;
});

// Interceptor for Response
axiosClient.interceptors.response.use(
  (response) => {
    // If backend wraps data in ApiResponse { code, message, result }
    if (response.data && response.data.result !== undefined) {
      return response.data.result;
    }
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;
    const isAuthRequest = originalRequest?.url?.startsWith('/auth/');

    if (error.response?.status === 401 && !isAuthRequest && !originalRequest?._retry) {
      originalRequest._retry = true;
      try {
        if (!refreshPromise) {
          refreshPromise = requestTokenRefresh()
            .finally(() => {
              refreshPromise = null;
            });
        }
        await refreshPromise;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        notifySessionExpired();
        return Promise.reject(refreshError);
      }
    }

    if (error.response?.status === 401 && !isAuthRequest) {
      notifySessionExpired();
    }

    if (!originalRequest?.skipGlobalError) {
      const payload = error.response?.data;
      const message = payload?.message || payload?.error ||
        (error.response?.status === 403 ? 'You do not have permission to perform this action' : 'Unable to complete the request');
      toast.error(message);
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
