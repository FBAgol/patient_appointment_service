package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.domain.enums.Weekday;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.WorkingHoursEntity;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHoursEntity, UUID> {

	List<WorkingHoursEntity> findAllByDoctorId(@Param("doctorId") UUID doctorId);

	/**
	 * Prüft ob es eine Zeitüberlappung gibt für einen Arzt an einem bestimmten
	 * Wochentag.
	 *
	 * Überlappungs-Logik: Zwei Zeiträume überlappen sich, wenn: bestehend.startTime
	 * < neues.endTime UND bestehend.endTime > neues.startTime
	 *
	 * excludeId: Bei UPDATE die eigene ID ausschließen (sonst findet man sich
	 * selbst). NULL = nicht ausschließen (bei CREATE).
	 */
	@Query("""
			SELECT COUNT(wh) > 0 FROM WorkingHoursEntity wh
			WHERE wh.doctor.id = :doctorId
			  AND wh.weekday = :weekday
			  AND wh.startTime < :endTime
			  AND wh.endTime > :startTime
			  AND (:excludeId IS NULL OR wh.id != :excludeId)
			""")
	boolean existsOverlapping(@Param("doctorId") UUID doctorId, @Param("weekday") Weekday weekday,
			@Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
			@Param("excludeId") UUID excludeId);
}
