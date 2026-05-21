package com.group3.swe_project.controller;

import com.group3.swe_project.model.ReportStatus;
import com.group3.swe_project.model.User;
import com.group3.swe_project.repository.PostDonationRepository;
import com.group3.swe_project.repository.UserRepository;
import com.group3.swe_project.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PostDonationRepository donationRepository;
    private final ReportService reportService;

    public AdminController(UserRepository userRepository, PostDonationRepository donationRepository,
                           ReportService reportService) {
        this.userRepository = userRepository;
        this.donationRepository = donationRepository;
        this.reportService = reportService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalDonations", donationRepository.count());
        model.addAttribute("reports", reportService.all());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        u.setEnabled(!u.isEnabled());
        userRepository.save(u);
        ra.addFlashAttribute("flash", "User " + (u.isEnabled() ? "enabled" : "disabled") + ".");
        return "redirect:/admin/users";
    }

    @PostMapping("/reports/{id}/status")
    public String updateReport(@PathVariable Long id,
                               @RequestParam ReportStatus status,
                               @RequestParam(required = false) String adminResponse,
                               RedirectAttributes ra) {
        reportService.updateStatus(id, status, adminResponse);
        ra.addFlashAttribute("flash", "Report updated.");
        return "redirect:/admin";
    }
}
