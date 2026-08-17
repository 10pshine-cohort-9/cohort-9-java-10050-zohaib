import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import LoginPage          from './pages/LoginPage';
import RegisterPage       from './pages/RegisterPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import ContactListPage    from './pages/ContactListPage';
import ContactFormPage    from './pages/ContactFormPage';
import ContactDetailPage  from './pages/ContactDetailPage';

import PrivateRoute from './components/common/PrivateRoute';
import Navbar       from './components/common/Navbar';
import { useAuth }  from './hooks/useAuth';

function App() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '120px' }}>
        <span>Loading…</span>
      </div>
    );
  }

  return (
    <Router>
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<PrivateRoute />}>
            <Route path="/contacts"          element={<ContactListPage />} />
            <Route path="/contacts/new"      element={<ContactFormPage />} />
            <Route path="/contacts/:id"      element={<ContactDetailPage />} />
            <Route path="/contacts/:id/edit" element={<ContactFormPage />} />
            <Route path="/change-password"   element={<ChangePasswordPage />} />
          </Route>

          <Route path="/" element={<Navigate to="/contacts" replace />} />
          <Route path="*" element={<Navigate to="/contacts" replace />} />
        </Routes>
      </main>
      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
    </Router>
  );
}

export default App;
