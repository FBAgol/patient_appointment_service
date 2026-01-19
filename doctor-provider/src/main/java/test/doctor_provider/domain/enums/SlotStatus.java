package test.doctor_provider.domain.enums;

/**
 * Status eines Termin-Slots
 */
public enum SlotStatus {
    AVAILABLE,  // Verfügbar für Buchung
    BOOKED,     // Bereits gebucht
    BLOCKED     // Blockiert (z.B. Pause, Urlaub)
}
