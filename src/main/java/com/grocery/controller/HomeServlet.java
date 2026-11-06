package com.grocery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Opening http://localhost:8080 goes straight to the admin login page. */
@Controller
public class HomeServlet {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/login";
    }
}
