package test.doctor_provider.application.port.incoming;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.enums.SlotStatus;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.domain.model.Slot;

/**
 * Inbound Port für Slot-Operationen. Definiert die Business-Use-Cases für
 * Termin-Slots.
 */
public interface SlotIncomingPort {

	/**
	 * Gibt alle Slots zurück, optional gefiltert nach Suchkriterien. Alle
	 * Filter-Parameter sind optional.
	 *
	 * Entspricht: GET /api/v1/slots (operationId: findSlots)
	 *
	 * @param doctorId
	 *            Filter nach Arzt-ID
	 * @param workingHoursId
	 *            Filter nach Working Hours ID
	 * @param dateFrom
	 *            Filter ab Datum (inklusive)
	 * @param dateTo
	 *            Filter bis Datum (inklusive)
	 * @param status
	 *            Filter nach Slot-Status (AVAILABLE, BOOKED, BLOCKED)
	 * @param page
	 *            Seitennummer (0-basiert)
	 * @param size
	 *            Anzahl der Elemente pro Seite
	 * @return Paginierte Liste von Slots
	 */
	Page<Slot> findAllSlots(Optional<UUID> doctorId, Optional<UUID> workingHoursId, Optional<LocalDate> dateFrom,
			Optional<LocalDate> dateTo, Optional<SlotStatus> status, int page, int size);

	/**
	 * Gibt einen spezifischen Slot anhand seiner ID zurück.
	 *
	 * Entspricht: GET /api/v1/slots/{id} (operationId: findSlotById)
	 *
	 * @param id
	 *            UUID des Slots
	 * @return Slot
	 */
	Slot getSlotById(UUID id);

	/**
	 * Blockiert einen Slot (setzt Status auf BLOCKED).
	 *
	 * Entspricht: PUT /api/v1/slots/{id}/block (operationId: blockSlot)
	 *
	 * @param id
	 *            UUID des zu blockierenden Slots
	 * @return Aktualisierter Slot mit Status BLOCKED
	 */
	Slot updateBlockSlotById(UUID id);

	/**
	 * Gibt einen blockierten Slot frei (setzt Status auf AVAILABLE).
	 *
	 * Entspricht: PUT /api/v1/slots/{id}/unblock (operationId: unblockSlot)
	 *
	 * @param id
	 *            UUID des freizugebenden Slots
	 * @return Aktualisierter Slot mit Status AVAILABLE
	 */
	Slot updateUnBlockSlotById(UUID id);

	/**
	 * Bucht einen Slot (setzt Status auf BOOKED).
	 *
	 * Entspricht: PUT /api/v1/slots/{id}/book (operationId: bookSlot)
	 *
	 * @param id
	 *            UUID des zu buchenden Slots
	 * @return Aktualisierter Slot mit Status BOOKED
	 */
	Slot updateBookSlotById(UUID id);
}
