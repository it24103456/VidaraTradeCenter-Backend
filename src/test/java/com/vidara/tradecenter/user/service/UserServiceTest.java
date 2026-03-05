package com.vidara.tradecenter.user.service;

import com.vidara.tradecenter.user.model.Role;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserRole;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for User-related service operations.
 * Uses @DataJpaTest with PostgreSQL (transactional rollback after each test).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create test user
        testUser = new User("John", "Doe", "john@example.com", "encodedPassword123");
        testUser.setPhone("0771234567");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = new User("Admin", "User", "admin@example.com", "encodedPassword456");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }


    // ==================== GET USER BY ID ====================

    @Nested
    @DisplayName("Get User By ID")
    class GetUserById {

        @Test
        @DisplayName("Should return user when valid ID is provided")
        void shouldReturnUserWhenValidId() {
            Optional<User> found = userRepository.findById(testUser.getId());

            assertTrue(found.isPresent());
            assertEquals("John", found.get().getFirstName());
            assertEquals("Doe", found.get().getLastName());
            assertEquals("john@example.com", found.get().getEmail());
        }

        @Test
        @DisplayName("Should return empty when user ID does not exist")
        void shouldReturnEmptyWhenInvalidId() {
            Optional<User> found = userRepository.findById(9999L);

            assertFalse(found.isPresent());
        }
    }


    // ==================== GET USER BY EMAIL ====================

    @Nested
    @DisplayName("Get User By Email")
    class GetUserByEmail {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            Optional<User> found = userRepository.findByEmail("john@example.com");

            assertTrue(found.isPresent());
            assertEquals("John", found.get().getFirstName());
        }

        @Test
        @DisplayName("Should return empty for non-existent email")
        void shouldReturnEmptyForNonExistentEmail() {
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            assertFalse(found.isPresent());
        }
    }


    // ==================== CHECK EMAIL EXISTS ====================

    @Nested
    @DisplayName("Check Email Exists")
    class CheckEmailExists {

        @Test
        @DisplayName("Should return true for existing email")
        void shouldReturnTrueForExistingEmail() {
            assertTrue(userRepository.existsByEmail("john@example.com"));
        }

        @Test
        @DisplayName("Should return false for non-existing email")
        void shouldReturnFalseForNonExistingEmail() {
            assertFalse(userRepository.existsByEmail("nobody@example.com"));
        }
    }


    // ==================== UPDATE USER PROFILE ====================

    @Nested
    @DisplayName("Update User Profile")
    class UpdateUserProfile {

        @Test
        @DisplayName("Should update user first name and last name")
        void shouldUpdateUserName() {
            testUser.setFirstName("Jane");
            testUser.setLastName("Smith");
            User updated = userRepository.save(testUser);

            assertEquals("Jane", updated.getFirstName());
            assertEquals("Smith", updated.getLastName());
            assertEquals("Jane Smith", updated.getFullName());
        }

        @Test
        @DisplayName("Should update user phone number")
        void shouldUpdateUserPhone() {
            testUser.setPhone("0779876543");
            User updated = userRepository.save(testUser);

            assertEquals("0779876543", updated.getPhone());
        }

        @Test
        @DisplayName("Should update user profile picture")
        void shouldUpdateProfilePicture() {
            testUser.setProfilePicture("https://example.com/photo.jpg");
            User updated = userRepository.save(testUser);

            assertEquals("https://example.com/photo.jpg", updated.getProfilePicture());
        }
    }


    // ==================== UPDATE USER STATUS ====================

    @Nested
    @DisplayName("Update User Status")
    class UpdateUserStatus {

        @Test
        @DisplayName("Should update status to INACTIVE")
        void shouldUpdateStatusToInactive() {
            testUser.setStatus(UserStatus.INACTIVE);
            User updated = userRepository.save(testUser);

            assertEquals(UserStatus.INACTIVE, updated.getStatus());
        }

        @Test
        @DisplayName("Should update status to BANNED")
        void shouldUpdateStatusToBanned() {
            testUser.setStatus(UserStatus.BANNED);
            User updated = userRepository.save(testUser);

            assertEquals(UserStatus.BANNED, updated.getStatus());
        }

        @Test
        @DisplayName("Should update status from BANNED back to ACTIVE")
        void shouldUpdateStatusFromBannedToActive() {
            testUser.setStatus(UserStatus.BANNED);
            userRepository.save(testUser);

            testUser.setStatus(UserStatus.ACTIVE);
            User updated = userRepository.save(testUser);

            assertEquals(UserStatus.ACTIVE, updated.getStatus());
        }
    }


    // ==================== GET ALL USERS ====================

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsers {

        @Test
        @DisplayName("Should return all users with pagination")
        void shouldReturnAllUsersWithPagination() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> page = userRepository.findAll(pageable);

            assertEquals(2, page.getTotalElements());
            assertEquals(1, page.getTotalPages());
        }

        @Test
        @DisplayName("Should return correct page size")
        void shouldReturnCorrectPageSize() {
            // Add more users
            for (int i = 0; i < 5; i++) {
                User user = new User("User" + i, "Test", "user" + i + "@example.com", "pass");
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
            }

            Pageable pageable = PageRequest.of(0, 3);
            Page<User> page = userRepository.findAll(pageable);

            assertEquals(7, page.getTotalElements());  // 2 from setup + 5 new
            assertEquals(3, page.getSize());
            assertEquals(3, page.getTotalPages());
        }
    }


    // ==================== FIND BY STATUS ====================

    @Nested
    @DisplayName("Find Users By Status")
    class FindByStatus {

        @Test
        @DisplayName("Should find only active users")
        void shouldFindActiveUsers() {
            // Set one user as inactive
            adminUser.setStatus(UserStatus.INACTIVE);
            userRepository.save(adminUser);

            Pageable pageable = PageRequest.of(0, 10);
            Page<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE, pageable);

            assertEquals(1, activeUsers.getTotalElements());
            assertEquals("john@example.com", activeUsers.getContent().get(0).getEmail());
        }

        @Test
        @DisplayName("Should return empty page when no users with status")
        void shouldReturnEmptyWhenNoUsersWithStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> bannedUsers = userRepository.findByStatus(UserStatus.BANNED, pageable);

            assertEquals(0, bannedUsers.getTotalElements());
            assertTrue(bannedUsers.getContent().isEmpty());
        }
    }


    // ==================== SEARCH USERS ====================

    @Nested
    @DisplayName("Search Users")
    class SearchUsers {

        @Test
        @DisplayName("Should find users by first name")
        void shouldFindUsersByFirstName() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchUsers("John", pageable);

            assertEquals(1, results.getTotalElements());
            assertEquals("john@example.com", results.getContent().get(0).getEmail());
        }

        @Test
        @DisplayName("Should find users by last name")
        void shouldFindUsersByLastName() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchUsers("Doe", pageable);

            assertEquals(1, results.getTotalElements());
        }

        @Test
        @DisplayName("Should find users by email")
        void shouldFindUsersByEmail() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchUsers("admin@example", pageable);

            assertEquals(1, results.getTotalElements());
            assertEquals("Admin", results.getContent().get(0).getFirstName());
        }

        @Test
        @DisplayName("Should return empty for non-matching search")
        void shouldReturnEmptyForNonMatchingSearch() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchUsers("zzzznonexistent", pageable);

            assertEquals(0, results.getTotalElements());
        }

        @Test
        @DisplayName("Search should be case-insensitive")
        void searchShouldBeCaseInsensitive() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> results = userRepository.searchUsers("john", pageable);

            assertEquals(1, results.getTotalElements());
        }
    }


    // ==================== COUNT BY STATUS ====================

    @Nested
    @DisplayName("Count By Status")
    class CountByStatus {

        @Test
        @DisplayName("Should count active users correctly")
        void shouldCountActiveUsers() {
            long count = userRepository.countByStatus(UserStatus.ACTIVE);
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should return zero for status with no users")
        void shouldReturnZeroForEmptyStatus() {
            long count = userRepository.countByStatus(UserStatus.BANNED);
            assertEquals(0, count);
        }

        @Test
        @DisplayName("Should update count after status change")
        void shouldUpdateCountAfterStatusChange() {
            testUser.setStatus(UserStatus.BANNED);
            userRepository.save(testUser);

            assertEquals(1, userRepository.countByStatus(UserStatus.ACTIVE));
            assertEquals(1, userRepository.countByStatus(UserStatus.BANNED));
        }
    }


    // ==================== USER ENTITY VALIDATIONS ====================

    @Nested
    @DisplayName("User Entity")
    class UserEntity {

        @Test
        @DisplayName("Should generate full name correctly")
        void shouldGenerateFullName() {
            assertEquals("John Doe", testUser.getFullName());
        }

        @Test
        @DisplayName("Should have createdAt after persist")
        void shouldHaveCreatedAtAfterPersist() {
            assertNotNull(testUser.getCreatedAt());
        }

        @Test
        @DisplayName("Should have updatedAt after persist")
        void shouldHaveUpdatedAtAfterPersist() {
            assertNotNull(testUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Should have auto-generated ID")
        void shouldHaveAutoGeneratedId() {
            assertNotNull(testUser.getId());
            assertTrue(testUser.getId() > 0);
        }

        @Test
        @DisplayName("Default status should be ACTIVE")
        void defaultStatusShouldBeActive() {
            User newUser = new User("New", "User", "new@example.com", "pass");
            User saved = userRepository.save(newUser);

            assertEquals(UserStatus.ACTIVE, saved.getStatus());
        }
    }
}
