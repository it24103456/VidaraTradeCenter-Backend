package com.vidara.tradecenter.user.repository;

import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (for login)
    Optional<User> findByEmail(String email);

    // Check if email exists (for registration)
    boolean existsByEmail(String email);

    // Find by password reset token (for password reset)
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    // Find by status
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    // Search by name or email
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // Count by status (for dashboard)
    long countByStatus(UserStatus status);
}