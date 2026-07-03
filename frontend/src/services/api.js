import axios from 'axios';

const BASE_URL = '';

const api = axios.create({
  baseURL: `${BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
});

const authApi = axios.create({
  baseURL: '/auth',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
});


api.interceptors.request.use((config) => {
  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };

  const csrfToken = getCookie('XSRF-TOKEN');
  
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }

  return config;
}, (error) => {
  return Promise.reject(error);
});

authApi.interceptors.request.use((config) => {
  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
  };
  const csrfToken = getCookie('XSRF-TOKEN');
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;
  }
  return config;
}, (error) => Promise.reject(error));

export const authService = {
  register: (registerRequest) => authApi.post(`/register`, registerRequest),

  login: (loginRequest) => authApi.post(`/login`, loginRequest),

  logout: () => authApi.post(`/logout`),

  getCurrentUser: (username) => authApi.get(`/me`, { params: { username } }),

  deleteUser: (username) => authApi.delete(`/delete/${username}`),
};

export const appointmentService = {
  create: (appointmentDto) => api.post('/appointments', appointmentDto),

  getAvailableSlots: (medicalServiceId, date, doctorId) => {
    const params = { medicalServiceId, date };
    if (doctorId) params.doctorId = doctorId;
    return api.get('/appointments/available-times', { params });
  },

  getByPatientId: (patientId, params) => api.get(`/appointments/patient/${patientId}`, { params }),

  getByDoctorId: (doctorId, params) => api.get(`/appointments/doctor/${doctorId}`, { params }),

  update: (id, newDateISOString) => 
    api.put(`/appointments/${id}`, null, { params: { appointmentFrom: newDateISOString } }),

  submitFeedback: (appointmentId, rating) => 
    api.post(`/appointments/${appointmentId}/feedback`, null, { params: { rating } }),

  delete: (id) => api.delete(`/appointments/${id}`),
};

export const patientService = {
  getPaged: (params) => api.get('/patients', { params }),

  getAll: () => api.get('/patients/all'), 

  getById: (id) => api.get(`/patients/${id}`),

  create: (patientDto) => api.post('/patients', patientDto),

  update: (id, patientDto) => api.put(`/patients/${id}`, patientDto),

  delete: (id) => api.delete(`/patients/${id}`),
};

export const doctorService = {
  getAll: () => api.get('/doctors'),

  getPaged: (params) => api.get('/doctors/paged', { params }), 

  getById: (id) => api.get(`/doctors/${id}`),

  create: (doctorDto) => api.post('/doctors', doctorDto),

  update: (id, doctorDto) => api.put(`/doctors/${id}`, doctorDto),

  delete: (id) => api.delete(`/doctors/${id}`),
};

export const doctorScheduleService = {
  getScheduleForDay: (doctorId, dateString) => 
    api.get('/doctor-schedule/day', { params: { doctorId, date: dateString } }),
  
  getByDoctorId: (doctorId) => 
    api.get(`/doctor-schedule/pto/${doctorId}`),

  schedulePTO: (ptoDto) => api.post('/doctor-schedule/schedulePTO', ptoDto),

  updatePTO: (ptoId, ptoDto) => api.put(`/doctor-schedule/PTO/${ptoId}`, ptoDto),

  deletePTO: (ptoId) => api.delete(`/doctor-schedule/PTO/${ptoId}`),
};

export const medicalServiceService = {
  getAll: () => api.get('/medical-services'),

  getById: (id) => api.get(`/medical-services/${id}`),

  getBySpecialization: (specialization) => api.get(`/medical-services/specialization/${specialization}`),

  create: (medicalServiceDto) => api.post('/medical-services', medicalServiceDto),

  update: (id, medicalServiceDto) => api.put(`/medical-services/${id}`, medicalServiceDto),

  delete: (id) => api.delete(`/medical-services/${id}`),
};

export const insuranceProviderService = {

  getPaged: (params) => api.get('/insurance-providers', { params }), 

  getAll: () => api.get('/insurance-providers/all'), 

  getById: (id) => api.get(`/insurance-providers/${id}`),

  create: (providerDto) => api.post('/insurance-providers', providerDto),

  update: (id, providerDto) => api.put(`/insurance-providers/${id}`, providerDto),

  delete: (id) => api.delete(`/insurance-providers/${id}`),
};

export const paymentService = {
  getAll: () => api.get('/payments'),

  getById: (id) => api.get(`/payments/${id}`),

  getByPatientId: (patientId) => api.get(`/payments/patient/${patientId}`),

  create: (paymentDto) => api.post('/payments', paymentDto),

  update: (id, paymentDto) => api.put(`/payments/${id}`, paymentDto),

  delete: (id) => api.delete(`/payments/${id}`),
};

export const serviceCoverageService = {
  getAll: () => api.get('/service-coverages'),

  create: (coverageDto) => api.post('/service-coverages', coverageDto),

  update: (id, coverageDto) => api.put(`/service-coverages/${id}`, coverageDto),

  delete: (id) => api.delete(`/service-coverages/${id}`),
};

export default api;