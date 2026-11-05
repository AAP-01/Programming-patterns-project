PRAGMA foreign_keys = ON;

INSERT INTO users (user_id, first_name, last_name, email, phone, organization, role) VALUES
    (1, 'System', 'Administrator', 'admin@conference.local', '+1-222-0001', 'Conference Main', 'ADMIN'),
    (2, 'Credo', 'Dotse', 'credo.dotse@example.com', '+1-222-0102', 'Vanier', 'PARTICIPANT'),
    (3, 'John Benedict', 'Castrvoerde', 'john.castroverde@example.com', '+1-222-0101', 'Vanier', 'PARTICIPANT'),
    (4, 'Giuliano', 'Di Fulvio', 'giuliano.difulvio@example.com', '+1-555-0103', 'Vanier', 'PARTICIPANT');

INSERT INTO credentials (user_id, username, password_hash) VALUES
    (1, 'admin', 'admin123'),
    (2, 'credo', 'demoPassword1'),
    (3, 'john', 'demoPassword2'),
    (4, 'giuliano', 'demoPassword3');

INSERT INTO conferences (conference_id, title, description, venue, start_date, end_date, registration_open_date, registration_close_date, capacity, status) VALUES
    (1, 'Games', 'Learn about making games.', 'Building A', '2026-05-15', '2026-05-17', '2026-04-01', '2026-05-10', 80, 'OPEN'),
    (2, 'Business', 'Talk about business.', 'Building B', '2026-06-10', '2026-06-11', '2026-05-01', '2026-06-05', 50, 'OPEN'),
    (3, 'Sports', 'Talk about sports', 'Auditorium', '2026-07-02', '2026-07-04', '2026-05-20', '2026-06-25', 90, 'DRAFT');

INSERT INTO registrations (conference_id, user_id, status, waiting_position) VALUES
    (1, 2, 'CONFIRMED', NULL),
    (1, 3, 'WAITLISTED', 1),
    (1, 4, 'WAITLISTED', 2),
    (2, 2, 'CONFIRMED', NULL),
    (2, 4, 'CONFIRMED', NULL);
