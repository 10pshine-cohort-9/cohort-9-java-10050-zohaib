import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import LoginPage          from './pages/LoginPage';
import RegisterPage       from './pages/RegisterPage';
import ChangePasswordPage from './pages/ChangePasswordPage';
import ProfilePage        from './pages/ProfilePage';
import HomePage           from './pages/HomePage';
import ContactListPage    from './pages/ContactListPage';
import ContactFormPage    from './pages/ContactFormPage';
import ContactDetailPage  from './pages/ContactDetailPage';

import PrivateRoute from './components/common/PrivateRoute';
import Navbar       from './components/common/Navbar';
import { useAuth }  from './hooks/useAuth';

function AppContent() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', paddingTop: '120px' }}>
        <span>Loading...</span>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/"         element={<HomePage />} />
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<PrivateRoute />}>
        <Route path="/contacts"          element={<><Navbar /><main className="main-content"><ContactListPage /></main></>} />
        <Route path="/contacts/new"      element={<><Navbar /><main className="main-content"><ContactFormPage /></main></>} />
        <Route path="/contacts/:id"      element={<><Navbar /><main className="main-content"><ContactDetailPage /></main></>} />
        <Route path="/contacts/:id/edit" element={<><Navbar /><main className="main-content"><ContactFormPage /></main></>} />
        <Route path="/change-password"   element={<><Navbar /><main className="main-content"><ChangePasswordPage /></main></>} />
        <Route path="/profile"           element={<><Navbar /><main className="main-content"><ProfilePage /></main></>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
    </Router>
  );
}

export default App;
