package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;

import org.mapstruct.Named;

import test.doctor_provider.domain.model.Slot;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SlotEntity;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.WorkingHoursEntity;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface SlotsEntityMapper {

  @Mapping(source = "workingHoursId", target = "workingHours", qualifiedByName = "workingHoursIdToEntity")
  SlotEntity toEntity(Slot slot);

  List<SlotEntity> toEntity(List<Slot> slots);

  @Mapping(source = "workingHours.id", target = "workingHoursId")
  Slot toDomain(SlotEntity slotEntity);


  @Named("workingHoursIdToEntity")
  default WorkingHoursEntity workingHoursIdToEntity(UUID workingHoursId) {
    if (workingHoursId == null) {
      return null;
    }

    WorkingHoursEntity workingHoursEntity = new WorkingHoursEntity();
    workingHoursEntity.setId(workingHoursId);
    return workingHoursEntity;
  }
}
