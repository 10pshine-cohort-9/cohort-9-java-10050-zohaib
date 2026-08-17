import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { useAuth } from '../hooks/useAuth';
import authService from '../services/authService';
import styles from './AuthPage.module.css';

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(false);

  const from = location.state?.from?.pathname || '/contacts';

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ mode: 'onBlur' });

  const onSubmit = async (data) => {
    setIsLoading(true);
    try {
      const response = await authService.login({
        identifier: data.identifier.trim(),
        password: data.password,
      });
      login(response.data);
      toast.success(`Welcome back, ${response.data.firstName}!`);
      navigate(from, { replace: true });
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Login failed. Please check your credentials.';
      toast.error(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Sign In</h1>
        <p className={styles.subtitle}>Enter your email or phone number to continue</p>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className={styles.field}>
            <label htmlFor="identifier" className={styles.label}>
              Email or Phone Number
            </label>
            <input
              id="identifier"
              type="text"
              autoComplete="username"
              className={`${styles.input} ${errors.identifier ? styles.inputError : ''}`}
              placeholder="e.g. user@example.com or +923001234567"
              {...register('identifier', {
                required: 'Email or phone number is required',
              })}
            />
            {errors.identifier && (
              <span className={styles.errorMsg} role="alert">
                {errors.identifier.message}
              </span>
            )}
          </div>

          <div className={styles.field}>
            <label htmlFor="password" className={styles.label}>
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              className={`${styles.input} ${errors.password ? styles.inputError : ''}`}
              placeholder="Your password"
              {...register('password', {
                required: 'Password is required',
              })}
            />
            {errors.password && (
              <span className={styles.errorMsg} role="alert">
                {errors.password.message}
              </span>
            )}
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={isLoading}
            aria-busy={isLoading}
          >
            {isLoading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p className={styles.footer}>
          Don't have an account?{' '}
          <Link to="/register" className={styles.link}>
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;
