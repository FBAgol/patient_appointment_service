package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.DoctorEntity;

import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, UUID> {

	@Query("""
			SELECT d FROM DoctorEntity d
			LEFT JOIN d.practice p
			LEFT JOIN d.specialities s
			WHERE (:firstName IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
			  AND (:lastName IS NULL OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
			  AND (:practiceId IS NULL OR d.practice.id = :practiceId)
			  AND (:cityId IS NULL OR p.city.id = :cityId)
			  AND (:specialityId IS NULL OR s.id = :specialityId)
			""")
	Page<DoctorEntity> findAllFiltered(@Param("firstName") String firstName, @Param("lastName") String lastName,
			@Param("practiceId") UUID practiceId, @Param("cityId") UUID cityId,
			@Param("specialityId") UUID specialityId, Pageable pageable);
}
