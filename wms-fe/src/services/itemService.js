import axiosClient from '../api/axiosClient';

const itemService = {
  getAll: (params) => {
    return axiosClient.get('/items', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/items/${id}`);
  },
  
  create: (data) => {
    return axiosClient.post('/items', data);
  },
  
  update: (id, data) => {
    return axiosClient.put(`/items/${id}`, data);
  },

  uploadImage: (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosClient.put(`/items/${id}/image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  deleteImage: (id) => axiosClient.delete(`/items/${id}/image`),
  
  delete: (id) => {
    return axiosClient.delete(`/items/${id}`);
  },
};

export default itemService;
