import api from './api';

const contactService = {
  getAll:  (params) => api.get('/contacts', { params }),
  getById: (id)     => api.get(`/contacts/${id}`),
  create:  (data)   => api.post('/contacts', data),
  update:  (id, data) => api.put(`/contacts/${id}`, data),
  delete:  (id)     => api.delete(`/contacts/${id}`),

  exportCsv: () =>
    api.get('/contacts/export', { responseType: 'blob' }),

  importCsv: (file) => {
    const form = new FormData();
    form.append('file', file);
    return api.post('/contacts/import', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export default contactService;
