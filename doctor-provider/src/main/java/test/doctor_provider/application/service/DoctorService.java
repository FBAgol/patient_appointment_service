package test.doctor_provider.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import test.doctor_provider.application.port.incoming.DoctorIncomingPort;
import test.doctor_provider.application.port.outgoing.DoctorOutgoingPort;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.domain.model.DoctorSearchCriteria;
import test.doctor_provider.domain.model.Page;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService implements DoctorIncomingPort {

	private final DoctorOutgoingPort doctorOutgoingPort;

	@Override
	public Page<Doctor> findAllDoctors(DoctorSearchCriteria criteria, int page, int size) {
		return doctorOutgoingPort.findAll(criteria, page, size);
	}

	@Override
	public Doctor creatDoctor(Doctor doctor) {
		return doctorOutgoingPort.save(doctor);
	}

	@Override
	public Doctor updateDoctor(UUID doctorId, Doctor doctor) {
		// 1. Prüfen ob Doctor existiert
		doctorOutgoingPort.findById(doctorId)
				.orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

		// 2. ID setzen und speichern
		doctor.setId(doctorId);
		return doctorOutgoingPort.modify(doctor);
	}

	@Override
	public void deleteDoctor(UUID doctorId) {
		// Prüfen ob Doctor existiert
		doctorOutgoingPort.findById(doctorId)
				.orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

		doctorOutgoingPort.deleteById(doctorId);
	}
}
