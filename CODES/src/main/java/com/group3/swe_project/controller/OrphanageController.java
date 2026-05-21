package com.group3.swe_project.controller;

import com.group3.swe_project.dto.ReportForm;
import com.group3.swe_project.model.PostDonation;
import com.group3.swe_project.repository.ClaimDonationRepository;
import com.group3.swe_project.security.AppUserDetails;
import com.group3.swe_project.service.DonationService;
import com.group3.swe_project.service.HistoryService;
import com.group3.swe_project.service.NotificationService;
import com.group3.swe_project.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orphanage")
public class OrphanageController {

    private final DonationService donationService;
    private final NotificationService notificationService;
    private final HistoryService historyService;
    private final ReportService reportService;
    private final ClaimDonationRepository claimRepo;

    public OrphanageController(DonationService donationService, NotificationService notificationService,
                               HistoryService historyService, ReportService reportService,
                               ClaimDonationRepository claimRepo) {
        this.donationService = donationService;
        this.notificationService = notificationService;
        this.historyService = historyService;
        this.reportService = reportService;
        this.claimRepo = claimRepo;
    }

    @GetMapping
    public String dashboard(@RequestParam(value = "sort", required = false) String sort,
                            @AuthenticationPrincipal AppUserDetails principal, Model model) {
        model.addAttribute("user", principal.getUser());
        model.addAttribute("donations", donationService.listAvailable(sort));
        model.addAttribute("currentSort", sort == null ? "posted" : sort);
        model.addAttribute("myClaims",
                claimRepo.findByOrphanage(principal.getUser(), Sort.by(Sort.Direction.DESC, "claimedAt")));
        model.addAttribute("unread", notificationService.unreadCount(principal.getUser()));
        return "orphanage/dashboard";
    }

    @GetMapping("/donations/{id}")
    public String detail(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal, Model model) {
        PostDonation d = donationService.get(id);
        model.addAttribute("donation", d);
        model.addAttribute("user", principal.getUser());
        return "orphanage/donation-detail";
    }

    @PostMapping("/donations/{id}/claim")
    public String claim(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal,
                        RedirectAttributes ra) {
        try {
            donationService.claim(principal.getUser(), id);
            ra.addFlashAttribute("flash", "Claim submitted! Contact details are now visible.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orphanage";
    }

    @PostMapping("/claims/{claimId}/cancel")
    public String cancel(@PathVariable Long claimId, @AuthenticationPrincipal AppUserDetails principal,
                         RedirectAttributes ra) {
        try {
            donationService.cancelClaim(principal.getUser(), claimId);
            ra.addFlashAttribute("flash", "Claim cancelled.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orphanage";
    }

    @GetMapping("/reports/new")
    public String reportForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ReportForm());
        }
        return "shared/report-new";
    }

    @PostMapping("/reports")
    public String fileReport(@Valid @ModelAttribute("form") ReportForm form, BindingResult br,
                             @AuthenticationPrincipal AppUserDetails principal, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "shared/report-new";
        }
        reportService.submit(principal.getUser(), form);
        ra.addFlashAttribute("flash", "Report filed. An admin will review it.");
        return "redirect:/orphanage";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal AppUserDetails principal, Model model) {
        model.addAttribute("notifications", notificationService.forUser(principal.getUser()));
        notificationService.markAllRead(principal.getUser());
        return "shared/notifications";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal AppUserDetails principal, Model model) {
        model.addAttribute("entries", historyService.forUser(principal.getUser()));
        return "shared/history";
    }
}
