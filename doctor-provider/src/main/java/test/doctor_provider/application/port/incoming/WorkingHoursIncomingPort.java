package test.doctor_provider.application.port.incoming;

import test.doctor_provider.domain.model.DoctorWorkingHours;

import java.util.List;
import java.util.UUID;

/**
 * Inbound Port für Working Hours Operationen.
 * Definiert die Business-Use-Cases für Arbeitszeiten von Ärzten.
 */
public interface WorkingHoursIncomingPort {

    /**
     * Erstellt neue Working Hours für einen Arzt.
     *
     * Entspricht: POST /api/v1/doctors/{doctorId}/working-hours (operationId: registerWorkingHours)
     *
     * ⚠️ Slots werden automatisch generiert (nächsten 4 Wochen, alle 30 Minuten)
     *
     * @param doctorId UUID des Arztes
     * @param workingHours Domain-Modell der zu erstellenden Working Hours (ohne ID)
     * @return Erstellte Working Hours mit generierter ID
     */
    DoctorWorkingHours createWorkingHours(UUID doctorId, DoctorWorkingHours workingHours);

    /**
     * Gibt alle Working Hours eines Arztes zurück.
     *
     * Entspricht: GET /api/v1/doctors/{doctorId}/working-hours (operationId: findWorkingHoursForDoctor)
     *
     * @param doctorId UUID des Arztes
     * @return Liste aller Working Hours des Arztes
     */
    List<DoctorWorkingHours> getWorkingHours(UUID doctorId);

    /**
     * Aktualisiert eine bestehende Working Hour.
     *
     * Entspricht: PUT /api/v1/working-hours/{id} (operationId: modifyWorkingHours)
     *
     * @param id UUID der zu aktualisierenden Working Hours
     * @param workingHours Domain-Modell mit aktualisierten Daten
     * @return Aktualisierte Working Hours
     */
    DoctorWorkingHours updateWorkingHours(UUID id, DoctorWorkingHours workingHours);

    /**
     * Löscht eine Working Hour aus dem System.
     *
     * Entspricht: DELETE /api/v1/working-hours/{id} (operationId: removeWorkingHours)
     *
     * @param id UUID der zu löschenden Working Hours
     */
    void deleteWorkingHours(UUID id);
}
