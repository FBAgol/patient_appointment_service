package test.doctor_provider.application.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import test.doctor_provider.application.port.incoming.SpecialityIncomingPort;
import test.doctor_provider.application.port.outgoing.SpecialityOutgoingPort;
import test.doctor_provider.domain.model.Speciality;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtiyService implements SpecialityIncomingPort {

  private final SpecialityOutgoingPort specialityOutgoingPort;

  @Override
  public List<Speciality> getAllSpecialities(){
    return specialityOutgoingPort.findAll();
  };
}
