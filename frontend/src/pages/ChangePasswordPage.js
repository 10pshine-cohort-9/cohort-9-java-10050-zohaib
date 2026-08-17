import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import authService from '../services/authService';
import styles from './AuthPage.module.css';

const ChangePasswordPage = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm({ mode: 'onBlur' });

  const newPassword = watch('newPassword');

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      await authService.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmNewPassword: data.confirmNewPassword,
      });
      toast.success('Password changed successfully.');
      reset();
      navigate('/contacts');
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Failed to change password. Please try again.';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Change Password</h1>
        <p className={styles.subtitle}>Enter your current password and choose a new one</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.field}>
            <label htmlFor="currentPassword" className={styles.label}>
              Current Password *
            </label>
            <input
              id="currentPassword"
              type="password"
              autoComplete="current-password"
              className={`${styles.input} ${errors.currentPassword ? styles.inputError : ''}`}
              placeholder="Your current password"
              {...register('currentPassword', {
                required: 'Current password is required',
              })}
            />
            {errors.currentPassword && (
              <span className={styles.errorMsg} role="alert">
                {errors.currentPassword.message}
              </span>
            )}
          </div>

          <div className={styles.field}>
            <label htmlFor="newPassword" className={styles.label}>
              New Password *
            </label>
            <input
              id="newPassword"
              type="password"
              autoComplete="new-password"
              className={`${styles.input} ${errors.newPassword ? styles.inputError : ''}`}
              placeholder="Minimum 8 characters"
              {...register('newPassword', {
                required: 'New password is required',
                minLength: { value: 8, message: 'At least 8 characters required' },
                maxLength: { value: 128, message: 'Max 128 characters' },
              })}
            />
            {errors.newPassword && (
              <span className={styles.errorMsg} role="alert">
                {errors.newPassword.message}
              </span>
            )}
          </div>

          <div className={styles.field}>
            <label htmlFor="confirmNewPassword" className={styles.label}>
              Confirm New Password *
            </label>
            <input
              id="confirmNewPassword"
              type="password"
              autoComplete="new-password"
              className={`${styles.input} ${errors.confirmNewPassword ? styles.inputError : ''}`}
              placeholder="Repeat your new password"
              {...register('confirmNewPassword', {
                required: 'Please confirm your new password',
                validate: (value) =>
                  value === newPassword || 'Passwords do not match',
              })}
            />
            {errors.confirmNewPassword && (
              <span className={styles.errorMsg} role="alert">
                {errors.confirmNewPassword.message}
              </span>
            )}
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={isLoading}
            aria-busy={isLoading}
          >
            {isLoading ? 'Updating…' : 'Update Password'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ChangePasswordPage;
