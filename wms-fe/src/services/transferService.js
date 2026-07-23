import axiosClient from '../api/axiosClient';

const transferService = {
  getAll: (params) => {
    return axiosClient.get('/transfer-orders', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/transfer-orders/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/transfer-orders', data);
  },
  
  execute: (id) => axiosClient.post(`/transfer-orders/${id}/execute`),
  complete: (id, locationId) => axiosClient.post(`/transfer-orders/${id}/complete`, null, {
    params: locationId ? { locationId } : undefined,
  }),
  cancel: (id) => axiosClient.post(`/transfer-orders/${id}/cancel`),
};

export default transferService;
