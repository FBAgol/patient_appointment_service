# 🏥 Implementierungs-Reihenfolge für Patient-Customer Service

## Hexagonale Architektur + Spec-First Ansatz

---

## ⚠️ **WICHTIG: Zuerst Doctor-Provider Service fertigstellen!**

Der Patient-Customer Service ist **abhängig vom Doctor-Provider Service**.

Beginne mit diesem Service erst, wenn Doctor-Provider folgende Features hat:
- ✅ Slot-API (GET /api/slots)
- ✅ Slot-Booking API (POST /api/slots/{id}/book)
- ✅ Slot-Release API (POST /api/slots/{id}/release)
- ✅ Doctor-API (GET /api/doctors)

---

## 📋 **Überblick: Die richtige Reihenfolge**

```
1. ✅ Datenbank-Design (Tabellen, Beziehungen)
2. ✅ Domain-Modelle erstellen
3. 🔄 OpenAPI Specification schreiben
4. 🔄 Code-Generierung (DTOs, API-Interfaces)
5. 🔄 Doctor-Provider REST Client konfigurieren
6. 🔄 Domain-Layer implementieren
7. 🔄 Application-Layer (Use Cases/Ports)
8. 🔄 Infrastructure-Layer (Adapters)
9. 🔄 Tests schreiben
10. 🔄 Integration & Deployment
```

---

## 🎯 **Phase 1: Datenbank & Domain-Design**

### **1.1. Datenbank-Tabellen**

```sql
-- Patient Tabelle
CREATE TABLE patient (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(50),
    date_of_birth DATE,
    address VARCHAR(255),
    city_id UUID,  -- FK zu doctor-provider.city (optional)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Appointment Tabelle
CREATE TABLE appointment (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patient(id),
    slot_id UUID NOT NULL,  -- FK zu doctor-provider.slot (externe Referenz!)
    doctor_id UUID NOT NULL,  -- Denormalisiert für schnellere Abfragen
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,  -- SCHEDULED, COMPLETED, CANCELLED
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Notification Tabelle (optional)
CREATE TABLE notification (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL REFERENCES patient(id),
    appointment_id UUID REFERENCES appointment(id),
    type VARCHAR(50) NOT NULL,  -- EMAIL, SMS
    status VARCHAR(20) NOT NULL,  -- PENDING, SENT, FAILED
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### **1.2. Domain-Modelle erstellen**

```
domain/
├── model/
│   ├── Patient.java
│   ├── Appointment.java
│   └── Notification.java
├── enums/
│   ├── AppointmentStatus.java
│   └── NotificationType.java
└── exception/
    └── (später in Phase 3)
```

**Patient.java**
```java
package test.patient_customer.domain.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    private UUID cityId;  // Optional: FK zu doctor-provider.city
}
```

**Appointment.java**
```java
package test.patient_customer.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.patient_customer.domain.enums.AppointmentStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    private UUID id;
    private UUID patientId;
    private UUID slotId;          // Referenz zu doctor-provider.slot
    private UUID doctorId;        // Denormalisiert
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String notes;
}
```

**AppointmentStatus.java**
```java
package test.patient_customer.domain.enums;

public enum AppointmentStatus {
    SCHEDULED,   // Termin gebucht
    COMPLETED,   // Termin abgeschlossen
    CANCELLED,   // Termin storniert
    NO_SHOW      // Patient nicht erschienen
}
```

---

## 📝 **Phase 2: OpenAPI Specification**

### **Reihenfolge der API-Definitionen:**

#### **2.1. Patient API**
```yaml
paths:
  /api/patients:
    post:
      summary: Register new patient
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreatePatientRequest'
      responses:
        '201':
          description: Patient created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PatientResponse'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
    
    get:
      summary: Get all patients (Admin only)
      responses:
        '200':
          description: List of patients
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/PatientResponse'
  
  /api/patients/{id}:
    get:
      summary: Get patient by ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Patient found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PatientResponse'
        '404':
          description: Patient not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    
    put:
      summary: Update patient
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdatePatientRequest'
      responses:
        '200':
          description: Patient updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PatientResponse'
```

#### **2.2. Appointment API (Integration mit Doctor-Provider)**
```yaml
paths:
  /api/appointments:
    post:
      summary: Book an appointment
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/BookAppointmentRequest'
      responses:
        '201':
          description: Appointment booked successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AppointmentResponse'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
        '409':
          description: Slot not available
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
    
    get:
      summary: Get appointments for patient
      parameters:
        - name: patientId
          in: query
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: List of appointments
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/AppointmentResponse'
  
  /api/appointments/{id}:
    get:
      summary: Get appointment by ID
      responses:
        '200':
          description: Appointment found
    
    delete:
      summary: Cancel appointment
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '204':
          description: Appointment cancelled
        '404':
          description: Appointment not found
        '409':
          description: Cannot cancel (too late, already completed, etc.)
