PRAGMA foreign_keys = ON;

-- Every available user will be in this table
CREATE TABLE users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    phone TEXT,
    organization TEXT,
    role TEXT NOT NULL CHECK (role IN ('ADMIN', 'PARTICIPANT')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Set of credentials a user will use when logging in
CREATE TABLE credentials (
    user_id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE   -- The user the credentials belong to
);

-- Every available conference will be in this table
CREATE TABLE conferences (
    conference_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    venue TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    registration_open_date TEXT NOT NULL,
    registration_close_date TEXT NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    status TEXT NOT NULL CHECK (status IN ('DRAFT', 'OPEN', 'FULL', 'CLOSED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (date(start_date) <= date(end_date)),
    CHECK (date(registration_open_date) <= date(registration_close_date))
);

-- A registration is created when a user enters a conference
CREATE TABLE registrations (
    registration_id INTEGER PRIMARY KEY AUTOINCREMENT,
    conference_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED')),
    waiting_position INTEGER,
    registered_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (conference_id, user_id),
    FOREIGN KEY (conference_id) REFERENCES conferences(conference_id) ON DELETE CASCADE,    -- The conference the registration is for
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,  -- The user who registered
    CHECK (
        (status = 'WAITLISTED' AND waiting_position IS NOT NULL AND waiting_position > 0)
        OR (status IN ('CONFIRMED', 'CANCELLED') AND waiting_position IS NULL)
    )
);


CREATE INDEX idx_registrations_conference_status
    ON registrations (conference_id, status);

CREATE INDEX idx_registrations_user
    ON registrations (user_id);
