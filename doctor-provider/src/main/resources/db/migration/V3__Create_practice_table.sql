CREATE TABLE practice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    street VARCHAR(300) NOT NULL,
    houseNumber VARCHAR(20) NOT NULL,
    phone VARCHAR(50) NOT NULL ,
    email VARCHAR(100) NOT NULL ,
    postal_code VARCHAR(20) NOT NULL ,
    city_id UUID NOT NULL,

    CONSTRAINT fk_practice_city FOREIGN KEY (city_id)
        REFERENCES city(id) ON DELETE CASCADE,

    CONSTRAINT practice_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT practice_street_not_empty CHECK (LENGTH(TRIM(street)) > 0),
    CONSTRAINT practice_house_number_not_empty CHECK (LENGTH(TRIM(houseNumber)) > 0),
    CONSTRAINT practice_email_not_empty CHECK (LENGTH(TRIM(email)) > 0),
    CONSTRAINT practice_phone_not_empty CHECK (LENGTH(TRIM(phone)) > 0),
    CONSTRAINT practice_postal_code_not_empty CHECK (LENGTH(TRIM(postal_code)) > 0),
    CONSTRAINT practice_city_id_not_empty CHECK (city_id IS NOT NULL),
    CONSTRAINT practice_email_format CHECK (email IS NULL OR email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT practice_phone_format CHECK (phone IS NULL OR phone ~* '^\+?[0-9\s\-\(\)]+$')
);
