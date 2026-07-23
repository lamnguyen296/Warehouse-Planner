import axios from 'axios';
import toast from 'react-hot-toast';
import { clearSession, getAccessToken, getRefreshToken, saveSession } from '../auth/session';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081/wms';
const axiosClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor for Request (Add Token)
axiosClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor for Response
axiosClient.interceptors.response.use(
  (response) => {
    // If backend wraps data in ApiResponse { code, message, result }
    if (response.data && response.data.result !== undefined) {
      return response.data.result;
    }
    return response.data;
  },
  (error) => {
    const originalRequest = error.config;
    const isAuthRequest = originalRequest?.url?.startsWith('/auth/');

    if (error.response?.status === 401 && !isAuthRequest && !originalRequest?._retry && getRefreshToken()) {
      originalRequest._retry = true;
      return axios.post(`${baseURL}/auth/refresh`, { refreshToken: getRefreshToken() })
        .then((response) => {
          const refreshed = response.data?.result || response.data;
          saveSession(refreshed);
          originalRequest.headers.Authorization = `Bearer ${refreshed.accessToken}`;
          return axiosClient(originalRequest);
        })
        .catch((refreshError) => {
          clearSession();
          if (window.location.pathname !== '/login') window.location.assign('/login');
          return Promise.reject(refreshError);
        });
    }

    if (error.response?.status === 401 && !isAuthRequest) {
      clearSession();
      if (window.location.pathname !== '/login') window.location.assign('/login');
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
