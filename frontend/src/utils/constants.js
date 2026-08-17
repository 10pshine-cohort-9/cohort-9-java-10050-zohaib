/**
 * Application-wide constants.
 */

export const API_BASE_URL = '/api';

export const ROUTES = {
  LOGIN: '/login',
  REGISTER: '/register',
  CONTACTS: '/contacts',
  CONTACT_NEW: '/contacts/new',
  CONTACT_DETAIL: (id) => `/contacts/${id}`,
  CONTACT_EDIT: (id) => `/contacts/${id}/edit`,
};

export const PAGINATION = {
  DEFAULT_PAGE: 0,
  DEFAULT_PAGE_SIZE: 10,
};
