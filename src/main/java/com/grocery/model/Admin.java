package com.grocery.model;

/**
 * An administrator of the Grocery Order system.
 *
 * OOP: Inheritance — Admin EXTENDS User, so it inherits id, username, email,
 *      password, phone, address and role. This is an IS-A relationship:
 *      an Admin IS-A User with extra fields and privileges.
 *
 * File format (one admin per line in admins.txt):
 *   id,username,email,password,phone,address,adminLevel,lastLogin,active
 */
public class Admin extends User {

    // Admin levels
    public static final String SUPER   = "SUPER";
    public static final String MANAGER = "MANAGER";
    public static final String STAFF   = "STAFF";

    // Extra fields only an Admin has (Encapsulation: private)
    private String  adminLevel;   // SUPER, MANAGER or STAFF
    private String  lastLogin;    // "yyyy-MM-dd HH:mm:ss" or "Never"
    private boolean active;       // inactive admins cannot log in

    public Admin() {
        super();
        this.setRole("ADMIN"); // an Admin's role is always ADMIN
    }

    public Admin(String id, String username, String email, String password,
                 String phone, String address,
                 String adminLevel, String lastLogin, boolean active) {
        // Reuse the parent (User) constructor
        super(id, username, email, password, phone, address, "ADMIN");
        this.adminLevel = adminLevel;
        this.lastLogin  = lastLogin;
        this.active     = active;
    }

    // ──────────────────────────────────────────
    // FILE HANDLING helpers
    // ──────────────────────────────────────────

    /** Build an Admin object from one line of admins.txt. */
    public static Admin fromLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) return null;   // skip broken lines
        try {
            return new Admin(
                    parts[0].trim(), parts[1].trim(), parts[2].trim(),
                    parts[3].trim(), parts[4].trim(), parts[5].trim(),
                    parts[6].trim(), parts[7].trim(),
                    Boolean.parseBoolean(parts[8].trim())
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * OOP: Polymorphism (method overriding) — implements the abstract
     * toLine() from User with the Admin's own file format.
     */
    @Override
    public String toLine() {
        return clean(getId()) + "," + clean(getUsername()) + "," + clean(getEmail()) + "," +
               clean(getPassword()) + "," + clean(getPhone()) + "," + clean(getAddress()) + "," +
               clean(adminLevel) + "," + clean(lastLogin) + "," + active;
    }

    /** Commas or new lines inside a value would break the comma-separated file. */
    private static String clean(String value) {
        if (value == null) return "";
        return value.replace(",", " ").replace("\r", " ").replace("\n", " ").trim();
    }

    // ──────────────────────────────────────────
    // ADMIN-ONLY BEHAVIOUR (Abstraction)
    // The servlet just asks "can this admin do X?" — the rules are hidden here.
    // ──────────────────────────────────────────

    /** Only an active SUPER admin can create, delete, activate or change levels. */
    public boolean canManageAdmins() {
        return active && SUPER.equals(adminLevel);
    }

    /** Can this admin edit the given admin's details? Super: anyone. Others: only themselves. */
    public boolean canEdit(Admin other) {
        return canManageAdmins() || (other != null && getId().equals(other.getId()));
    }

    /** OOP: Polymorphism — implements the abstract method from User. */
    @Override
    public String getPermissionsSummary() {
        if (SUPER.equals(adminLevel))   return "Full access: register, edit, delete and manage all admins";
        if (MANAGER.equals(adminLevel)) return "View all admins and edit own profile";
        return "View all admins and edit own profile (staff)";
    }

    /** OOP: Polymorphism — overrides toString() from User. */
    @Override
    public String toString() {
        return "Admin{id='" + getId() + "', username='" + getUsername() +
                "', level='" + adminLevel + "', active=" + active + "}";
    }

    // Getters and setters
    public String  getAdminLevel()                   { return adminLevel; }
    public void    setAdminLevel(String adminLevel)  { this.adminLevel = adminLevel; }

    public String  getLastLogin()                    { return lastLogin; }
    public void    setLastLogin(String lastLogin)    { this.lastLogin = lastLogin; }

    public boolean isActive()                        { return active; }
    public void    setActive(boolean active)         { this.active = active; }
}
