import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import styles from './HomePage.module.css';

const FEATURES = [
  {
    icon: '&#128101;',
    title: 'Manage All Contacts',
    desc: 'Store and organise all your personal and professional contacts in one place.',
  },
  {
    icon: '&#128269;',
    title: 'Instant Search',
    desc: 'Find any contact in seconds by name, company, phone number or email.',
  },
  {
    icon: '&#128222;',
    title: 'Multiple Phone & Email',
    desc: 'Add several phone numbers and email addresses per contact, each with a label.',
  },
  {
    icon: '&#128274;',
    title: 'Secure & Private',
    desc: 'Your contacts are private to your account and protected with JWT authentication.',
  },
  {
    icon: '&#128196;',
    title: 'Paginated List',
    desc: 'Browse contacts with a clean paginated table  -  no clutter, no confusion.',
  },
  {
    icon: '&#9889;',
    title: 'Fast CRUD',
    desc: 'Create, view, edit and delete contacts instantly  -  all from a single page.',
  },
];

const MOCK_CONTACTS = [
  { name: 'Alice Johnson', title: 'Engineer',  color: '#6366f1' },
  { name: 'Bob Williams',  title: 'Designer',  color: '#0ea5e9' },
  { name: 'Carol Brown',   title: 'Manager',   color: '#10b981' },
  { name: 'David Smith',   title: 'Developer', color: '#f59e0b' },
];

const HomePage = () => {
  const { user, loading } = useAuth();
  const navigate = useNavigate();

  if (!loading && user) {
    navigate('/contacts', { replace: true });
    return null;
  }

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <span>Loading...</span>
      </div>
    );
  }

  return (
    <div className={styles.page}>

      <nav className={styles.nav}>
        <Link to="/" className={styles.navBrand}>
          <span className={styles.navBrandIcon}>&#128199;</span>
          Contact Manager
        </Link>
        <div className={styles.navActions}>
          <Link to="/login"    className={styles.navSignIn}>Sign In</Link>
          <Link to="/register" className={styles.navRegister}>Get Started Free</Link>
        </div>
      </nav>

      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <span className={styles.heroBadge}>Free &amp; Secure</span>
          <h1 className={styles.heroTitle}>
            Your Contacts,<br />
            <span className={styles.heroAccent}>Perfectly Organised</span>
          </h1>
          <p className={styles.heroSubtitle}>
            Contact Manager is a simple, fast, and secure web application that lets you
            store, search, and manage all your personal and professional contacts in one place.
            Register in seconds and get started for free.
          </p>
          <div className={styles.heroButtons}>
            <Link to="/register" className={styles.btnPrimary}>
              Create Free Account
            </Link>
            <Link to="/login" className={styles.btnOutline}>
              Sign In
            </Link>
          </div>

          <div className={styles.heroStats}>
            <div className={styles.stat}>
              <span className={styles.statNum}>100%</span>
              <span className={styles.statLabel}>Free to use</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statNum}>JWT</span>
              <span className={styles.statLabel}>Secured</span>
            </div>
            <div className={styles.statDivider} />
            <div className={styles.stat}>
              <span className={styles.statNum}>REST</span>
              <span className={styles.statLabel}>Powered API</span>
            </div>
          </div>
        </div>

        <div className={styles.heroVisual} aria-hidden="true">
          <div className={styles.mockCard}>
            <div className={styles.mockCardHeader}>
              <div className={styles.mockDot} style={{ background: '#ef4444' }} />
              <div className={styles.mockDot} style={{ background: '#f59e0b' }} />
              <div className={styles.mockDot} style={{ background: '#22c55e' }} />
              <span className={styles.mockCardTitle}>Contacts</span>
            </div>
            <div className={styles.mockSearch}>
              <span className={styles.mockSearchIcon}>&#128269;</span>
              <span className={styles.mockSearchText}>Search contacts...</span>
            </div>
            {MOCK_CONTACTS.map((c) => (
              <div key={c.name} className={styles.mockRow}>
                <div className={styles.mockAvatar} style={{ background: c.color }}>
                  {c.name[0]}
                </div>
                <div className={styles.mockInfo}>
                  <span className={styles.mockName}>{c.name}</span>
                  <span className={styles.mockTitle}>{c.title}</span>
                </div>
                <div className={styles.mockActions}>
                  <span className={styles.mockBtn}>View</span>
                  <span className={styles.mockBtnEdit}>Edit</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className={styles.features}>
        <div className={styles.featuresInner}>
          <h2 className={styles.featuresTitle}>Everything you need</h2>
          <p className={styles.featuresSubtitle}>
            Built with Spring Boot, React, and SQL Server  -  a full-stack contact management system.
          </p>
          <div className={styles.featuresGrid}>
            {FEATURES.map((f) => (
              <div key={f.title} className={styles.featureCard}>
                <div
                  className={styles.featureIcon}
                  dangerouslySetInnerHTML={{ __html: f.icon }}
                />
                <h3 className={styles.featureTitle}>{f.title}</h3>
                <p className={styles.featureDesc}>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className={styles.cta}>
        <h2 className={styles.ctaTitle}>Ready to get started?</h2>
        <p className={styles.ctaSubtitle}>
          Create your free account now  -  no credit card required.
        </p>
        <div className={styles.ctaButtons}>
          <Link to="/register" className={styles.btnPrimary}>Create Free Account</Link>
          <Link to="/login"    className={styles.btnOutlineLight}>Sign In</Link>
        </div>
      </section>

      <footer className={styles.footer}>
        <span>Contact Manager &copy; {new Date().getFullYear()}</span>
        <span className={styles.footerSep}>|</span>
        <Link to="/login"    className={styles.footerLink}>Sign In</Link>
        <Link to="/register" className={styles.footerLink}>Register</Link>
      </footer>

    </div>
  );
};

export default HomePage;
