/**
 * Shared form validation helpers used with react-hook-form.
 */

export const emailValidation = {
  required: 'Email is required',
  pattern: {
    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    message: 'Enter a valid email address',
  },
};

export const passwordValidation = {
  required: 'Password is required',
  minLength: {
    value: 8,
    message: 'Password must be at least 8 characters',
  },
};

export const requiredField = (fieldName) => ({
  required: `${fieldName} is required`,
});

export const phoneValidation = {
  pattern: {
    value: /^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$/,
    message: 'Enter a valid phone number',
  },
};
