package test.doctor_provider.domain.enums;

/**
 * Status eines Termin-Slots
 */
public enum SlotStatus {
    AVAILABLE("available"),  // Verfügbar für Buchung
    BOOKED("booked"),     // Bereits gebucht
    BLOCKED("blocked");
    // Blockiert (z.B. Pause, Urlaub)

    private final String value;

    SlotStatus(String value) {
        this.value = value;
    }

    public static SlotStatus fromValue(String value) {
        for (SlotStatus status : SlotStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException(
                "Invalid SlotStatus value: " + value
        );
    }

}
