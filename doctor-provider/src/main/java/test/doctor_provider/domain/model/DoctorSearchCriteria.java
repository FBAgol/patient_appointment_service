package test.doctor_provider.domain.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

/**
 * Werteobjekt für Arzt-Suchkriterien. Alle Felder sind optional (null = nicht
 * filtern).
 *
 * Wird verwendet von: - DoctorIncomingPort (Application Port) - DoctorService
 * (Application Service) - DoctorOutgoingPort → DoctorPersistenceAdapter
 * (Infrastruktur)
 */
@Getter
@Builder
public class DoctorSearchCriteria {

	/** Filter nach Vorname (Teilstring-Suche, case-insensitive) */
	private final String firstName;

	/** Filter nach Nachname (Teilstring-Suche, case-insensitive) */
	private final String lastName;

	/** Filter nach Praxis-ID */
	private final UUID practiceId;

	/** Filter nach Stadt-ID (benötigt JOIN zu practice) */
	private final UUID cityId;

	/** Filter nach Fachrichtung-ID (benötigt JOIN zu doctor_speciality) */
	private final UUID specialityId;
}

