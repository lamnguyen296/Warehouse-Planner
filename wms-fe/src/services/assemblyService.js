import axiosClient from '../api/axiosClient';

const assemblyService = {
  getAll: (params) => {
    return axiosClient.get('/assembly-orders', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/assembly-orders/${id}`);
  },

  create: (data) => {
    return axiosClient.post('/assembly-orders', data);
  },
  
  startAssembly: (id) => {
    return axiosClient.post(`/assembly-orders/${id}/start`);
  },

  cancel: (id) => axiosClient.post(`/assembly-orders/${id}/cancel`),

  completeOrderPayload: (id, payload) => {
    return axiosClient.post(`/inventory-ops/assembly-orders/${id}/complete`, payload);
  }
};

export default assemblyService;
