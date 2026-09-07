import React from 'react';

// TODO: Implement login form using react-hook-form
const LoginForm = ({ onSubmit }) => {
  return (
    <form onSubmit={onSubmit}>
      <input type="email" name="email" placeholder="Email" required />
      <input type="password" name="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
  );
};

export default LoginForm;
