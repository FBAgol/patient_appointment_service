package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.api.model.SpecialityDto;
import test.doctor_provider.domain.model.Speciality;

import java.util.List;


@Mapper(componentModel = "spring")
public interface SpecialityWebMapper {

	/**
	 * Konvertiert Speciality Domain zu SpecialityDto (Einzelnes Objekt)
	 *
	 * Datenfluss: Server → Client (Response)

	 * WICHTIG:
	 * - MapStruct konvertiert automatisch zwischen Enum-Typen mit gleichen Werten
	 * - Wird auch intern von toDto(List<Speciality>) aufgerufen
	**/
	SpecialityDto toDto(Speciality speciality);

	/**
	 * Konvertiert eine Liste von Speciality Domain zu List<SpecialityDto>
	 *
	 * Datenfluss: Server → Client (Response für GET /api/v1/specialities)
	 *
	 * WICHTIG:
	 * - MapStruct ruft automatisch toDto(Speciality) für jedes Element auf
	 * - Laut API: Response ist ein Array (keine Paginierung!)
	 */
	List<SpecialityDto> toDto(List<Speciality> specialities);
}
