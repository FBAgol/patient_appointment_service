CREATE TABLE doctor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    practice_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    CONSTRAINT fk_doctor_practice FOREIGN KEY (practice_id)
        REFERENCES practice(id) ON DELETE CASCADE,

    CONSTRAINT doctor_first_name_not_empty CHECK (LENGTH(TRIM(first_name)) > 0),
    CONSTRAINT doctor_last_name_not_empty CHECK (LENGTH(TRIM(last_name)) > 0)
);

-- Indizes
CREATE INDEX idx_doctor_practice ON doctor(practice_id);
CREATE INDEX idx_doctor_name ON doctor(last_name, first_name);
