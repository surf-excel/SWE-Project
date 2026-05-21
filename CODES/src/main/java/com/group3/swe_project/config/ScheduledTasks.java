package com.group3.swe_project.config;

import com.group3.swe_project.service.DonationService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTasks {

    private final DonationService donationService;

    public ScheduledTasks(DonationService donationService) {
        this.donationService = donationService;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 30 * 1000L)
    public void expireDonations() {
        donationService.expireOverdue();
    }
}
