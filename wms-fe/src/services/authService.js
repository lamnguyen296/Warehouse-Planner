import axiosClient from '../api/axiosClient';

const authService = {
  login: (username, password) => {
    return axiosClient.post('/auth/token', { username, password }, { skipGlobalError: true });
  },

  refresh: (refreshToken) => axiosClient.post('/auth/refresh', { refreshToken }, { skipGlobalError: true }),

  logout: (accessToken) => axiosClient.post('/auth/logout', { accessToken }, { skipGlobalError: true }),

  myInfo: () => axiosClient.get('/users/myInfo', { skipGlobalError: true }),
};

export default authService;
