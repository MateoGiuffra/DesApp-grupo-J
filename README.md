# Football API — Project Status

Short summary of what has been implemented so far.

## Overview
This is a Spring Boot application that exposes a REST API for football data (teams, players) and user authentication. It integrates with WhoScored to fetch statistics and fixtures, provides scheduling, caching, concurrency for scraping, AOP-based endpoint logging, and cookie+JWT authentication.

## Major implemented features

- Proxy-based HTTP fetching (used to access WhoScored through a proxy)
  - Implementation: [`com.desapp.football_api.service.WhoScoredService.fetchJSONString`](src/main/java/com/desapp/football_api/service/WhoScoredService.java) and [`com.desapp.football_api.service.WhoScoredService.fetchPage`](src/main/java/com/desapp/football_api/service/WhoScoredService.java) — see [src/main/java/com/desapp/football_api/service/WhoScoredService.java](src/main/java/com/desapp/football_api/service/WhoScoredService.java).

- WhoScored scraping & parsing
  - High-level scraping helpers: [`com.desapp.football_api.service.WhoScoredService.getIdFromFirstResult`](src/main/java/com/desapp/football_api/service/WhoScoredService.java) — see [src/main/java/com/desapp/football_api/service/WhoScoredService.java](src/main/java/com/desapp/football_api/service/WhoScoredService.java).
  - JSON/fixtures parsing and utilities: [`com.desapp.football_api.utils.WhoScoredHelper.getIdsFromResponse`](src/main/java/com/desapp/football_api/utils/WhoScoredHelper.java), [`com.desapp.football_api.utils.WhoScoredHelper.parseFixtures`](src/main/java/com/desapp/football_api/utils/WhoScoredHelper.java) — see [src/main/java/com/desapp/football_api/utils/WhoScoredHelper.java](src/main/java/com/desapp/football_api/utils/WhoScoredHelper.java).
  - Link builders: [`com.desapp.football_api.utils.WhoScoredLink`](src/main/java/com/desapp/football_api/utils/WhoScoredLink.java) — see [src/main/java/com/desapp/football_api/utils/WhoScoredLink.java](src/main/java/com/desapp/football_api/utils/WhoScoredLink.java).
  - Token/CSV helper: [src/main/java/com/desapp/football_api/utils/ParserUtil.java](src/main/java/com/desapp/football_api/utils/ParserUtil.java).

- Spring Boot app structure
  - Main class: [src/main/java/com/desapp/football_api/FootballApiApplication.java](src/main/java/com/desapp/football_api/FootballApiApplication.java) (scheduling and caching enabled via annotations).

- Concurrency for scraping
  - Concurrent scraping of players with a thread pool + futures: [`com.desapp.football_api.service.TeamService.scrapePlayersFromTeam`](src/main/java/com/desapp/football_api/service/TeamService.java) — see [src/main/java/com/desapp/football_api/service/TeamService.java](src/main/java/com/desapp/football_api/service/TeamService.java).

- Aspect-Oriented Programming (AOP)
  - Logging aspect that persists endpoint access logs: [`com.desapp.football_api.aspects.EndpointLoggingAspect`](src/main/java/com/desapp/football_api/aspects/EndpointLoggingAspect.java) and the marker [`com.desapp.football_api.aspects.NonCacheable`](src/main/java/com/desapp/football_api/aspects/NonCacheable.java) — see [src/main/java/com/desapp/football_api/aspects/EndpointLoggingAspect.java](src/main/java/com/desapp/football_api/aspects/EndpointLoggingAspect.java) and [src/main/java/com/desapp/football_api/aspects/NonCacheable.java](src/main/java/com/desapp/football_api/aspects/NonCacheable.java).
  - Endpoint logging service/controller: [`com.desapp.football_api.service.EndpointLogService`](src/main/java/com/desapp/football_api/service/EndpointLogService.java) and [src/main/java/com/desapp/football_api/controller/web_services/EndpointLogController.java](src/main/java/com/desapp/football_api/controller/web_services/EndpointLogController.java).

