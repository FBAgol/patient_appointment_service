package test.doctor_provider.application.port.incoming;

import java.util.Optional;
import java.util.UUID;

import test.doctor_provider.domain.model.Page;
import test.doctor_provider.domain.model.Practice;

public interface PracticeIncomingPort {

	Page<Practice> getAllPractices(Optional<UUID> cityId, Optional<String> practiceName, int page, int size);

	Practice createPractice(Practice practice);

	Practice updatePractice(UUID practiceId, Practice practice);

	void deletePractice(UUID practiceId);
}
