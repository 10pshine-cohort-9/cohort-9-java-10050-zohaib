import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { useAuth } from '../hooks/useAuth';
import authService from '../services/authService';
import styles from './ProfilePage.module.css';

const ProfilePage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [changePassOpen, setChangePassOpen] = useState(false);
  const [resetting, setResetting] = useState(false);
  const [showCurrent,  setShowCurrent]  = useState(false);
  const [showNew,      setShowNew]      = useState(false);
  const [showConfirm,  setShowConfirm]  = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm({ mode: 'onBlur' });

  const newPassword = watch('newPassword');

  if (!user) return null;

  const initials = `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase();

  const handleLogout = () => {
    logout();
    toast.info('You have been signed out.');
    navigate('/login');
  };

  const openChangePass = () => {
    reset();
    setChangePassOpen(true);
  };

  const closeChangePass = () => {
    reset();
    setShowCurrent(false);
    setShowNew(false);
    setShowConfirm(false);
    setChangePassOpen(false);
  };

  const onReset = async (data) => {
    setResetting(true);
    try {
      await authService.changePassword({
        currentPassword:    data.currentPassword,
        newPassword:        data.newPassword,
        confirmNewPassword: data.confirmNewPassword,
      });
      toast.success('Password reset successfully.');
      closeChangePass();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reset password.');
    } finally {
      setResetting(false);
    }
  };

  return (
    <div className={styles.page}>
      <h1 className={styles.pageTitle}>My Profile</h1>

      <div className={styles.card}>
        <div className={styles.header}>
          <div className={styles.avatar}>{initials}</div>
          <div className={styles.headerInfo}>
            <h2 className={styles.name}>{user.firstName} {user.lastName}</h2>
            <span className={styles.role}>
              {user.role === 'ROLE_ADMIN' ? 'Administrator' : 'User'}
            </span>
          </div>
        </div>

        <div className={styles.fields}>
          <div className={styles.fieldRow}>
            <span className={styles.fieldLabel}>First Name</span>
            <span className={styles.fieldValue}>{user.firstName}</span>
          </div>
          <div className={styles.fieldRow}>
            <span className={styles.fieldLabel}>Last Name</span>
            <span className={styles.fieldValue}>{user.lastName}</span>
          </div>
          <div className={styles.fieldRow}>
            <span className={styles.fieldLabel}>Email</span>
            <span className={styles.fieldValue}>
              {user.email || <em className={styles.empty}>Not set</em>}
            </span>
          </div>
          <div className={styles.fieldRow}>
            <span className={styles.fieldLabel}>Phone</span>
            <span className={styles.fieldValue}>
              {user.phone || <em className={styles.empty}>Not set</em>}
            </span>
          </div>
          <div className={styles.fieldRow}>
            <span className={styles.fieldLabel}>Role</span>
            <span className={styles.fieldValue}>
              {user.role === 'ROLE_ADMIN' ? 'Administrator' : 'User'}
            </span>
          </div>
        </div>

        <div className={styles.actions}>
          <button className={styles.changePassBtn} onClick={openChangePass}>
            Change Password
          </button>
          <button className={styles.logoutBtn} onClick={handleLogout}>
            Logout
          </button>
          <Link to="/contacts" className={styles.backBtn}>
            Back to Contacts
          </Link>
        </div>
      </div>

      {changePassOpen && (
        <div className={styles.overlay} role="dialog" aria-modal="true" aria-label="Change Password">
          <div className={styles.modal}>
            <div className={styles.modalHeader}>
              <h2 className={styles.modalTitle}>Change Password</h2>
              <button
                className={styles.modalClose}
                onClick={closeChangePass}
                aria-label="Close"
              >
                &#x2715;
              </button>
            </div>

            <form onSubmit={handleSubmit(onReset)} noValidate className={styles.modalForm}>
              <div className={styles.formField}>
                <label className={styles.formLabel}>Current Password *</label>
                <div style={{ position:'relative', display:'flex', alignItems:'center' }}>
                  <input
                    type={showCurrent ? 'text' : 'password'}
                    autoComplete="current-password"
                    style={{ paddingRight:'44px', width:'100%' }}
                    className={`${styles.formInput} ${errors.currentPassword ? styles.formInputErr : ''}`}
                    placeholder="Enter your current password"
                    {...register('currentPassword', { required: 'Current password is required' })}
                  />
                  <button type="button" onClick={() => setShowCurrent(v => !v)} aria-label="Toggle password visibility" style={{ position:'absolute', right:'10px', background:'transparent', border:'none', cursor:'pointer', color:'#94a3b8', fontSize:'1rem', padding:'4px' }}>{showCurrent ? "\uD83D\uDC41\uFE0F" : "\uD83D\uDC41"}</button>
                </div>
                {errors.currentPassword && (
                  <span className={styles.formErr}>{errors.currentPassword.message}</span>
                )}
              </div>

              <div className={styles.formField}>
                <label className={styles.formLabel}>New Password *</label>
                <input
                  type="password"
                  autoComplete="new-password"
                  className={`${styles.formInput} ${errors.newPassword ? styles.formInputErr : ''}`}
                  placeholder="Min 4 chars, 1 uppercase, 1 special character"
                  {...register('newPassword', {
                    required: 'New password is required',
                    minLength: { value: 4, message: 'At least 4 characters required' },
                    validate: {
                      hasUppercase: (v) => /[A-Z]/.test(v) || 'Must contain at least 1 uppercase letter',
                      hasSpecial:   (v) => /[!@#\$%^&*()_+-={}|,.<>?]/.test(v) || 'Must contain at least 1 special character',
                    },
                  })}
                />
                {errors.newPassword && (
                  <span className={styles.formErr}>{errors.newPassword.message}</span>
                )}
              </div>

              <div className={styles.formField}>
                <label className={styles.formLabel}>Confirm New Password *</label>
                <div style={{ position:'relative', display:'flex', alignItems:'center' }}>
                  <input
                    type={showConfirm ? 'text' : 'password'}
                    autoComplete="new-password"
                    style={{ paddingRight:'44px', width:'100%' }}
                    className={`${styles.formInput} ${errors.confirmNewPassword ? styles.formInputErr : ''}`}
                    placeholder="Repeat new password"
                    {...register('confirmNewPassword', {
                      required: 'Please confirm your new password',
                      validate: (v) => v === newPassword || 'Passwords do not match',
                    })}
                  />
                  <button type="button" onClick={() => setShowConfirm(v => !v)} aria-label="Toggle password visibility" style={{ position:'absolute', right:'10px', background:'transparent', border:'none', cursor:'pointer', color:'#94a3b8', fontSize:'1rem', padding:'4px' }}>{showConfirm ? "\uD83D\uDC41\uFE0F" : "\uD83D\uDC41"}</button>
                </div>
                {errors.confirmNewPassword && (
                  <span className={styles.formErr}>{errors.confirmNewPassword.message}</span>
                )}
              </div>

              <div className={styles.modalActions}>
                <button
                  type="button"
                  className={styles.cancelBtn}
                  onClick={closeChangePass}
                  disabled={resetting}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={styles.resetBtn}
                  disabled={resetting}
                >
                  {resetting ? 'Resetting...' : 'Reset Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfilePage;
