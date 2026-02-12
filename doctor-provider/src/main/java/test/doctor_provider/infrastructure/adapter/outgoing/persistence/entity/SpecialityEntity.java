package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import test.doctor_provider.domain.enums.SpecialityTyp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "speciality")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class SpecialityEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Enumerated(EnumType.STRING) // Speichert den Enum-Wert als String in der DB
	@JdbcTypeCode(SqlTypes.NAMED_ENUM) // Spezifiziert, dass es sich um einen benannten Enum handelt
	@Column(name = "name", nullable = false, columnDefinition = "speciality_type")
	private SpecialityTyp name;
}
