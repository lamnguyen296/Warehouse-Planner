import axiosClient from '../api/axiosClient';

const workshopService = {
  getAll: (params) => {
    return axiosClient.get('/workshop-requests', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/workshop-requests/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/workshop-requests', data);
  },
  
  update: (id, data) => {
    return axiosClient.put(`/workshop-requests/${id}`, data);
  },
  
  delete: (id) => {
    return axiosClient.delete(`/workshop-requests/${id}`);
  },

  approveRequest: (id) => {
    return axiosClient.post(`/workshop-requests/${id}/approve`);
  },

  submitRequest: (id) => {
    return axiosClient.post(`/workshop-requests/${id}/submit`);
  },

  cancelRequest: (id) => axiosClient.post(`/workshop-requests/${id}/cancel`),
};

export default workshopService;
