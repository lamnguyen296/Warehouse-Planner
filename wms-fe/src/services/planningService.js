import axiosClient from '../api/axiosClient';

const planningService = {
  getAll: (params) => {
    return axiosClient.get('/plannings', { params });
  },
  
  getById: (id) => {
    return axiosClient.get(`/plannings/${id}`);
  },
  
  generateFromWorkshop: (workshopRequestId) => {
    return axiosClient.post(`/plannings/run-engine/${workshopRequestId}`);
  },

  approvePlanning: (id) => {
    return axiosClient.post(`/plannings/${id}/approve`);
  },

  startExecution: (id) => axiosClient.post(`/plannings/${id}/start-execution`),
  failPlanning: (id) => axiosClient.post(`/plannings/${id}/fail`),
  getByWorkshopRequest: (workshopRequestId) =>
    axiosClient.get(`/plannings/by-workshop-request/${workshopRequestId}`),
};

export default planningService;
