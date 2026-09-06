-- =============================================================
-- Hotel Reservation System - Housekeeping Module (Ahamed M.A.S)
-- Database: hotel_reservation
--
-- NOTE: The Spring Boot app auto-creates these tables at startup
-- (spring.jpa.hibernate.ddl-auto=update). This script is provided
-- for manual setup, reference, and the project report.
-- Run AFTER creating the database:
--     CREATE DATABASE hotel_reservation;
--     USE hotel_reservation;
-- =============================================================

USE hotel_reservation;

-- -------------------------------------------------------------
-- 1. Housekeeping cleaning tasks
--    Lifecycle: PENDING -> IN_PROGRESS -> COMPLETED
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS housekeeping_tasks (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    room_number VARCHAR(255) NOT NULL,
    assigned_to VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    notes       VARCHAR(1000),
    created_at  DATETIME(6),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- 2. Room cleaning status board
--    Lifecycle: DIRTY -> IN_PROGRESS -> CLEAN -> INSPECTED
--    (Rows are created implicitly with the room - in future by
--     the GM module; one row per room, room_number is unique)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS room_cleaning_status (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    room_number VARCHAR(255) NOT NULL,
    status      VARCHAR(255) NOT NULL DEFAULT 'DIRTY',
    updated_at  DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY UK_room_cleaning_status_room (room_number)
);

-- -------------------------------------------------------------
-- 3. Maintenance / repair requests
--    Lifecycle: OPEN -> IN_PROGRESS -> RESOLVED
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS maintenance_requests (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    room_number VARCHAR(255)  NOT NULL,
    issue       VARCHAR(1000) NOT NULL,
    reported_by VARCHAR(255)  NOT NULL,
    status      VARCHAR(255)  NOT NULL DEFAULT 'OPEN',
    created_at  DATETIME(6),
    PRIMARY KEY (id)
);

-- -------------------------------------------------------------
-- Sample data (mirrors the demo data seeded by the app)
-- -------------------------------------------------------------
INSERT INTO room_cleaning_status (room_number, status, updated_at) VALUES
    ('101', 'DIRTY', NOW()),
    ('102', 'DIRTY', NOW()),
    ('103', 'DIRTY', NOW()),
    ('104', 'DIRTY', NOW());

INSERT INTO housekeeping_tasks (room_number, assigned_to, status, notes, created_at) VALUES
    ('101', 'hkstaff', 'PENDING', 'Checkout cleaning - change linens, restock minibar', NOW());

INSERT INTO maintenance_requests (room_number, issue, reported_by, status, created_at) VALUES
    ('103', 'AC not cooling, makes rattling noise', 'hkstaff', 'OPEN', NOW());
