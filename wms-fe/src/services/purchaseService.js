import axiosClient from '../api/axiosClient';

const purchaseService = {
  getAll: (params) => {
    return axiosClient.get('/purchase-requests', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/purchase-requests/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/purchase-requests', data);
  },

  approve: (id) => {
    return axiosClient.post(`/purchase-requests/${id}/approve`);
  },

  order: (id) => {
    return axiosClient.post(`/purchase-requests/${id}/order`);
  },

  cancel: (id) => axiosClient.post(`/purchase-requests/${id}/cancel`),
  
  receiveGoods: (id, payload) => {
    return axiosClient.post(`/inventory-ops/purchase-requests/${id}/receive`, payload);
  }
};

export default purchaseService;
