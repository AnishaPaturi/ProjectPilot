import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authAPI = {
  register: (name, email, password) => api.post('/auth/register', { name, email, password }),
  login: (email, password) => api.post('/auth/login', { email, password }),
};

export const preferencesAPI = {
  get: (userId) => api.get(`/preferences/${userId}`),
  save: (userId, data) => api.post(`/preferences/${userId}`, data),
};

export const recommendationsAPI = {
  suggestSubdomains: (domain) => api.post('/recommendations/suggest-subdomains', { domain }),
  generate: (userId, subdomain) => api.post(`/recommendations/generate/${userId}`, { subdomain }),
  getSaved: (userId) => api.get(`/recommendations/saved/${userId}`),
  downloadPdfUrl: (paperId) => `${API_BASE_URL}/recommendations/download/${paperId}`,
};

export default api;
