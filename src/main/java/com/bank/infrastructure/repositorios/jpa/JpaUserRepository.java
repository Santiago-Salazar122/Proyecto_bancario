package com.bank.infrastructure.repositorios.jpa;

import com.bank.domain.model.user.User;
import com.bank.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad User → tabla "users" en MySQL.
 * Spring Data JPA genera la implementación automáticamente.
 * Cada método "findBy..." se traduce a una consulta SQL:
 *   findByIdentificationId("123") → SELECT * FROM users WHERE identification_id = '123'
 */
public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentificationId(String identificationId);
    Optional<User> findByRelatedId(String relatedId);
    boolean existsByIdentificationId(String identificationId);
    List<User> findByRole(UserRole role);
}
