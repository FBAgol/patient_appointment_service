package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.api.model.CreatePracticeRequest;
import test.doctor_provider.api.model.PracticeDto;
import test.doctor_provider.api.model.UpdatePracticeRequest;
import test.doctor_provider.domain.model.Practice;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PracticeWebMapper {


	PracticeDto toDto(Practice practice);

	List<PracticeDto> toDto(List<Practice> practices);


	@Mapping(target = "id", ignore = true)
	Practice toDomain(CreatePracticeRequest request);


	@Mapping(source = "id", target = "id")
	Practice toDomain(UUID id, UpdatePracticeRequest request);
}
