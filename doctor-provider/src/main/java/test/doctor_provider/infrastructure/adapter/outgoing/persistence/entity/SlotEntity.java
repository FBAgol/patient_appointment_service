package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import test.doctor_provider.domain.enums.SlotStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "slot")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlotEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	/**
	 * Start-Zeitpunkt mit Timezone
	 *
	 * WICHTIG: - ZonedDateTime für TIMESTAMP WITH TIME ZONE in PostgreSQL -
	 * Speichert Datum, Zeit UND Timezone - Beispiel:
	 * 2026-02-12T10:00:00+01:00[Europe/Berlin]
	 */
	@Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private ZonedDateTime startTime;

	/**
	 * End-Zeitpunkt mit Timezone
	 *
	 * WICHTIG: - DB hat Check-Constraint: start_time < end_time
	 */
	@Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private ZonedDateTime endTime;

	/**
	 * Slot-Status als PostgreSQL ENUM
	 *
	 * WICHTIG: - PostgreSQL ENUM-Typ: slot_status - Java Enum: SlotStatus - Werte:
	 * available, booked, blocked
	 */
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "status", nullable = false, columnDefinition = "slot_status")
	private SlotStatus status;

	/**
	 * n:1 Beziehung zu WorkingHoursEntity
	 *
	 * WICHTIG: - Slots gehören zu Working Hours (nicht direkt zu Doctor!) - Doctor
	 * wird über workingHours.doctor ermittelt - CASCADE DELETE: Wenn Working Hours
	 * gelöscht wird, werden alle Slots gelöscht
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "working_hours_id", nullable = false)
	private WorkingHoursEntity workingHours;
}
