package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.domain.enums.SpecialityTyp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    private UUID id;
    private SpecialityTyp name; // z.B. "Allgemeinmedizin", "Kardiologie"
}
