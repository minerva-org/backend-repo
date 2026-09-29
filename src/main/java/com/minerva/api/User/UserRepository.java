package com.minerva.api.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByPersonaEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<User> findByPersonaId(String personaId);
}