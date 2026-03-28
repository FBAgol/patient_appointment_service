package test.doctor_provider.infrastructure.outgoing.persistence.outgoingAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import test.doctor_provider.application.port.outgoing.DoctorOutgoingPort;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.DoctorSearchCriteria;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.outgoing.persistence.mapper.DoctorEntityMapper;
import test.doctor_provider.infrastructure.outgoing.persistence.repository.DoctorRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorPersistenceAdapter implements DoctorOutgoingPort {

	private final DoctorRepository doctorRepository;
	private final DoctorEntityMapper doctorEntityMapper;

	@Override
	public Page<Doctor> findAll(DoctorSearchCriteria criteria, int page, int size) {
		var entityPage = doctorRepository.findAllFiltered(
				criteria.getFirstName(),
				criteria.getLastName(),
				criteria.getPracticeId(),
				criteria.getCityId(),
				criteria.getSpecialityId(),
				PageRequest.of(page, size));

		Page<Doctor> result = new Page<>();
		result.setItems(entityPage.getContent().stream().map(doctorEntityMapper::toDomain).toList());
		result.setPage(entityPage.getNumber());
		result.setSize(entityPage.getSize());
		result.setTotalElements(entityPage.getTotalElements());
		result.setTotalPages(entityPage.getTotalPages());
		return result;
	}

	@Override
	public Optional<Doctor> findById(UUID id) {
		return doctorRepository.findById(id).map(doctorEntityMapper::toDomain);
	}

	@Override
	public Doctor save(Doctor doctor) {
		var entity = doctorEntityMapper.toEntity(doctor);
		var saved = doctorRepository.save(entity);
		return doctorEntityMapper.toDomain(saved);
	}

	@Override
	public Doctor modify(Doctor doctor) {
		var entity = doctorEntityMapper.toEntity(doctor);
		var saved = doctorRepository.save(entity);
		return doctorEntityMapper.toDomain(saved);
	}

	@Override
	public void deleteById(UUID id) {
		doctorRepository.deleteById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		return doctorRepository.existsById(id);
	}
}
