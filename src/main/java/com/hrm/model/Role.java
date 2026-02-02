package com.hrm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Role model for RBAC
 * Supports dynamic permission assignment
 */
public class Role {
    private String code;
    private String name;
    private String description;
    private boolean systemRole; // System roles cannot be deleted
    private List<Permission> permissions;

    public Role(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.systemRole = false;
        this.permissions = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(Permission permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public void clearPermissions() {
        permissions.clear();
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getCode().equals(permissionCode));
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}

