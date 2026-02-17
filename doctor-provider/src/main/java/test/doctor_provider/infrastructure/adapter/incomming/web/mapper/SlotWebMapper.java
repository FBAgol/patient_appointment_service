package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.api.model.SlotDto;
import test.doctor_provider.domain.model.Slot;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SlotWebMapper {

	SlotDto toDto(Slot slot);

	List<SlotDto> toDto(List<Slot> slots);
}
