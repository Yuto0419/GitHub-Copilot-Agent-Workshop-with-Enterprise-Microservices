package com.skishop.auth.controller;

import com.skishop.auth.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller for authentication and Microsoft Graph API calls
 * Implementation using Microsoft Entra ID Spring Boot Starter
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final GraphService graphService;

    /**
     * Home page
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Home page (after authentication)
     */
    @GetMapping("/home")
    public String homeAuthenticated(@AuthenticationPrincipal OidcUser principal, Model model) {
        if (principal != null) {
            model.addAttribute("userName", principal.getFullName());
            model.addAttribute("userEmail", principal.getEmail());
        }
        return "home";
    }

    /**
     * Display ID token details
     */
    @GetMapping("/token_details")
    public String tokenDetails(@AuthenticationPrincipal OidcUser principal, Model model) {
        if (principal != null) {
            Map<String, Object> claims = principal.getIdToken().getClaims();
            model.addAttribute("claims", claims);
            model.addAttribute("idToken", principal.getIdToken().getTokenValue());
        }
        return "token_details";
    }

    /**
     * Call Microsoft Graph API to retrieve user information
     */
    @GetMapping("/call_graph")
    public String callGraph(Authentication authentication, Model model) {
        try {
            Map<String, Object> userDetails = graphService.getUserDetails(authentication);
            
            if (userDetails != null) {
                model.addAttribute("user", userDetails);
                model.addAttribute("displayName", userDetails.get("displayName"));
                model.addAttribute("mail", userDetails.get("mail"));
                model.addAttribute("jobTitle", userDetails.get("jobTitle"));
                model.addAttribute("mobilePhone", userDetails.get("mobilePhone"));
                model.addAttribute("officeLocation", userDetails.get("officeLocation"));
                
                log.info("Successfully retrieved user details from Microsoft Graph: {}", 
                    userDetails.get("displayName"));
            } else {
                model.addAttribute("error", "Unable to retrieve user details from Microsoft Graph");
            }
        } catch (Exception e) {
            log.error("Error calling Microsoft Graph API: {}", e.getMessage());
            model.addAttribute("error", "Error retrieving user details: " + e.getMessage());
        }
        
        return "call_graph";
    }

    /**
     * User profile
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal OidcUser principal, Model model) {
        if (principal != null) {
            model.addAttribute("principal", principal);
            model.addAttribute("name", principal.getFullName());
            model.addAttribute("email", principal.getEmail());
            model.addAttribute("preferredUsername", principal.getPreferredUsername());
        }
        return "profile";
    }

    /**
     * REST API endpoint: User information
     */
    @GetMapping("/api/user/me")
    @ResponseBody
    public Map<String, Object> getUserInfo(@AuthenticationPrincipal OidcUser principal) {
        if (principal == null) {
            return Map.of("error", "User not authenticated");
        }
        
        return Map.of(
            "name", principal.getFullName(),
            "email", principal.getEmail(),
            "preferredUsername", principal.getPreferredUsername(),
            "subject", principal.getSubject(),
            "authorities", principal.getAuthorities()
        );
    }

    /**
     * REST API endpoint: Microsoft Graph user information
     */
    @GetMapping("/api/graph/user")
    @ResponseBody
    public Map<String, Object> getGraphUserInfo(Authentication authentication) {
        try {
            Map<String, Object> userDetails = graphService.getUserDetails(authentication);
            if (userDetails != null) {
                return userDetails;
            } else {
                return Map.of("error", "Unable to retrieve user details from Microsoft Graph");
            }
        } catch (Exception e) {
            log.error("Error calling Microsoft Graph API: {}", e.getMessage());
            return Map.of("error", "Failed to retrieve user details from Microsoft Graph: " + e.getMessage());
        }
    }

    /**
     * Login failure page
     */
    @GetMapping("/login")
    public String login(Model model, String error) {
        if (error != null) {
            model.addAttribute("error", "Authentication failed. Please try again.");
        }
        return "login";
    }
}
