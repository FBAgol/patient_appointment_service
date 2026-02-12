package test.doctor_provider.application.port.incoming;

import java.util.List;

import test.doctor_provider.domain.model.Speciality;

public interface SpecialityIncomingPort {

	/**
	 * WICHTIG: Dieser Endpoint ist NICHT paginiert (kleine, feste Liste von
	 * Fachrichtungen)
	 */
	List<Speciality> getAllSpecialities();
}
