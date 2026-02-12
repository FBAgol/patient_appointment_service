package test.doctor_provider.domain.enums;

import lombok.Getter;

@Getter
public enum Weekday {
	MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6), SUNDAY(7);

	private final int value;

	Weekday(int value) {
		this.value = value;
	}

	/**
	 * Konvertiert eine Zahl (1-7) zu einem Weekday Enum.
	 *
	 * @param value
	 *            Zahl zwischen 1 und 7
	 * @return Weekday Enum
	 * @throws IllegalArgumentException
	 *             wenn value nicht zwischen 1-7 liegt
	 */
	public static Weekday fromValue(int value) {
		for (Weekday weekday : Weekday.values()) {
			if (weekday.value == value) {
				return weekday;
			}
		}
		throw new IllegalArgumentException("Invalid weekday value: " + value + ". Must be between 1 and 7.");
	}
}
