package com.group3.swe_project.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "claim_donations")
public class ClaimDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long claimId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "donation_id", nullable = false)
    private PostDonation donation;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "orphanage_id", nullable = false)
    private User orphanage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClaimStatus status = ClaimStatus.PENDING;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt = LocalDateTime.now();

    public Long getClaimId() { return claimId; }
    public void setClaimId(Long claimId) { this.claimId = claimId; }

    public PostDonation getDonation() { return donation; }
    public void setDonation(PostDonation donation) { this.donation = donation; }

    public User getOrphanage() { return orphanage; }
    public void setOrphanage(User orphanage) { this.orphanage = orphanage; }

    public ClaimStatus getStatus() { return status; }
    public void setStatus(ClaimStatus status) { this.status = status; }

    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
}
