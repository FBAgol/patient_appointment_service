package test.doctor_provider.domain.model;

import java.util.UUID;

import test.doctor_provider.domain.enums.SpecialityTyp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
	private UUID id;
	private SpecialityTyp name; // z.B. "Allgemeinmedizin", "Kardiologie"
}
