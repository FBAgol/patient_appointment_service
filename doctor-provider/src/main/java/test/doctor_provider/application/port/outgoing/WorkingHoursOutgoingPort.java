package test.doctor_provider.application.port.outgoing;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.enums.Weekday;
import test.doctor_provider.domain.model.DoctorWorkingHours;

public interface WorkingHoursOutgoingPort {

	/**
	 * Speichert neue Working Hours für einen Arzt.
	 *
	 * ⚠️ Hinweis: Slots werden automatisch generiert (in einem separaten
	 * Use-Case/Service)
	 *
	 * @param workingHours
	 *            Zu speichernde Working Hours (ohne ID, aber mit doctorId)
	 * @return Gespeicherte Working Hours (mit generierter ID)
	 */
	DoctorWorkingHours save(DoctorWorkingHours workingHours);

	/**
	 * Gibt alle Working Hours eines Arztes zurück.
	 *
	 * @param doctorId
	 *            UUID des Arztes
	 * @return Liste aller Working Hours des Arztes (kann leer sein)
	 */
	List<DoctorWorkingHours> findAllByDoctorId(UUID doctorId);

	/**
	 * Sucht eine Working Hour anhand ihrer ID.
	 *
	 * Wird verwendet für: - PUT/DELETE Operationen (Existenz-Prüfung) -
	 * Slot-Generierung für spezifische Working Hour
	 *
	 * @param id
	 *            UUID der Working Hours
	 * @return Optional mit DoctorWorkingHours, leer wenn nicht gefunden
	 */
	Optional<DoctorWorkingHours> findById(UUID id);

	/**
	 * Aktualisiert eine bestehende Working Hour.
	 *
	 * ⚠️ Hinweis: workingHours.id muss gesetzt sein!
	 *
	 * @param workingHours
	 *            Zu aktualisierende Working Hours (mit ID)
	 * @return Aktualisierte Working Hours
	 */
	DoctorWorkingHours update(DoctorWorkingHours workingHours);

	/**
	 * Löscht eine Working Hour anhand ihrer ID.
	 *
	 * ⚠️ Wichtig: Zugehörige Slots sollten ebenfalls gelöscht werden (CASCADE oder
	 * manuell)
	 *
	 * @param id
	 *            UUID der zu löschenden Working Hours
	 */
	void deleteById(UUID id);

	/**
	 * Prüft, ob eine Working Hour mit der gegebenen ID existiert.
	 *
	 * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft
	 * werden soll.
	 *
	 * Verwendung: - Validierung vor PUT/DELETE - Validierung vor Slot-Generierung
	 *
	 * @param id
	 *            UUID der Working Hours
	 * @return true wenn Working Hour existiert, sonst false
	 */
	boolean existsById(UUID id);

	/**
	 * Prüft, ob es eine Zeitüberlappung für einen Arzt an einem bestimmten
	 * Wochentag gibt.
	 *
	 * Verwendung bei POST/PUT: - Verhindert überlappende Working Hours (409
	 * Conflict) - Beispiel: Arzt hat bereits 08:00-12:00 am Montag → 10:00-14:00
	 * würde überlappen
	 *
	 * ⚠️ Wichtig: Beim UPDATE muss die eigene ID ausgeschlossen werden!
	 *
	 * @param doctorId
	 *            UUID des Arztes
	 * @param weekday
	 *            Wochentag (MONDAY, TUESDAY, etc.)
	 * @param startTime
	 *            Startzeit der neuen/zu ändernden Working Hour
	 * @param endTime
	 *            Endzeit der neuen/zu ändernden Working Hour
	 * @param excludeId
	 *            Optional: ID auszuschließen (bei UPDATE die eigene ID)
	 * @return true wenn Überlappung existiert, sonst false
	 */
	boolean existsOverlapping(UUID doctorId, Weekday weekday, LocalTime startTime, LocalTime endTime,
			Optional<UUID> excludeId);
}
