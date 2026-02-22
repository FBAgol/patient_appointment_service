package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SpecialityEntity;

import java.util.Set;
import java.util.UUID;

@Repository
public interface SpecialityRepository extends JpaRepository<SpecialityEntity, UUID> {

	/**
	 * Prüft, ob ALLE übergebenen IDs in der DB existieren. Zählt die gefundenen IDs
	 * und vergleicht mit der erwarteten Anzahl.
	 *
	 * @param ids
	 *            Set von UUIDs zu prüfen
	 * @param count
	 *            Erwartete Anzahl (ids.size())
	 * @return true wenn alle IDs existieren
	 */
	@Query("SELECT COUNT(s.id) = :count FROM SpecialityEntity s WHERE s.id IN :ids")
	boolean existsAllByIds(@Param("ids") Set<UUID> ids, @Param("count") long count);
}
