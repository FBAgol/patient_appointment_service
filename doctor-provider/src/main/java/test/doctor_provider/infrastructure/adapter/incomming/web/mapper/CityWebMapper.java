package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.api.model.CityDto;
import test.doctor_provider.domain.model.City;

import java.util.List;

/**
 * WebMapper für City - Konvertiert zwischen Client und Server
 *
 * Mapping-Richtungen:
 * - City → CityDto (toDto): Server → Client
 * - List<City> → List<CityDto>: Für paginierte API-Responses
 *
 * WICHTIG:
 * - id wird NICHT ignoriert, weil der Client die ID sehen muss!
 */
@Mapper(componentModel = "spring")
public interface CityWebMapper {

	CityDto toDto(City city);

	/**
	 * Konvertiert eine Liste von City Domain zu List<CityDto>
	 *
	 * Datenfluss: Server → Client (Response für GET /api/v1/cities)
	 *
	 * WICHTIG:
	 * - Diese Methode ist für die paginierte Liste in der API-Response
	 * - MapStruct ruft automatisch toDto(City) für jedes Element auf
	 *
   * */
	List<CityDto> toDto(List<City> cities);
}
