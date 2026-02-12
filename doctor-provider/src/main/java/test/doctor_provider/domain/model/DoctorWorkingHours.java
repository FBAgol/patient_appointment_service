package test.doctor_provider.domain.model;

import java.time.LocalTime;
import java.util.UUID;

import test.doctor_provider.domain.enums.Weekday;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWorkingHours {
	private UUID id;
	private UUID doctorId; // Verknüpfung zum ARZT, nicht zur Praxis!
	private Weekday weekday; // ← Jetzt Enum statt Integer! Type-safe!
	private LocalTime startTime; // z.B. 08:00
	private LocalTime endTime; // z.B. 16:00
}
