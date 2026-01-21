-- =============================================
-- Doctor Table
-- =============================================
CREATE TABLE doctor (
    id BIGSERIAL PRIMARY KEY,
    practice_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    license_number VARCHAR(50) NOT NULL UNIQUE,
    years_of_experience INTEGER,
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_doctor_practice FOREIGN KEY (practice_id)
        REFERENCES practice(id) ON DELETE CASCADE,

    CONSTRAINT doctor_first_name_not_empty CHECK (LENGTH(TRIM(first_name)) > 0),
    CONSTRAINT doctor_last_name_not_empty CHECK (LENGTH(TRIM(last_name)) > 0),
    CONSTRAINT doctor_email_valid CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indizes
CREATE INDEX idx_doctor_practice ON doctor(practice_id);
CREATE INDEX idx_doctor_name ON doctor(last_name, first_name);
CREATE INDEX idx_doctor_email ON doctor(email);
