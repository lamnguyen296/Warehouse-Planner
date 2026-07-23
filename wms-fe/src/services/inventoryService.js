import axiosClient from '../api/axiosClient';

const inventoryService = {
  getAll: (params) => {
    return axiosClient.get('/inventory', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/inventory/${id}`);
  },
  
  create: (data) => axiosClient.post('/inventory', data),
  update: (id, data) => axiosClient.put(`/inventory/${id}`, data),
  delete: (id) => axiosClient.delete(`/inventory/${id}`),
  getTransactions: () => axiosClient.get('/inventory-transactions'),
  getTransactionAttachments: (transactionId) =>
    axiosClient.get(`/inventory-transactions/${transactionId}/attachments`),
  uploadTransactionAttachment: (transactionId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosClient.post(`/inventory-transactions/${transactionId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  deleteTransactionAttachment: (transactionId, attachmentId) =>
    axiosClient.delete(`/inventory-transactions/${transactionId}/attachments/${attachmentId}`),
  getReservations: () => axiosClient.get('/inventory-reservations'),
};

export default inventoryService;
