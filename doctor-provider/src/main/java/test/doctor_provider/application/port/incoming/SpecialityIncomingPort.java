package test.doctor_provider.application.port.incoming;

import test.doctor_provider.domain.model.Speciality;

import java.util.List;


public interface SpecialityIncomingPort {

    /**
     * WICHTIG: Dieser Endpoint ist NICHT paginiert (kleine, feste Liste von Fachrichtungen)
     */
    List<Speciality> getAllSpecialities();
}
