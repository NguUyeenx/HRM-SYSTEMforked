package com.hrm.service;

import com.hrm.model.User;
import com.hrm.util.SessionContext;

/**
 * Authentication Service
 * Handles login/logout operations
 */
public class AuthService {
    private static AuthService instance;
    private MockDataService mockDataService;

    private AuthService() {
        mockDataService = MockDataService.getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authenticate user with username and password
     * @param username the username
     * @param password the password
     * @return User object if successful, null otherwise
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }

        User user = mockDataService.getUser(username.trim().toLowerCase());

        if (user != null && user.isActive() && user.getPassword().equals(password)) {
            // Set current session
            SessionContext.getInstance().setCurrentUser(user);
            return user;
        }

        return null;
    }

    /**
     * Logout current user
     */
    public void logout() {
        SessionContext.getInstance().clearSession();
    }

    /**
     * Check if user is logged in
     * @return true if logged in
     */
    public boolean isLoggedIn() {
        return SessionContext.getInstance().getCurrentUser() != null;
    }

    /**
     * Get current logged-in user
     * @return current user or null
     */
    public User getCurrentUser() {
        return SessionContext.getInstance().getCurrentUser();
    }

    /**
     * Check if current user has permission
     * @param permissionCode permission code to check
     * @return true if has permission
     */
    public boolean hasPermission(String permissionCode) {
        User user = getCurrentUser();
        return user != null && user.hasPermission(permissionCode);
    }

    /**
     * Check if current user has role
     * @param roleCode role code to check
     * @return true if has role
     */
    public boolean hasRole(String roleCode) {
        User user = getCurrentUser();
        return user != null && user.hasRole(roleCode);
    }
}
