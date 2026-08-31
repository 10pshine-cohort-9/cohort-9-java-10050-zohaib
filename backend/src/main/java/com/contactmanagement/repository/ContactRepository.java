package com.contactmanagement.repository;

import com.contactmanagement.model.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Contact c
            LEFT JOIN c.phones p
            LEFT JOIN c.emails e
            WHERE c.userId = :userId
              AND (
                    LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(c.company)   LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(p.number)    LIKE LOWER(CONCAT('%', :q, '%'))
                 OR LOWER(e.address)   LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    Page<Contact> searchByUserIdAndQuery(
            @Param("userId") Long userId,
            @Param("q") String query,
            Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);
}
