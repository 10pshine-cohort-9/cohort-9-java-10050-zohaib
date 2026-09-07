import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { useAuth } from '../hooks/useAuth';
import authService from '../services/authService';
import styles from './AuthPage.module.css';

const RegisterPage = () => {
  const { login } = useAuth();
  const navigate  = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const { register, handleSubmit, setError, formState: { errors } } = useForm({ mode: 'onBlur' });

  const onSubmit = async (data) => {
    if (!data.email?.trim() && !data.phone?.trim()) {
      setError('email', { type: 'manual', message: 'Provide at least an email or a phone number.' });
      return;
    }
    setIsLoading(true);
    try {
      const payload = {
        firstName: data.firstName.trim(),
        lastName:  data.lastName.trim(),
        password:  data.password,
      };
      if (data.email?.trim()) payload.email = data.email.trim();
      if (data.phone?.trim()) payload.phone = data.phone.trim();

      const response = await authService.register(payload);
      login(response.data);
      toast.success('Account created! Welcome!');
      navigate('/contacts', { replace: true });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally { setIsLoading(false); }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Create Account</h1>
        <p className={styles.subtitle}>Fill in your details to get started</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="firstName" className={styles.label}>First Name *</label>
              <input id="firstName" type="text" autoComplete="given-name"
                className={`${styles.input} ${errors.firstName ? styles.inputError : ''}`}
                placeholder="First name"
                {...register('firstName', { required: 'First name is required' })} />
              {errors.firstName && <span className={styles.errorMsg}>{errors.firstName.message}</span>}
            </div>
            <div className={styles.field}>
              <label htmlFor="lastName" className={styles.label}>Last Name *</label>
              <input id="lastName" type="text" autoComplete="family-name"
                className={`${styles.input} ${errors.lastName ? styles.inputError : ''}`}
                placeholder="Last name"
                {...register('lastName', { required: 'Last name is required' })} />
              {errors.lastName && <span className={styles.errorMsg}>{errors.lastName.message}</span>}
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="email" className={styles.label}>Email</label>
            <input id="email" type="email" autoComplete="email"
              className={`${styles.input} ${errors.email ? styles.inputError : ''}`}
              placeholder="user@example.com (optional if phone provided)"
              {...register('email', {
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Enter a valid email' }
              })} />
            {errors.email && <span className={styles.errorMsg}>{errors.email.message}</span>}
          </div>

          <div className={styles.field}>
            <label htmlFor="phone" className={styles.label}>Phone Number</label>
            <input id="phone" type="tel" autoComplete="tel"
              className={`${styles.input} ${errors.phone ? styles.inputError : ''}`}
              placeholder="+923001234567 (optional if email provided)"
              {...register('phone', {
                pattern: { value: /^\+?[1-9]\d{6,14}$/, message: 'Enter a valid phone number' }
              })} />
            {errors.phone && <span className={styles.errorMsg}>{errors.phone.message}</span>}
          </div>

          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>Password *</label>
            <div className={styles.inputWrapper}>
              <input id="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
                placeholder="Minimum 8 characters"
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'At least 8 characters required' }
                })} />
              <button
                type="button"
                className={styles.eyeBtn}
                onClick={() => setShowPassword(v => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? "\uD83D\uDC41\uFE0F" : "\uD83D\uDC41"}
              </button>
            </div>
            {errors.password && <span className={styles.errorMsg}>{errors.password.message}</span>}
          </div>

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" className={styles.link}>Sign in</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;
