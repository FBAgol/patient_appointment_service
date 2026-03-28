package test.doctor_provider.application.port.incoming;

import java.util.UUID;

import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.DoctorSearchCriteria;
import test.doctor_provider.domain.model.Page;

/**
 * Inbound Port für Doctor-Operationen. Definiert die Business-Use-Cases für
 * Ärzte.
 *
 * Hinweis: Methodennamen sind identisch mit den API-Operationen (operationId in
 * OpenAPI), um die Zuordnung zwischen HTTP-Adapter (DoctorController) und
 * Use-Case klar zu machen.
 */
public interface DoctorIncomingPort {

	/**
	 * Gibt alle Ärzte zurück, optional gefiltert nach Suchkriterien.
	 *
	 * Entspricht: GET /api/v1/internal/doctors und GET
	 * /api/v1/external/doctors
	 *
	 * @param criteria
	 *            Suchkriterien (alle Felder optional, null = nicht filtern)
	 * @param page
	 *            Seitennummer (0-basiert)
	 * @param size
	 *            Anzahl der Elemente pro Seite
	 * @return Paginierte Liste von Ärzten
	 */
	Page<Doctor> findAllDoctors(DoctorSearchCriteria criteria, int page, int size);

	/**
	 * Erstellt einen neuen Arzt im System.
	 *
	 * Entspricht: POST /api/v1/internal/doctor (operationId: registerDoctor)
	 *
	 * @param doctor
	 *            Domain-Modell des zu erstellenden Arztes (ohne ID)
	 * @return Erstellter Arzt mit generierter ID
	 */
	Doctor creatDoctor(Doctor doctor);

	/**
	 * Aktualisiert einen bestehenden Arzt anhand seiner ID.
	 *
	 * Entspricht: PUT /api/v1/internal/doctor/{id} (operationId: modifyDoctor)
	 *
	 * @param doctorId
	 *            UUID des zu aktualisierenden Arztes
	 * @param doctor
	 *            Domain-Modell mit aktualisierten Daten
	 * @return Aktualisierter Arzt
	 */
	Doctor updateDoctor(UUID doctorId, Doctor doctor);

	/**
	 * Löscht einen Arzt aus dem System anhand seiner ID.
	 *
	 * Entspricht: DELETE /api/v1/internal/doctor/{id} (operationId: removeDoctor)
	 *
	 * @param doctorId
	 *            UUID des zu löschenden Arztes
	 */
	void deleteDoctor(UUID doctorId);
}
