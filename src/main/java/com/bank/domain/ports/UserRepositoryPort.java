package com.bank.domain.ports;

import com.bank.domain.model.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(int userId);

    Optional<User> findByRelatedId(String relatedId);
}

