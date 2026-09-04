import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import contactService from '../services/contactService';
import styles from './ContactDetailPage.module.css';

const ContactDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [contact, setContact]   = useState(null);
  const [loading, setLoading]   = useState(true);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await contactService.getById(id);
        setContact(res.data);
      } catch {
        toast.error('Contact not found.');
        navigate('/contacts');
      } finally { setLoading(false); }
    };
    fetch();
  }, [id, navigate]);

  const handleDelete = async () => {
    if (!window.confirm(`Delete "${contact.firstName} ${contact.lastName}"?`)) return;
    setDeleting(true);
    try {
      await contactService.delete(id);
      toast.success('Contact deleted.');
      navigate('/contacts');
    } catch {
      toast.error('Failed to delete contact.');
      setDeleting(false);
    }
  };

  if (loading) return <div className={styles.state}>Loading...</div>;
  if (!contact) return null;

  const fullName = `${contact.firstName} ${contact.lastName}`.trim();
  const initials = `${contact.firstName?.[0] ?? ''}${contact.lastName?.[0] ?? ''}`.toUpperCase();

  return (
    <div className={styles.page}>
      <div className={styles.topBar}>
        <Link to="/contacts" className={styles.backLink}>Back to Contacts</Link>
        <div className={styles.topActions}>
          <Link to={`/contacts/${id}/edit`} className={styles.editBtn}>Edit</Link>
          <button className={styles.deleteBtn} onClick={handleDelete} disabled={deleting}>
            {deleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>

      <div className={styles.card}>
        <div className={styles.profileHeader}>
          <div className={styles.avatar}>{initials}</div>
          <div>
            <h1 className={styles.fullName}>{fullName}</h1>
            {contact.title   && <p className={styles.sub}>{contact.title}</p>}
            {contact.company && <p className={styles.sub}>{contact.company}</p>}
          </div>
        </div>

        <div className={styles.sections}>
          {contact.phones?.length > 0 && (
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>Phone Numbers</h2>
              <ul className={styles.itemList}>
                {contact.phones.map((p) => (
                  <li key={p.id} className={styles.item}>
                    <span className={styles.itemLabel}>{p.label}</span>
                    <a href={`tel:${p.number}`} className={styles.itemValue}>{p.number}</a>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {contact.emails?.length > 0 && (
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>Email Addresses</h2>
              <ul className={styles.itemList}>
                {contact.emails.map((e) => (
                  <li key={e.id} className={styles.item}>
                    <span className={styles.itemLabel}>{e.label}</span>
                    <a href={`mailto:${e.address}`} className={styles.itemValue}>{e.address}</a>
                  </li>
                ))}
              </ul>
            </section>
          )}

          {contact.address && (
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>Address</h2>
              <p className={styles.plainText}>{contact.address}</p>
            </section>
          )}

          {contact.notes && (
            <section className={styles.section}>
              <h2 className={styles.sectionTitle}>Notes</h2>
              <p className={styles.plainText}>{contact.notes}</p>
            </section>
          )}
        </div>

        {contact.createdAt && (
          <p className={styles.timestamps}>
            Added {new Date(contact.createdAt).toLocaleDateString()}
            {contact.updatedAt && ` - Updated ${new Date(contact.updatedAt).toLocaleDateString()}`}
          </p>
        )}
      </div>
    </div>
  );
};

export default ContactDetailPage;
