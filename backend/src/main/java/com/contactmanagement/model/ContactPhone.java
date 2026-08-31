package com.contactmanagement.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_phones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(name = "label", nullable = false, length = 50)
    private String label;

    @Column(name = "number", nullable = false, length = 50)
    private String number;
}
