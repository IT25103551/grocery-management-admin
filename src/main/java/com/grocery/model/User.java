package com.grocery.model;

/**
 * Base class for every person who can log in to the system.
 *
 * OOP: Abstraction — User is ABSTRACT, so you can never create a plain "User".
 *      It only defines what every user has in common. A concrete subclass
 *      (Admin) must fill in the abstract methods below.
 *
 * OOP: Encapsulation / Information hiding — all fields are private and can only
 *      be read or changed through public getters and setters.
 */
public abstract class User {

    private String id;
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String role;

    // Default constructor
    public User() {}

    // Full constructor — called by subclasses using super(...)
    public User(String id, String username, String email,
                String password, String phone, String address, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    // ── Abstract methods: every subclass MUST implement these ──

    /** Convert the object into one line for its text file. */
    public abstract String toLine();

    /** Short description of what this kind of user is allowed to do. */
    public abstract String getPermissionsSummary();

    // ── Shared behaviour (inherited by every subclass) ──

    /** Check a password typed at login against the stored one. */
    public boolean checkPassword(String typedPassword) {
        return password != null && password.equals(typedPassword);
    }

    // ── Getters and setters (Encapsulation) ──

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }

    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    public String getPassword()                  { return password; }
    public void   setPassword(String password)   { this.password = password; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String phone)     { this.phone = phone; }

    public String getAddress()                   { return address; }
    public void   setAddress(String address)     { this.address = address; }

    public String getRole()                  { return role; }
    public void   setRole(String role)       { this.role = role; }

    @Override
    public String toString() {
        return "User{id='" + id + "', username='" + username +
                "', email='" + email + "', role='" + role + "'}";
    }
}
