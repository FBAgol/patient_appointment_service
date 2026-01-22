CREATE TYPE slot_status AS ENUM ('available', 'booked', 'blocked');

CREATE TABLE slot
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id  UUID NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    status     slot_status NOT NULL,

    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor (id) ON DELETE CASCADE,

    CONSTRAINT chk_slot_start_end_time CHECK (start_time < end_time)
);

CREATE UNIQUE INDEX idx_slot_doctor_time ON slot(doctor_id, start_time, end_time);

