# AgriVerse Backend

A Spring Boot 3 (Java 21) **modular monolith** implementing the full REST API
Specification (v1.1) for AgriVerse, "the GeeksforGeeks of agriculture" — over
70 endpoints across 18 functional groups, built against the Database Design
Specification's 30-entity schema and the Software Architecture Document's
module boundaries.

This was generated directly from the four spec documents (Product
Specification, Software Architecture Document, Database Design
Specification, REST API Specification) rather than written ad hoc — see
[Traceability](#traceability-back-to-the-specs) below.

## Stack

| Concern            | Choice                                              |
|--------------------|------------------------------------------------------|
| Language / runtime | Java 21, Spring Boot 3.3                              |
| Web                | Spring Web (MVC), springdoc-openapi (Swagger UI)      |
| Auth               | Spring Security 6, stateless JWT (jjwt) + opaque refresh tokens |
| Persistence        | Spring Data JPA / Hibernate, PostgreSQL, Flyway migrations |
| Caching            | Spring Cache + Redis (session-independent JWT model; weather/search caching seam) |
| Mail               | Spring Mail (verification / password-reset emails)   |
| Validation         | Jakarta Bean Validation                               |
| Build              | Maven                                                  |

## Module layout

Each package is a self-contained module — `entity → repository → service →
controller`, with DTOs at the boundary — mirroring the Software Architecture
Document's Section 2 module diagram:

```
com.agriverse.api
├── common/          # base entities, DTOs, exceptions, security config, pagination, mail
├── security/         # JWT issuing/validation, Spring Security principal
├── identity/          # Users, Roles, Authors, Auth flows            (Auth, Users)
├── content/           # Categories, Tags, Articles                   (Categories, Articles)
├── engagement/       # Bookmarks, Comments, Replies, Likes, Notifications
├── learning/          # Roadmaps, Roadmap Steps, Quizzes, Attempts
├── reference/         # Crops, Plant Diseases, Government Schemes, Machinery, Market Prices, Weather
├── analytics/         # Reading History, Search History (read models only, no public endpoints)
├── search/            # Search, Autocomplete, Trending
├── recommendation/    # Personalized + content-based recommendations
└── admin/             # Editorial review, comment moderation, user admin, analytics overview
```

## Getting started

```bash
# 1. Start Postgres + Redis
docker compose up -d

# 2. Configure environment (or just rely on the docker-compose defaults)
cp .env.example .env

# 3. Run
mvn spring-boot:run
```

The API is served at `http://localhost:8080/api/v1/...`, with interactive
docs at `http://localhost:8080/swagger-ui.html`. Flyway applies
`V1__init_schema.sql` and `V2__seed_rbac.sql` automatically on first boot.

### Running tests

```bash
mvn test
```

Tests run against an in-memory H2 database (`application-test.yml`) so no
external services are required.

### Building a container

```bash
docker build -t agriverse-backend .
docker run -p 8080:8080 --env-file .env agriverse-backend
```

## Auth model

- **Access tokens**: short-lived (15 min default) stateless JWTs signed
  with HMAC-SHA256, carrying `sub` (user public UUID), `email`, and `role`.
- **Refresh tokens**: opaque random strings, stored **hashed** (SHA-256) in
  `refresh_tokens`, revocable, 30-day default TTL.
- **Roles**: `READER < AUTHOR < EDITOR < ADMIN`, seeded in
  `V2__seed_rbac.sql`. Enforced via Spring Security's
  `hasRole`/`hasAnyRole` at the `SecurityFilterChain` level for coarse
  gates (e.g. `/api/v1/admin/**`) and `@PreAuthorize` at the controller
  method level for finer distinctions (e.g. ADMIN-only user management
  inside the EDITOR/ADMIN-accessible admin area).

## Design decisions & spec gaps closed

A few places where the four specs didn't fully agree, resolved pragmatically
and documented in code comments at the point of resolution:

1. **`Roadmap_Steps` needed an external ID.** The DB spec's table listing
   omits a `public_id` column, but the API spec's roadmap-progress endpoints
   require an opaque step identifier in requests and responses. Resolved by
   giving `RoadmapStep` a `public_id`, consistent with every other entity's
   convention.
2. **Quiz questions/options don't need a stored external ID.** They're only
   ever referenced within a single quiz-detail round trip, so their public
   IDs are derived deterministically (`UUID.nameUUIDFromBytes`) rather than
   adding more schema.
3. **Refresh/verification tokens aren't in the DB spec's 30-entity
   inventory.** They're auth infrastructure needed to implement the
   Authentication endpoint group's contract (refresh, password reset, email
   verification) — added as two additional tables, clearly marked as such.
4. **`GET /api/v1/weather`'s `savedLocationId`** references a "saved
   locations" feature that has no backing entity in the current DB spec.
   Implemented to fail with the spec's own documented 404 case
   ("does not belong to the authenticated user") rather than silently
   no-op, with a comment pointing at the gap for whoever adds that table.
5. **Search is Postgres-backed for Phase 1**, matching the response
   contract (`content`, `totalElements`, `tookMs`, highlighted snippets)
   exactly. The target design (SAD Section 11) fronts this with
   Meilisearch behind Redis — `SearchService` is written so that swap only
   touches its internals, not the controller or DTOs.
6. **Weather's Redis layer is a documented follow-up, not faked.** Spring's
   `@Cacheable` silently no-ops on self-invocation within the same class;
   rather than add an annotation that looks functional but isn't, the
   durable `Weather_Cache` fallback is implemented for real and the Redis
   layer is called out as needing a separate proxied bean.

## What's stubbed vs. fully wired

Fully implemented against real Postgres tables and business rules: Auth,
Users, Categories, Articles (including the draft → in_review → published →
archived workflow and ownership checks), Bookmarks, Comments/Replies/Likes,
Notifications, Roadmaps + progress tracking, Quizzes + grading, Crops,
Government Schemes, Plant Diseases, Machinery, Market Prices, and the full
Admin group.

Intentionally left as clearly-marked integration seams (would need external
credentials/services this environment doesn't have):

- **Social login** (`POST /auth/social-login`) — provider token
  verification (Google/GitHub) is stubbed to a clear error; wire in the
  provider SDK in `AuthService.verifyProviderToken`.
- **Weather provider** — `WeatherService.fetchFromProvider` returns a
  placeholder payload; swap in a real weather API client.
- **Comment spam/profanity detection** — `CommentService.containsSuspiciousContent`
  is a placeholder heuristic; swap in a real moderation service.
- **Search** — Postgres `LIKE`-based for now; see gap #5 above.
- **Email delivery** — real SMTP config needed in `.env`; without it,
  `MailService` logs and continues rather than blocking registration.

## Traceability back to the specs

- **Product Specification** → informed which endpoints are "must-have" vs.
  supporting (e.g. Plant Disease Library and reference data treated as
  first-class per Section 9.1).
- **Software Architecture Document** → module boundaries (Section 2),
  security model (Section 3.2), caching strategy (Section 16),
  recommendation strategy phasing (Section 12).
- **Database Design Specification** → every entity, column, and index in
  `V1__init_schema.sql` and the JPA entities under `*/entity/` map directly
  to its six domains.
- **REST API Specification** → every controller method corresponds to a
  named endpoint from the spec, with matching request/response shapes,
  status codes, and validation rules.
