package test.doctor_provider.infrastructure.incomming.web.incommingAdapter;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import test.doctor_provider.api.InternalSpecialitiesApi;
import test.doctor_provider.api.model.SpecialityDto;
import test.doctor_provider.application.port.incoming.SpecialityIncomingPort;
import test.doctor_provider.infrastructure.incomming.web.mapper.SpecialityWebMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SpecialityIncommingAdapter implements InternalSpecialitiesApi {
	private final SpecialityIncomingPort specialityIncomingPort;
	private final SpecialityWebMapper specialityWebMapper;

	@Override
	public ResponseEntity<List<SpecialityDto>> findAllSpecialities() {
		return null;
	}

}
