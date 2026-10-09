# Design Patterns Implemented in Hotel Reservation System

Spring Boot web app for hotel operations: reservations, front-desk / finance,
housekeeping, inventory, employee management, and user administration.
Server-rendered with Thymeleaf, secured by role with Spring Security, persisted with Spring Data JPA + MySQL/MariaDB.

This document records the design patterns actually implemented in the codebase, with pointers to the source. Pricing (`nightlyRate x nights`) is intentionally listed as *not* patterned — see the last section.

## 1. Singleton (Creational, via Spring container)

All services, controllers, repositories, and config seeders are Spring beans,
singleton-scoped by default. No manual `getInstance()` code.

- `src/main/java/com/hotel/reservation/service/GuestBookingService.java:26` — `@Service`
- `src/main/java/com/hotel/reservation/service/RoomService.java:15` — `@Service`
- `src/main/java/com/hotel/reservation/service/AuditLogService.java:9` — `@Service`
- `src/main/java/com/hotel/reservation/service/DeskOperations.java:18` — `@Service`
- `src/main/java/com/hotel/reservation/config/DataLoader.java:11` — `@Component`
- `src/main/java/com/hotel/reservation/config/HousekeepingDataLoader.java:17` — `@Component`
- `src/main/java/com/hotel/reservation/security/CustomUserDetailsService.java:9` — `@Service`

## 2. Dependency Injection / Inversion of Control (Architectural)

Dependencies are provided through constructor injection. Classes never call `new` on services or repositories.

- `src/main/java/com/hotel/reservation/controller/GuestBookingController.java:27` —
  `GuestBookingController(GuestBookingService guests)`
- `src/main/java/com/hotel/reservation/service/RoomService.java:22-29` —
  `RoomService(DeskRoomRepository, RoomCleaningStatusService, AuditLogService)`
- `src/main/java/com/hotel/reservation/service/GuestBookingService.java:35-43` —
  `GuestBookingService(DeskRoomRepository, DeskStayRepository, DeskReservationRepository, AuditLogService)`
- `src/main/java/com/hotel/reservation/controller/HousekeepingController.java:26-34` —
  `HousekeepingController(HousekeepingTaskService, RoomCleaningStatusService, MaintenanceRequestService, UserRepository)`

## 3. Model-View-Controller (Architectural)

`@Controller` handlers build a `Model` and return a Thymeleaf view name.
Entities are the model; `src/main/resources/templates/**/*.html` are the views.

- `src/main/java/com/hotel/reservation/controller/HousekeepingController.java:109-118` —
  `roomStatusBoard()` adds `rooms`, `dirtyCount`, ... and returns `"housekeeping/rooms"`
- `src/main/java/com/hotel/reservation/controller/GuestBookingController.java:31-96` —
  `page()` adds `rooms`, `availability`, `totals`, `managed`, `receipt` and returns `"book"`
- `src/main/java/com/hotel/reservation/controller/AdminUserController.java:17-21` —
  `listUsers()` adds `users` and returns `"admin/users"`
- Views: `src/main/resources/templates/book.html`,
  `src/main/resources/templates/housekeeping/rooms.html`,
  `src/main/resources/templates/admin/users.html`

## 4. Repository / Data Access Object (Architectural)

All persistence goes through Spring Data `JpaRepository` interfaces. Services contain no JDBC or raw SQL (apart from a few `@Query` lock lookups).

- `src/main/java/com/hotel/reservation/repository/DeskRoomRepository.java:13` —
  `interface DeskRoomRepository extends JpaRepository<DeskRoom, Long>`
- `src/main/java/com/hotel/reservation/repository/DeskReservationRepository.java:11`
- `src/main/java/com/hotel/reservation/repository/DeskStayRepository.java:9`
- `src/main/java/com/hotel/reservation/repository/UserRepository.java:6`
- `src/main/java/com/hotel/reservation/repository/RoomCleaningStatusRepository.java:7`
- `src/main/java/com/hotel/reservation/repository/AuditLogRepository.java:6`
- `src/main/java/com/hotel/reservation/repository/EmployeeRepository.java:7-8`

## 5. Service Layer / Facade (Architectural)

`DeskOperations` fronts rooms, stays, reservations, and payments behind one staff-facing API so controllers stay thin. `RoomService` plays the same role for the GM room domain (room save + cleaning-status sync + audit).

- `src/main/java/com/hotel/reservation/service/DeskOperations.java:18-19` —
  `@Service @Transactional`, methods such as check-in / payment / reservation update
- `src/main/java/com/hotel/reservation/service/RoomService.java:53-73` —
  `saveRoom()` validates, saves `DeskRoom`, calls `roomCleaningStatusService.ensureRoom()`, writes audit log

## 6. Proxy via AOP (Structural)

`@Transactional` and `@PreAuthorize` are implemented by Spring runtime proxies.
The annotations declaratively add transaction boundaries and authorization without explicit proxy code.

- `src/main/java/com/hotel/reservation/service/GuestBookingService.java:27` — `@Transactional`
- `src/main/java/com/hotel/reservation/service/DeskOperations.java:19` — `@Transactional`
- `src/main/java/com/hotel/reservation/service/InventoryService.java:17` — `@Transactional`
- `src/main/java/com/hotel/reservation/service/RoomService.java:32` —
  `@PreAuthorize("hasRole('GM')")`
- `src/main/java/com/hotel/reservation/service/DeskOperations.java:63` —
  `@PreAuthorize("hasRole('RESERVATIONS')")`
- `src/main/java/com/hotel/reservation/service/InventoryService.java:18` —
  `@PreAuthorize("hasRole('INVENTORY')")`
