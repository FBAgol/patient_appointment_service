package test.doctor_provider.infrastructure.incomming.web.incommingAdapter;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import test.doctor_provider.api.InternalCitiesApi;
import test.doctor_provider.api.model.FindAllCitiesExternal200Response;
import test.doctor_provider.application.port.incoming.CityIncomingPort;
import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.incomming.web.mapper.CityWebMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CityIncomingAdapter implements InternalCitiesApi {

	private final CityIncomingPort cityIncomingPort;
	private final CityWebMapper cityWebMapper;

	@Override
	public ResponseEntity<FindAllCitiesExternal200Response> findAllCities(String name, String postalCode, Integer page,
			Integer size) {

		// the Datas requested from the application layer
		Page<City> result = cityIncomingPort.getAllCities(Optional.ofNullable(name), Optional.ofNullable(postalCode),
				page != null ? page : 0, size != null ? size : 10);

		// ToDto
		FindAllCitiesExternal200Response response = new FindAllCitiesExternal200Response();
		response.setItems(cityWebMapper.toDto(result.getItems()));
		response.setPage(result.getPage());
		response.setSize(result.getSize());
		response.setTotalElements((int) result.getTotalElements());
		response.setTotalPages(result.getTotalPages());

		return ResponseEntity.ok(response);
	}
}
