package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;

import test.doctor_provider.domain.model.Speciality;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SpecialityEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecialityEntityMapper {

  SpecialityEntity toEntity(Speciality speciality);

  List<SpecialityEntity> toEntity(List<Speciality> specialities);

  Speciality toDomain(SpecialityEntity specialityEntity);

  List<Speciality> toDomain(List<SpecialityEntity> specialityEntities);
}
