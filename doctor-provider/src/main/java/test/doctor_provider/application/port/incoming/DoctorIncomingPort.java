package test.doctor_provider.application.port.incoming;

import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.Page;

import java.util.Optional;
import java.util.UUID;

/**
 * Inbound Port für Doctor-Operationen.
 * Definiert die Business-Use-Cases für Ärzte.
 *
 * Hinweis: Methodennamen sind identisch mit den API-Operationen (operationId in OpenAPI),
 * um die Zuordnung zwischen HTTP-Adapter (DoctorController) und Use-Case klar zu machen.
 */
public interface DoctorIncomingPort {

    /**
     * Gibt alle Ärzte zurück, optional gefiltert nach Suchkriterien.
     * Alle Filter-Parameter sind optional.
     *
     * Entspricht: GET /api/v1/doctors (operationId: findAllDoctors)
     *
     * @param firstName Filter nach Vorname (Teilstring-Suche, case-insensitive)
     * @param lastName Filter nach Nachname (Teilstring-Suche, case-insensitive)
     * @param practiceId Filter nach Praxis-ID
     * @param cityId Filter nach Stadt-ID
     * @param specialityId Filter nach Fachrichtung-ID
     * @param page Seitennummer (0-basiert)
     * @param size Anzahl der Elemente pro Seite
     * @return Paginierte Liste von Ärzten
     */
    Page<Doctor> findAllDoctors(
            Optional<String> firstName,
            Optional<String> lastName,
            Optional<UUID> practiceId,
            Optional<UUID> cityId,
            Optional<UUID> specialityId,
            int page,
            int size);

    /**
     * Erstellt einen neuen Arzt im System.
     *
     * Entspricht: POST /api/v1/doctor (operationId: registerDoctor)
     *
     * @param doctor Domain-Modell des zu erstellenden Arztes (ohne ID)
     * @return Erstellter Arzt mit generierter ID
     */
    Doctor creatDoctor(Doctor doctor);

    /**
     * Aktualisiert einen bestehenden Arzt anhand seiner ID.
     *
     * Entspricht: PUT /api/v1/doctor/{id} (operationId: modifyDoctor)
     *
     * @param doctorId UUID des zu aktualisierenden Arztes
     * @param doctor Domain-Modell mit aktualisierten Daten
     * @return Aktualisierter Arzt
     */
    Doctor updateDoctor(UUID doctorId, Doctor doctor);

    /**
     * Löscht einen Arzt aus dem System anhand seiner ID.
     *
     * Entspricht: DELETE /api/v1/doctor/{id} (operationId: removeDoctor)
     *
     * @param doctorId UUID des zu löschenden Arztes
     */
    void deleteDoctor(UUID doctorId);
}
