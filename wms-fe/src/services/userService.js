import axiosClient from '../api/axiosClient';

const userService = {
  getAll: () => axiosClient.get('/users'),
  create: (data) => axiosClient.post('/users', data),
  update: (id, data) => axiosClient.put(`/users/${id}`, data),
  getRoles: () => axiosClient.get('/roles'),
};

export default userService;
