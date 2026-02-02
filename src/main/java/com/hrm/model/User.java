package com.hrm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User model for authentication
 * Supports dynamic RBAC with user-specific permission exceptions
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private boolean active;
    private boolean locked;
    private List<Role> roles;
    // User-specific permission exceptions (key = permissionCode, value = granted/denied)
    private Map<String, Boolean> userPermissions;

    public User(int id, String username, String password, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.active = true;
        this.locked = false;
        this.roles = new ArrayList<>();
        this.userPermissions = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public Map<String, Boolean> getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(Map<String, Boolean> userPermissions) {
        this.userPermissions = userPermissions;
    }

    /**
     * Grant a specific permission to this user (exception)
     */
    public void grantPermission(String permissionCode) {
        userPermissions.put(permissionCode, true);
    }

    /**
     * Deny a specific permission from this user (exception)
     */
    public void denyPermission(String permissionCode) {
        userPermissions.put(permissionCode, false);
    }

    /**
     * Remove a user-specific permission exception
     */
    public void removePermissionException(String permissionCode) {
        userPermissions.remove(permissionCode);
    }

    /**
     * Check if user has a permission using dynamic RBAC formula:
     * Effective = (Role Permissions) ∪ (Granted) - (Denied)
     */
    public boolean hasPermission(String permissionCode) {
        // First check user-specific exceptions
        if (userPermissions.containsKey(permissionCode)) {
            return userPermissions.get(permissionCode);
        }
        // Then check role permissions
        return roles.stream()
                .anyMatch(role -> role.hasPermission(permissionCode));
    }

    /**
     * Check if permission is explicitly granted/denied for this user
     * Returns: null = from role, true = explicitly granted, false = explicitly denied
     */
    public Boolean getPermissionStatus(String permissionCode) {
        return userPermissions.get(permissionCode);
    }

    public boolean hasRole(String roleCode) {
        return roles.stream()
                .anyMatch(role -> role.getCode().equals(roleCode));
    }

    public String getRoleNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(roles.get(i).getName());
        }
        return sb.toString();
    }

    public String getRoleCodes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < roles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(roles.get(i).getCode());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}

