import React, { useState, useRef, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../../hooks/useAuth';
import styles from './Navbar.module.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    setMenuOpen(false);
    logout();
    toast.info('You have been signed out.');
    navigate('/login');
  };

  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '';

  return (
    <nav className={styles.navbar} role="navigation" aria-label="Main navigation">
      <div className={styles.brand}>
        <Link to={user ? '/contacts' : '/login'} className={styles.brandLink}>
          Contact Manager
        </Link>
      </div>

      {user && (
        <div className={styles.navLinks}>
          <NavLink
            to="/contacts"
            className={({ isActive }) =>
              `${styles.navLink} ${isActive ? styles.navLinkActive : ''}`
            }
          >
            Contacts
          </NavLink>
        </div>
      )}

      <div className={styles.spacer} />

      {user ? (
        <div className={styles.userMenu} ref={menuRef}>
          <button
            className={styles.avatarBtn}
            onClick={() => setMenuOpen((prev) => !prev)}
            aria-expanded={menuOpen}
            aria-haspopup="true"
            aria-label="Open profile menu"
          >
            <span className={styles.avatarCircle} aria-hidden="true">{initials}</span>
            <span className={styles.greeting}>{user.firstName}</span>
            <span className={styles.chevron} aria-hidden="true">
              {menuOpen ? '▲' : '▼'}
            </span>
          </button>

          {menuOpen && (
            <div className={styles.dropdown} role="menu">
              <div className={styles.dropdownHeader}>
                <span className={styles.dropdownName}>{user.firstName} {user.lastName}</span>
                <span className={styles.dropdownEmail}>{user.email || user.phone || ''}</span>
              </div>

              <div className={styles.dropdownDivider} />

              <Link
                to="/profile"
                className={styles.dropdownItem}
                role="menuitem"
                onClick={() => setMenuOpen(false)}
              >
                <span className={styles.dropdownIcon}>👤</span>
                My Profile
              </Link>

              <Link
                to="/change-password"
                className={styles.dropdownItem}
                role="menuitem"
                onClick={() => setMenuOpen(false)}
              >
                <span className={styles.dropdownIcon}>🔑</span>
                Change Password
              </Link>

              <div className={styles.dropdownDivider} />

              <button
                className={`${styles.dropdownItem} ${styles.dropdownItemDanger}`}
                role="menuitem"
                onClick={handleLogout}
              >
                <span className={styles.dropdownIcon}>🚪</span>
                Sign Out
              </button>
            </div>
          )}
        </div>
      ) : (
        <div className={styles.authLinks}>
          <Link to="/login"    className={styles.navLinkGuest}>Sign In</Link>
          <Link to="/register" className={styles.navLinkGuestPrimary}>Register</Link>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
