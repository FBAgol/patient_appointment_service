package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.DoctorEntity;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntitiy;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SpecialityEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DoctorEntityMapper {

  // Domain → Entity (Server → Datenbank)
  @Mapping(source = "practiceId", target = "practice", qualifiedByName = "practiceIdToEntity")
  @Mapping(source = "specialityIds", target = "specialities", qualifiedByName = "specialityIdsToEntities")
  DoctorEntity toEntity(Doctor doctor);

  /*die Oberen Annotation brauchen wir hierh nicht , weil:
  * MapStruct denkt sich: "Aha, ich soll eine Liste von DoctorEntity in eine Liste von Doctor umwandeln.
  * Ich habe schon eine Methode, die ein einzelnes DoctorEntity → Doctor konvertiert.
  * Ich rufe einfach diese Methode für jedes Element in der Liste auf!"*/
  List<DoctorEntity> toEntity(List<Doctor> doctors);

  // Entity → Domain (Datenbank → Server)
  @Mapping(source = "practice.id", target = "practiceId")
  @Mapping(source = "specialities", target = "specialityIds", qualifiedByName = "specialityEntitiesToIds")
  Doctor toDomain(DoctorEntity doctorEntity);

  List<Doctor> toDomain(List<DoctorEntity> doctorEntities);

  // Custom Mapping: UUID → PracticeEntity
  @Named("practiceIdToEntity")
  default PracticeEntitiy practiceIdToEntity(UUID practiceId) {
    if (practiceId == null) {
      return null;
    }
    PracticeEntitiy practice = new PracticeEntitiy();
    practice.setId(practiceId);
    return practice;
  }

  // Custom Mapping: Set<UUID> → Set<SpecialityEntity>
  @Named("specialityIdsToEntities")
  default Set<SpecialityEntity> specialityIdsToEntities(Set<UUID> specialityIds) {
    if (specialityIds == null || specialityIds.isEmpty()) {
      return Collections.emptySet();
    }
    return specialityIds.stream()
        .map(id -> {
          SpecialityEntity entity = new SpecialityEntity();
          entity.setId(id);
          return entity;
        })
        .collect(Collectors.toSet());
  }

  // Custom Mapping: Set<SpecialityEntity> → Set<UUID>
  @Named("specialityEntitiesToIds")
  default Set<UUID> specialityEntitiesToIds(Set<SpecialityEntity> specialities) {
    if (specialities == null || specialities.isEmpty()) {
      return Collections.emptySet();
    }
    return specialities.stream()
        .map(SpecialityEntity::getId)
        .collect(Collectors.toSet());
  }
}
