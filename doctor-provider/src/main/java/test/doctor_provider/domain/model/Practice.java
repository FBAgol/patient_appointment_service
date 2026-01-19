package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Practice {
    private UUID id;
    private String name;
    private String street;
    private String houseNumber;
    private String phoneNumber;
    private String email;
    private UUID cityId; // ← NUR die ID! Beziehung zu City (FK in practice-Tabelle)
}
