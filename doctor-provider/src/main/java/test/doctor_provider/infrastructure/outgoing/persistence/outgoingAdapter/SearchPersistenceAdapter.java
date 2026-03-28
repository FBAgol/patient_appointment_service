package test.doctor_provider.infrastructure.outgoing.persistence.outgoingAdapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import test.doctor_provider.application.port.outgoing.CityOutgoingPort;
import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.outgoing.persistence.mapper.CityEntityMapper;
import test.doctor_provider.infrastructure.outgoing.persistence.repository.CityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchPersistenceAdapter implements CityOutgoingPort {

	private final CityRepository cityRepository;
	private final CityEntityMapper cityEntityMapper;

	@Override
	public Page<City> findAll(Optional<String> name, Optional<String> postalCode, int page, int size) {

		var entityPage = cityRepository.findAllFiltered(name.orElse(null), postalCode.orElse(null),
				PageRequest.of(page, size));

		Page<City> result = new Page<>();
		result.setItems(entityPage.getContent().stream().map(cityEntityMapper::toDomain).toList());
		result.setPage(entityPage.getNumber());
		result.setSize(entityPage.getSize());
		result.setTotalElements(entityPage.getTotalElements());
		result.setTotalPages(entityPage.getTotalPages());

		return result;
	}

	@Override
	public Optional<City> findById(UUID id) {
		return cityRepository.findById(id).map(cityEntityMapper::toDomain);
	}

	@Override
	public boolean existsById(UUID id) {
		return cityRepository.existsById(id);
	}
}
