package com.agriverse.api.identity.config;

import com.agriverse.api.content.entity.Category;
import com.agriverse.api.content.entity.Tag;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.content.repository.TagRepository;
import com.agriverse.api.identity.entity.Role;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.entity.UserStatus;
import com.agriverse.api.identity.repository.RoleRepository;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.reference.entity.Crop;
import com.agriverse.api.reference.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Runs on every startup, in any profile (Postgres+Flyway or the in-memory
 * H2 test profile, which has {@code flyway.enabled=false} and therefore
 * never applies {@code V2__seed_rbac.sql}):
 *
 * 1. Ensures the four RBAC roles exist (idempotent — a no-op once Flyway
 *    or a prior run has already seeded them).
 * 2. Ensures a baseline category taxonomy exists (idempotent, same
 *    reasoning) — without this, GET /categories returns an empty list on
 *    every fresh H2 boot, and nothing in the product (article creation,
 *    the category picker, browse-by-category) works until an ADMIN
 *    manually calls POST /categories at least once.
 * 3. Ensures a baseline crop master list exists, linked into the category
 *    taxonomy above — without this, every crop-picker in the product
 *    (Government Scheme / Machinery / Plant Disease forms) is empty,
 *    since nothing else ever creates a Crop.
 * 4. Ensures a baseline tag list exists — without this, the article
 *    editor's tag picker is empty, since nothing else ever creates a Tag.
 * 5. If {@code agriverse.admin-bootstrap.email} is configured, creates
 *    (or promotes) that account to ADMIN + ACTIVE. This is the supported
 *    way to get a first admin without hand-editing a database — including
 *    H2, where there's no persistent file to connect a SQL client to
 *    between runs.
 *
 * Intended for local/dev/test use. Leave {@code admin-bootstrap.email}
 * unset in real production once a genuine admin account exists — but the
 * role, category, crop, and tag seeding are safe (and needed) in any
 * environment, since they're purely additive and only ever run against an
 * empty table.
 */
