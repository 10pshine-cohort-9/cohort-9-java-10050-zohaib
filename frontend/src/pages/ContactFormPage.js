import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { toast } from 'react-toastify';
import contactService from '../services/contactService';
import styles from './ContactFormPage.module.css';

const PHONE_LABELS = ['mobile', 'home', 'work', 'other'];
const EMAIL_LABELS = ['personal', 'work', 'other'];

const ContactFormPage = () => {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [loading, setLoading]   = useState(false);
  const [fetching, setFetching] = useState(isEdit);

  const { register, handleSubmit, control, reset, formState: { errors } } = useForm({
    mode: 'onBlur',
    defaultValues: {
      firstName: '', lastName: '', title: '', company: '', address: '', notes: '',
      phones: [{ label: 'mobile', number: '' }],
      emails: [{ label: 'personal', address: '' }],
    },
  });

  const { fields: phoneFields, append: appendPhone, remove: removePhone } =
    useFieldArray({ control, name: 'phones' });
  const { fields: emailFields, append: appendEmail, remove: removeEmail } =
    useFieldArray({ control, name: 'emails' });

  useEffect(() => {
    if (!isEdit) return;
    const fetch = async () => {
      try {
        const res = await contactService.getById(id);
        const c = res.data;
        reset({
          firstName: c.firstName || '', lastName:  c.lastName  || '',
          title:     c.title     || '', company:   c.company   || '',
          address:   c.address   || '', notes:     c.notes     || '',
          phones: c.phones?.length ? c.phones : [{ label: 'mobile', number: '' }],
          emails: c.emails?.length ? c.emails : [{ label: 'personal', address: '' }],
        });
      } catch {
        toast.error('Failed to load contact.');
        navigate('/contacts');
      } finally { setFetching(false); }
    };
    fetch();
  }, [id, isEdit, reset, navigate]);

  const onSubmit = async (data) => {
    const filledPhones = data.phones.filter(p => p.number?.trim());
    const filledEmails = data.emails.filter(e => e.address?.trim());

    if (filledPhones.length === 0 && filledEmails.length === 0) {
      toast.error('Please add at least one phone number or email address.');
      return;
    }

    const payload = {
      firstName: data.firstName.trim(),
      lastName:  data.lastName.trim(),
      title:     data.title?.trim()   || null,
      company:   data.company?.trim() || null,
      address:   data.address?.trim() || null,
      notes:     data.notes?.trim()   || null,
      phones: filledPhones.map(p => ({ id: p.id || null, label: p.label, number: p.number.trim() })),
      emails: filledEmails.map(e => ({ id: e.id || null, label: e.label, address: e.address.trim() })),
    };

    setLoading(true);
    try {
      if (isEdit) {
        await contactService.update(id, payload);
        toast.success('Contact updated.');
        navigate(`/contacts/${id}`);
      } else {
        const res = await contactService.create(payload);
        toast.success('Contact created.');
        navigate(`/contacts/${res.data.id}`);
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save contact.');
    } finally { setLoading(false); }
  };

  if (fetching) return <div className={styles.state}>Loading...</div>;

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>{isEdit ? 'Edit Contact' : 'New Contact'}</h1>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="firstName" className={styles.label}>First Name *</label>
              <input id="firstName" type="text"
                className={`${styles.input} ${errors.firstName ? styles.inputError : ''}`}
                {...register('firstName', { required: 'First name is required' })} />
              {errors.firstName && <span className={styles.err}>{errors.firstName.message}</span>}
            </div>
            <div className={styles.field}>
              <label htmlFor="lastName" className={styles.label}>Last Name *</label>
              <input id="lastName" type="text"
                className={`${styles.input} ${errors.lastName ? styles.inputError : ''}`}
                {...register('lastName', { required: 'Last name is required' })} />
              {errors.lastName && <span className={styles.err}>{errors.lastName.message}</span>}
            </div>
          </div>

          <div className={styles.field}>
              <label htmlFor="title" className={styles.label}>Title</label>
              <input id="title" type="text" className={styles.input}
                placeholder="e.g. Software Engineer"
                {...register('title')} />
            </div>

          <div className={styles.field}>
            <label htmlFor="address" className={styles.label}>Address</label>
            <input id="address" type="text" className={styles.input}
              {...register('address')} />
          </div>

          <div className={styles.field}>
            <label htmlFor="notes" className={styles.label}>Notes</label>
            <textarea id="notes" rows={3} className={styles.textarea}
              {...register('notes')} />
          </div>

          <fieldset className={styles.fieldset}>
            <legend className={styles.legend}>Phone Numbers <span className={styles.required}>*</span></legend>
            <p className={styles.fieldHint}>At least one phone or email is required.</p>
            {phoneFields.map((field, index) => (
              <div key={field.id} className={styles.multiRow}>
                <select className={styles.select}
                  aria-label={`Phone label ${index + 1}`}
                  {...register(`phones.${index}.label`)}>
                  {PHONE_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
                <input type="tel"
                  className={`${styles.input} ${errors.phones?.[index]?.number ? styles.inputError : ''}`}
                  placeholder="+1 555 000 0000"
                  aria-label={`Phone number ${index + 1}`}
                  {...register(`phones.${index}.number`, {
                    pattern: { value: /^\+?[0-9\s\-().]{6,30}$/, message: 'Invalid phone' }
                  })} />
                <button type="button" className={styles.removeBtn}
                  onClick={() => removePhone(index)} aria-label="Remove phone">x</button>
                {errors.phones?.[index]?.number && (
                  <span className={`${styles.err} ${styles.fullWidth}`}>
                    {errors.phones[index].number.message}
                  </span>
                )}
              </div>
            ))}
            <button type="button" className={styles.addBtn}
              onClick={() => appendPhone({ label: 'mobile', number: '' })}>
              + Add Phone
            </button>
          </fieldset>

          <fieldset className={styles.fieldset}>
            <legend className={styles.legend}>Email Addresses</legend>
            {emailFields.map((field, index) => (
              <div key={field.id} className={styles.multiRow}>
                <select className={styles.select}
                  aria-label={`Email label ${index + 1}`}
                  {...register(`emails.${index}.label`)}>
                  {EMAIL_LABELS.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
                <input type="email"
                  className={`${styles.input} ${errors.emails?.[index]?.address ? styles.inputError : ''}`}
                  placeholder="email@example.com"
                  aria-label={`Email address ${index + 1}`}
                  {...register(`emails.${index}.address`, {
                    pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email' }
                  })} />
                <button type="button" className={styles.removeBtn}
                  onClick={() => removeEmail(index)} aria-label="Remove email">x</button>
                {errors.emails?.[index]?.address && (
                  <span className={`${styles.err} ${styles.fullWidth}`}>
                    {errors.emails[index].address.message}
                  </span>
                )}
              </div>
            ))}
            <button type="button" className={styles.addBtn}
              onClick={() => appendEmail({ label: 'personal', address: '' })}>
              + Add Email
            </button>
          </fieldset>

          <div className={styles.formActions}>
            <button type="button" className={styles.cancelBtn}
              onClick={() => navigate(isEdit ? `/contacts/${id}` : '/contacts')}>
              Cancel
            </button>
            <button type="submit" className={styles.submitBtn}
              disabled={loading} aria-busy={loading}>
              {loading ? 'Saving...' : isEdit ? 'Save Changes' : 'Create Contact'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ContactFormPage;