```

#### **2.3. Schemas definieren**
```yaml
components:
  schemas:
    CreatePatientRequest:
      type: object
      required:
        - firstName
        - lastName
        - email
      properties:
        firstName:
          type: string
          minLength: 2
          maxLength: 100
        lastName:
          type: string
          minLength: 2
          maxLength: 100
        email:
          type: string
          format: email
        phoneNumber:
          type: string
          pattern: '^[0-9+\-\s()]+$'
        dateOfBirth:
          type: string
          format: date
        address:
          type: string
          maxLength: 255
        cityId:
          type: string
          format: uuid
    
    PatientResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        firstName:
          type: string
        lastName:
          type: string
        email:
          type: string
        phoneNumber:
          type: string
        dateOfBirth:
          type: string
          format: date
        address:
          type: string
    
    BookAppointmentRequest:
      type: object
      required:
        - patientId
        - slotId
      properties:
        patientId:
          type: string
          format: uuid
        slotId:
          type: string
          format: uuid
          description: Slot ID from doctor-provider service
        notes:
          type: string
          maxLength: 500
    
    AppointmentResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        patientId:
          type: string
          format: uuid
        slotId:
          type: string
          format: uuid
        doctorId:
          type: string
          format: uuid
        appointmentDate:
          type: string
          format: date-time
        status:
          type: string
          enum: [SCHEDULED, COMPLETED, CANCELLED, NO_SHOW]
        notes:
          type: string
        doctorName:
          type: string
          description: Fetched from doctor-provider service
        practiceName:
          type: string
          description: Fetched from doctor-provider service
