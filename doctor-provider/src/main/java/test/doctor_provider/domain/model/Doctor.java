package test.doctor_provider.domain.model;

import java.util.Set;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain-Modell für einen Arzt.
 *
 * practiceId kann null sein (Arzt ohne Praxis). specialityIds kann null oder
 * leer sein (Arzt ohne Fachrichtungen).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
	private UUID id;
	private String firstName;
	private String lastName;
	private UUID practiceId; // FK zu Practice (n:1) - kann null sein
	private Set<UUID> specialityIds; // FKs zu Specialities (n:m) - kann null/leer sein
}
