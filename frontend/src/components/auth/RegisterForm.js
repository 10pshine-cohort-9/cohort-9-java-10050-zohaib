import React from 'react';

// TODO: Implement registration form using react-hook-form
const RegisterForm = ({ onSubmit }) => {
  return (
    <form onSubmit={onSubmit}>
      <input type="text" name="firstName" placeholder="First Name" required />
      <input type="text" name="lastName" placeholder="Last Name" required />
      <input type="email" name="email" placeholder="Email" required />
      <input type="password" name="password" placeholder="Password" required />
      <button type="submit">Register</button>
    </form>
  );
};

export default RegisterForm;
