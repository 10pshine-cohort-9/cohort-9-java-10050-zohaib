import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../hooks/useAuth';
import styles from './Navbar.module.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    toast.info('You have been signed out.');
    navigate('/login');
  };

  return (
    <nav className={styles.navbar} role="navigation" aria-label="Main navigation">
      <div className={styles.brand}>
        <Link to="/contacts" className={styles.brandLink}>
          Contact Manager
        </Link>
      </div>

      <div className={styles.spacer} />

      {user ? (
        <div className={styles.userMenu}>
          <span className={styles.greeting}>
            {user.firstName} {user.lastName}
          </span>
          <button
            className={styles.menuBtn}
            onClick={() => setMenuOpen((prev) => !prev)}
            aria-expanded={menuOpen}
            aria-haspopup="true"
            aria-label="User menu"
          >
            ▾
          </button>

          {menuOpen && (
            <div className={styles.dropdown} role="menu">
              <Link
                to="/change-password"
                className={styles.dropdownItem}
                role="menuitem"
                onClick={() => setMenuOpen(false)}
              >
                Change Password
              </Link>
              <button
                className={styles.dropdownItem}
                role="menuitem"
                onClick={() => { setMenuOpen(false); handleLogout(); }}
              >
                Sign Out
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className={styles.authLinks}>
          <Link to="/login"    className={styles.navLink}>Sign In</Link>
          <Link to="/register" className={`${styles.navLink} ${styles.navLinkPrimary}`}>
            Register
          </Link>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