- Authentication with cookies + JWT
  - JWT utilities: [`com.desapp.football_api.security.JwtUtil`](src/main/java/com/desapp/football_api/security/JwtUtil.java) — see [src/main/java/com/desapp/football_api/security/JwtUtil.java](src/main/java/com/desapp/football_api/security/JwtUtil.java).
  - Cookie helper: [`com.desapp.football_api.service.CookieService.createCookieToResponse`](src/main/java/com/desapp/football_api/service/CookieService.java) — see [src/main/java/com/desapp/football_api/service/CookieService.java](src/main/java/com/desapp/football_api/service/CookieService.java).
  - Authentication filter: [`com.desapp.football_api.config.JwtAuthenticationFilter`](src/main/java/com/desapp/football_api/config/JwtAuthenticationFilter.java) — see [src/main/java/com/desapp/football_api/config/JwtAuthenticationFilter.java](src/main/java/com/desapp/football_api/config/JwtAuthenticationFilter.java).
  - User flows: [`com.desapp.football_api.service.UserService.register`](src/main/java/com/desapp/football_api/service/UserService.java) and [`com.desapp.football_api.service.UserService.login`](src/main/java/com/desapp/football_api/service/UserService.java) — see [src/main/java/com/desapp/football_api/service/UserService.java](src/main/java/com/desapp/football_api/service/UserService.java).

- Endpoints protected by Spring Security
  - Security configuration + public endpoints: [`com.desapp.football_api.config.SecurityConfig.filterChain`](src/main/java/com/desapp/football_api/config/SecurityConfig.java) and [`com.desapp.football_api.config.PublicEndpointsManager.PUBLIC_ENDPOINTS`](src/main/java/com/desapp/football_api/config/PublicEndpointsManager.java) — see [src/main/java/com/desapp/football_api/config/SecurityConfig.java](src/main/java/com/desapp/football_api/config/SecurityConfig.java) and [src/main/java/com/desapp/football_api/config/PublicEndpointsManager.java](src/main/java/com/desapp/football_api/config/PublicEndpointsManager.java).

- Scheduler
  - Periodic tasks: [`com.desapp.football_api.scheduler.FootballScheduler.runEvery4Hours`](src/main/java/com/desapp/football_api/scheduler/FootballScheduler.java) — see [src/main/java/com/desapp/football_api/scheduler/FootballScheduler.java](src/main/java/com/desapp/football_api/scheduler/FootballScheduler.java).

- Data persistence
  - JPA entities: teams and players: [src/main/java/com/desapp/football_api/model/Team.java](src/main/java/com/desapp/football_api/model/Team.java) and [src/main/java/com/desapp/football_api/model/player/Player.java](src/main/java/com/desapp/football_api/model/player/Player.java).
  - Repositories are used across services (e.g. `TeamRepository`, `PlayerRepository`) — see usage in [src/main/java/com/desapp/football_api/service/TeamService.java](src/main/java/com/desapp/football_api/service/TeamService.java) and [src/main/java/com/desapp/football_api/service/PlayerService.java](src/main/java/com/desapp/football_api/service/PlayerService.java).

- Cache
  - Caching enabled in the application via `@EnableCaching` on the main class: [src/main/java/com/desapp/football_api/FootballApiApplication.java](src/main/java/com/desapp/football_api/FootballApiApplication.java).
  - Marker to opt-out of caching for specific methods: [`com.desapp.football_api.aspects.NonCacheable`](src/main/java/com/desapp/football_api/aspects/NonCacheable.java).

## Notes & small guide

- Run the app
  - Standard Gradle/Spring Boot run: `./gradlew bootRun` (project root). Main: [src/main/java/com/desapp/football_api/FootballApiApplication.java](src/main/java/com/desapp/football_api/FootballApiApplication.java).

- Environment/config
  - The WhoScored proxy base and JWT secret are read from environment variables (see [`com.desapp.football_api.service.WhoScoredService`](src/main/java/com/desapp/football_api/service/WhoScoredService.java) and [`com.desapp.football_api.security.JwtUtil`](src/main/java/com/desapp/football_api/security/JwtUtil.java)).

- Tests
  - There are unit and integration tests for scraping helpers, services and security in `src/test/java/...` (examples: [src/test/java/com/desapp/football_api/utils/WhoScoredHelperTest.java](src/test/java/com/desapp/football_api/utils/WhoScoredHelperTest.java), [src/test/java/com/desapp/football_api/services/UserServiceTest.java](src/test/java/com/desapp/football_api/services/UserServiceTest.java)).

## TODO / Planned
- "Predictions": mention included in project goals — no dedicated implementation referenced in the current code excerpts. If you want a predictions module, we can design/add endpoints and services to compute/store forecasts and wire them into the scraping pipeline.

---

If you want, I can:
- generate a polished README section for deployment or env examples;
- add a "Predictions" design and a starter service/controller files.