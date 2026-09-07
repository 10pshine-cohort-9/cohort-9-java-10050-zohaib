import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import authService from '../services/authService';
import styles from './AuthPage.module.css';

const EyeBtn = ({ show, onToggle }) => (
  <button
    type="button"
    className={styles.eyeBtn}
    onClick={onToggle}
    aria-label={show ? 'Hide password' : 'Show password'}
  >
    {show ? "\uD83D\uDC41\uFE0F" : "\uD83D\uDC41"}
  </button>
);

const ChangePasswordPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [showCurrent,  setShowCurrent]  = useState(false);
  const [showNew,      setShowNew]      = useState(false);
  const [showConfirm,  setShowConfirm]  = useState(false);

  const { register, handleSubmit, watch, reset, formState: { errors } } = useForm({ mode: 'onBlur' });
  const newPassword = watch('newPassword');

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      await authService.changePassword({
        currentPassword:    data.currentPassword,
        newPassword:        data.newPassword,
        confirmNewPassword: data.confirmNewPassword,
      });
      toast.success('Password changed successfully.');
      reset();
      navigate('/contacts');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to change password.');
    } finally { setIsLoading(false); }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Change Password</h1>
        <p className={styles.subtitle}>Enter your current password and choose a new one</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>

          <div className={styles.field}>
            <label htmlFor="currentPassword" className={styles.label}>Current Password *</label>
            <div className={styles.inputWrapper}>
              <input
                id="currentPassword"
                type={showCurrent ? 'text' : 'password'}
                autoComplete="current-password"
                className={`${styles.input} ${errors.currentPassword ? styles.inputError : ''}`}
                placeholder="Your current password"
                {...register('currentPassword', { required: 'Current password is required' })}
              />
              <EyeBtn show={showCurrent} onToggle={() => setShowCurrent(v => !v)} />
            </div>
            {errors.currentPassword && <span className={styles.errorMsg}>{errors.currentPassword.message}</span>}
          </div>

          <div className={styles.field}>
            <label htmlFor="newPassword" className={styles.label}>New Password *</label>
            <div className={styles.inputWrapper}>
              <input
                id="newPassword"
                type={showNew ? 'text' : 'password'}
                autoComplete="new-password"
                className={`${styles.input} ${errors.newPassword ? styles.inputError : ''}`}
                placeholder="Min 4 chars, 1 uppercase, 1 special character"
                {...register('newPassword', {
                  required: 'New password is required',
                  minLength: { value: 4, message: 'At least 4 characters required' },
                  validate: {
                    hasUppercase: (v) => /[A-Z]/.test(v) || 'Must contain at least 1 uppercase letter',
                    hasSpecial:   (v) => /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(v) || 'Must contain at least 1 special character',
                  },
                })}
              />
              <EyeBtn show={showNew} onToggle={() => setShowNew(v => !v)} />
            </div>
            {errors.newPassword && <span className={styles.errorMsg}>{errors.newPassword.message}</span>}
          </div>

          <div className={styles.field}>
            <label htmlFor="confirmNewPassword" className={styles.label}>Confirm New Password *</label>
            <div className={styles.inputWrapper}>
              <input
                id="confirmNewPassword"
                type={showConfirm ? 'text' : 'password'}
                autoComplete="new-password"
                className={`${styles.input} ${errors.confirmNewPassword ? styles.inputError : ''}`}
                placeholder="Repeat your new password"
                {...register('confirmNewPassword', {
                  required: 'Please confirm your new password',
                  validate: (v) => v === newPassword || 'Passwords do not match',
                })}
              />
              <EyeBtn show={showConfirm} onToggle={() => setShowConfirm(v => !v)} />
            </div>
            {errors.confirmNewPassword && <span className={styles.errorMsg}>{errors.confirmNewPassword.message}</span>}
          </div>

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Updating...' : 'Update Password'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordPage;
