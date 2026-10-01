# Hotel Reservation System

Spring Boot web app for hotel operations: reservations, front-desk / finance, housekeeping, inventory, employee management, and user administration. Server-rendered with Thymeleaf, secured by role with Spring Security, persisted with Spring Data JPA + MySQL.

## Tech Stack

- Java 17, Maven (`mvnw` / `mvnw.cmd`)
- Spring Boot 4.1.1 (`com.hotel:reservation:0.0.1-SNAPSHOT`)
- Spring WebMVC, Spring Data JPA (Hibernate `ddl-auto=update`), Spring Security, Validation
- Thymeleaf + `thymeleaf-extras-springsecurity6`
- MySQL (`mysql-connector-j`, runtime)
- Lombok, Spring Boot Test starters

## Modules / Roles

Route access is enforced in `src/main/java/com/hotel/reservation/config/SecurityConfig.java:72-101`. Login redirects per role (`SecurityConfig.java:45-70`).

| Role | Login landing | Area |
|---|---|---|
| `ROLE_ADMIN` | `/admin/users` | User CRUD, activate/deactivate, audit logs (`/admin/audit-logs`) |
| `ROLE_GM` | `/manager/dashboard` | General Manager: employee CRUD |
| `ROLE_RESERVATIONS` | `/reservations` | Create / edit / cancel reservations |
| `ROLE_INVENTORY` | `/inventory` | Items, stock IN/OUT movements, history |
| `ROLE_FINANCE` | `/finance` | Stays, payments, check-in, billed/collected/outstanding totals |
| `ROLE_HOUSEKEEPING` | `/housekeeping` | Cleaning tasks, room status board, maintenance requests |

Feature details:

- **Reservations** (`ReservationsController.java`): room list + reservation dashboard, create with overlap validation, edit, cancel.
- **Finance / Front Desk** (`FinanceController.java`): stay list, record payment (`POST /finance/payments`), check-in reservation (`POST /finance/checkin`).
- **Housekeeping** (`HousekeepingController.java`): task lifecycle `PENDING -> IN_PROGRESS -> COMPLETED`; room board `DIRTY -> IN_PROGRESS -> CLEAN -> INSPECTED`; maintenance `OPEN -> IN_PROGRESS -> RESOLVED`.
- **Inventory** (`InventoryController.java`): item CRUD + archive, stock movement with idempotency `requestKey`, paged history.
- **GM** (`GeneralManagerController.java`): employee CRUD.
- **Admin** (`AdminUserController.java`, `AuditLogController.java`): user CRUD, audit log view.

## Prerequisites

- JDK 17
- Maven (or use included `./mvnw`)
- MySQL 8 running on `localhost:3306`

## Configuration

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_reservation?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root123
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

Change username/password to match your local MySQL before running.

## Run

```bash
# from project root
./mvnw spring-boot:run
# or
./mvnw package
java -jar target/reservation-0.0.1-SNAPSHOT.jar
```

Then open `http://localhost:8080/login`.

Tables are auto-created by Hibernate. For manual/reference setup of the housekeeping module, see:

```bash
mysql -u root -p < database/housekeeping-module.sql
```

That script mirrors `database/housekeeping-module.sql:1-71` and seeds rooms 101-104 plus one sample task and maintenance request.

## Default Users

Seeded on first startup by `config/DataLoader.java:26-42` (passwords BCrypt-encoded). Only created if missing.

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ROLE_ADMIN` |
| `gm` | `gm123` | `ROLE_GM` |
| `reservations` | `res123` | `ROLE_RESERVATIONS` |
| `inventory` | `inv123` | `ROLE_INVENTORY` |
| `finance` | `fin123` | `ROLE_FINANCE` |
| `housekeeping` / `hkstaff`* | `hk123` | `ROLE_HOUSEKEEPING` |

\* `housekeeping` is the login user; `hkstaff` appears as sample `assigned_to` / `reported_by` data in the housekeeping seed.

## Project Structure

```
src/main/java/com/hotel/reservation/
  ReservationApplication.java
  config/       # SecurityConfig, DataLoader, HousekeepingDataLoader
  controller/   # Admin, GM, Reservations, Finance, Housekeeping, Inventory, Login
  service/      # DeskOperations, UserService, EmployeeService, etc.
  entity/       # User, Role, DeskRoom/Reservation/Stay/Payment, InventoryItem, StockMovement, HousekeepingTask, etc.
  repository/   # Spring Data JPA repositories
  security/     # CustomUserDetailsService
src/main/resources/
  application.properties
  templates/    # Thymeleaf: login, admin/, manager/, reservations/, finance/, housekeeping/, inventory/, error/
  static/css/housekeeping.css  static/js/housekeeping.js
database/
  housekeeping-module.sql
```

## Tests

```bash
./mvnw test
```

Covers context load via `ReservationApplicationTests`.
