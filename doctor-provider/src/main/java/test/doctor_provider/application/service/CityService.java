package test.doctor_provider.application.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import test.doctor_provider.application.port.incoming.CityIncomingPort;
import test.doctor_provider.application.port.outgoing.CityOutgoingPort;
import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CityService implements CityIncomingPort {

	private final CityOutgoingPort cityOutgoingPort;

	@Override
	public Page<City> getAllCities(Optional<String> name, Optional<String> postalCode, int page, int size) {
		// Delegiert die Anfrage an die Outgoing Port
		return cityOutgoingPort.findAll(name, postalCode, page, size);
	}

}
