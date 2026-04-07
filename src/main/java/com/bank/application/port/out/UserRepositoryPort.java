package com.bank.application.port.out;

import com.bank.domain.model.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(int userId);

    Optional<User> findByRelatedId(String relatedId);
}
