CREATE TABLE doctor_working_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    weekday INT NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    CONSTRAINT fk_doctor_working_hours_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,

    CONSTRAINT chk_start_end_time CHECK (start_time < end_time)
);