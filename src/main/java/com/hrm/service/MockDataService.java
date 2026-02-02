package com.hrm.service;

import com.hrm.model.Permission;
import com.hrm.model.Role;
import com.hrm.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock Data Service - In-memory data storage
 * Singleton pattern for global access
 * Supports dynamic RBAC with user/role/permission management
 */
public class MockDataService {
    private static MockDataService instance;

    private Map<String, Permission> permissions;
    private Map<String, Role> roles;
    private Map<String, User> users;
    private int nextUserId = 5;

    private MockDataService() {
        permissions = new HashMap<>();
        roles = new HashMap<>();
        users = new HashMap<>();
    }

    public static synchronized MockDataService getInstance() {
        if (instance == null) {
            instance = new MockDataService();
        }
        return instance;
    }

    /**
     * Initialize all mock data
     */
    public void initializeData() {
        initializePermissions();
        initializeRoles();
        initializeUsers();
    }

    private void initializePermissions() {
        // Leave Management Permissions
        addPermission("LEAVE_CREATE", "Tao don nghi phep", "Leave");
        addPermission("LEAVE_VIEW_SELF", "Xem nghi phep ca nhan", "Leave");
        addPermission("LEAVE_VIEW_ALL", "Xem tat ca nghi phep", "Leave");
        addPermission("LEAVE_APPROVE", "Duyet nghi phep", "Leave");
        addPermission("LEAVE_MANAGE", "Quan ly nghi phep", "Leave");

        // Performance Evaluation Permissions
        addPermission("EVAL_VIEW_SELF", "Xem danh gia ca nhan", "Evaluation");
        addPermission("EVAL_VIEW_ALL", "Xem tat ca danh gia", "Evaluation");
        addPermission("EVAL_REVIEW", "Danh gia nhan vien", "Evaluation");
        addPermission("EVAL_MANAGE", "Quan ly danh gia", "Evaluation");

        // Employee Permissions
        addPermission("VIEW_SELF", "Xem thong tin ca nhan", "Employee");
        addPermission("EMPLOYEE_VIEW", "Xem danh sach nhan vien", "Employee");
        addPermission("EMPLOYEE_CREATE", "Tao nhan vien", "Employee");
        addPermission("EMPLOYEE_UPDATE", "Cap nhat nhan vien", "Employee");
        addPermission("EMPLOYEE_DELETE", "Xoa nhan vien", "Employee");

        // User Management Permissions
        addPermission("USER_VIEW", "Xem danh sach tai khoan", "User");
        addPermission("USER_CREATE", "Tao tai khoan", "User");
        addPermission("USER_UPDATE", "Cap nhat tai khoan", "User");
        addPermission("USER_DELETE", "Xoa tai khoan", "User");

        // Role Management Permissions
        addPermission("ROLE_VIEW", "Xem vai tro", "Role");
        addPermission("ROLE_CREATE", "Tao vai tro", "Role");
        addPermission("ROLE_UPDATE", "Cap nhat vai tro", "Role");
        addPermission("ROLE_DELETE", "Xoa vai tro", "Role");

        // Report Permissions
        addPermission("REPORT_VIEW", "Xem bao cao", "Report");
        addPermission("REPORT_EXPORT", "Xuat bao cao", "Report");

        // Settings Permissions
        addPermission("SETTINGS_VIEW", "Xem cai dat", "Settings");
        addPermission("SETTINGS_UPDATE", "Cap nhat cai dat", "Settings");
    }

    private void addPermission(String code, String name, String module) {
        permissions.put(code, new Permission(code, name, module));
    }

