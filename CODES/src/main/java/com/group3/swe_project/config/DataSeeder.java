package com.group3.swe_project.config;

import com.group3.swe_project.model.*;
import com.group3.swe_project.repository.*;
import com.group3.swe_project.service.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
public class DataSeeder {

    private static Path logosDir;

    @Bean
    public CommandLineRunner seed(UserRepository userRepo,
                                  PostDonationRepository donationRepo,
                                  ClaimDonationRepository claimRepo,
                                  NotificationRepository notificationRepo,
                                  HistoryRepository historyRepo,
                                  ReportRepository reportRepo,
                                  FileStorageService storage,
                                  PasswordEncoder encoder) {
        return args -> {
            logosDir = storage.getRoot().resolve("logos");
            Files.createDirectories(logosDir);

            // ---------- Admin & demo accounts ----------
            User admin = ensureUser(userRepo, encoder,
                    "admin@foodwaste.local", "admin123",
                    "Platform Admin", "0000000000", "HQ", Role.ADMIN,
                    avatar("Platform Admin", "1f2937"), 23.7806, 90.4006);

            User demoR = ensureUser(userRepo, encoder,
                    "demo-restaurant@foodwaste.local", "demo1234",
                    "Demo Restaurant", "0123456789", "123 Demo Street, Banani, Dhaka", Role.RESTAURANT,
                    avatar("Demo Restaurant", "f97316"), 23.7937, 90.4066);
            User demoO = ensureUser(userRepo, encoder,
                    "demo-orphanage@foodwaste.local", "demo1234",
                    "Demo Orphanage", "0987654321", "456 Hope Lane, Dhanmondi, Dhaka", Role.ORPHANAGE,
                    avatar("Demo Orphanage", "8b5cf6"), 23.7461, 90.3742);

            // ---------- Restaurants (with logos + Dhaka/Chattogram coords) ----------
            List<User> restaurants = new ArrayList<>();
            restaurants.add(demoR);
            restaurants.add(ensureUser(userRepo, encoder,
                    "kitchen@spice-route.local", "demo1234",
                    "Spice Route Bistro", "01711000111", "12 Banani Rd, Dhaka", Role.RESTAURANT,
                    avatar("Spice Route", "ef4444"), 23.7937, 90.4066));
            restaurants.add(ensureUser(userRepo, encoder,
                    "manager@green-leaf.local", "demo1234",
                    "Green Leaf Cafe", "01722000222", "45 Gulshan Ave, Dhaka", Role.RESTAURANT,
                    avatar("Green Leaf", "10b981"), 23.7925, 90.4078));
            restaurants.add(ensureUser(userRepo, encoder,
                    "chef@harborview.local", "demo1234",
                    "Harborview Grill", "01733000333", "9 Dock Street, Chattogram", Role.RESTAURANT,
                    avatar("Harborview", "0ea5e9"), 22.3300, 91.8167));
            restaurants.add(ensureUser(userRepo, encoder,
                    "owner@bakers-corner.local", "demo1234",
                    "Baker's Corner", "01744000444", "78 Mirpur 10, Dhaka", Role.RESTAURANT,
                    avatar("Bakers Corner", "d97706"), 23.8067, 90.3683));
            restaurants.add(ensureUser(userRepo, encoder,
                    "info@royal-curry.local", "demo1234",
                    "Royal Curry House", "01755000555", "30 Dhanmondi 27, Dhaka", Role.RESTAURANT,
                    avatar("Royal Curry", "ca8a04"), 23.7461, 90.3742));
            restaurants.add(ensureUser(userRepo, encoder,
                    "hello@noodle-bowl.local", "demo1234",
                    "Noodle Bowl Express", "01766000666", "8 Banani 11, Dhaka", Role.RESTAURANT,
                    avatar("Noodle Bowl", "db2777"), 23.7944, 90.4012));
            restaurants.add(ensureUser(userRepo, encoder,
                    "team@ocean-pearl.local", "demo1234",
                    "Ocean Pearl Seafood", "01777000777", "15 Patenga Beach Rd, Chattogram", Role.RESTAURANT,
                    avatar("Ocean Pearl", "0891b2"), 22.2412, 91.7949));

            // ---------- Orphanages ----------
            List<User> orphanages = new ArrayList<>();
            orphanages.add(demoO);
            orphanages.add(ensureUser(userRepo, encoder,
                    "care@sunrise-home.local", "demo1234",
                    "Sunrise Children's Home", "01811000111", "5 Uttara Sector 7, Dhaka", Role.ORPHANAGE,
                    avatar("Sunrise Home", "f59e0b"), 23.8741, 90.3987));
            orphanages.add(ensureUser(userRepo, encoder,
                    "admin@hope-shelter.local", "demo1234",
                    "Hope Shelter", "01822000222", "21 Mohammadpur, Dhaka", Role.ORPHANAGE,
                    avatar("Hope Shelter", "ec4899"), 23.7639, 90.3589));
            orphanages.add(ensureUser(userRepo, encoder,
                    "contact@little-stars.local", "demo1234",
                    "Little Stars Orphanage", "01833000333", "14 Agrabad, Chattogram", Role.ORPHANAGE,
                    avatar("Little Stars", "6366f1"), 22.3261, 91.8050));
            orphanages.add(ensureUser(userRepo, encoder,
                    "office@new-dawn.local", "demo1234",
                    "New Dawn Home", "01844000444", "67 Bashundhara R/A, Dhaka", Role.ORPHANAGE,
                    avatar("New Dawn", "14b8a6"), 23.8128, 90.4264));
            orphanages.add(ensureUser(userRepo, encoder,
                    "hello@safe-haven.local", "demo1234",
                    "Safe Haven Children's Trust", "01855000555", "33 Khulshi, Chattogram", Role.ORPHANAGE,
                    avatar("Safe Haven", "7c3aed"), 22.3673, 91.8123));
            orphanages.add(ensureUser(userRepo, encoder,
                    "info@bright-future.local", "demo1234",
                    "Bright Future Home", "01866000666", "9 Tejgaon I/A, Dhaka", Role.ORPHANAGE,
                    avatar("Bright Future", "059669"), 23.7644, 90.3924));

            LocalDateTime now = LocalDateTime.now();

            // ---------- Backfill imagePath on existing donations (idempotent) ----------
            for (PostDonation d : donationRepo.findAll()) {
                if (d.getImagePath() == null || d.getImagePath().isBlank()) {
                    String url = imageForFood(d.getFoodItemName());
                    if (url != null) {
                        d.setImagePath(url);
                        donationRepo.save(d);
                    }
                }
            }

            // ---------- Freshen previously seeded AVAILABLE donations that have since expired ----------
            // Any donation matching a known "available" seed name that is now EXPIRED and has no
            // associated claim gets its deadline pushed forward and status flipped back to AVAILABLE,
            // so the presentation always has live items on the board.
            java.util.Set<String> availableSeedNames = java.util.Set.of(
                    "Vegetable biryani", "Mixed grill platter", "Sourdough loaves", "Chicken kebabs",
                    "Fresh fruit salad", "Pasta primavera", "Mutton korma", "Naan bread",
                    "Chicken fried rice", "Beef noodle soup", "Grilled fish fillet", "Chocolate brownies",
                    "Pastries assortment");
            int refreshIdx = 0;
            for (PostDonation d : donationRepo.findAll()) {
                if (d.getStatus() == DonationStatus.EXPIRED
                        && availableSeedNames.contains(d.getFoodItemName())
                        && claimRepo.findByDonation(d).isEmpty()) {
                    d.setStatus(DonationStatus.AVAILABLE);
                    d.setPickupDeadline(now.plusHours(2L + refreshIdx * 3L));
                    d.setCreatedAt(now.minusMinutes(30L + refreshIdx * 17L));
                    donationRepo.save(d);
                    refreshIdx++;
                }
            }

            // ---------- One-time bulk seed of donations / claims / reports ----------
            if (userRepo.findByEmail("kitchen@spice-route.local").isPresent()
                    && donationRepo.count() >= 18) {
                return;
            }

            // Available donations (future deadlines)
            String[][] availableData = {
                    {"Vegetable biryani",   "30 portions", "Best served warm. Bring sealed containers."},
                    {"Mixed grill platter", "15 portions", "Use the back entrance after 9pm."},
                    {"Sourdough loaves",    "20 loaves",   "Wrap separately, still oven-fresh."},
                    {"Chicken kebabs",      "40 skewers",  "Spicy. Marinated overnight."},
                    {"Fresh fruit salad",   "5 kg",        "Refrigerated, consume within 4 hours."},
                    {"Pasta primavera",     "25 portions", "Vegetarian, no nuts."},
                    {"Mutton korma",        "10 portions", "Rich gravy — bring deep trays."},
                    {"Naan bread",          "60 pieces",   "Best within 6 hours of pickup."},
                    {"Chicken fried rice",  "35 portions", "Hot and ready. Pickup before 10pm."},
                    {"Beef noodle soup",    "12 litres",   "Insulated containers recommended."},
                    {"Grilled fish fillet", "18 portions", "Lemon butter sauce on the side."},
                    {"Chocolate brownies",  "4 trays",     "Cut into 24 squares each."},
            };
            User[] availableRestaurants = {
                    restaurants.get(1), restaurants.get(2), restaurants.get(4),
                    restaurants.get(3), restaurants.get(2), restaurants.get(5),
                    restaurants.get(1), restaurants.get(4),
                    restaurants.get(6), restaurants.get(7), restaurants.get(7), restaurants.get(4)
            };
            for (int i = 0; i < availableData.length; i++) {
                PostDonation d = donation(availableRestaurants[i],
                        availableData[i][0], availableData[i][1],
                        now.plusHours(2 + i * 3), availableData[i][2],
                        DonationStatus.AVAILABLE, now.minusMinutes(30L + i * 17L));
                d.setImagePath(imageForFood(availableData[i][0]));
                donationRepo.save(d);
                history(historyRepo, d.getRestaurant(), ActionType.POST_DONATION,
                        "Posted donation '" + d.getFoodItemName() + "' (" + d.getQuantity() + ")");
            }

            // Claimed donations (pending pickup)
            String[] claimedFoods = {"Rice & dal combo", "Vegetable thali", "Chicken curry"};
            for (int i = 0; i < 3; i++) {
                User r = restaurants.get(1 + i);
                User o = orphanages.get(1 + i);
                String name = claimedFoods[i] + " " + (i + 1);
                PostDonation d = donation(r, name, (40 + i * 10) + " portions",
                        now.plusHours(4 + i), "Use side gate. Confirm by phone first.",
                        DonationStatus.CLAIMED, now.minusHours(1 + i));
                d.setImagePath(imageForFood(claimedFoods[i]));
                donationRepo.save(d);
                ClaimDonation c = new ClaimDonation();
                c.setDonation(d);
                c.setOrphanage(o);
                c.setStatus(ClaimStatus.PENDING);
                c.setClaimedAt(now.minusMinutes(20L + i * 13L));
                claimRepo.save(c);

                history(historyRepo, r, ActionType.POST_DONATION,
                        "Posted donation '" + d.getFoodItemName() + "'");
                history(historyRepo, o, ActionType.CLAIMED,
                        "Claimed '" + d.getFoodItemName() + "' from " + r.getFullName());
                notify(notificationRepo, r, c, NotificationType.CLAIMED,
                        o.getFullName() + " claimed your donation '" + d.getFoodItemName() + "'.");
                notify(notificationRepo, o, c, NotificationType.CLAIMED,
                        "You claimed '" + d.getFoodItemName() + "' from " + r.getFullName() + ".");
            }

            // Completed pickups
            String[] doneFoods = {"Sandwich platter", "Pizza slices", "Burger box", "Veggie wraps"};
            for (int i = 0; i < 4; i++) {
                User r = restaurants.get(1 + (i % (restaurants.size() - 1)));
                User o = orphanages.get(1 + (i % (orphanages.size() - 1)));
                String name = doneFoods[i] + " " + (i + 1);
                PostDonation d = donation(r, name, (20 + i * 5) + " portions",
                        now.minusHours(2 + i), "Picked up successfully.",
                        DonationStatus.PICKED_UP, now.minusHours(8 + i * 2));
                d.setImagePath(imageForFood(doneFoods[i]));
                donationRepo.save(d);
                ClaimDonation c = new ClaimDonation();
                c.setDonation(d);
                c.setOrphanage(o);
                c.setStatus(ClaimStatus.COMPLETED);
                c.setClaimedAt(now.minusHours(6 + i * 2));
                claimRepo.save(c);

                history(historyRepo, r, ActionType.POST_DONATION, "Posted '" + d.getFoodItemName() + "'");
                history(historyRepo, o, ActionType.CLAIMED, "Claimed '" + d.getFoodItemName() + "'");
                history(historyRepo, r, ActionType.PICKED_UP, "Marked '" + d.getFoodItemName() + "' as picked up");
                history(historyRepo, o, ActionType.PICKED_UP, "Received '" + d.getFoodItemName() + "'");
                notify(notificationRepo, o, c, NotificationType.PICKED_UP,
                        "Pickup confirmed for '" + d.getFoodItemName() + "'. Thank you!");
            }

            // Expired donations
            String[] expiredFoods = {"Soup of the day", "Salad bowls"};
            for (int i = 0; i < expiredFoods.length; i++) {
                PostDonation d = donation(restaurants.get(2 + i),
                        expiredFoods[i] + " " + (i + 1), "8 litres",
                        now.minusHours(3 + i), "Nobody claimed in time.",
                        DonationStatus.EXPIRED, now.minusHours(10 + i * 3));
                d.setImagePath(imageForFood(expiredFoods[i]));
                donationRepo.save(d);
                history(historyRepo, d.getRestaurant(), ActionType.POST_DONATION,
                        "Posted '" + d.getFoodItemName() + "'");
                notify(notificationRepo, d.getRestaurant(), null, NotificationType.EXPIRED,
                        "Your donation '" + d.getFoodItemName() + "' has expired.");
            }

            // Cancelled claim (donation returned to available)
            {
                User r = restaurants.get(5);
                User o = orphanages.get(2);
                PostDonation d = donation(r,
                        "Pastries assortment", "3 dozen",
                        now.plusHours(6), "Originally claimed and cancelled — now back on the board.",
                        DonationStatus.AVAILABLE, now.minusHours(2));
                d.setImagePath(imageForFood("Pastries"));
                donationRepo.save(d);
                ClaimDonation c = new ClaimDonation();
                c.setDonation(d);
                c.setOrphanage(o);
                c.setStatus(ClaimStatus.CANCELLED);
                c.setClaimedAt(now.minusMinutes(90));
                claimRepo.save(c);
                history(historyRepo, o, ActionType.CANCELLED,
                        "Cancelled claim on '" + d.getFoodItemName() + "'");
                notify(notificationRepo, r, c, NotificationType.CLAIMED,
                        o.getFullName() + " cancelled their claim on '" + d.getFoodItemName() + "'.");
            }

            // Reports
            seedReport(reportRepo, historyRepo, orphanages.get(1), restaurants.get(3),
                    ReportType.WRONG_QUANTITY,
                    "Received much less than the advertised quantity (about half).",
                    ReportStatus.INVESTIGATING, "Restaurant has been contacted for clarification.");
            seedReport(reportRepo, historyRepo, orphanages.get(2), restaurants.get(2),
                    ReportType.FOOD_SAFETY,
                    "One of the soup containers had an off smell. Did not serve.",
                    ReportStatus.RESOLVED, "Restaurant issued an apology and improved labeling.");
            seedReport(reportRepo, historyRepo, demoO, null,
                    ReportType.OTHER,
                    "Suggestion: add a notes field for dietary restrictions.",
                    ReportStatus.PENDING, null);
        };
    }

