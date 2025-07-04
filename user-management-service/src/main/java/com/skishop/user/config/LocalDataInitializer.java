package com.skishop.user.config;

import com.skishop.user.entity.Role;
import com.skishop.user.entity.User;
import com.skishop.user.repository.RoleRepository;
import com.skishop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Test data initializer for local development environment
 */
@Component
@ConditionalOnProperty(name = "init.test.data", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class LocalDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Initializing test data for local development...");
            initializeTestData();
            log.info("Test data initialization completed");
        } else {
            log.info("Test data already exists, skipping initialization");
        }
    }

    private void initializeTestData() {
        try {
            // Create default role
            Role userRole = createDefaultRole();
            
            // Create test users
            createTestUsers(userRole);
            
        } catch (Exception e) {
            log.error("Failed to initialize test data", e);
        }
    }

    private Role createDefaultRole() {
        Role role = Role.builder()
                .name("USER")
                .description("Default user role")
                .build();
        return roleRepository.save(role);
    }

    private void createTestUsers(Role userRole) {
        // Active user
        User activeUser = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("testUser!Passw0rd"))
                .firstName("Test")
                .lastName("User")
                .phoneNumber("090-1234-5678")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(User.Gender.OTHER)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .role(userRole)
                .build();
        
        // Pending verification user
        User pendingUser = User.builder()
                .email("pending@example.com")
                .passwordHash(passwordEncoder.encode("pendingUser!Passw0rd"))
                .firstName("Pending")
                .lastName("User")
                .phoneNumber("090-8765-4321")
                .birthDate(LocalDate.of(1985, 5, 15))
                .gender(User.Gender.MALE)
                .status(User.UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .role(userRole)
                .build();
        
        // Admin user
        User adminUser = User.builder()
                .email("admin@skishop.com")
                .passwordHash(passwordEncoder.encode("adminUser!Passw0rd"))
                .firstName("Admin")
                .lastName("User")
                .phoneNumber("090-0000-1111")
                .birthDate(LocalDate.of(1980, 12, 31))
                .gender(User.Gender.FEMALE)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .role(userRole)
                .build();

        userRepository.save(activeUser);
        userRepository.save(pendingUser);
        userRepository.save(adminUser);

        log.info("Created test users:");
        log.info("  - Active User: test@example.com / testUser!Passw0rd");
        log.info("  - Pending User: pending@example.com / pendingUser!Passw0rd");
        log.info("  - Admin User: admin@skishop.com / adminUser!Passw0rd");
    }
}
