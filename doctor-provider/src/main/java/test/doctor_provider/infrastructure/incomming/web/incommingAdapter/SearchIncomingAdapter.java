package test.doctor_provider.infrastructure.incomming.web.incommingAdapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import test.doctor_provider.api.ExternalSearchApi;
import test.doctor_provider.api.model.FindAllCitiesExternal200Response;
import test.doctor_provider.api.model.FindAvailableSlotsExternal200Response;
import test.doctor_provider.api.model.SearchDoctorsExternal200Response;
import test.doctor_provider.api.model.SpecialityDto;
import test.doctor_provider.application.port.incoming.CityIncomingPort;
import test.doctor_provider.application.port.incoming.DoctorIncomingPort;
import test.doctor_provider.application.port.incoming.SpecialityIncomingPort;
import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.DoctorSearchCriteria;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.incomming.web.mapper.CityWebMapper;

import lombok.RequiredArgsConstructor;

import test.doctor_provider.infrastructure.incomming.web.mapper.DoctorWebMapper;
import test.doctor_provider.infrastructure.incomming.web.mapper.SpecialityWebMapper;

@RestController
@RequiredArgsConstructor
public class SearchIncomingAdapter implements ExternalSearchApi {

	private final CityIncomingPort cityIncomingPort;
  private final SpecialityIncomingPort specialityIncomingPort;
  private final DoctorIncomingPort doctorIncomingPort;
  private final SpecialityWebMapper specialityWebMapper;
	private final CityWebMapper cityWebMapper;
  private final DoctorWebMapper doctorWebMapper;

	@Override
	public ResponseEntity<List<SpecialityDto>> findAllSpecialitiesExternal() {
    List<SpecialityDto> response = specialityWebMapper.toDto(specialityIncomingPort.getAllSpecialities());
		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<FindAllCitiesExternal200Response> findAllCitiesExternal(String name, String postalCode,
			Integer page, Integer size) {

		Page<City> result = cityIncomingPort.getAllCities(Optional.ofNullable(name), Optional.ofNullable(postalCode),
				page != null ? page : 0, size != null ? size : 10);

		FindAllCitiesExternal200Response response = new FindAllCitiesExternal200Response();
		response.setItems(cityWebMapper.toDto(result.getItems()));
		response.setPage(result.getPage());
		response.setSize(result.getSize());
		response.setTotalElements((int) result.getTotalElements());
		response.setTotalPages(result.getTotalPages());

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<SearchDoctorsExternal200Response> searchDoctorsExternal(UUID specialityId, UUID cityId,
			Integer page, Integer size) {

		DoctorSearchCriteria criteria = DoctorSearchCriteria.builder()
				.specialityId(specialityId)
				.cityId(cityId)
				.build();

		Page<Doctor> result = doctorIncomingPort.findAllDoctors(
				criteria,
				page != null ? page : 0,
				size != null ? size : 10
		);

    SearchDoctorsExternal200Response response = new SearchDoctorsExternal200Response();
    response.setItems(doctorWebMapper.toDto(result.getItems()));
    response.setPage(result.getPage());
    response.setSize(result.getSize());
    response.setTotalElements((int) result.getTotalElements());
    response.setTotalPages(result.getTotalPages());

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<FindAvailableSlotsExternal200Response> findAvailableSlotsExternal(UUID doctorId,
			LocalDate date, LocalDate dateFrom, LocalDate dateTo, Integer page, Integer size) {
		// TODO: implementieren
		return null;
	}
}
