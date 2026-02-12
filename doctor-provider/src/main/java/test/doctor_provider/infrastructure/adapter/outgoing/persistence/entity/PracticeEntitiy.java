package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "practice")
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class PracticeEntitiy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name="name", nullable = false, length = 200)
    private  String name;

    @Column(name="street", nullable = false, length = 300)
    private String street;

    @Column(name="houseNumber", nullable = false, length = 20)
    private String houseNumber;

    @Column(name="phone", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name="email", nullable = false, length = 100)
    private  String email;

    @Column(name="postal_code", nullable = false, length = 20)
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

}
