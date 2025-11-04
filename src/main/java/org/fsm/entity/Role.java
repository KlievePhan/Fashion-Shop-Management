package org.fsm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;               // ROLE_ADMIN, ROLE_STAFF, ...

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
