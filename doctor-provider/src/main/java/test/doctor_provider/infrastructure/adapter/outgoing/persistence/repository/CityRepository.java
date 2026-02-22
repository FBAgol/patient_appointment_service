package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.CityEntity;

import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, UUID> {

	/**
	 * Sucht alle Städte mit optionalen Filtern und Paginierung.
	 *
	 * Beide Filter sind optional (NULL = ignorieren): - name: Teilstring-Suche,
	 * case-insensitive (LIKE '%name%') - postalCode: Teilstring-Suche (LIKE
	 * '%postalCode%')
	 */
	@Query("""
			SELECT c FROM CityEntity c
			WHERE (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))
			  AND (:postalCode IS NULL OR c.postalCode LIKE CONCAT('%', :postalCode, '%'))
			""")
	Page<CityEntity> findAllFiltered(@Param("name") String name, @Param("postalCode") String postalCode,
			Pageable pageable);
}
