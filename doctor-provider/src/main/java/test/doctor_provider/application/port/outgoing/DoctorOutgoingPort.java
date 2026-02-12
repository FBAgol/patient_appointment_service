package test.doctor_provider.application.port.outgoing;

import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.Page;

public interface DoctorOutgoingPort {

	/**
	 * Sucht alle Ärzte mit optionalen Filtern und Paginierung.
	 *
	 * ⚠️ cityId-Filter: Benötigt JOIN zu practice Tabelle SELECT d.* FROM doctor d
	 * JOIN practice p ON d.practice_id = p.id WHERE p.city_id = ?
	 *
	 * @param firstName
	 *            Filter nach Vorname (Teilstring-Suche, case-insensitive)
	 * @param lastName
	 *            Filter nach Nachname (Teilstring-Suche, case-insensitive)
	 * @param practiceId
	 *            Filter nach Praxis-ID
	 * @param cityId
	 *            Filter nach Stadt-ID (benötigt JOIN zu practice)
	 * @param specialityId
	 *            Filter nach Fachrichtung-ID (benötigt JOIN zu doctor_speciality)
	 * @param page
	 *            Seitennummer (0-basiert)
	 * @param size
	 *            Anzahl der Elemente pro Seite
	 * @return Paginierte Liste von Ärzten
	 */
	Page<Doctor> findAll(Optional<String> firstName, Optional<String> lastName, Optional<UUID> practiceId,
			Optional<UUID> cityId, Optional<UUID> specialityId, int page, int size);

	/**
	 * Sucht einen Arzt anhand seiner ID.
	 *
	 * Wird verwendet für: - PUT/DELETE Operationen (Existenz-Prüfung) - Anzeige von
	 * Arzt-Details
	 *
	 * @param id
	 *            UUID des Arztes
	 * @return Optional mit Doctor, leer wenn nicht gefunden
	 */
	Optional<Doctor> findById(UUID id);

	/**
	 * Speichert einen neuen Arzt.
	 *
	 * @param doctor
	 *            Zu speichernder Arzt (ohne ID)
	 * @return Gespeicherter Arzt (mit generierter ID)
	 */
	Doctor save(Doctor doctor);

	/**
	 * Aktualisiert einen bestehenden Arzt.
	 *
	 * ⚠️ Hinweis: doctor.id muss gesetzt sein!
	 *
	 * @param doctor
	 *            Zu aktualisierender Arzt (mit ID)
	 * @return Aktualisierter Arzt
	 */
	Doctor modify(Doctor doctor);

	/**
	 * Löscht einen Arzt anhand seiner ID.
	 *
	 * @param id
	 *            UUID des zu löschenden Arztes
	 */
	void deleteById(UUID id);

	/**
	 * Prüft, ob ein Arzt mit der gegebenen ID existiert.
	 *
	 * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft
	 * werden soll.
	 *
	 * Verwendung: - Validierung vor PUT/DELETE
	 *
	 * @param id
	 *            UUID des Arztes
	 * @return true wenn Arzt existiert, sonst false
	 */
	boolean existsById(UUID id);
}
