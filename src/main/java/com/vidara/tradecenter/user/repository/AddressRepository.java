package com.vidara.tradecenter.user.repository;

import com.vidara.tradecenter.user.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Find all addresses for a user
    List<Address> findByUserId(Long userId);

    // Find specific address for a user (security check)
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    // Find default address for a user
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    // Unset all defaults for a user (before setting new default)
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetAllDefaultsForUser(@Param("userId") Long userId);

    // Count addresses for a user
    long countByUserId(Long userId);

    // Delete all addresses for a user
    void deleteByUserId(Long userId);
}