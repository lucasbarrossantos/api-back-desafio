package com.pitang.repository;

import com.pitang.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

	Page<User> findAllByFirstNameContainingIgnoreCase(String model, Pageable pageable);

	Optional<User> findByEmailAndId(String email, UUID id);

}
