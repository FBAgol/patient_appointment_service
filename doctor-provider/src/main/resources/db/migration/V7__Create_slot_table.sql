CREATE TYPE slot_status AS ENUM ('available', 'booked', 'blocked');

CREATE TABLE slot
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    working_hours_id  UUID NOT NULL,
    start_time        TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time          TIMESTAMP WITH TIME ZONE NOT NULL,
    status            slot_status NOT NULL,

    CONSTRAINT fk_slot_working_hours FOREIGN KEY (working_hours_id)
        REFERENCES doctor_working_hours (id) ON DELETE CASCADE,

    CONSTRAINT chk_slot_start_end_time CHECK (start_time < end_time)
);

-- Index für schnelle Abfragen nach Working Hours und Zeit
CREATE UNIQUE INDEX idx_slot_working_hours_time ON slot(working_hours_id, start_time, end_time);

-- Index für Slot-Suche nach Datum und Status
CREATE INDEX idx_slot_start_time_status ON slot(start_time, status);

COMMENT ON COLUMN slot.working_hours_id IS 'FK zu doctor_working_hours - Slots gehören zu Working Hours';
COMMENT ON TABLE slot IS 'Slots werden aus Working Hours generiert. Cascade Delete wenn Working Hours gelöscht werden.';

