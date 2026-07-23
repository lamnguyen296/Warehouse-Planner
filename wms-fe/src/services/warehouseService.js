import axiosClient from '../api/axiosClient';

const warehouseService = {
  getAll: (params) => {
    return axiosClient.get('/warehouses', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/warehouses/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/warehouses', data);
  },
  
  update: (id, data) => {
    return axiosClient.put(`/warehouses/${id}`, data);
  },
  
  delete: (id) => {
    return axiosClient.delete(`/warehouses/${id}`);
  }
};

export default warehouseService;
