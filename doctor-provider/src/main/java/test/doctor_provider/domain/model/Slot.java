package test.doctor_provider.domain.model;

import java.time.LocalDateTime;
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
    private LocalDateTime startTime;    // z.B. 2026-01-20 10:00
    private LocalDateTime endTime;      // z.B. 2026-01-20 10:30
    private SlotStatus status;          // AVAILABLE, BOOKED, BLOCKED
}
