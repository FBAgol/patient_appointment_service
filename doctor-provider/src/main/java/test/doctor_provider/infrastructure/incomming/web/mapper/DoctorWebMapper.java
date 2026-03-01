package test.doctor_provider.infrastructure.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;
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

	// DoctorDto hat JsonNullable<UUID> practiceId (optional in API)
	// Doctor (Domain) hat UUID practiceId
	// MapStruct braucht diese Methoden um zwischen den Typen zu konvertieren
	default JsonNullable<UUID> map(UUID value) {
		return JsonNullable.of(value);
	}

	default UUID map(JsonNullable<UUID> value) {
		if (value == null || !value.isPresent()) {
			return null;
		}
		return value.get();
	}
}

