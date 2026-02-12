package test.doctor_provider.application.port.outgoing;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import test.doctor_provider.domain.model.Speciality;

public interface SpecialityOutgoingPort {

	/**
	 * @return Liste aller Fachrichtungen
	 */
	List<Speciality> findAll();

	/**
	 * Sucht eine Fachrichtung anhand ihrer ID.
	 *
	 * Wird z.B. verwendet für: - Validierung bei Doctor.specialityIds - Anzeige von
	 * Details zu einer Fachrichtung
	 *
	 * @param id
	 *            UUID der Fachrichtung
	 * @return Optional mit Speciality, leer wenn nicht gefunden
	 */
	Optional<Speciality> findById(UUID id);

	/**
	 * Sucht mehrere Fachrichtungen anhand ihrer IDs.
	 *
	 * Wird verwendet bei: - Doctor hat Set<UUID> specialityIds → Lade alle
	 * zugehörigen Specialities - Bulk-Validierung: Existieren alle IDs?
	 *
	 * @param ids
	 *            Set von UUIDs
	 * @return Liste von gefundenen Fachrichtungen (kann kleiner sein als ids.size()
	 *         wenn einige nicht existieren)
	 */
	List<Speciality> findAllByIds(Set<UUID> ids);

	/**
	 * Prüft, ob eine Fachrichtung mit der gegebenen ID existiert.
	 *
	 * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft
	 * werden soll.
	 *
	 * Verwendung: - Validierung bei Doctor.specialityIds - Validierung bei Filtern
	 * (z.B. GET /api/v1/doctors?specialityId=...)
	 *
	 * @param id
	 *            UUID der Fachrichtung
	 * @return true wenn Fachrichtung existiert, sonst false
	 */
	boolean existsById(UUID id);

	/**
	 * Prüft, ob alle Fachrichtungen mit den gegebenen IDs existieren.
	 *
	 * Verwendung: - Validierung bei Doctor-Erstellung: Existieren alle
	 * specialityIds? - Bulk-Validierung
	 *
	 * @param ids
	 *            Set von UUIDs zu prüfen
	 * @return true wenn ALLE IDs existieren, sonst false
	 */
	boolean existsAllByIds(Set<UUID> ids);
}
