package com.skishop.user.service;

import com.skishop.user.entity.User;
import com.skishop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Common service for user search and filtering
 * Unifies complex search criteria processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * Unified user search API
     */
    public Page<User> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        log.debug("Executing user search: criteria={}", criteria);
        
        Specification<User> spec = buildSpecification(criteria);
        Page<User> result = userRepository.findAll(spec, pageable);
        
        log.debug("User search results: totalElements={}, totalPages={}", 
                result.getTotalElements(), result.getTotalPages());
        
        return result;
    }

    /**
     * Build specification from search criteria
     */
    public Specification<User> buildSpecification(UserSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search keyword condition
            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                Predicate searchPredicate = buildSearchPredicate(root, criteriaBuilder, criteria.getSearch());
                predicates.add(searchPredicate);
            }

            // Status condition
            if (criteria.getStatus() != null && !criteria.getStatus().trim().isEmpty()) {
                Predicate statusPredicate = buildStatusPredicate(root, criteriaBuilder, criteria.getStatus());
                predicates.add(statusPredicate);
            }

            // Role condition
            if (criteria.getRole() != null && !criteria.getRole().trim().isEmpty()) {
                Predicate rolePredicate = buildRolePredicate(root, criteriaBuilder, criteria.getRole());
                predicates.add(rolePredicate);
            }

            // Email verification status condition
            if (criteria.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), criteria.getEmailVerified()));
            }

            // Phone verification status condition
            if (criteria.getPhoneVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("phoneVerified"), criteria.getPhoneVerified()));
            }

            // Creation date range condition
            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }
            
            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build search keyword condition
     */
    private Predicate buildSearchPredicate(jakarta.persistence.criteria.Root<User> root, 
                                         jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                                         String search) {
        String searchPattern = "%" + search.toLowerCase() + "%";
        
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), searchPattern)
        );
    }

    /**
     * Build status predicate
     */
    private Predicate buildStatusPredicate(jakarta.persistence.criteria.Root<User> root, 
                                         jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                                         String status) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            return criteriaBuilder.equal(root.get("status"), userStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value: {}", status);
            // Ignore condition for invalid status
            return criteriaBuilder.conjunction();
        }
    }

    /**
     * Build role predicate
     */
    private Predicate buildRolePredicate(jakarta.persistence.criteria.Root<User> root, 
                                       jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                                       String role) {
        return criteriaBuilder.equal(root.join("role").get("name"), role);
    }

    /**
     * Search criteria class
     */
    public static class UserSearchCriteria {
        private String search;
        private String status;
        private String role;
        private Boolean emailVerified;
        private Boolean phoneVerified;
        private java.time.LocalDateTime createdAfter;
        private java.time.LocalDateTime createdBefore;

        // Getters and Setters
        public String getSearch() { return search; }
        public void setSearch(String search) { this.search = search; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public Boolean getPhoneVerified() { return phoneVerified; }
        public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

        public java.time.LocalDateTime getCreatedAfter() { return createdAfter; }
        public void setCreatedAfter(java.time.LocalDateTime createdAfter) { this.createdAfter = createdAfter; }

        public java.time.LocalDateTime getCreatedBefore() { return createdBefore; }
        public void setCreatedBefore(java.time.LocalDateTime createdBefore) { this.createdBefore = createdBefore; }

        @Override
        public String toString() {
            return "UserSearchCriteria{" +
                    "search='" + search + '\'' +
                    ", status='" + status + '\'' +
                    ", role='" + role + '\'' +
                    ", emailVerified=" + emailVerified +
                    ", phoneVerified=" + phoneVerified +
                    ", createdAfter=" + createdAfter +
                    ", createdBefore=" + createdBefore +
                    '}';
        }
    }
}
