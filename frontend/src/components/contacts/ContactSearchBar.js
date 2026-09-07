import React from 'react';

// TODO: Implement debounced search input
const ContactSearchBar = ({ onSearch }) => {
  return (
    <div className="search-bar">
      <input
        type="search"
        placeholder="Search contacts by name or email..."
        onChange={(e) => onSearch(e.target.value)}
        aria-label="Search contacts"
      />
    </div>
  );
};

export default ContactSearchBar;
