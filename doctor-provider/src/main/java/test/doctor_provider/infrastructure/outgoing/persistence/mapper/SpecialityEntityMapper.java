package test.doctor_provider.infrastructure.outgoing.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import test.doctor_provider.domain.model.Speciality;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.SpecialityEntity;

@Mapper(componentModel = "spring")
public interface SpecialityEntityMapper {

	SpecialityEntity toEntity(Speciality speciality);

	List<SpecialityEntity> toEntity(List<Speciality> specialities);

	Speciality toDomain(SpecialityEntity specialityEntity);

	List<Speciality> toDomain(List<SpecialityEntity> specialityEntities);
}
