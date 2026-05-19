package com.bank.domain.ports;

import com.bank.domain.model.user.User;
import com.bank.domain.enums.UserRole;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long userId);
    Optional<User> findByIdentificationId(String identificationId);
    Optional<User> findByRelatedId(String relatedId);
    boolean existsByIdentificationId(String identificationId);
    List<User> findByRole(UserRole role);
    List<User> findAll();
}
