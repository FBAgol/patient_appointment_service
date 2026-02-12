package test.doctor_provider.application.port.outgoing;

import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;

public interface CityOutgoingPort {

	/**
	 * Sucht alle Städte mit optionalen Filtern und Paginierung.
	 *
	 * @param name
	 *            Optional: Filter nach Stadt-Name (Teilstring-Suche,
	 *            case-insensitive)
	 * @param postalCode
	 *            Optional: Filter nach Postleitzahl (Teilstring-Suche)
	 * @param page
	 *            Seitennummer (0-basiert)
	 * @param size
	 *            Anzahl der Elemente pro Seite
	 * @return Paginierte Liste von Städten
	 */
	Page<City> findAll(Optional<String> name, Optional<String> postalCode, int page, int size);

	/**
	 * Sucht eine Stadt anhand ihrer ID.
	 *
	 * Wird z.B. verwendet für Validierungen: - Beim Anlegen einer Practice:
	 * Existiert die cityId? - Beim Filtern von Doctors: Ist die cityId gültig?
	 *
	 * @param id
	 *            UUID der Stadt
	 * @return Optional mit City, leer wenn nicht gefunden
	 */
	Optional<City> findById(UUID id);

	/**
	 * Prüft, ob eine Stadt mit der gegebenen ID existiert.
	 *
	 * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft
	 * werden soll.
	 *
	 * Verwendung: - Validierung bei Practice.cityId - Validierung bei Filtern (z.B.
	 * GET /api/v1/doctors?cityId=...)
	 *
	 * @param id
	 *            UUID der Stadt
	 * @return true wenn Stadt existiert, sonst false
	 */
	boolean existsById(UUID id);
}
