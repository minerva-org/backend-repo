package com.minerva.api.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRespository extends JpaRepository<User,Long>{
    Optional<User> findByUsername(String username);
    
}

