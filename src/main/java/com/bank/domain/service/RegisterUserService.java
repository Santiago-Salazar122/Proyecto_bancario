package com.bank.domain.service;

import com.bank.domain.model.user.User;
import com.bank.domain.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de dominio: Registrar un nuevo usuario.
 * Valida que el identificationId sea único antes de guardar.
 */
@Service
@Transactional
public class RegisterUserService {
    private final UserRepositoryPort userRepository;
    public RegisterUserService(UserRepositoryPort userRepository) { this.userRepository = userRepository; }

    public User execute(User user) {
        if (userRepository.existsByIdentificationId(user.getIdentificationId()))
            throw new IllegalStateException(
                "A user with identification ID already exists: " + user.getIdentificationId());
        return userRepository.save(user);
    }
}
