package test.doctor_provider.application.port.outgoing;

import test.doctor_provider.domain.enums.SlotStatus;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.domain.model.Slot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SlotOutgoingPort {

    /**
     * Sucht alle Slots mit optionalen Filtern und Paginierung.
     *
     * ⚠️ doctorId-Filter benötigt JOIN zu working_hours Tabelle:
     * SELECT s.* FROM slot s
     * JOIN working_hours wh ON s.working_hours_id = wh.id
     * WHERE wh.doctor_id = ?
     *
     * @param doctorId Filter nach Arzt-ID (benötigt JOIN zu working_hours)
     * @param workingHoursId Filter nach Working Hours ID
     * @param dateFrom Filter ab Datum (inklusive)
     * @param dateTo Filter bis Datum (inklusive)
     * @param status Filter nach Slot-Status (AVAILABLE, BOOKED, BLOCKED)
     * @param page Seitennummer (0-basiert)
     * @param size Anzahl der Elemente pro Seite
     * @return Paginierte Liste von Slots
     */
    Page<Slot> findAll(
            Optional<UUID> doctorId,
            Optional<UUID> workingHoursId,
            Optional<LocalDate> dateFrom,
            Optional<LocalDate> dateTo,
            Optional<SlotStatus> status,
            int page,
            int size);

    /**
     * Sucht einen Slot anhand seiner ID.
     *
     * @param id UUID des Slots
     * @return Optional mit Slot, leer wenn nicht gefunden
     */
    Optional<Slot> findById(UUID id);

    /**
     * Speichert einen neuen Slot.
     *
     * Wird normalerweise NICHT direkt verwendet, da Slots automatisch generiert werden.
     * Kann für manuelle Slot-Erstellung nützlich sein.
     *
     * @param slot Zu speichernder Slot (ohne ID)
     * @return Gespeicherter Slot (mit generierter ID)
     */
    Slot save(Slot slot);

    /**
     * Speichert mehrere Slots auf einmal (Bulk-Operation).
     *
     * ⚠️ WICHTIG: Wird bei der automatischen Slot-Generierung verwendet!
     * - Beim Erstellen von Working Hours → Generiere Slots für 4 Wochen
     * - Performance-Optimierung: Batch-Insert statt einzelne INSERTs
     *
     * @param slots Liste von zu speichernden Slots
     * @return Liste von gespeicherten Slots (mit generierten IDs)
     */
    List<Slot> saveAll(List<Slot> slots);

    /**
     * Aktualisiert einen bestehenden Slot.
     *
     * Wird für Status-Änderungen verwendet (AVAILABLE ↔ BLOCKED ↔ BOOKED).
     *
     * @param slot Zu aktualisierender Slot (mit ID)
     * @return Aktualisierter Slot
     */
    Slot modify(Slot slot);

    /**
     * Blockiert einen Slot (setzt Status auf BLOCKED).
     *
     * Convenience-Methode: Lädt Slot, ändert Status, speichert.
     *
     * @param id UUID des zu blockierenden Slots
     * @return Aktualisierter Slot mit Status BLOCKED
     */
    Slot modifyBlockSlot(UUID id);

    /**
     * Gibt einen blockierten Slot frei (setzt Status auf AVAILABLE).
     *
     * Convenience-Methode: Lädt Slot, ändert Status, speichert.
     *
     * @param id UUID des freizugebenden Slots
     * @return Aktualisierter Slot mit Status AVAILABLE
     */
    Slot modifyUnblockSlot(UUID id);

    /**
     * Bucht einen Slot (setzt Status auf BOOKED).
     *
     * Convenience-Methode: Lädt Slot, ändert Status, speichert.
     *
     * @param id UUID des zu buchenden Slots
     * @return Aktualisierter Slot mit Status BOOKED
     */
    Slot modifyBookSlot(UUID id);

    /**
     * Löscht einen Slot anhand seiner ID.
     *
     * Wird normalerweise NICHT direkt verwendet.
     * Slots werden gelöscht, wenn Working Hours gelöscht werden (CASCADE).
     *
     * @param id UUID des zu löschenden Slots
     */
    void deleteById(UUID id);

    /**
     * Löscht alle Slots einer Working Hour.
     *
     * ⚠️ WICHTIG: Wird verwendet bei DELETE /api/v1/working-hours/{id}
     * - Entweder hier implementiert ODER via DB CASCADE DELETE
     *
     * @param workingHoursId UUID der Working Hours
     */
    void deleteAllByWorkingHoursId(UUID workingHoursId);

    /**
     * Prüft, ob ein Slot mit der gegebenen ID existiert.
     *
     * Performance-Optimierung: Schneller als findById(), wenn nur Existenz geprüft werden soll.
     *
     * Verwendung:
     * - Validierung vor Status-Änderungen
     *
     * @param id UUID des Slots
     * @return true wenn Slot existiert, sonst false
     */
    boolean existsById(UUID id);

    /**
     * Prüft, ob ein Slot mit dem gegebenen Status existiert.
     *
     * Verwendung für Validierungen:
     * - Slot blockieren: Ist Slot AVAILABLE? (sonst 409 Conflict)
     * - Slot buchen: Ist Slot AVAILABLE? (sonst 409 Conflict)
     * - Slot freigeben: Ist Slot BLOCKED? (sonst 409 Conflict)
     *
     * @param id UUID des Slots
     * @param status Zu prüfender Status
     * @return true wenn Slot mit diesem Status existiert
     */
    boolean existsByIdAndStatus(UUID id, SlotStatus status);
}
