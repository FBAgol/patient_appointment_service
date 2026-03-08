package test.doctor_provider.infrastructure.outgoing.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.PracticeEntitiy;

@Mapper(componentModel = "spring")
public interface PracticeEntityMapper {

	@Mapping(source = "phone", target = "phoneNumber")
	@Mapping(source = "cityId", target = "city.id")
	PracticeEntitiy toEntity(Practice practice);

	List<PracticeEntitiy> toEntity(List<Practice> practices);

	@Mapping(source = "phoneNumber", target = "phone")
	@Mapping(source = "city.id", target = "cityId")
	Practice toDomain(PracticeEntitiy practiceEntitiy);

	List<Practice> toDomain(List<PracticeEntitiy> practiceEntitiys);

}
