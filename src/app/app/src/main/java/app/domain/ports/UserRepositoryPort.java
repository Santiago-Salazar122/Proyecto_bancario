package app.domain.ports;

import app.domain.model.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findById(int userId);

    Optional<User> findByRelatedId(String relatedId);
}

