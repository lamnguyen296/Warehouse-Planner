import axiosClient from '../api/axiosClient';

const recycleService = {
  getAll: (params) => {
    return axiosClient.get('/recycle-orders', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/recycle-orders/${id}`);
  },

  create: (data) => {
    return axiosClient.post('/recycle-orders', data);
  },
  
  startRecycle: (id) => {
    return axiosClient.post(`/recycle-orders/${id}/start`);
  },

  cancel: (id) => axiosClient.post(`/recycle-orders/${id}/cancel`),

  completeOrder: (id, actualYield) => {
    return axiosClient.post(`/inventory-ops/recycle-orders/${id}/complete`, { actualYield });
  }
};

export default recycleService;
