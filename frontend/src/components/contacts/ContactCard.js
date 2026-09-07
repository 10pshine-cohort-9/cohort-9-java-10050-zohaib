import React from 'react';
import { Link } from 'react-router-dom';

// TODO: Implement contact card with name, email, phone, and action buttons
const ContactCard = ({ contact, onDelete }) => {
  return (
    <div className="contact-card">
      <h3>{contact.firstName} {contact.lastName}</h3>
      <p>{contact.email}</p>
      <p>{contact.phone}</p>
      <Link to={`/contacts/${contact.id}`}>View</Link>
      <Link to={`/contacts/${contact.id}/edit`}>Edit</Link>
      <button onClick={() => onDelete(contact.id)}>Delete</button>
    </div>
  );
};

export default ContactCard;
