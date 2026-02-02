package com.hrm.model;

/**
 * UserPermission - Represents a user-specific permission exception
 * Based on NV9 spec: Quyền ngoại lệ cho tài khoản (choPhep = true/false)
 * 
 * Formula: Effective Permissions = (Role Permissions) ∪ (Granted) - (Denied)
 */
public class UserPermission {
    private int userId;
    private String permissionCode;
    private boolean granted;  // true = explicitly granted, false = explicitly denied
    private String note;

    public UserPermission(int userId, String permissionCode, boolean granted) {
        this.userId = userId;
        this.permissionCode = permissionCode;
        this.granted = granted;
    }

    public UserPermission(int userId, String permissionCode, boolean granted, String note) {
        this(userId, permissionCode, granted);
        this.note = note;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return permissionCode + (granted ? " [GRANTED]" : " [DENIED]");
    }
}
