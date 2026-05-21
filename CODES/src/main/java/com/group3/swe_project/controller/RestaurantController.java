package com.group3.swe_project.controller;

import com.group3.swe_project.dto.DonationForm;
import com.group3.swe_project.model.PostDonation;
import com.group3.swe_project.model.User;
import com.group3.swe_project.repository.ClaimDonationRepository;
import com.group3.swe_project.security.AppUserDetails;
import com.group3.swe_project.service.DonationService;
import com.group3.swe_project.service.HistoryService;
import com.group3.swe_project.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final DonationService donationService;
    private final NotificationService notificationService;
    private final HistoryService historyService;
    private final ClaimDonationRepository claimRepo;

    public RestaurantController(DonationService donationService, NotificationService notificationService,
                                HistoryService historyService, ClaimDonationRepository claimRepo) {
        this.donationService = donationService;
        this.notificationService = notificationService;
        this.historyService = historyService;
        this.claimRepo = claimRepo;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal AppUserDetails principal, Model model) {
        User restaurant = principal.getUser();
        model.addAttribute("user", restaurant);
        model.addAttribute("donations", donationService.listByRestaurant(restaurant));
        model.addAttribute("unread", notificationService.unreadCount(restaurant));
        return "restaurant/dashboard";
    }

    @GetMapping("/donations/new")
    public String newDonation(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new DonationForm());
        }
        return "restaurant/donation-new";
    }

    @PostMapping("/donations")
    public String createDonation(@Valid @ModelAttribute("form") DonationForm form, BindingResult br,
                                 @AuthenticationPrincipal AppUserDetails principal, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "restaurant/donation-new";
        }
        donationService.post(principal.getUser(), form);
        ra.addFlashAttribute("flash", "Donation posted!");
        return "redirect:/restaurant";
    }

    @GetMapping("/donations/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @AuthenticationPrincipal AppUserDetails principal) {
        PostDonation d = donationService.get(id);
        if (!d.getRestaurant().getUserId().equals(principal.getUserId())) {
            return "redirect:/restaurant";
        }
        model.addAttribute("donation", d);
        model.addAttribute("claims", claimRepo.findByDonation(d));
        return "restaurant/donation-detail";
    }

    @PostMapping("/claims/{claimId}/picked-up")
    public String markPickedUp(@PathVariable Long claimId,
                               @AuthenticationPrincipal AppUserDetails principal,
                               RedirectAttributes ra) {
        try {
            donationService.markPickedUp(principal.getUser(), claimId);
            ra.addFlashAttribute("flash", "Marked as picked up.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/restaurant";
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
