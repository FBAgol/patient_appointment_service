package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntitiy;

import java.util.UUID;

@Repository
public interface PracticeRepository extends JpaRepository<PracticeEntitiy, UUID> {

	@Query("""
			SELECT p FROM PracticeEntitiy p
			WHERE (:cityId IS NULL OR p.city.id = :cityId)
			  AND (:practiceName IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :practiceName, '%')))
			""")
	Page<PracticeEntitiy> findAllFiltered(@Param("cityId") UUID cityId,
			@Param("practiceName") String practiceName, Pageable pageable);

	@Query("""
			SELECT COUNT(p.id) > 0 FROM PracticeEntitiy p WHERE LOWER(p.name) = LOWER(:name)
			""")
	boolean existsByName(@Param("name") String name);

	@Query("""
			SELECT COUNT(p.id) > 0 FROM PracticeEntitiy p WHERE LOWER(p.name) = LOWER(:name) AND p.id <> :excludeId
			""")
	boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);
}
