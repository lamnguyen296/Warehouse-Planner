import axiosClient, { setCsrfToken } from '../api/axiosClient';

const loadCsrfToken = async () => {
  const csrfToken = await axiosClient.get('/auth/csrf', { skipGlobalError: true });
  setCsrfToken(csrfToken);
  return csrfToken;
};

const authService = {
  csrf: loadCsrfToken,

  login: async (username, password) => {
    await loadCsrfToken();
    return axiosClient.post('/auth/token', { username, password }, { skipGlobalError: true });
  },

  refresh: () => axiosClient.post('/auth/refresh', null, { skipGlobalError: true }),

  logout: async () => {
    await loadCsrfToken();
    return axiosClient.post('/auth/logout', null, { skipGlobalError: true });
  },

  myInfo: () => axiosClient.get('/users/myInfo', { skipGlobalError: true }),
};

export default authService;
