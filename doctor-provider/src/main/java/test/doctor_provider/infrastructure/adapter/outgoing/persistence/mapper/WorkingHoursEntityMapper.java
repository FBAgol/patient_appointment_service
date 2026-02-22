package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;

import org.mapstruct.Named;

import test.doctor_provider.domain.model.DoctorWorkingHours;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.DoctorEntity;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.WorkingHoursEntity;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface WorkingHoursEntityMapper {

  @Mapping(source = "doctorId", target = "doctor", qualifiedByName = "doctorIdToEntity")
  WorkingHoursEntity toEntity(DoctorWorkingHours doctorWorkingHours);

  List<WorkingHoursEntity> toEntity(List<DoctorWorkingHours> doctorWorkingHours);

  @Mapping(source = "doctor.id", target = "doctorId")
  DoctorWorkingHours toDomain(WorkingHoursEntity workingHoursEntity);

  @Named("doctorIdToEntity")
  default DoctorEntity doctorIdToEntity(UUID doctorId) {
    if (doctorId == null) {
      return null;
    }

    DoctorEntity doctorEntity = new DoctorEntity();
    doctorEntity.setId(doctorId);
    return doctorEntity; // Hier kannst du die Logik anpassen, falls nötig (z.B. Umwandlung oder Validierung)
  }

}



