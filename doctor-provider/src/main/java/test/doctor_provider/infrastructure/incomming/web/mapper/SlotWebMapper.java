package test.doctor_provider.infrastructure.incomming.web.mapper;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.mapstruct.Mapper;

import test.doctor_provider.api.model.SlotDto;
import test.doctor_provider.domain.model.Slot;

@Mapper(componentModel = "spring")
public interface SlotWebMapper {

	SlotDto toDto(Slot slot);

	List<SlotDto> toDto(List<Slot> slots);

	// SlotDto hat OffsetDateTime (generiert aus OpenAPI)
	// Slot (Domain) hat ZonedDateTime
	default OffsetDateTime map(ZonedDateTime value) {
		if (value == null) {
			return null;
		}
		return value.toOffsetDateTime();
	}

	default ZonedDateTime map(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.toZonedDateTime();
	}
}
