import axiosClient from '../api/axiosClient';

const notificationService = {
  getAll: () => axiosClient.get('/notifications'),
  getUnreadCount: () => axiosClient.get('/notifications/unread-count'),
  markAsRead: (id) => axiosClient.put(`/notifications/${id}/read`),
  markAllAsRead: () => axiosClient.put('/notifications/read-all'),
};

export default notificationService;