    // ---------- Image helpers ----------

    /**
     * Writes a locally-served SVG logo (initials on a brand colour) and returns its /uploads path.
     * Served by the app itself, so it works offline and is never blocked by client ad/tracker filters
     * (unlike a third-party avatar service).
     */
    private static String avatar(String name, String bgHex) {
        String slug = slugify(name);
        String initials = initials(name);
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="256" height="256" viewBox="0 0 256 256">
                  <rect width="256" height="256" rx="48" fill="#%s"/>
                  <text x="128" y="128" font-family="Arial, Helvetica, sans-serif" font-size="104" font-weight="bold" fill="#ffffff" text-anchor="middle" dominant-baseline="central">%s</text>
                </svg>
                """.formatted(bgHex, initials);
        try {
            Files.writeString(logosDir.resolve(slug + ".svg"), svg, StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to write logo " + slug, e);
        }
        return "/uploads/logos/" + slug + ".svg";
    }

    private static String slugify(String name) {
        String s = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return s.isBlank() ? "logo" : s;
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String word : name.trim().split("\\s+")) {
            if (!word.isBlank() && Character.isLetterOrDigit(word.charAt(0))) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (sb.length() == 2) break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Map food-name keywords to a stable Unsplash photo URL. */
    private static String imageForFood(String foodName) {
        if (foodName == null) return null;
        String n = foodName.toLowerCase(Locale.ROOT);
        String id;
        if (n.contains("biryani") || n.contains("fried rice")) {
            id = "1631452180519-c014fe946bc7";
        } else if (n.contains("grill") && n.contains("fish")) {
            id = "1467003909585-2f8a72700288";
        } else if (n.contains("grill") || n.contains("kebab") || n.contains("skewer")) {
            id = "1544025162-d76694265947";
        } else if (n.contains("sourdough") || n.contains("bread") || n.contains("loaves")) {
            id = "1509440159596-0249088772ff";
        } else if (n.contains("naan")) {
            id = "1610057099443-fde8c4d50f91";
        } else if (n.contains("fruit")) {
            id = "1490474418585-ba9bad8fd0ea";
        } else if (n.contains("pasta")) {
            id = "1551183053-bf91a1d81141";
        } else if (n.contains("korma") || n.contains("curry") || n.contains("thali")) {
            id = "1631292784640-2b24be784d5d";
        } else if (n.contains("rice") || n.contains("dal")) {
            id = "1596797038530-2c107229654b";
        } else if (n.contains("sandwich") || n.contains("wrap")) {
            id = "1528735602780-2552fd46c7af";
        } else if (n.contains("pizza")) {
            id = "1513104890138-7c749659a591";
        } else if (n.contains("burger")) {
            id = "1568901346375-23c9450c58cd";
        } else if (n.contains("noodle") || n.contains("ramen")) {
            id = "1569718212165-3a8278d5f624";
        } else if (n.contains("soup")) {
            id = "1547592180-85f173990554";
        } else if (n.contains("salad")) {
            id = "1546069901-ba9599a7e63c";
        } else if (n.contains("brownie") || n.contains("chocolate")) {
            id = "1606312619070-d48b4c652a52";
        } else if (n.contains("pastr") || n.contains("cake") || n.contains("dessert")) {
            id = "1486427944299-d1955d23e34d";
        } else {
            id = "1504674900247-0877df9cc836"; // generic plated food
        }
        return "https://images.unsplash.com/photo-" + id + "?w=800&q=80&auto=format&fit=crop";
    }

    // ---------- User helpers ----------

    private static User ensureUser(UserRepository repo, PasswordEncoder enc,
                                   String email, String password, String fullName,
                                   String phone, String address, Role role,
                                   String logoUrl, Double lat, Double lon) {
        User existing = repo.findByEmail(email).orElse(null);
        if (existing != null) {
            return backfillUserExtras(repo, existing, logoUrl, lat, lon);
        }
        User u = new User();
        u.setEmail(email);
        u.setPassword(enc.encode(password));
        u.setFullName(fullName);
        u.setPhoneNumber(phone);
        u.setAddress(address);
        u.setRole(role);
        u.setEnabled(true);
        u.setLogoPath(logoUrl);
        u.setLatitude(lat);
        u.setLongitude(lon);
        return repo.save(u);
    }

    private static User backfillUserExtras(UserRepository repo, User u,
                                           String logoUrl, Double lat, Double lon) {
        boolean changed = false;
        // Set when missing, and migrate any stale third-party (ui-avatars) URL to the local logo.
        String current = u.getLogoPath();
        boolean needsLogo = current == null || current.isBlank() || current.contains("ui-avatars.com");
        if (needsLogo && logoUrl != null) {
            u.setLogoPath(logoUrl);
            changed = true;
        }
        if (u.getLatitude() == null && lat != null) {
            u.setLatitude(lat);
            changed = true;
        }
        if (u.getLongitude() == null && lon != null) {
            u.setLongitude(lon);
            changed = true;
        }
        return changed ? repo.save(u) : u;
    }

    private static PostDonation donation(User restaurant, String name, String qty,
                                         LocalDateTime deadline, String instructions,
                                         DonationStatus status, LocalDateTime createdAt) {
        PostDonation d = new PostDonation();
        d.setRestaurant(restaurant);
        d.setFoodItemName(name);
        d.setQuantity(qty);
        d.setPickupDeadline(deadline);
        d.setSpecialInstructions(instructions);
        d.setStatus(status);
        d.setCreatedAt(createdAt);
        return d;
    }

    private static void notify(NotificationRepository repo, User user, ClaimDonation claim,
                               NotificationType type, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setClaim(claim);
        n.setType(type);
        n.setMessage(message);
        repo.save(n);
    }

    private static void history(HistoryRepository repo, User user, ActionType type, String details) {
        History h = new History();
        h.setUser(user);
        h.setActionType(type);
        h.setDetails(details);
        repo.save(h);
    }

    private static void seedReport(ReportRepository reportRepo, HistoryRepository historyRepo,
                                   User reporter, User reported, ReportType type,
                                   String description, ReportStatus status, String response) {
        Report r = new Report();
        r.setReporter(reporter);
        r.setReportedUser(reported);
        r.setReportType(type);
        r.setDescription(description);
        r.setStatus(status);
        r.setAdminResponse(response);
        reportRepo.save(r);
        history(historyRepo, reporter, ActionType.REPORTS, "Filed a report: " + type);
    }
}
