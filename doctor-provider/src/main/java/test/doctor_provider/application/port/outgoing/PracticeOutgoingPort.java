package test.doctor_provider.application.port.outgoing;

import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.model.Page;
import test.doctor_provider.domain.model.Practice;

public interface PracticeOutgoingPort {

	/**
	 * Sucht alle Praxen mit optionalen Filtern und Paginierung.
	 *
	 * @param cityId
	 *            Optional: Filter nach Stadt-ID
	 * @param practiceName
	 *            Optional: Filter nach Praxis-Name (Teilstring-Suche)
	 * @param page
	 *            Seitennummer (0-basiert)
	 * @param size
	 *            Anzahl der Elemente pro Seite
	 * @return Paginierte Liste von Praxen
	 */
	Page<Practice> findAll(Optional<UUID> cityId, Optional<String> practiceName, int page, int size);

	/**
	 * Sucht eine Praxis anhand ihrer ID.
	 *
	 * Wird verwendet für: - Validierungen bei Doctor.practiceId - PUT/DELETE
	 * Operationen (Existenz-Prüfung)
	 *
	 * @param id
	 *            UUID der Praxis
	 * @return Optional mit Practice, leer wenn nicht gefunden
	 */
	Optional<Practice> findById(UUID id);

	/**
	 * Speichert eine neue Praxis.
	 *
	 * @param practice
	 *            Zu speichernde Praxis (ohne ID)
	 * @return Gespeicherte Praxis (mit generierter ID)
	 */
	Practice save(Practice practice);

	/**
	 * Aktualisiert eine bestehende Praxis.
	 *
	 * @param practice
	 *            Zu aktualisierende Praxis (mit ID)
	 * @return Aktualisierte Praxis
	 */
	Practice remove(Practice practice);

	/**
	 * Löscht eine Praxis anhand ihrer ID.
	 *
	 * @param id
	 *            UUID der zu löschenden Praxis
	 */
	void removeById(UUID id);

	/**
	 * Prüft, ob eine Praxis mit der gegebenen ID existiert.
	 *
	 * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft
	 * werden soll.
	 *
	 * Verwendung: - Validierung bei Doctor.practiceId - Validierung vor PUT/DELETE
	 *
	 * @param id
	 *            UUID der Praxis
	 * @return true wenn Praxis existiert, sonst false
	 */
	boolean existsById(UUID id);

	/**
	 * Prüft, ob eine Praxis mit dem gegebenen Namen bereits existiert.
	 *
	 * Verwendung: - Unique-Constraint-Prüfung bei POST /api/v1/practice -
	 * Konflikt-Prüfung bei PUT /api/v1/practice/{id} - Verhindert Duplikate (409
	 * Conflict)
	 *
	 * @param name
	 *            Name der Praxis (case-sensitive oder case-insensitive je nach
	 *            DB-Config)
	 * @return true wenn Praxis mit diesem Namen existiert
	 */
	boolean existsByName(String name);

	/**
	 * Prüft, ob eine andere Praxis (außer der mit gegebener ID) den Namen bereits
	 * verwendet.
	 *
	 * Verwendung bei PUT: - Prüfe ob neuer Name bereits von ANDERER Praxis
	 * verwendet wird - Erlaubt: Praxis behält ihren eigenen Namen - Verhindert:
	 * Praxis nimmt Namen einer anderen Praxis (409 Conflict)
	 *
	 * @param name
	 *            Name der Praxis
	 * @param excludeId
	 *            ID der Praxis, die ausgeschlossen werden soll (die gerade
	 *            aktualisiert wird)
	 * @return true wenn eine ANDERE Praxis diesen Namen hat
	 */
	boolean existsByNameAndIdNot(String name, UUID excludeId);
}
