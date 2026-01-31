-- Erstelle ENUM-Typ für Wochentage (konsistent mit OpenAPI-Spec)
CREATE TYPE weekday_enum AS ENUM (
    'MONDAY',
    'TUESDAY',
    'WEDNESDAY',
    'THURSDAY',
    'FRIDAY',
    'SATURDAY',
    'SUNDAY'
);

CREATE TABLE doctor_working_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    weekday weekday_enum NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CONSTRAINT fk_doctor_working_hours_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,

    CONSTRAINT chk_start_end_time CHECK (start_time < end_time)
);

COMMENT ON COLUMN doctor_working_hours.weekday IS 'Wochentag als ENUM - konsistent mit OpenAPI Weekday-Enum';
