package com.vidara.tradecenter.user.service;

import com.vidara.tradecenter.user.model.Role;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserRole;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private User adminUser;

    // Unique suffix to avoid collisions with existing production data
    private String testSuffix;
    private String testEmail;
    private String adminEmail;

    @BeforeEach
    void setUp() {
        testSuffix = UUID.randomUUID().toString().substring(0, 8);
        testEmail = "test-john-" + testSuffix + "@example.com";
        adminEmail = "test-admin-" + testSuffix + "@example.com";

        // Create test user
        testUser = new User("John", "Doe", testEmail, "encodedPassword123");
        testUser.setPhone("0771234567");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = entityManager.persistAndFlush(testUser);

        // Create admin user
        adminUser = new User("Admin", "User", adminEmail, "encodedPassword456");
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = entityManager.persistAndFlush(adminUser);

        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        userRepository.deleteById(testUser.getId());
        userRepository.deleteById(adminUser.getId());
        entityManager.flush();
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
            assertEquals(testEmail, found.get().getEmail());
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
            Optional<User> found = userRepository.findByEmail(testEmail);

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
            assertTrue(userRepository.existsByEmail(testEmail));
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

            assertTrue(page.getTotalElements() >= 2, "Should have at least 2 test users");
        }

        @Test
        @DisplayName("Should return correct page size")
        void shouldReturnCorrectPageSize() {
            // Count existing before adding
            long existingCount = userRepository.count();

            // Add more users
            for (int i = 0; i < 5; i++) {
                User user = new User("User" + i, "Test", "test-extra-" + testSuffix + "-" + i + "@example.com", "pass");
                user.setStatus(UserStatus.ACTIVE);
                entityManager.persistAndFlush(user);
            }

            Pageable pageable = PageRequest.of(0, 3);
            Page<User> page = userRepository.findAll(pageable);

            assertEquals(existingCount + 5, page.getTotalElements());
            assertEquals(3, page.getSize());
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
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 100);
            Page<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE, pageable);

            assertTrue(activeUsers.getTotalElements() >= 1, "Should have at least 1 active user");
            assertTrue(activeUsers.getContent().stream().anyMatch(u -> u.getEmail().equals(testEmail)),
                    "Test user should be in active list");
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
            Pageable pageable = PageRequest.of(0, 100);
            Page<User> results = userRepository.searchUsers(testSuffix, pageable);

            assertTrue(results.getTotalElements() >= 1, "Should find at least 1 user by suffix");
            assertTrue(results.getContent().stream().anyMatch(u -> u.getEmail().equals(testEmail)),
                    "Should find user by unique suffix in email");
        }

        @Test
        @DisplayName("Should find users by last name")
        void shouldFindUsersByLastName() {
            Pageable pageable = PageRequest.of(0, 100);
            Page<User> results = userRepository.searchUsers("Doe", pageable);

            assertTrue(results.getTotalElements() >= 1, "Should find at least 1 user with last name Doe");
        }

        @Test
        @DisplayName("Should find users by email")
        void shouldFindUsersByEmail() {
            Pageable pageable = PageRequest.of(0, 100);
            Page<User> results = userRepository.searchUsers("test-admin-" + testSuffix, pageable);

            assertTrue(results.getTotalElements() >= 1, "Should find admin user by email");
            assertTrue(results.getContent().stream().anyMatch(u -> u.getFirstName().equals("Admin")),
                    "Should contain Admin user");
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
            // Search with lowercase version of part of the unique suffix
            Pageable pageable = PageRequest.of(0, 100);
            Page<User> results = userRepository.searchUsers(testSuffix.toLowerCase(), pageable);

            assertTrue(results.getTotalElements() >= 1, "Case-insensitive search should find users");
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
            assertTrue(count >= 2, "Should have at least 2 active users");
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
            long activeBefore = userRepository.countByStatus(UserStatus.ACTIVE);
            long bannedBefore = userRepository.countByStatus(UserStatus.BANNED);

            testUser.setStatus(UserStatus.BANNED);
            userRepository.save(testUser);
            entityManager.flush();

            assertEquals(activeBefore - 1, userRepository.countByStatus(UserStatus.ACTIVE));
            assertEquals(bannedBefore + 1, userRepository.countByStatus(UserStatus.BANNED));
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
            User newUser = new User("New", "User", "test-new-" + testSuffix + "@example.com", "pass");
            User saved = userRepository.save(newUser);

            assertEquals(UserStatus.ACTIVE, saved.getStatus());
        }
    }
}
