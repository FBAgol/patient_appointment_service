CREATE TABLE city (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    zip_code VARCHAR(20) NOT NULL,
    CONSTRAINT city_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT city_zip_code_not_empty CHECK (LENGTH(TRIM(zip_code)) > 0)
);