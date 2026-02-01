package com.gallary.gallaryV1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gallary.gallaryV1.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<User> findByResetToken(String resetToken);
}
