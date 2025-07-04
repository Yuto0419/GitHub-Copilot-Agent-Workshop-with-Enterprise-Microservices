package com.skishop.auth.controller;

import com.skishop.auth.config.TestSecurityConfig;
import com.skishop.auth.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.cloud.azure.active-directory.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
    "spring.security.oauth2.client.registration.azure.client-id=test-client-id",
    "spring.security.oauth2.client.registration.azure.client-secret=test-client-secret",
    "spring.security.oauth2.client.provider.azure.issuer-uri=http://localhost:8080/test-issuer",
    "jwt.secret=test-secret-key-for-jwt-that-is-long-enough-for-hs256-algorithm",
    "jwt.expiration=3600000",
    "app.protect.authenticated=/token_details,/call_graph,/profile,/admin/**"
})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GraphService graphService;

    @Test
    void testHomePageAccessible() throws Exception {
        // Home page should be accessible without authentication according to SecurityConfig
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @WithMockUser
    void testUserInfoEndpoint() throws Exception {
        // Create a mock for GraphService that returns a Map<String, Object> with the correct structure
        Map<String, Object> mockUserData = new HashMap<>();
        mockUserData.put("displayName", "Test User");
        mockUserData.put("mail", "test@example.com");
        mockUserData.put("id", "test-user-id");
        mockUserData.put("userPrincipalName", "test@example.com"); // Required for call_graph.html
        mockUserData.put("givenName", "Test"); // Required for call_graph.html
        mockUserData.put("surname", "User"); // Required for call_graph.html
        mockUserData.put("preferredLanguage", "en-US"); // Required for call_graph.html
        mockUserData.put("businessPhones", Arrays.asList("+1-555-0100")); // Required for call_graph.html
        mockUserData.put("jobTitle", "Software Engineer");
        mockUserData.put("mobilePhone", "+1234567890");
        mockUserData.put("officeLocation", "Seattle");
        
        when(graphService.getUserDetails(any())).thenReturn(mockUserData);
        
        // Test the call_graph endpoint (with an existing template)
        mockMvc.perform(get("/call_graph"))
                .andExpect(status().isOk())
                .andExpect(view().name("call_graph"))
                .andExpect(model().attribute("displayName", "Test User"))
                .andExpect(model().attribute("mail", "test@example.com"));
    }

    @Test
    void testLoginPageMapping() throws Exception {
        // Test that AuthController's /login endpoint is correctly mapped
        // Verify that login.html template exists and renders normally
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser
    void testApiUserMeEndpoint() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not authenticated"));
    }

    @Test
    @WithMockUser
    void testApiGraphUserEndpoint() throws Exception {
        // Create a mock for GraphService that returns a Map<String, Object>
        Map<String, Object> mockUserData = new HashMap<>();
        mockUserData.put("displayName", "Test User");
        mockUserData.put("mail", "test@example.com");
        mockUserData.put("id", "test-user-id");
        
        when(graphService.getUserDetails(any())).thenReturn(mockUserData);
        
        mockMvc.perform(get("/api/graph/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.mail").value("test@example.com"));
    }
}
