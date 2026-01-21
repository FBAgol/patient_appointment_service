CREATE TABLE speciality (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT speciality_name_not_empty CHECK (LENGTH(TRIM(name)) > 0)
);
