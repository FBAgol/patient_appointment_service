package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.api.model.CreateDoctorRequest;
import test.doctor_provider.api.model.DoctorDto;
import test.doctor_provider.api.model.UpdateDoctorRequest;
import test.doctor_provider.domain.model.Doctor;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

  // für Get und Post Dto
	DoctorDto toDto(Doctor doctor);

  // für Get alle Ärzte (Liste)
	List<DoctorDto> toDto(List<Doctor> doctors);

  // für Post
	@Mapping(target = "id", ignore = true)
	Doctor toDomain(CreateDoctorRequest request);

    // für Put
	@Mapping(source = "id", target = "id")
	Doctor toDomain(UUID id, UpdateDoctorRequest request);
}
