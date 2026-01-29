package test.doctor_provider.domain.model;

import java.util.Set;
import java.util.UUID;

import jakarta.annotation.Nullable;
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
    @Nullable
    private UUID practiceId;// FK zu Practice (n:1) - für bestehende Praxis
    @Nullable
    private Set<UUID> specialityIds;        // FKs zu Specialities (n:m über Junction-Table)
}