- Method security enabled in `src/main/java/com/hotel/reservation/config/SecurityConfig.java:27` —
  `@EnableMethodSecurity`

## 7. Command (Behavioral)

Startup seeders implement `CommandLineRunner`, i.e. encapsulated commands the framework executes at boot.

- `src/main/java/com/hotel/reservation/config/DataLoader.java:12` —
  `public class DataLoader implements CommandLineRunner` (roles + default users)
- `src/main/java/com/hotel/reservation/config/HousekeepingDataLoader.java:19` —
  `public class HousekeepingDataLoader implements CommandLineRunner` (housekeeping demo data)

## 8. State, lightweight (Behavioral)

Booking and cleaning lifecycles are modelled as explicit status sets with guarded transitions in services. This is a lightweight State pattern: the states are named (enums/constants) and transitions validated, but there are no per-state classes.

- `src/main/java/com/hotel/reservation/entity/DeskReservation.java:13-17` —
  `enum Status { CONFIRMED, CANCELLED, CHECKED_IN }`
- `src/main/java/com/hotel/reservation/entity/RoomCleaningStatus.java:11-14` —
  `STATUS_DIRTY / STATUS_IN_PROGRESS / STATUS_CLEAN / STATUS_INSPECTED`
- Guards: `src/main/java/com/hotel/reservation/service/GuestBookingService.java:136-137`
  (`Only confirmed reservations can be modified`),
  `src/main/java/com/hotel/reservation/service/GuestBookingService.java:167-168`
  (`Only confirmed reservations can be cancelled`)

## 9. Strategy, lightweight (Behavioral)

Role-based post-login routing selects a target URL per role — one algorithm interface (`AuthenticationSuccessHandler`) with a runtime branch.

- `src/main/java/com/hotel/reservation/config/SecurityConfig.java:36-81` —
  `customSuccessHandler()`: `ROLE_ADMIN -> /admin/users`, `ROLE_GM -> /manager/dashboard`,
  `ROLE_RESERVATIONS -> /reservations`, `ROLE_INVENTORY -> /inventory`,
  `ROLE_FINANCE -> /finance`, `ROLE_HOUSEKEEPING -> /housekeeping`

## 10. Front Controller (Architectural)

Spring's `DispatcherServlet` is the single entry point; `@Controller` classes with `@RequestMapping`/`@GetMapping`/`@PostMapping` handle routes, and a `@ControllerAdvice` centralizes business-error handling for staff flows.

- `src/main/java/com/hotel/reservation/controller/GuestBookingController.java:21-22` —
  `@Controller @RequestMapping("/book")`
- `src/main/java/com/hotel/reservation/controller/HousekeepingController.java:17-18` —
  `@Controller @RequestMapping("/housekeeping")`
- `src/main/java/com/hotel/reservation/controller/AdminUserController.java:8-9` —
  `@Controller @RequestMapping("/admin/users")`
- `src/main/java/com/hotel/reservation/controller/DeskErrorAdvice.java:6-7` —
  `@ControllerAdvice(assignableTypes = {FinanceController.class, ReservationsController.class})`

## Explicitly not patterned: room-price calculation

There is no `PricingStrategy` / Factory for pricing. The total is computed inline as `nightlyRate.multiply(BigDecimal.valueOf(nights))` with
`ChronoUnit.DAYS.between(arrival, departure)`, duplicated across layers, plus a copy-pasted `money()` validator. This is the seam to introduce a Strategy
if seasonal, weekend, or member pricing is ever needed.

- `src/main/java/com/hotel/reservation/entity/DeskReservation.java:62-66` (constructor)
- `src/main/java/com/hotel/reservation/entity/DeskReservation.java:131-135` (`update()`)
- `src/main/java/com/hotel/reservation/service/GuestBookingService.java:59-65` (`money()`),
  `:105-106` (total check)
- `src/main/java/com/hotel/reservation/service/DeskOperations.java:210-215`, `:354-355`
- `src/main/java/com/hotel/reservation/controller/GuestBookingController.java:57-66` (display totals)

## Summary table

| Pattern | Type | Where | Evidence |
|---|---|---|---|
| Singleton (Spring bean) | Creational | All services/controllers/configs | `service/GuestBookingService.java:26`, `config/DataLoader.java:11` |
| Dependency Injection | Architectural | Constructor injection everywhere | `controller/GuestBookingController.java:27`, `service/RoomService.java:22-29` |
| MVC | Architectural | Controllers + Thymeleaf + entities | `controller/HousekeepingController.java:109-118` → `templates/housekeeping/rooms.html` |
| Repository / DAO | Architectural | Spring Data JPA interfaces | `repository/DeskRoomRepository.java:13`, `repository/UserRepository.java:6` |
| Service Layer / Facade | Architectural | `DeskOperations`, `RoomService` | `service/DeskOperations.java:18-19`, `service/RoomService.java:53-73` |
| Proxy (AOP) | Structural | `@Transactional`, `@PreAuthorize` | `service/GuestBookingService.java:27`, `service/RoomService.java:32` |
| Command | Behavioral | Startup seeders | `config/DataLoader.java:12`, `config/HousekeepingDataLoader.java:19` |
| State (lightweight) | Behavioral | Reservation / cleaning lifecycles | `entity/DeskReservation.java:13-17`, `entity/RoomCleaningStatus.java:11-14` |
| Strategy (lightweight) | Behavioral | Role-based login redirect | `config/SecurityConfig.java:36-81` |
| Front Controller | Architectural | `DispatcherServlet` + controllers + advice | `controller/GuestBookingController.java:21-22`, `controller/DeskErrorAdvice.java:6-7` |
