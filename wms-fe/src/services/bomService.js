import axiosClient from '../api/axiosClient';

const bomService = {
  getAll: (params) => {
    return axiosClient.get('/boms', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/boms/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/boms', data);
  },
  
  update: (id, data) => {
    return axiosClient.put(`/boms/${id}`, data);
  },
  
  delete: (id) => {
    return axiosClient.delete(`/boms/${id}`);
  }
};

export default bomService;
