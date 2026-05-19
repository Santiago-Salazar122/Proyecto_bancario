package com.bank.infrastructure.repositorios.adapter;

import com.bank.domain.model.user.User;
import com.bank.domain.enums.UserRole;
import com.bank.domain.ports.UserRepositoryPort;
import com.bank.infrastructure.repositorios.jpa.JpaUserRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador: conecta UserRepositoryPort (puerto del dominio) con JpaUserRepository (MySQL).
 * Patrón Adapter de la Arquitectura Hexagonal.
 * Si cambias de MySQL a otra BD, solo cambias este archivo.
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final JpaUserRepository jpa;
    public UserRepositoryAdapter(JpaUserRepository jpa) { this.jpa = jpa; }

    @Override public User save(User user)                                 { return jpa.save(user); }
    @Override public Optional<User> findById(Long id)                     { return jpa.findById(id); }
    @Override public Optional<User> findByIdentificationId(String id)     { return jpa.findByIdentificationId(id); }
    @Override public Optional<User> findByRelatedId(String relatedId)     { return jpa.findByRelatedId(relatedId); }
    @Override public boolean existsByIdentificationId(String id)          { return jpa.existsByIdentificationId(id); }
    @Override public List<User> findByRole(UserRole role)                 { return jpa.findByRole(role); }
    @Override public List<User> findAll()                                 { return jpa.findAll(); }
}
