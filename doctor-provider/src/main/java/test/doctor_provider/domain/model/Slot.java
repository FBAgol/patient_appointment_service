package test.doctor_provider.domain.model;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.domain.enums.SlotStatus;

/**
 * Ein konkreter Termin-Slot für einen Arzt.
 * Wird aus den doctor_working_hours generiert.
 *
 * Die Beziehung ist unidirektional: Slot → Doctor (FK: doctor_id in slot-Tabelle)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Slot {
    private UUID id;
    private UUID doctorId;              // ← NUR die ID! Nicht das ganze Doctor-Objekt
    private ZonedDateTime startTime;    // z.B. 2026-01-20T10:00+01:00[Europe/Berlin]
    private ZonedDateTime endTime;      // z.B. 2026-01-20T10:30+01:00[Europe/Berlin]
    private SlotStatus status;          // AVAILABLE, BOOKED, BLOCKED
}