```

---

## 🔌 **Phase 3: Doctor-Provider REST Client konfigurieren**

### **3.1. REST Client Interface erstellen**

```
infrastructure/
├── adapter/
│   ├── client/
│   │   ├── DoctorProviderClient.java (Interface)
│   │   ├── DoctorProviderClientImpl.java (RestTemplate Implementation)
│   │   └── dto/
│   │       ├── SlotDTO.java
│   │       ├── DoctorDTO.java
│   │       └── BookSlotRequest.java
```

**DoctorProviderClient.java**
```java
package test.patient_customer.infrastructure.adapter.client;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DoctorProviderClient {
    
    /**
     * Holt verfügbare Slots vom Doctor-Provider Service
     */
    List<SlotDTO> getAvailableSlots(UUID doctorId, LocalDate date);
    
    /**
     * Bucht einen Slot beim Doctor-Provider Service
     */
    SlotDTO bookSlot(UUID slotId, UUID patientId);
    
    /**
     * Gibt einen Slot beim Doctor-Provider Service frei (bei Stornierung)
     */
    void releaseSlot(UUID slotId);
    
    /**
     * Holt Arzt-Informationen
     */
    DoctorDTO getDoctorById(UUID doctorId);
}
```

**DoctorProviderClientImpl.java**
```java
package test.patient_customer.infrastructure.adapter.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DoctorProviderClientImpl implements DoctorProviderClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${doctor-provider.base-url}")
    private String doctorProviderBaseUrl;
    
    @Override
    public List<SlotDTO> getAvailableSlots(UUID doctorId, LocalDate date) {
        String url = doctorProviderBaseUrl + "/api/slots?doctorId=" + doctorId + "&date=" + date;
        
        try {
            SlotDTO[] slots = restTemplate.getForObject(url, SlotDTO[].class);
            return Arrays.asList(slots != null ? slots : new SlotDTO[0]);
        } catch (HttpClientErrorException e) {
            throw new DoctorProviderIntegrationException(
                "Failed to fetch slots from doctor-provider", e
            );
        }
    }
    
    @Override
    public SlotDTO bookSlot(UUID slotId, UUID patientId) {
        String url = doctorProviderBaseUrl + "/api/slots/" + slotId + "/book";
        
        BookSlotRequest request = new BookSlotRequest(patientId);
        
        try {
            return restTemplate.postForObject(url, request, SlotDTO.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new SlotNotAvailableException("Slot is no longer available");
        } catch (HttpClientErrorException e) {
            throw new DoctorProviderIntegrationException(
                "Failed to book slot at doctor-provider", e
            );
        }
    }
    
    @Override
    public void releaseSlot(UUID slotId) {
        String url = doctorProviderBaseUrl + "/api/slots/" + slotId + "/release";
        
        try {
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpClientErrorException e) {
            throw new DoctorProviderIntegrationException(
                "Failed to release slot at doctor-provider", e
            );
        }
    }
    
    @Override
    public DoctorDTO getDoctorById(UUID doctorId) {
        String url = doctorProviderBaseUrl + "/api/doctors/" + doctorId;
        
        try {
            return restTemplate.getForObject(url, DoctorDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Doctor", doctorId);
        } catch (HttpClientErrorException e) {
            throw new DoctorProviderIntegrationException(
                "Failed to fetch doctor from doctor-provider", e
            );
        }
    }
}
```

### **3.2. Configuration**

**application.properties**
```properties
# Doctor-Provider Service Integration
doctor-provider.base-url=http://localhost:8081
# oder in Production:
# doctor-provider.base-url=http://doctor-provider-service:8081

# RestTemplate Configuration
rest.connection.timeout=5000
rest.read.timeout=10000
```

**RestTemplateConfig.java**
```java
package test.patient_customer.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

---

## 🏛️ **Phase 4: Domain-Layer implementieren**

### **4.1. Domain Exceptions**
```
domain/exception/
├── DomainException.java
├── ValidationException.java
├── EntityNotFoundException.java
├── SlotNotAvailableException.java
└── DoctorProviderIntegrationException.java
```

### **4.2. Domain Services**
```
domain/service/
└── AppointmentDomainService.java
```

**AppointmentDomainService.java**
```java
package test.patient_customer.domain.service;

import test.patient_customer.domain.enums.AppointmentStatus;
import test.patient_customer.domain.exception.ValidationException;
import test.patient_customer.domain.model.Appointment;

import java.time.LocalDateTime;

public class AppointmentDomainService {
    
    /**
     * Validiert ob ein Termin storniert werden kann
     */
    public void validateCancellation(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Appointment ist bereits storniert");
        }
        
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Abgeschlossene Termine können nicht storniert werden");
        }
        
        // Business-Rule: Min. 24 Stunden vor Termin stornieren
        LocalDateTime minimumCancellationTime = appointment.getAppointmentDate().minusHours(24);
        if (LocalDateTime.now().isAfter(minimumCancellationTime)) {
            throw new ValidationException(
                "Termine können nur mindestens 24 Stunden vor dem Termin storniert werden"
            );
        }
    }
}
```

---

## 🔌 **Phase 5: Application-Layer**

### **5.1. Output Ports**
```java
// application/port/out/
public interface PatientRepository {
    Patient save(Patient patient);
    Optional<Patient> findById(UUID id);
    Optional<Patient> findByEmail(String email);
    List<Patient> findAll();
    void deleteById(UUID id);
}

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByPatientId(UUID patientId);
    List<Appointment> findByDoctorId(UUID doctorId);
    void deleteById(UUID id);
}
```

### **5.2. Input Ports (Use Cases)**
```java
// application/port/in/
public interface ManagePatientUseCase {
    PatientResponse registerPatient(CreatePatientRequest request);
    PatientResponse getPatientById(UUID id);
    PatientResponse updatePatient(UUID id, UpdatePatientRequest request);
}

public interface BookAppointmentUseCase {
    AppointmentResponse bookAppointment(BookAppointmentRequest request);
    AppointmentResponse getAppointmentById(UUID id);
    List<AppointmentResponse> getAppointmentsByPatient(UUID patientId);
    void cancelAppointment(UUID appointmentId);
}
```

### **5.3. Use Case Implementierung**

**AppointmentService.java**
```java
package test.patient_customer.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import test.patient_customer.application.port.in.BookAppointmentUseCase;
import test.patient_customer.application.port.out.AppointmentRepository;
import test.patient_customer.application.port.out.PatientRepository;
import test.patient_customer.domain.enums.AppointmentStatus;
import test.patient_customer.domain.exception.EntityNotFoundException;
import test.patient_customer.domain.model.Appointment;
import test.patient_customer.domain.service.AppointmentDomainService;
import test.patient_customer.infrastructure.adapter.client.DoctorProviderClient;
import test.patient_customer.infrastructure.adapter.client.dto.SlotDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService implements BookAppointmentUseCase {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorProviderClient doctorProviderClient;
    private final AppointmentDomainService appointmentDomainService;
    private final AppointmentMapper appointmentMapper;
    
    @Override
    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        // 1. Validieren dass Patient existiert
        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException("Patient", request.getPatientId()));
        
        // 2. Slot beim Doctor-Provider Service buchen
        SlotDTO bookedSlot = doctorProviderClient.bookSlot(
            request.getSlotId(), 
            request.getPatientId()
        );
        
        // 3. Appointment in eigener DB speichern
        Appointment appointment = new Appointment();
        appointment.setId(UUID.randomUUID());
        appointment.setPatientId(patient.getId());
        appointment.setSlotId(bookedSlot.getId());
        appointment.setDoctorId(bookedSlot.getDoctorId());
        appointment.setAppointmentDate(bookedSlot.getStartTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.getNotes());
        
        Appointment saved = appointmentRepository.save(appointment);
        
        // 4. Response mit zusätzlichen Arzt-Infos vom Doctor-Provider
        return appointmentMapper.toResponse(saved, bookedSlot);
    }
    
    @Override
    public void cancelAppointment(UUID appointmentId) {
        // 1. Appointment finden
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new EntityNotFoundException("Appointment", appointmentId));
        
        // 2. Domain-Validierung
        appointmentDomainService.validateCancellation(appointment);
        
        // 3. Slot beim Doctor-Provider Service freigeben
        doctorProviderClient.releaseSlot(appointment.getSlotId());
        
        // 4. Status in eigener DB aktualisieren
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
    
    @Override
    public List<AppointmentResponse> getAppointmentsByPatient(UUID patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        
        return appointments.stream()
            .map(appointmentMapper::toResponse)
            .collect(Collectors.toList());
    }
}
```

---

## 🧪 **Phase 6: Tests**

### **6.1. Integration Tests mit WireMock**

**AppointmentServiceIntegrationTest.java**
```java
@SpringBootTest
@AutoConfigureMockMvc
class AppointmentServiceIntegrationTest {
    
    @Autowired
    private BookAppointmentUseCase bookAppointmentUseCase;
    
    @MockBean
    private DoctorProviderClient doctorProviderClient;
    
    @Test
    @DisplayName("Sollte Appointment erfolgreich buchen")
    void shouldBookAppointment() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();
        
        SlotDTO slotDTO = new SlotDTO();
        slotDTO.setId(slotId);
        slotDTO.setDoctorId(UUID.randomUUID());
        slotDTO.setStartTime(LocalDateTime.now().plusDays(7));
        
        when(doctorProviderClient.bookSlot(eq(slotId), eq(patientId)))
            .thenReturn(slotDTO);
        
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(patientId);
        request.setSlotId(slotId);
        
        // When
        AppointmentResponse response = bookAppointmentUseCase.bookAppointment(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        verify(doctorProviderClient).bookSlot(slotId, patientId);
    }
}
```

---

## 🚀 **Phase 7: Deployment**

### **7.1. Docker Compose (beide Services)**

```yaml
version: '3.8'

services:
  postgres-doctor:
    image: postgres:15
    environment:
      POSTGRES_DB: doctor_provider
      POSTGRES_USER: doctor_user
      POSTGRES_PASSWORD: doctor_pass
    ports:
      - "5432:5432"
  
  postgres-patient:
    image: postgres:15
    environment:
      POSTGRES_DB: patient_customer
      POSTGRES_USER: patient_user
      POSTGRES_PASSWORD: patient_pass
    ports:
      - "5433:5432"
  
  doctor-provider:
    build: ./doctor-provider
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-doctor:5432/doctor_provider
    depends_on:
      - postgres-doctor
  
  patient-customer:
    build: ./patient-customer
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-patient:5432/patient_customer
      DOCTOR_PROVIDER_BASE_URL: http://doctor-provider:8081
    depends_on:
      - postgres-patient
      - doctor-provider
```

---

## ✅ **Zusammenfassung: Patient-Customer Reihenfolge**

1. ✅ Warte bis Doctor-Provider Slot-APIs fertig sind
2. ✅ Domain-Modelle (Patient, Appointment)
3. ✅ OpenAPI Spec (Patient CRUD, Appointment Booking)
4. ✅ **Doctor-Provider REST Client** (wichtigster Schritt!)
5. ✅ Application Services (mit Integration)
6. ✅ Infrastructure (Persistence + REST Controller)
7. ✅ Tests (mit Mock für Doctor-Provider Client)
8. ✅ Deployment mit Docker Compose

**Geschätzte Dauer: 3-4 Wochen** 📅