    private void initializeRoles() {
        // ADMIN Role - All permissions
        Role admin = new Role("ADMIN", "Quan tri vien", "Toan quyen quan tri he thong");
        admin.setSystemRole(true);
        for (Permission p : permissions.values()) {
            admin.addPermission(p);
        }
        roles.put("ADMIN", admin);

        // HR Role
        Role hr = new Role("HR", "Nhan su", "Quan ly nhan vien, hop dong, nghi phep");
        hr.addPermission(permissions.get("LEAVE_VIEW_ALL"));
        hr.addPermission(permissions.get("LEAVE_MANAGE"));
        hr.addPermission(permissions.get("LEAVE_APPROVE"));
        hr.addPermission(permissions.get("EVAL_VIEW_ALL"));
        hr.addPermission(permissions.get("EVAL_MANAGE"));
        hr.addPermission(permissions.get("EMPLOYEE_VIEW"));
        hr.addPermission(permissions.get("EMPLOYEE_CREATE"));
        hr.addPermission(permissions.get("EMPLOYEE_UPDATE"));
        hr.addPermission(permissions.get("VIEW_SELF"));
        hr.addPermission(permissions.get("LEAVE_VIEW_SELF"));
        hr.addPermission(permissions.get("LEAVE_CREATE"));
        hr.addPermission(permissions.get("EVAL_VIEW_SELF"));
        hr.addPermission(permissions.get("REPORT_VIEW"));
        roles.put("HR", hr);

        // MANAGER Role
        Role manager = new Role("MANAGER", "Quan ly", "Quan ly team, duyet phep, danh gia nhan vien");
        manager.addPermission(permissions.get("LEAVE_APPROVE"));
        manager.addPermission(permissions.get("LEAVE_VIEW_ALL"));
        manager.addPermission(permissions.get("EVAL_REVIEW"));
        manager.addPermission(permissions.get("EVAL_VIEW_ALL"));
        manager.addPermission(permissions.get("EMPLOYEE_VIEW"));
        manager.addPermission(permissions.get("VIEW_SELF"));
        manager.addPermission(permissions.get("LEAVE_VIEW_SELF"));
        manager.addPermission(permissions.get("LEAVE_CREATE"));
        manager.addPermission(permissions.get("EVAL_VIEW_SELF"));
        manager.addPermission(permissions.get("REPORT_VIEW"));
        roles.put("MANAGER", manager);

        // EMPLOYEE Role
        Role employee = new Role("EMPLOYEE", "Nhan vien", "Xem thong tin ca nhan, dang ky nghi phep");
        employee.addPermission(permissions.get("VIEW_SELF"));
        employee.addPermission(permissions.get("LEAVE_VIEW_SELF"));
        employee.addPermission(permissions.get("LEAVE_CREATE"));
        employee.addPermission(permissions.get("EVAL_VIEW_SELF"));
        roles.put("EMPLOYEE", employee);
    }

    private void initializeUsers() {
        // Admin user
        User admin = new User(1, "admin", "123", "Administrator", "admin@hrm.local");
        admin.addRole(roles.get("ADMIN"));
        users.put("admin", admin);

        // HR user
        User hr = new User(2, "hr", "123", "Tran Thi Binh", "hr@hrm.local");
        hr.addRole(roles.get("HR"));
        users.put("hr", hr);

        // Manager user
        User manager = new User(3, "manager", "123", "Le Van Cuong", "manager@hrm.local");
        manager.addRole(roles.get("MANAGER"));
        users.put("manager", manager);

        // Employee user
        User employee = new User(4, "employee", "123", "Nguyen Van An", "employee@hrm.local");
        employee.addRole(roles.get("EMPLOYEE"));
        users.put("employee", employee);
    }

    // ==================== Permission Methods ====================
    
    public Permission getPermission(String code) {
        return permissions.get(code);
    }

    public List<Permission> getAllPermissions() {
        return new ArrayList<>(permissions.values());
    }

    public List<Permission> getPermissionsByModule(String module) {
        List<Permission> result = new ArrayList<>();
        for (Permission p : permissions.values()) {
            if (p.getModule().equals(module)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<String> getPermissionModules() {
        List<String> modules = new ArrayList<>();
        for (Permission p : permissions.values()) {
            if (!modules.contains(p.getModule())) {
                modules.add(p.getModule());
            }
        }
        return modules;
    }

    // ==================== Role Methods ====================

    public Role getRole(String code) {
        return roles.get(code);
    }

    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public void saveRole(Role role) {
        roles.put(role.getCode(), role);
    }

    public boolean deleteRole(String code) {
        Role role = roles.get(code);
        if (role != null && !role.isSystemRole()) {
            // Check if role is assigned to any user
            for (User user : users.values()) {
                if (user.hasRole(code)) {
                    return false; // Cannot delete role in use
                }
            }
            roles.remove(code);
            return true;
        }
        return false;
    }

    // ==================== User Methods ====================

    public User getUser(String username) {
        return users.get(username);
    }

    public User getUserById(int id) {
        for (User user : users.values()) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User createUser(String username, String password, String fullName, String email) {
        if (users.containsKey(username)) {
            return null; // Username exists
        }
        User user = new User(nextUserId++, username, password, fullName, email);
        users.put(username, user);
        return user;
    }

    public void saveUser(User user) {
        users.put(user.getUsername(), user);
    }

    public boolean deleteUser(String username) {
        if (users.containsKey(username) && !username.equals("admin")) {
            users.remove(username);
            return true;
        }
        return false;
    }

    public boolean isUsernameExists(String username) {
        return users.containsKey(username);
    }

    // ==================== User Permission Exception Methods ====================

    public void grantUserPermission(int userId, String permissionCode) {
        User user = getUserById(userId);
        if (user != null && permissions.containsKey(permissionCode)) {
            user.grantPermission(permissionCode);
        }
    }

    public void denyUserPermission(int userId, String permissionCode) {
        User user = getUserById(userId);
        if (user != null && permissions.containsKey(permissionCode)) {
            user.denyPermission(permissionCode);
        }
    }

    public void removeUserPermissionException(int userId, String permissionCode) {
        User user = getUserById(userId);
        if (user != null) {
            user.removePermissionException(permissionCode);
        }
    }
}

