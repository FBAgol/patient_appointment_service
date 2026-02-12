package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import test.doctor_provider.domain.enums.Weekday;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_working_hours")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkingHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Startzeit (z.B. 08:00)
     *
     * WICHTIG:
     * - LocalTime für TIME in PostgreSQL
     * - Nur Uhrzeit, kein Datum!
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Endzeit (z.B. 16:00)
     *
     * WICHTIG:
     * - LocalTime für TIME in PostgreSQL
     * - DB hat Check-Constraint: start_time < end_time
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Wochentag als PostgreSQL ENUM
     *
     * WICHTIG:
     * - PostgreSQL ENUM-Typ: weekday_enum
     * - Java Enum: Weekday
     * - Werte: MONDAY, TUESDAY, WEDNESDAY, etc.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "weekday", nullable = false, columnDefinition = "weekday_enum")
    private Weekday weekday;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;
}
