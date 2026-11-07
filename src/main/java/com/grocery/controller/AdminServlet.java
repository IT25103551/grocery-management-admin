package com.grocery.controller;

import com.grocery.model.Admin;
import com.grocery.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Admin Management.
 * Receives browser requests, calls AdminService, and chooses which page to show.
 * The logged-in admin is kept in the HTTP session under "loggedInUser".
 */
@Controller
@RequestMapping("/admin")
public class AdminServlet {

    private final AdminService adminService = new AdminService();

    // ── LOGIN / LOGOUT ─────────────────────────

    @GetMapping("/login")
    public String showLoginForm(HttpSession session) {
        if (currentAdmin(session) != null) return "redirect:/admin/dashboard";
        return "admin/admin-login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            session.setAttribute("loggedInUser", admin);
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Invalid credentials or account is inactive.");
        return "admin/admin-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login?loggedOut=true";
    }

    // ── DASHBOARD ──────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (currentAdmin(session) == null) return "redirect:/admin/login";
        Map<String, Object> stats = adminService.getDashboardStats();
        model.addAllAttributes(stats);
        return "admin/dashboard";
    }

    // ── CREATE ─────────────────────────────────

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/list?denied=true";
        return "admin/admin-register";
    }

    @PostMapping("/register")
    public String registerAdmin(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(defaultValue = "") String phone,
                                @RequestParam(defaultValue = "") String address,
                                @RequestParam String adminLevel,
                                HttpSession session,
                                Model model) {
        if (!isSuperAdmin(session)) return "redirect:/admin/list?denied=true";

        Admin admin = new Admin();
        admin.setUsername(username.trim());
        admin.setEmail(email.trim());
        admin.setPassword(password);
        admin.setPhone(phone.trim());
        admin.setAddress(address.trim());
        admin.setAdminLevel(adminLevel);

        if (adminService.register(admin)) {
            return "redirect:/admin/list?added=true";
        }
        model.addAttribute("error", "Username or email already exists.");
        return "admin/admin-register";
    }

    // ── READ (list + search) ───────────────────

    @GetMapping("/list")
    public String listAdmins(@RequestParam(required = false) String keyword,
                             HttpSession session, Model model) {
        if (currentAdmin(session) == null) return "redirect:/admin/login";
        model.addAttribute("admins", adminService.search(keyword));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "admin/admin-list";
    }

    // ── UPDATE ─────────────────────────────────

    // Show the edit form (super admin: anyone; other admins: only themselves)
    @GetMapping("/edit")
    public String showEditForm(@RequestParam String id, HttpSession session, Model model) {
        Admin current = currentAdmin(session);
        if (current == null) return "redirect:/admin/login";

        Admin target = adminService.findById(id);
        if (target == null) return "redirect:/admin/list";
        if (!current.canEdit(target)) return "redirect:/admin/list?denied=true";

        model.addAttribute("admin", target);
        return "admin/admin-edit";
    }

    @PostMapping("/edit")
    public String updateAdmin(@RequestParam String id,
                              @RequestParam String email,
                              @RequestParam(defaultValue = "") String phone,
                              @RequestParam(defaultValue = "") String address,
                              @RequestParam(defaultValue = "") String newPassword,
                              HttpSession session,
                              Model model) {
        Admin current = currentAdmin(session);
        if (current == null) return "redirect:/admin/login";

        Admin target = adminService.findById(id);
        if (target == null) return "redirect:/admin/list";
        if (!current.canEdit(target)) return "redirect:/admin/list?denied=true";

        String error = adminService.updateDetails(id, email.trim(), phone.trim(),
                                                  address.trim(), newPassword);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("admin", adminService.findById(id));
            return "admin/admin-edit";
        }

        // If admins edited their own profile, refresh the copy in the session
        if (current.getId().equals(id)) {
            session.setAttribute("loggedInUser", adminService.findById(id));
        }
        return "redirect:/admin/list?updated=true";
    }

    // Activate / deactivate
    @PostMapping("/toggle")
    public String toggleActive(@RequestParam String id, HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/list?denied=true";
        if (isSelf(session, id))     return "redirect:/admin/list?selfAction=true";
        adminService.toggleActive(id);
        return "redirect:/admin/list?toggled=true";
    }

    // Change level
    @PostMapping("/level")
    public String changeLevel(@RequestParam String id,
                              @RequestParam String adminLevel,
                              HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/list?denied=true";
        if (isSelf(session, id))     return "redirect:/admin/list?selfAction=true";
        adminService.changeLevel(id, adminLevel);
        return "redirect:/admin/list?levelChanged=true";
    }

    // ── DELETE ─────────────────────────────────

    @PostMapping("/delete")
    public String deleteAdmin(@RequestParam String id, HttpSession session) {
        if (!isSuperAdmin(session)) return "redirect:/admin/list?denied=true";
        if (isSelf(session, id))     return "redirect:/admin/list?selfAction=true";
        adminService.delete(id);
        return "redirect:/admin/list?deleted=true";
    }

    // ── HELPERS ────────────────────────────────

    /** The admin stored in the session, or null if nobody is logged in. */
    private Admin currentAdmin(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        return (obj instanceof Admin) ? (Admin) obj : null;
    }

    private boolean isSuperAdmin(HttpSession session) {
        Admin admin = currentAdmin(session);
        return admin != null && admin.canManageAdmins();
    }

    /** Stops a super admin from deleting, deactivating or demoting their own account. */
    private boolean isSelf(HttpSession session, String id) {
        Admin admin = currentAdmin(session);
        return admin != null && admin.getId().equals(id);
    }
}
