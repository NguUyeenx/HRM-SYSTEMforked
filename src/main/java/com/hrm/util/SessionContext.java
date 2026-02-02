package com.hrm.util;

import com.hrm.model.User;

/**
 * Session Context - Manages current user session
 * Singleton pattern
 */
public class SessionContext {
    private static SessionContext instance;
    private User currentUser;

    private SessionContext() {
    }

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasPermission(String permissionCode) {
        return currentUser != null && currentUser.hasPermission(permissionCode);
    }

    public boolean hasRole(String roleCode) {
        return currentUser != null && currentUser.hasRole(roleCode);
    }
}
