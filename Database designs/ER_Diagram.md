# Conference Management System ER Diagram

```mermaid
erDiagram
    USERS ||--|| CREDENTIALS : has
    USERS ||--o{ REGISTRATIONS : makes
    CONFERENCES ||--o{ REGISTRATIONS : receives

    USERS {
        int user_id PK
        string first_name
        string last_name
        string email UK
        string phone
        string organization
        string role
        datetime created_at
    }

    CREDENTIALS {
        int user_id PK, FK
        string username UK
        string password_hash
    }

    CONFERENCES {
        int conference_id PK
        string title
        string description
        string venue
        date start_date
        date end_date
        date registration_open_date
        date registration_close_date
        int capacity
        string status
        datetime created_at
    }

    REGISTRATIONS {
        int registration_id PK
        int conference_id FK
        int user_id FK
        string status
        int waiting_position
        datetime registered_at
    }
```

## Tables and relationships

- Each user has one credentials record. Credentials are stored separately from profile information.
- A user can register for many conferences, and a conference can have many registrations.
- Each registration belongs to one user and one conference.
- A user can only have one registration per conference.

## Constraints

- User email and credential username must be unique.
- User role must be `ADMIN` or `PARTICIPANT`.
- Conference capacity must be greater than zero.
- Conference and registration statuses are restricted to the values defined in `schema.sql`.
- Conference start date must not be after its end date. Registration open date must not be after its close date.
- A waitlisted registration must have a positive waiting-list position. Confirmed and cancelled registrations must not have a waiting-list position.
- Deleting a user or conference deletes their related credentials or registrations through the foreign-key cascade rules.
