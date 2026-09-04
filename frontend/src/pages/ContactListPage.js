import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { toast } from 'react-toastify';
import contactService from '../services/contactService';
import styles from './ContactListPage.module.css';

const PAGE_SIZE = 10;
const PHONE_LABELS = ['mobile', 'home', 'work', 'other'];
const EMAIL_LABELS = ['personal', 'work', 'other'];

const EMPTY_FORM = {
  firstName: '', lastName: '', title: '', address: '', notes: '',
  phones: [{ label: 'mobile', number: '' }],
  emails: [{ label: 'personal', address: '' }],
};

const ContactForm = ({ onSave, onCancel, saving }) => {
  const { register, handleSubmit, control, formState: { errors } } = useForm({ mode: 'onBlur' });
  const { fields: pf, append: ap, remove: rp } = useFieldArray({ control, name: 'phones' });
  const { fields: ef, append: ae, remove: re } = useFieldArray({ control, name: 'emails' });

  const onSubmit = (data) => {
    const phones = data.phones.filter(p => p.number?.trim());
    const emails = data.emails.filter(e => e.address?.trim());
    if (!phones.length && !emails.length) {
      toast.error('Add at least one phone number or email address.');
      return;
    }
    onSave({ ...data, phones, emails });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.formBody}>
      <div className={styles.formRow}>
        <div className={styles.formField}>
          <label className={styles.formLabel}>First Name *</label>
          <input type="text"
            className={`${styles.formInput} ${errors.firstName ? styles.formInputErr : ''}`}
            {...register('firstName', { required: 'Required' })} />
          {errors.firstName && <span className={styles.formErr}>{errors.firstName.message}</span>}
        </div>
        <div className={styles.formField}>
          <label className={styles.formLabel}>Last Name *</label>
          <input type="text"
            className={`${styles.formInput} ${errors.lastName ? styles.formInputErr : ''}`}
            {...register('lastName', { required: 'Required' })} />
          {errors.lastName && <span className={styles.formErr}>{errors.lastName.message}</span>}
        </div>
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Title</label>
        <input type="text" className={styles.formInput}
          placeholder="e.g. Software Engineer" {...register('title')} />
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Address</label>
        <input type="text" className={styles.formInput} {...register('address')} />
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Notes</label>
        <textarea rows={2} className={styles.formTextarea} {...register('notes')} />
      </div>

      <fieldset className={styles.formFieldset}>
        <legend className={styles.formLegend}>
          Phone Numbers <span className={styles.reqNote}>(at least one phone or email required)</span>
        </legend>
        {pf.map((f, i) => (
          <div key={f.id} className={styles.multiRow}>
            <select className={styles.formSelect} {...register(`phones.${i}.label`)}>
              {PHONE_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
            <input type="tel" className={styles.formInput}
              placeholder="+1 555 000 0000"
              {...register(`phones.${i}.number`, {
                pattern: { value: /^\+?[0-9\s\-().]{6,30}$/, message: 'Invalid phone' }
              })} />
            <button type="button" className={styles.removeBtn} onClick={() => rp(i)}>&#x2715;</button>
          </div>
        ))}
        <button type="button" className={styles.addBtn}
          onClick={() => ap({ label: 'mobile', number: '' })}>+ Add Phone</button>
      </fieldset>

      <fieldset className={styles.formFieldset}>
        <legend className={styles.formLegend}>Email Addresses</legend>
        {ef.map((f, i) => (
          <div key={f.id} className={styles.multiRow}>
            <select className={styles.formSelect} {...register(`emails.${i}.label`)}>
              {EMAIL_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
            <input type="email" className={styles.formInput}
              placeholder="email@example.com"
              {...register(`emails.${i}.address`, {
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email' }
              })} />
            <button type="button" className={styles.removeBtn} onClick={() => re(i)}>&#x2715;</button>
          </div>
        ))}
        <button type="button" className={styles.addBtn}
          onClick={() => ae({ label: 'personal', address: '' })}>+ Add Email</button>
      </fieldset>

      <div className={styles.modalActions}>
        <button type="button" className={styles.cancelBtn} onClick={onCancel}>Cancel</button>
        <button type="submit" className={styles.saveBtn} disabled={saving}>
          {saving ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  );
};

const EditForm = ({ contact, onSave, onCancel, saving }) => {
  const defaultValues = {
    firstName: contact.firstName || '',
    lastName:  contact.lastName  || '',
    title:     contact.title     || '',
    address:   contact.address   || '',
    notes:     contact.notes     || '',
    phones: contact.phones?.length ? contact.phones : [{ label: 'mobile', number: '' }],
    emails: contact.emails?.length ? contact.emails : [{ label: 'personal', address: '' }],
  };

  const { register, handleSubmit, control, formState: { errors } } =
    useForm({ mode: 'onBlur', defaultValues });
  const { fields: pf, append: ap, remove: rp } = useFieldArray({ control, name: 'phones' });
  const { fields: ef, append: ae, remove: re } = useFieldArray({ control, name: 'emails' });

  const onSubmit = (data) => {
    const phones = data.phones.filter(p => p.number?.trim());
    const emails = data.emails.filter(e => e.address?.trim());
    if (!phones.length && !emails.length) {
      toast.error('Add at least one phone number or email address.');
      return;
    }
    onSave({ ...data, phones, emails });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className={styles.formBody}>
      <div className={styles.formRow}>
        <div className={styles.formField}>
          <label className={styles.formLabel}>First Name *</label>
          <input type="text"
            className={`${styles.formInput} ${errors.firstName ? styles.formInputErr : ''}`}
            {...register('firstName', { required: 'Required' })} />
          {errors.firstName && <span className={styles.formErr}>{errors.firstName.message}</span>}
        </div>
        <div className={styles.formField}>
          <label className={styles.formLabel}>Last Name *</label>
          <input type="text"
            className={`${styles.formInput} ${errors.lastName ? styles.formInputErr : ''}`}
            {...register('lastName', { required: 'Required' })} />
          {errors.lastName && <span className={styles.formErr}>{errors.lastName.message}</span>}
        </div>
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Title</label>
        <input type="text" className={styles.formInput}
          placeholder="e.g. Software Engineer" {...register('title')} />
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Address</label>
        <input type="text" className={styles.formInput} {...register('address')} />
      </div>

      <div className={styles.formField}>
        <label className={styles.formLabel}>Notes</label>
        <textarea rows={2} className={styles.formTextarea} {...register('notes')} />
      </div>

      <fieldset className={styles.formFieldset}>
        <legend className={styles.formLegend}>
          Phone Numbers <span className={styles.reqNote}>(at least one phone or email required)</span>
        </legend>
        {pf.map((f, i) => (
          <div key={f.id} className={styles.multiRow}>
            <select className={styles.formSelect} {...register(`phones.${i}.label`)}>
              {PHONE_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
            <input type="tel" className={styles.formInput}
              placeholder="+1 555 000 0000"
              {...register(`phones.${i}.number`, {
                pattern: { value: /^\+?[0-9\s\-().]{6,30}$/, message: 'Invalid phone' }
              })} />
            <button type="button" className={styles.removeBtn} onClick={() => rp(i)}>&#x2715;</button>
          </div>
        ))}
        <button type="button" className={styles.addBtn}
          onClick={() => ap({ label: 'mobile', number: '' })}>+ Add Phone</button>
      </fieldset>

      <fieldset className={styles.formFieldset}>
        <legend className={styles.formLegend}>Email Addresses</legend>
        {ef.map((f, i) => (
          <div key={f.id} className={styles.multiRow}>
            <select className={styles.formSelect} {...register(`emails.${i}.label`)}>
              {EMAIL_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
            </select>
            <input type="email" className={styles.formInput}
              placeholder="email@example.com"
              {...register(`emails.${i}.address`, {
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email' }
              })} />
            <button type="button" className={styles.removeBtn} onClick={() => re(i)}>&#x2715;</button>
          </div>
        ))}
        <button type="button" className={styles.addBtn}
          onClick={() => ae({ label: 'personal', address: '' })}>+ Add Email</button>
      </fieldset>

      <div className={styles.modalActions}>
        <button type="button" className={styles.cancelBtn} onClick={onCancel}>Cancel</button>
        <button type="submit" className={styles.saveBtn} disabled={saving}>
          {saving ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </form>
  );
};

const Modal = ({ title, onClose, children }) => (
  <div className={styles.overlay} role="dialog" aria-modal="true" aria-label={title}>
    <div className={styles.modal}>
      <div className={styles.modalHeader}>
        <h2 className={styles.modalTitle}>{title}</h2>
        <button className={styles.modalClose} onClick={onClose} aria-label="Close">&#x2715;</button>
      </div>
      <div className={styles.modalContent}>{children}</div>
    </div>
  </div>
);

const ContactListPage = () => {
  const [contacts, setContacts]     = useState([]);
  const [totalElements, setTotal]   = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage]             = useState(0);
  const [searchInput, setSearch]    = useState('');
  const [activeSearch, setActive]   = useState('');
  const [loading, setLoading]       = useState(false);
  const [saving, setSaving]         = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [editContact, setEditContact] = useState(null);
  const [deleteContact, setDeleteContact] = useState(null);
  const [loadingEdit, setLoadingEdit] = useState(false);
  const [deleting, setDeleting]     = useState(false);

  const [exporting, setExporting]   = useState(false);
  const [importing, setImporting]   = useState(false);
  const importInputRef              = useRef(null);
  const timer                       = useRef(null);

  const loadContacts = useCallback(async (pageNum, query) => {
    setLoading(true);
    try {
      const params = { page: pageNum, size: PAGE_SIZE, sortBy: 'lastName', sortDir: 'asc' };
      if (query) params.search = query;
      const res = await contactService.getAll(params);
      setContacts(res.data.content);
      setTotal(res.data.totalElements);
      setTotalPages(res.data.totalPages);
    } catch {
      toast.error('Failed to load contacts.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadContacts(page, activeSearch); }, [page, activeSearch, loadContacts]);

  const handleSearch = (e) => {
    const val = e.target.value;
    setSearch(val);
    clearTimeout(timer.current);
    timer.current = setTimeout(() => { setPage(0); setActive(val.trim()); }, 400);
  };

  const openEdit = async (id) => {
    setLoadingEdit(true);
    try {
      const res = await contactService.getById(id);
      setEditContact(res.data);
    } catch {
      toast.error('Failed to load contact details.');
    } finally {
      setLoadingEdit(false);
    }
  };

  const handleCreate = async (data) => {
    setSaving(true);
    try {
      const payload = buildPayload(data);
      await contactService.create(payload);
      toast.success('Contact created.');
      setCreateOpen(false);
      loadContacts(page, activeSearch);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create contact.');
    } finally { setSaving(false); }
  };

  const handleUpdate = async (data) => {
    setSaving(true);
    try {
      const payload = buildPayload(data, editContact);
      await contactService.update(editContact.id, payload);
      toast.success('Contact updated.');
      setEditContact(null);
      loadContacts(page, activeSearch);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update contact.');
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await contactService.delete(deleteContact.id);
      toast.success(`"${deleteContact.firstName} ${deleteContact.lastName}" deleted.`);
      setDeleteContact(null);
      const newPage = contacts.length === 1 && page > 0 ? page - 1 : page;
      setPage(newPage);
      loadContacts(newPage, activeSearch);
    } catch {
      toast.error('Failed to delete contact.');
    } finally { setDeleting(false); }
  };

  const buildPayload = (data, existing) => ({
    firstName: data.firstName.trim(),
    lastName:  data.lastName.trim(),
    title:     data.title?.trim()   || null,
    company:   existing?.company    || null,
    address:   data.address?.trim() || null,
    notes:     data.notes?.trim()   || null,
    phones: data.phones.filter(p => p.number?.trim())
                       .map(p => ({ id: p.id || null, label: p.label, number: p.number.trim() })),
    emails: data.emails.filter(e => e.address?.trim())
                       .map(e => ({ id: e.id || null, label: e.label, address: e.address.trim() })),
  });

  const handleExport = async () => {
    setExporting(true);
    try {
      const res = await contactService.exportCsv();
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'text/csv' }));
      const a   = document.createElement('a');
      a.href    = url;
      a.download = `contacts_${new Date().toISOString().slice(0, 10)}.csv`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success('Contacts exported successfully.');
    } catch {
      toast.error('Failed to export contacts.');
    } finally {
      setExporting(false);
    }
  };

  const handleImportFile = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    e.target.value = '';
    setImporting(true);
    try {
      const res = await contactService.importCsv(file);
      toast.success(res.data.message);
      if (res.data.errors?.length > 0) {
        res.data.errors.forEach(err => toast.warn(err, { autoClose: 6000 }));
      }
      setPage(0);
      loadContacts(0, activeSearch);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to import contacts.');
    } finally {
      setImporting(false);
    }
  };

  return (
    <div className={styles.page}>

      <div className={styles.header}>
        <h1 className={styles.title}>Contacts</h1>
        <div className={styles.headerActions}>
          <button
            className={styles.exportBtn}
            onClick={handleExport}
            disabled={exporting}
            title="Export all contacts to CSV"
          >
            {exporting ? 'Exporting...' : 'Export CSV'}
          </button>
          <button
            className={styles.importBtn}
            onClick={() => importInputRef.current?.click()}
            disabled={importing}
            title="Import contacts from a CSV file"
          >
            {importing ? 'Importing...' : 'Import CSV'}
          </button>
          <input
            ref={importInputRef}
            type="file"
            accept=".csv"
            style={{ display: 'none' }}
            onChange={handleImportFile}
            aria-label="Import CSV file"
          />
          <button className={styles.newBtn} onClick={() => setCreateOpen(true)}>
            + New Contact
          </button>
        </div>
      </div>

      <div className={styles.toolbar}>
        <input
          type="search"
          className={styles.searchInput}
          placeholder="Search by first name, last name, email or phone..."
          value={searchInput}
          onChange={handleSearch}
          aria-label="Search contacts"
        />
        {totalElements > 0 && (
          <span className={styles.count}>
            {totalElements} contact{totalElements !== 1 ? 's' : ''}
          </span>
        )}
      </div>

      {loading ? (
        <div className={styles.state}>Loading...</div>
      ) : contacts.length === 0 ? (
        <div className={styles.state}>
          {activeSearch
            ? `No contacts matching "${activeSearch}".`
            : 'No contacts yet. Click "+ New Contact" to add one!'}
        </div>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Title</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {contacts.map((c) => (
                <tr key={c.id} className={styles.row}>
                  <td className={styles.nameCell}>
                    <Link to={`/contacts/${c.id}`} className={styles.nameLink}>
                      {c.firstName} {c.lastName}
                    </Link>
                  </td>
                  <td>{c.title || <span className={styles.empty}>-</span>}</td>
                  <td>{c.primaryPhone || <span className={styles.empty}>-</span>}</td>
                  <td>{c.primaryEmail || <span className={styles.empty}>-</span>}</td>
                  <td className={styles.actions}>
                    <Link to={`/contacts/${c.id}`} className={styles.viewBtn}>View</Link>
                    <button
                      className={styles.editBtn}
                      onClick={() => openEdit(c.id)}
                      disabled={loadingEdit}
                    >
                      Edit
                    </button>
                    <button
                      className={styles.deleteBtn}
                      onClick={() => setDeleteContact(c)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div className={styles.pagination}>
          <button
            className={styles.arrowBtn}
            onClick={() => setPage((p) => p - 1)}
            disabled={page === 0}
            aria-label="Previous page"
          >
            &#8592;
          </button>
          <div className={styles.pageNumbers}>
            {Array.from({ length: totalPages }, (_, i) => i).map((p) => (
              <button
                key={p}
                className={`${styles.pageNumBtn} ${p === page ? styles.pageNumActive : ''}`}
                onClick={() => setPage(p)}
                aria-label={`Page ${p + 1}`}
                aria-current={p === page ? 'page' : undefined}
              >
                {p + 1}
              </button>
            ))}
          </div>
          <button
            className={styles.arrowBtn}
            onClick={() => setPage((p) => p + 1)}
            disabled={page >= totalPages - 1}
            aria-label="Next page"
          >
            &#8594;
          </button>
        </div>
      )}

      {createOpen && (
        <Modal title="Create New Contact" onClose={() => setCreateOpen(false)}>
          <ContactForm
            onSave={handleCreate}
            onCancel={() => setCreateOpen(false)}
            saving={saving}
          />
        </Modal>
      )}

      {editContact && (
        <Modal title="Update Contact" onClose={() => setEditContact(null)}>
          <EditForm
            contact={editContact}
            onSave={handleUpdate}
            onCancel={() => setEditContact(null)}
            saving={saving}
          />
        </Modal>
      )}

      {deleteContact && (
        <Modal title="Delete Contact" onClose={() => setDeleteContact(null)}>
          <div className={styles.deleteConfirm}>
            <div className={styles.deleteIcon}>&#9888;</div>
            <p className={styles.deleteMsg}>
              Are you sure you want to delete{' '}
              <strong>{deleteContact.firstName} {deleteContact.lastName}</strong>?
              <br />
              <span className={styles.deleteWarn}>This action cannot be undone.</span>
            </p>
            <div className={styles.modalActions}>
              <button
                className={styles.cancelBtn}
                onClick={() => setDeleteContact(null)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                className={styles.confirmDeleteBtn}
                onClick={handleDelete}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Yes, Delete'}
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default ContactListPage;
