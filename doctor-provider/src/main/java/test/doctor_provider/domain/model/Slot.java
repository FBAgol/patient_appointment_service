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
 * Beziehung:
 * - Slot → WorkingHours (FK: working_hours_id) - EINZIGE direkte Beziehung
 *
 * Um die doctorId zu bekommen:
 * SELECT wh.doctor_id FROM slot s JOIN working_hours wh ON s.working_hours_id = wh.id
 *
 * ⚠️ Design-Entscheidung: Normalisiert (keine Redundanz)
 * - Kein doctor_id in slot-Tabelle
 * - Doctor wird über working_hours ermittelt (JOIN)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Slot {
    private UUID id;
    private UUID workingHoursId;        // ← EINZIGE FK (zu WorkingHours)
    private ZonedDateTime startTime;    // z.B. 2026-01-20T10:00+01:00[Europe/Berlin]
    private ZonedDateTime endTime;      // z.B. 2026-01-20T10:30+01:00[Europe/Berlin]
    private SlotStatus status;          // AVAILABLE, BOOKED, BLOCKED
}
