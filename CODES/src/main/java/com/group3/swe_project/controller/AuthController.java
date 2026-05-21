package com.group3.swe_project.controller;

import com.group3.swe_project.dto.RegisterRequest;
import com.group3.swe_project.model.Role;
import com.group3.swe_project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterRequest());
        }
        model.addAttribute("roles", new Role[]{Role.RESTAURANT, Role.ORPHANAGE});
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterRequest form,
                           BindingResult br, RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.RESTAURANT, Role.ORPHANAGE});
            return "register";
        }
        try {
            userService.register(form);
        } catch (IllegalArgumentException ex) {
            br.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("roles", new Role[]{Role.RESTAURANT, Role.ORPHANAGE});
            return "register";
        }
        ra.addFlashAttribute("flash", "Account created. Please log in.");
        return "redirect:/login";
    }
}
