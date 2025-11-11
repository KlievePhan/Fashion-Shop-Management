package org.fsm.repository;

import org.fsm.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCode(String code);
    boolean existsByCode(String code);
    
 // Add this new method to fix the error
    List<Role> findAllByCodeIn(List<String> codes);
}
