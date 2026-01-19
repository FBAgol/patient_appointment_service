package test.doctor_provider.domain.model;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    private UUID id;
    private String firstName;
    private String lastName;
    private UUID practiceId;                // FK zu Practice (n:1)
    private Set<UUID> specialityIds;        // FKs zu Specialities (n:m über Junction-Table)
}
