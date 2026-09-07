import { useState, useEffect } from 'react';
import contactService from '../services/contactService';

export const useContacts = (params) => {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchContacts = async () => {
      setLoading(true);
      setError(null);
      try {
        const response = await contactService.getAll(params);
        setContacts(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to fetch contacts');
      } finally {
        setLoading(false);
      }
    };

    fetchContacts();
  }, [JSON.stringify(params)]); // eslint-disable-line react-hooks/exhaustive-deps

  return { contacts, loading, error, setContacts };
};
