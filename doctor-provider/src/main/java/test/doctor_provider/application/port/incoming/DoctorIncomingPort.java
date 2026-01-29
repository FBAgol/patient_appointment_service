package test.doctor_provider.application.port.incoming;

import test.doctor_provider.domain.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.annotation.Nullable;

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
     * Alle Filter-Parameter sind optional (können null sein).
     *
     * @param firstName Filter nach Vorname (Teilstring-Suche, case-insensitive), kann null sein
     * @param lastName Filter nach Nachname (Teilstring-Suche, case-insensitive), kann null sein
     * @param practiceId Filter nach Praxis-ID, kann null sein
     * @param specialityId Filter nach Fachrichtung-ID, kann null sein
     * @param pageable Pagination und Sortierung (required)
     * @return Paginierte Liste von Ärzten
     */
    Page<Doctor> getAllDoctors(
            @Nullable String firstName,
            @Nullable String lastName,
            @Nullable UUID practiceId,
            @Nullable UUID specialityId,
            Pageable pageable);

    /**
     * Gibt einen spezifischen Arzt anhand seiner ID zurück.
     *
     * @param doctorId UUID des Arztes
     * @return Optional mit Doctor, leer wenn nicht gefunden
     */
    Optional<Doctor> getDoctorById(UUID doctorId);

    /**
     * Erstellt einen neuen Arzt im System.
     *
     * @param doctor Domain-Modell des zu erstellenden Arztes (ohne ID)
     * @return Erstellter Arzt mit generierter ID
     */
    Doctor createDoctor(Doctor doctor);

    /**
     * Aktualisiert einen bestehenden Arzt.
     *
     * @param doctorId UUID des zu aktualisierenden Arztes
     * @param doctor Domain-Modell mit aktualisierten Daten
     * @return Aktualisierter Arzt
     */
    Doctor updateDoctor(UUID doctorId, Doctor doctor);

    /**
     * Löscht einen Arzt aus dem System.
     *
     * @param doctorId UUID des zu löschenden Arztes
     */
    void deleteDoctor(UUID doctorId);
}
