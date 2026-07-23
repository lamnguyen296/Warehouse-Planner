import axiosClient from '../api/axiosClient';

const locationService = {
  getAll: (params) => {
    return axiosClient.get('/locations', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/locations/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/locations', data);
  },
  
  update: (id, data) => {
    return axiosClient.put(`/locations/${id}`, data);
  },
  
  delete: (id) => {
    return axiosClient.delete(`/locations/${id}`);
  }
};

export default locationService;
