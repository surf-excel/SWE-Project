package com.group3.swe_project.controller;

import com.group3.swe_project.dto.ProfileForm;
import com.group3.swe_project.model.User;
import com.group3.swe_project.repository.UserRepository;
import com.group3.swe_project.security.AppUserDetails;
import com.group3.swe_project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal AppUserDetails principal, Model model) {
        // Read fresh from the DB: the security principal is captured at login and would otherwise
        // show a stale logo/name until the next re-login.
        User user = userRepository.findById(principal.getUserId()).orElse(principal.getUser());
        if (!model.containsAttribute("form")) {
            ProfileForm form = new ProfileForm();
            form.setFullName(user.getFullName());
            form.setPhoneNumber(user.getPhoneNumber());
            form.setAddress(user.getAddress());
            form.setLatitude(user.getLatitude());
            form.setLongitude(user.getLongitude());
            model.addAttribute("form", form);
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping
    public String update(@Valid @ModelAttribute("form") ProfileForm form, BindingResult br,
                         @AuthenticationPrincipal AppUserDetails principal,
                         RedirectAttributes ra, Model model) {
        if (br.hasErrors()) {
            model.addAttribute("user", userRepository.findById(principal.getUserId()).orElse(principal.getUser()));
            model.addAttribute("error", "Please fix the highlighted fields: " + br.getAllErrors().get(0).getDefaultMessage());
            return "profile";
        }
        try {
            userService.updateProfile(principal.getUser(), form);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/profile";
        }
        ra.addFlashAttribute("flash", "Profile updated.");
        return "redirect:/profile";
    }
}
