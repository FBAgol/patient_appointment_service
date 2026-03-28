package test.doctor_provider.infrastructure.outgoing.persistence.outgoingAdapter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import test.doctor_provider.application.port.outgoing.SpecialityOutgoingPort;
import test.doctor_provider.domain.model.Speciality;
import test.doctor_provider.infrastructure.outgoing.persistence.mapper.SpecialityEntityMapper;
import test.doctor_provider.infrastructure.outgoing.persistence.repository.SpecialityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpecialityPersistenceAdapter implements SpecialityOutgoingPort {

	private final SpecialityRepository specialityRepository;
	private final SpecialityEntityMapper specialityEntityMapper;

	@Override
	public List<Speciality> findAll() {
		return specialityEntityMapper.toDomain(specialityRepository.findAll());
	}

	@Override
	public Optional<Speciality> findById(UUID id) {
		return specialityRepository.findById(id).map(specialityEntityMapper::toDomain);
	}

	@Override
	public List<Speciality> findAllByIds(Set<UUID> ids) {
		return specialityEntityMapper.toDomain(specialityRepository.findAllById(ids));
	}

	@Override
	public boolean existsById(UUID id) {
		return specialityRepository.existsById(id);
	}

	@Override
	public boolean existsAllByIds(Set<UUID> ids) {
		return specialityRepository.existsAllByIds(ids, ids.size());
	}
}

