package com.contactmanagement.repository;

import com.contactmanagement.model.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactPhoneRepository extends JpaRepository<ContactPhone, Long> {
}
