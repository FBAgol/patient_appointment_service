package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.api.model.CreateWorkingHoursRequest;
import test.doctor_provider.api.model.UpdateWorkingHoursRequest;
import test.doctor_provider.api.model.WorkingHoursDto;
import test.doctor_provider.domain.model.DoctorWorkingHours;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WorkingHoursWebMapper {

  // für Get und Post Dto
	WorkingHoursDto toDto(DoctorWorkingHours doctorWorkingHour);
 // für Get alle Arbeitszeiten (Liste)
	List<WorkingHoursDto> toDto(List<DoctorWorkingHours> doctorWorkingHours);
 // für Post
	@Mapping(target = "id", ignore = true)
	DoctorWorkingHours toDomain(CreateWorkingHoursRequest request);
 // für Put
	@Mapping(source = "id", target = "id")
	DoctorWorkingHours toDomain(UUID id, UpdateWorkingHoursRequest request);
}
