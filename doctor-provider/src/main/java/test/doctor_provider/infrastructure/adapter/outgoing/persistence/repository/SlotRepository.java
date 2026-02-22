package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.domain.enums.SlotStatus;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SlotEntity;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<SlotEntity, UUID> {

	/**
	 * ⚠️ SlotEntity hat KEIN "date"-Feld! Es hat startTime (ZonedDateTime).
	 * Deshalb: CAST(s.startTime AS LocalDate) um nur das Datum zu vergleichen.
	 */
	@Query("""
			SELECT s FROM SlotEntity s
			JOIN s.workingHours wh
			WHERE (:doctorId IS NULL OR wh.doctor.id = :doctorId)
			  AND (:workingHoursId IS NULL OR s.workingHours.id = :workingHoursId)
			  AND (:dateFrom IS NULL OR CAST(s.startTime AS LocalDate) >= :dateFrom)
			  AND (:dateTo IS NULL OR CAST(s.startTime AS LocalDate) <= :dateTo)
			  AND (:status IS NULL OR s.status = :status)
			""")
	Page<SlotEntity> findAllFiltered(@Param("doctorId") UUID doctorId,
			@Param("workingHoursId") UUID workingHoursId, @Param("dateFrom") LocalDate dateFrom,
			@Param("dateTo") LocalDate dateTo, @Param("status") SlotStatus status, Pageable pageable);

	void deleteAllByWorkingHoursId(UUID workingHoursId);

	boolean existsByIdAndStatus(UUID id, SlotStatus status);
}