@Component
@RequiredArgsConstructor
public class StartupDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDataSeeder.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CropRepository cropRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @Override
    @Transactional
    public void run(String... args) {
        seedRolesIfMissing();
        seedCategoriesIfMissing();
        seedCropsIfMissing();
        seedTagsIfMissing();
        bootstrapAdminIfConfigured();
    }

    private void seedRolesIfMissing() {
        if (roleRepository.count() > 0) {
            return;
        }
        roleRepository.save(new Role(Role.READER, "Default role for every registered user."));
        roleRepository.save(new Role(Role.AUTHOR, "Can create and edit their own articles."));
        roleRepository.save(new Role(Role.EDITOR, "Can review, publish, and manage any article; moderates comments."));
        roleRepository.save(new Role(Role.ADMIN, "Full platform administration."));
        log.info("Seeded RBAC roles (READER, AUTHOR, EDITOR, ADMIN) — none were present.");
    }

    /**
     * A starter taxonomy covering the domains the reference-data modules
     * (Crops, Plant Diseases, Machinery, Government Schemes) already
     * assume exist. Treat this as a reasonable Phase 1 default, not a
     * fixed list — ADMIN can add/edit/reorder via the Categories API same
     * as anything seeded here.
     */
    private void seedCategoriesIfMissing() {
        if (categoryRepository.count() > 0) {
            return;
        }

        seedTree("Crop Farming", "crop-farming", "Growing and managing field crops.",
                List.of("Cereals", "Vegetables", "Fruits & Orchards", "Cash Crops"));
        seedTree("Plant Health & Diseases", "plant-health", "Identifying and treating crop diseases and pests.",
                List.of("Pest Management", "Fungal Diseases", "Nutrient Deficiencies"));
        seedTree("Livestock & Dairy", "livestock-dairy", "Raising livestock and managing dairy operations.",
                List.of("Cattle", "Poultry", "Dairy Management"));
        seedTree("Farm Machinery & Equipment", "farm-machinery", "Choosing and using farm equipment.",
                List.of("Tillage Equipment", "Irrigation Systems", "Harvesting Equipment"));
        seedTree("Government Schemes & Finance", "government-schemes", "Subsidies, insurance, and financial support for farmers.",
                List.of("Subsidies", "Crop Insurance", "Agricultural Loans"));
        seedTree("Soil & Irrigation", "soil-irrigation", "Soil health and water management.",
                List.of("Soil Health", "Water Management"));
        seedTree("Market & Post-Harvest", "market-post-harvest", "Storage, pricing, and selling produce.",
                List.of("Storage & Handling", "Pricing & Market Trends"));

        log.info("Seeded baseline category taxonomy — none were present.");
    }

    private void seedTree(String name, String slug, String description, List<String> childNames) {
        Category parent = new Category();
        parent.setName(name);
        parent.setSlug(slug);
        parent.setDescription(description);
        parent.setDisplayOrder(0);
        categoryRepository.save(parent);

        int order = 0;
        for (String childName : childNames) {
            Category child = new Category();
            child.setName(childName);
            child.setSlug(slug + "-" + com.agriverse.api.common.util.SlugUtil.slugify(childName));
            child.setParentCategory(parent);
            child.setDisplayOrder(order++);
            categoryRepository.save(child);
        }
    }

    /**
     * Links each crop into the category taxonomy above by slug where a
     * sensible subcategory exists, falling back to the "Crop Farming"
     * parent otherwise. Runs after {@link #seedCategoriesIfMissing()} in
     * {@link #run}, so these lookups succeed on a genuinely fresh boot;
     * harmless (category = null) if categories were seeded some other way
     * and these slugs don't happen to exist.
     */
    private void seedCropsIfMissing() {
        if (cropRepository.count() > 0) {
            return;
        }

        Category cereals = categoryRepository.findBySlug("crop-farming-cereals").orElse(null);
        Category vegetables = categoryRepository.findBySlug("crop-farming-vegetables").orElse(null);
        Category cashCrops = categoryRepository.findBySlug("crop-farming-cash-crops").orElse(null);
        Category cropFarming = categoryRepository.findBySlug("crop-farming").orElse(null);

        seedCrop("Rice", "Oryza sativa", cereals);
        seedCrop("Wheat", "Triticum aestivum", cereals);
        seedCrop("Maize", "Zea mays", cereals);
        seedCrop("Barley", "Hordeum vulgare", cereals);
        seedCrop("Potato", "Solanum tuberosum", vegetables);
        seedCrop("Tomato", "Solanum lycopersicum", vegetables);
        seedCrop("Onion", "Allium cepa", vegetables);
        seedCrop("Cotton", "Gossypium hirsutum", cashCrops);
        seedCrop("Sugarcane", "Saccharum officinarum", cashCrops);
        seedCrop("Jute", "Corchorus olitorius", cashCrops);
        seedCrop("Soybean", "Glycine max", cropFarming);
        seedCrop("Groundnut", "Arachis hypogaea", cropFarming);
        seedCrop("Chickpea", "Cicer arietinum", cropFarming);
        seedCrop("Mustard", "Brassica juncea", cropFarming);
        seedCrop("Sunflower", "Helianthus annuus", cropFarming);

        log.info("Seeded baseline crop master list — none were present.");
    }

    private void seedCrop(String name, String scientificName, Category category) {
        Crop crop = new Crop();
        crop.setName(name);
        crop.setScientificName(scientificName);
        crop.setCategory(category);
        cropRepository.save(crop);
    }

    private void seedTagsIfMissing() {
        if (tagRepository.count() > 0) {
            return;
        }

        List<String> starterTags = List.of(
                "Organic Farming", "Irrigation", "Pest Control", "Soil Testing", "Crop Rotation",
                "Government Subsidy", "Monsoon Farming", "Drought Resistant", "Export Crops",
                "Sustainable Farming", "Fertilizers", "High Yield Varieties", "Post-Harvest",
                "Kharif Season", "Rabi Season");

        for (String name : starterTags) {
            Tag tag = new Tag();
            tag.setName(name);
            tag.setSlug(com.agriverse.api.common.util.SlugUtil.slugify(name));
            tagRepository.save(tag);
        }

        log.info("Seeded baseline tag list — none were present.");
    }

    private void bootstrapAdminIfConfigured() {
        String email = adminBootstrapProperties.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        Role adminRole = roleRepository.findByName(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing after seeding — this should not happen"));

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user != null) {
            user.setRole(adminRole);
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerifiedAt(Instant.now());
            userRepository.save(user);
            log.info("Promoted existing user '{}' to ADMIN via admin-bootstrap.", email);
            return;
        }

        String rawPassword = adminBootstrapProperties.getPassword();
        boolean generated = rawPassword == null || rawPassword.isBlank();
        if (generated) {
            rawPassword = generateRandomPassword();
        }

        User admin = new User();
        admin.setFullName("Admin");
        admin.setEmail(email.toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setRole(adminRole);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerifiedAt(Instant.now());
        userRepository.save(admin);

        if (generated) {
            log.warn("=====================================================================");
            log.warn(" Bootstrapped ADMIN account: {}", email);
            log.warn(" Generated password (shown once — change it after first login): {}", rawPassword);
            log.warn("=====================================================================");
        } else {
            log.info("Bootstrapped ADMIN account '{}' using the configured admin-bootstrap password.", email);
        }
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
