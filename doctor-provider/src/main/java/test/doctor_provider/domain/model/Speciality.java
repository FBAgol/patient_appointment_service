package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    private UUID id;
    private String name; // z.B. "Allgemeinmedizin", "Kardiologie"
}
