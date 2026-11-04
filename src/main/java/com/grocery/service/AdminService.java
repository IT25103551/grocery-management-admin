package com.grocery.service;

import com.grocery.model.Admin;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * All business logic and FILE HANDLING for admins.
 * Data is stored in admins.txt — one admin per line, values separated by commas.
 *
 *  CREATE  → register()
 *  READ    → getAllAdmins(), findById(), findByUsername(), search(), login()
 *  UPDATE  → updateDetails(), changeLevel(), toggleActive(), update()
 *  DELETE  → delete()
 */
public class AdminService {

    // Relative to the project folder (IntelliJ runs the app from there)
    private static final String FILE_PATH = "src/main/resources/data/admins.txt";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ──────────────────────────────────────────
    // CREATE — Register a new admin (append one line to the file)
    // ──────────────────────────────────────────
    public boolean register(Admin admin) {
        if (findByUsername(admin.getUsername()) != null) return false;
        if (findByEmail(admin.getEmail())       != null) return false;

        admin.setId(generateId());
        admin.setRole("ADMIN");
        admin.setActive(true);
        admin.setLastLogin("Never");

        // FileWriter(path, true) → APPEND mode: add to the end, keep existing lines
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(admin.toLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────
    // READ — Load every admin from the file
    // ──────────────────────────────────────────
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return admins;

        // try-with-resources closes the reader automatically
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Admin a = Admin.fromLine(line);
                    if (a != null) admins.add(a);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return admins;
    }

    // READ — Find admin by ID
    public Admin findById(String id) {
        return getAllAdmins().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // READ — Find admin by username (used for login and duplicate check)
    public Admin findByUsername(String username) {
        return getAllAdmins().stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    // READ — Find admin by email (duplicate check)
    public Admin findByEmail(String email) {
        return getAllAdmins().stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // READ — Search admins by username, email or level
    public List<Admin> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllAdmins();
        String k = keyword.trim().toLowerCase();
        return getAllAdmins().stream()
                .filter(a -> a.getUsername().toLowerCase().contains(k)
                          || a.getEmail().toLowerCase().contains(k)
                          || a.getAdminLevel().toLowerCase().contains(k))
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────
    // LOGIN — check username, active status and password
    // ──────────────────────────────────────────
    public Admin login(String username, String password) {
        Admin admin = findByUsername(username);
        if (admin == null)                   return null;
        if (!admin.isActive())               return null; // deactivated admin cannot log in
        if (!admin.checkPassword(password))  return null; // inherited from User

        // Record the login time in the file
        admin.setLastLogin(LocalDateTime.now().format(FORMATTER));
        update(admin);
        return admin;
    }

    // ──────────────────────────────────────────
    // UPDATE — replace one admin and rewrite the file
    // ──────────────────────────────────────────
    public boolean update(Admin updatedAdmin) {
        List<Admin> admins = getAllAdmins();
        boolean found = false;

        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(updatedAdmin.getId())) {
                admins.set(i, updatedAdmin);
                found = true;
                break;
            }
        }
        if (!found) return false;
        return writeAllAdmins(admins);
    }

    // UPDATE — Edit profile details. A blank password means "keep the old one".
    // Returns an error message, or null if it worked.
    public String updateDetails(String id, String email, String phone,
                                String address, String newPassword) {
        Admin admin = findById(id);
        if (admin == null) return "Admin not found.";

        Admin sameEmail = findByEmail(email);
        if (sameEmail != null && !sameEmail.getId().equals(id)) {
            return "That email is already used by another admin.";
        }

        admin.setEmail(email);
        admin.setPhone(phone);
        admin.setAddress(address);
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) return "Password must be at least 6 characters.";
            admin.setPassword(newPassword);
        }
        return update(admin) ? null : "Could not save changes.";
    }

    // UPDATE — Activate / deactivate (a soft alternative to deleting)
    public boolean toggleActive(String adminId) {
        Admin admin = findById(adminId);
        if (admin == null) return false;
        admin.setActive(!admin.isActive());
        return update(admin);
    }

    // UPDATE — Change admin level (SUPER / MANAGER / STAFF)
    public boolean changeLevel(String adminId, String newLevel) {
        Admin admin = findById(adminId);
        if (admin == null) return false;
        admin.setAdminLevel(newLevel);
        return update(admin);
    }

    // ──────────────────────────────────────────
    // DELETE — remove an admin and rewrite the file
    // ──────────────────────────────────────────
    public boolean delete(String id) {
        List<Admin> admins = getAllAdmins();
        boolean removed = admins.removeIf(a -> a.getId().equals(id));
        if (!removed) return false;
        return writeAllAdmins(admins);
    }

    // ──────────────────────────────────────────
    // DASHBOARD — statistics calculated from admins.txt
    // ──────────────────────────────────────────
    public Map<String, Object> getDashboardStats() {
        List<Admin> admins = getAllAdmins();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAdmins",    admins.size());
        stats.put("activeAdmins",   admins.stream().filter(Admin::isActive).count());
        stats.put("inactiveAdmins", admins.stream().filter(a -> !a.isActive()).count());

        stats.put("superCount",   countLevel(admins, Admin.SUPER));
        stats.put("managerCount", countLevel(admins, Admin.MANAGER));
        stats.put("staffCount",   countLevel(admins, Admin.STAFF));

        // Activity log: 5 most recent logins (date format sorts correctly as text)
        List<Admin> recentLogins = admins.stream()
                .filter(a -> !"Never".equals(a.getLastLogin()))
                .sorted(Comparator.comparing(Admin::getLastLogin).reversed())
                .limit(5)
                .collect(Collectors.toList());
        stats.put("recentLogins", recentLogins);

        // Admins who have never logged in
        stats.put("neverLoggedIn",
                admins.stream().filter(a -> "Never".equals(a.getLastLogin())).count());

        return stats;
    }

    private long countLevel(List<Admin> admins, String level) {
        return admins.stream().filter(a -> level.equals(a.getAdminLevel())).count();
    }

    // ──────────────────────────────────────────
    // HELPER — overwrite the whole file with the given list
    // FileWriter(path, false) → OVERWRITE mode
    // ──────────────────────────────────────────
    private boolean writeAllAdmins(List<Admin> admins) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Admin a : admins) {
                writer.write(a.toLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Unique ID based on the current time, e.g. A1731330000000
    private String generateId() {
        return "A" + System.currentTimeMillis();
    }
}
