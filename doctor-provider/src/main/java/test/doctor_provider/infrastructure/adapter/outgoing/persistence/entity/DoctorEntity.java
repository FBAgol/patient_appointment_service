package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "practice_id")
	private PracticeEntitiy practice;

	/**
	 * n:m Beziehung zu SpecialityEntity (über Join-Tabelle doctor_speciality)
	 *
	 * WICHTIG: - @ManyToMany: Viele Ärzte ↔ Viele Fachrichtungen - @JoinTable:
	 * Join-Tabelle "doctor_speciality" - joinColumns: FK zu doctor (diese Seite) -
	 * inverseJoinColumns: FK zu speciality (andere Seite) - fetch = LAZY:
	 * Performance! - Set statt List: Keine Duplikate
	 *
	 * ⚠️ Im Domain-Modell: Set<UUID> specialityIds
	 */
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "doctor_speciality", joinColumns = @JoinColumn(name = "doctor_id"), inverseJoinColumns = @JoinColumn(name = "speciality_id"))
	@Builder.Default
	private Set<SpecialityEntity> specialities = new HashSet<>();
}
