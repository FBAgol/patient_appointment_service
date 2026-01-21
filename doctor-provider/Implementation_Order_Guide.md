# 🏗️ Implementierungs-Reihenfolge für Patient Appointment Service

## Hexagonale Architektur + Spec-First Ansatz

---

## 🎯 **Service-Reihenfolge: Welcher Service zuerst?**

In deinem `patient_appointment_service` Projekt hast du 3 Komponenten:

```
patient_appointment_service/
├── doctor-provider/        ← Backend Service 1
├── patient-customer/       ← Backend Service 2  
└── customer-fe/            ← Frontend (Vue.js)
```

### **Die richtige Reihenfolge:**

```
1️⃣ ZUERST: doctor-provider Service (Backend)
   ↓
2️⃣ DANN: patient-customer Service (Backend)
   ↓
3️⃣ ZULETZT: customer-fe (Frontend)
```

---

## 🤔 **Warum diese Reihenfolge?**

### **1️⃣ Doctor-Provider Service ZUERST**

**Warum?**
- ✅ **Keine Abhängigkeiten** zu anderen Services
- ✅ Stellt **Arzt-Daten** und **Slot-Verfügbarkeit** bereit
- ✅ Patient-Customer Service **braucht** Doctor-Provider (um Slots zu buchen)
- ✅ Frontend **braucht** Doctor-Provider (um Ärzte/Slots anzuzeigen)

**Was stellt dieser Service bereit?**
- Ärzte verwalten (CRUD)
- Praxen verwalten
- Arbeitszeiten definieren
- Slots generieren
- Slot-Verfügbarkeit bereitstellen
- **API für andere Services:** Slots abfragen, Slots blockieren/freigeben

---

### **2️⃣ Patient-Customer Service DANACH**

**Warum?**
- ⚠️ **Abhängig von Doctor-Provider Service**
- ✅ Benötigt Doctor-Provider API um:
  - Verfügbare Slots abzufragen
  - Slots zu buchen (über REST Call zu Doctor-Provider)
  - Arzt-Informationen anzuzeigen
- ✅ Frontend **braucht** Patient-Customer (um Buchungen zu machen)

**Was stellt dieser Service bereit?**
- Patienten registrieren/verwalten
- Termine buchen (durch REST Call zu Doctor-Provider)
- Termine stornieren
- Patient-Historie anzeigen
- Benachrichtigungen senden

**Integration mit Doctor-Provider:**
```java
// Patient-Customer ruft Doctor-Provider auf
@Service
public class AppointmentService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<SlotDTO> getAvailableSlots(UUID doctorId, LocalDate date) {
        // REST Call zu Doctor-Provider
        String url = "http://doctor-provider-service/api/slots?doctorId=" + doctorId;
        return restTemplate.getForObject(url, List.class);
    }
    
    public void bookSlot(UUID slotId, UUID patientId) {
        // REST Call zu Doctor-Provider um Slot zu buchen
        String url = "http://doctor-provider-service/api/slots/" + slotId + "/book";
        restTemplate.post(url, new BookingRequest(patientId));
    }
}
```

---

### **3️⃣ Frontend ZULETZT**

**Warum?**
- ⚠️ **Abhängig von BEIDEN Backend-Services**
- ✅ Braucht fertige APIs von beiden Services
- ✅ Kann erst entwickelt werden, wenn Backend-Endpoints existieren

**Was macht das Frontend?**
- Ärzte suchen (Doctor-Provider API)
- Verfügbare Slots anzeigen (Doctor-Provider API)
- Termin buchen (Patient-Customer API)
- Buchungen anzeigen (Patient-Customer API)
- Patient-Profil verwalten (Patient-Customer API)

---

## 📊 **Abhängigkeits-Diagramm:**

```
┌─────────────────────┐
│  customer-fe        │  (3. Frontend - Vue.js)
│  (Frontend)         │
└──────────┬──────────┘
           │ ruft auf
           ├──────────────────┐
           │                  │
           ▼                  ▼
┌─────────────────────┐  ┌─────────────────────┐
│ doctor-provider     │  │ patient-customer    │
│ (Backend 1)         │◄─┤ (Backend 2)         │
│                     │  │                     │
│ - Ärzte/Praxen      │  │ - Patienten         │
│ - Working Hours     │  │ - Buchungen         │
│ - Slots generieren  │  │ - REST Client zu ←──┘
│ - Slot-Verfügbarkeit│  │   Doctor-Provider   │
└─────────────────────┘  └─────────────────────┘
   (1. ZUERST)              (2. DANN)
```

---

## 🛠️ **Entwicklungs-Phasen:**

### **Phase A: Doctor-Provider Service (4-6 Wochen)**

**Sprint 1-2: Core Features**
1. Database Setup
2. Domain Models
3. OpenAPI Specification
4. Stammdaten APIs (City, Speciality)
5. Practice CRUD
6. Doctor CRUD

**Sprint 3-4: Working Hours & Slots**
1. Working Hours CRUD
2. Slot Generation Logik
3. Slot Management
4. Slot Search/Filter API

**Sprint 5: Integration vorbereiten**
1. REST Endpoints für externe Services
2. `/api/slots` - GET (mit Filter)
3. `/api/slots/{id}/book` - POST
4. `/api/slots/{id}/release` - POST
5. Tests & Deployment

---

### **Phase B: Patient-Customer Service (3-4 Wochen)**

**Sprint 1: Basis**
1. Database Setup
2. Domain Models (Patient, Appointment)
3. OpenAPI Specification
4. Patient CRUD

**Sprint 2: Integration mit Doctor-Provider**
1. REST Client konfigurieren (RestTemplate/Feign)
2. Doctor-Provider API konsumieren
3. Slot-Buchungs-Logik implementieren

**Sprint 3: Appointment Management**
1. Appointment CRUD
2. Appointment Status Management
3. Stornierung mit Doctor-Provider Integration
4. Notification Service (Email/SMS)

**Sprint 4: Tests & Deployment**
1. Unit Tests
2. Integration Tests (mit WireMock für Doctor-Provider)
3. E2E Tests
4. Deployment

---

### **Phase C: Frontend (2-3 Wochen)**

**Sprint 1: Setup & Basis-UI**
1. Vue.js Setup
2. Router konfigurieren
3. API Services (Axios)
4. Authentifizierung (falls benötigt)

**Sprint 2: Arzt-Suche & Slots**
1. Arzt-Suchseite (Filter: Stadt, Fachrichtung)
2. Arzt-Detailseite
3. Slot-Kalender-Ansicht
4. Slot-Auswahl UI

**Sprint 3: Buchung & Verwaltung**
1. Buchungs-Flow
2. Buchungs-Bestätigung
3. Meine Termine (Liste)
4. Termin stornieren

**Sprint 4: Polish & Testing**
1. UI/UX Verbesserungen
2. Error Handling
3. Loading States
4. E2E Tests (Cypress)

---

## 🎯 **Minimaler MVP (Minimum Viable Product):**

Wenn du schnell ein funktionierendes System brauchst:

### **MVP Phase 1: Doctor-Provider (2 Wochen)**
```
✅ Practice CRUD
✅ Doctor CRUD
✅ Working Hours CRUD
✅ Slot Generation (einfach)
✅ GET /api/slots (Liste)
✅ POST /api/slots/{id}/book
```

### **MVP Phase 2: Patient-Customer (1 Woche)**
```
✅ Patient Registration
✅ REST Client zu Doctor-Provider
✅ POST /api/appointments (bucht Slot beim Doctor-Provider)
✅ GET /api/appointments (Liste für Patient)
```

### **MVP Phase 3: Frontend (1 Woche)**
```
✅ Arzt-Liste
✅ Slot-Liste für einen Arzt
✅ Buchungs-Button
✅ Meine Termine
```

**Total MVP: 4 Wochen** ⚡

---

## 📋 **Detaillierte Implementierungs-Reihenfolge für Doctor-Provider Service**

Die folgenden Abschnitte beschreiben die **detaillierte Implementierung des Doctor-Provider Service**.

Für Patient-Customer und Frontend: Siehe separate Guides (werden nach Fertigstellung des Doctor-Provider erstellt).

---

## 📋 **Überblick: Die richtige Reihenfolge (Doctor-Provider Service)**

```
1. ✅ Datenbank-Design (Tabellen, Beziehungen) - ERLEDIGT
2. ✅ Domain-Modelle erstellen - ERLEDIGT
3. 🔄 OpenAPI Specification schreiben
4. 🔄 Code-Generierung (DTOs, API-Interfaces)
5. 🔄 Domain-Layer implementieren
6. 🔄 Application-Layer (Use Cases/Ports)
7. 🔄 Infrastructure-Layer (Adapters)
8. 🔄 Tests schreiben
9. 🔄 Integration & Deployment
```

---

## 🎯 **Phase 1: Datenbank & Domain** ✅ ERLEDIGT

### Was du bereits hast:
- ✅ Datenbank-Tabellen definiert
- ✅ Domain-Modelle erstellt:
  - `Doctor.java`
  - `Practice.java`
  - `DoctorWorkingHours.java`
  - `Slot.java`
  - `Speciality.java`
  - `City.java`
- ✅ Enums erstellt:
  - `SpecialityTyp.java`
  - `Weekday.java`
  - `SlotStatus.java`

---

## 📝 **Phase 2: OpenAPI Specification schreiben**

### Reihenfolge der API-Definitionen:

#### **2.1. Zuerst: Shared Components (Stammdaten)**
Beginne mit den **einfachsten Entities ohne komplexe Abhängigkeiten**:

```yaml
# Reihenfolge im YAML:
1. City API (GET /cities)
2. Speciality API (GET /specialities)
```

**Warum zuerst?**
- Keine Abhängigkeiten zu anderen Entities
- Nur lesende Operationen (GET)
- Andere Entities brauchen diese als Referenzen

#### **2.2. Dann: Practice API**
```yaml
3. Practice CRUD
   - POST /practices (Create)
   - GET /practices (List)
   - GET /practices/{id} (Read)
   - PUT /practices/{id} (Update)
   - DELETE /practices/{id} (Delete)
```

**Warum jetzt?**
- Braucht nur City als FK
- Doctors hängen von Practice ab
- Relativ einfache Business-Logik

#### **2.3. Dann: Doctor API**
```yaml
4. Doctor CRUD
   - POST /practices/{practiceId}/doctors (Create Doctor für eine Practice)
   - GET /doctors (List mit Filter)
   - GET /doctors/{id} (Read)
   - PUT /doctors/{id} (Update)
   - DELETE /doctors/{id} (Delete)
   - POST /doctors/{id}/specialities/{specialityId} (Speciality zuordnen)
   - DELETE /doctors/{id}/specialities/{specialityId} (Speciality entfernen)
```

**Warum jetzt?**
- Hängt von Practice und Speciality ab (FKs)
- Basis für WorkingHours und Slots

#### **2.4. Dann: DoctorWorkingHours API**
```yaml
5. WorkingHours CRUD
   - POST /doctors/{doctorId}/working-hours (Create)
   - GET /doctors/{doctorId}/working-hours (List für einen Arzt)
   - PUT /working-hours/{id} (Update)
   - DELETE /working-hours/{id} (Delete)
```

**Warum jetzt?**
- Hängt von Doctor ab
- Slots werden daraus generiert

#### **2.5. Zuletzt: Slot API (Komplexeste Business-Logik)**
```yaml
6. Slot Operations
   - POST /doctors/{doctorId}/slots/generate (Slots aus WorkingHours generieren)
   - GET /slots (Search/Filter nach Datum, Arzt, Speciality, City)
   - GET /slots/{id} (Read)
   - PUT /slots/{id}/block (Slot blockieren)
   - PUT /slots/{id}/unblock (Slot freigeben)
   - POST /slots/{id}/book (Slot buchen) - wird später für Patient Service relevant
```

**Warum zuletzt?**
- Hängt von allen anderen Entities ab
- Komplexeste Business-Logik
- State-Management (AVAILABLE → BOOKED → BLOCKED)

---

### **Detaillierte Schritte für Phase 2:**

```bash
# Datei: src/main/resources/openapi/doctor-provider-api.yaml
```

**Schritt 2.1: Schemas definieren (in dieser Reihenfolge)**
1. Error-Schemas (ErrorResponse, ValidationErrorResponse)
2. City Schemas (CityResponse)
3. Speciality Schemas (SpecialityResponse)
4. Practice Schemas (CreatePracticeRequest, UpdatePracticeRequest, PracticeResponse)
5. Doctor Schemas (CreateDoctorRequest, UpdateDoctorRequest, DoctorResponse)
6. WorkingHours Schemas (CreateWorkingHoursRequest, UpdateWorkingHoursRequest, WorkingHoursResponse)
7. Slot Schemas (SlotResponse, SlotSearchRequest, GenerateSlotsRequest)

**Schritt 2.2: Paths definieren (in dieser Reihenfolge)**
1. `/cities` - GET
2. `/specialities` - GET
3. `/practices` - POST, GET, GET/{id}, PUT/{id}, DELETE/{id}
4. `/doctors` - POST, GET, GET/{id}, PUT/{id}, DELETE/{id}
5. `/doctors/{doctorId}/specialities/{specialityId}` - POST, DELETE
6. `/doctors/{doctorId}/working-hours` - POST, GET
7. `/working-hours/{id}` - PUT, DELETE
8. `/doctors/{doctorId}/slots/generate` - POST
9. `/slots` - GET (mit Query-Params für Filter)
10. `/slots/{id}` - GET, PUT (block/unblock)

**Schritt 2.3: Code generieren**
```bash
mvn clean compile
```

---

## 🚨 **Phase 2.5: Error Handling Strategie definieren**

### **Die 3 Ebenen des Error Handlings:**

Bevor du mit der Implementierung beginnst, ist es wichtig, die Error-Handling-Strategie zu verstehen:

#### **Ebene 1: Bean Validation (OpenAPI → Controller)**

**Was?** Automatische Validierung durch `@Valid` und Bean Validation Annotations

**Wo definiert?** In der OpenAPI Specification

**Beispiel in OpenAPI:**
```yaml
components:
  schemas:
    CreatePracticeRequest:
      type: object
      required:
        - name
        - email
        - cityId
      properties:
        name:
          type: string
          minLength: 2
          maxLength: 100
        email:
          type: string
          format: email
        phoneNumber:
          type: string
          pattern: '^[0-9+\-\s()]+$'
```

**Was wird generiert:**
```java
public class CreatePracticeRequest {
    @NotNull
    @Size(min = 2, max = 100)
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]+$")
    private String phoneNumber;
}
```

**Wann wird das geprüft?** Automatisch im Controller durch `@Valid`

**HTTP Status:** 400 Bad Request

---

#### **Ebene 2: Domain Exceptions (Domain-Layer)**

**Was?** Business-Logik-Fehler und komplexe Validierungen

**Wo implementiert?** In `domain/exception/`

**Welche Exceptions brauchst du:**

1. **DomainException.java** (Basis-Klasse)
   ```java
   public abstract class DomainException extends RuntimeException {
       public DomainException(String message) {
           super(message);
       }
       
       public DomainException(String message, Throwable cause) {
           super(message, cause);
       }
   }
   ```

2. **ValidationException.java** (Business-Validierung)
   - Beispiel: "StartTime muss vor EndTime liegen"
   - Beispiel: "Arbeitszeit darf max. 12 Stunden betragen"
   - HTTP Status: 400 Bad Request

3. **EntityNotFoundException.java** (Entity nicht gefunden)
   - Beispiel: "Doctor mit ID xyz nicht gefunden"
   - HTTP Status: 404 Not Found

4. **SlotNotAvailableException.java** (State-Fehler)
   - Beispiel: "Slot ist bereits gebucht"
   - HTTP Status: 409 Conflict

5. **WorkingHoursOverlapException.java** (Business-Regel verletzt)
   - Beispiel: "Arzt hat bereits Arbeitszeiten zu dieser Zeit"
   - HTTP Status: 409 Conflict

**Wann werden die geworfen?**
- In Domain Services
- Bei Verletzung von Business-Regeln
- Bei komplexen Validierungen

---

#### **Ebene 3: Global Exception Handler (Infrastructure-Layer)**

**Was?** Zentrale Übersetzung von Exceptions zu HTTP-Responses

**Wo implementiert?** In `infrastructure/adapter/rest/`

**Komponenten:**

1. **ErrorResponse.java** (Standard Error DTO)
   ```java
   @Data
   @AllArgsConstructor
   public class ErrorResponse {
       private LocalDateTime timestamp;
       private int status;
       private String error;
       private String message;
       private String path;
   }
   ```

2. **ValidationErrorResponse.java** (Für Bean Validation)
   ```java
   @Data
   @AllArgsConstructor
   public class ValidationErrorResponse {
       private LocalDateTime timestamp;
       private int status;
       private String error;
       private String message;
       private String path;
       private List<FieldError> validationErrors;
   }
   
   @Data
   @AllArgsConstructor
   public static class FieldError {
       private String field;
       private String message;
   }
   ```

3. **GlobalExceptionHandler.java** (@ControllerAdvice)
   - Fängt alle Exceptions
   - Mappt Domain Exceptions zu HTTP Status Codes
   - Gibt einheitliches Error-Format zurück

---

### **Error Handling in OpenAPI definieren:**

**Für jeden Endpoint alle möglichen Error-Responses definieren:**

```yaml
paths:
  /practices/{id}:
    get:
      summary: Get practice by ID
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Practice found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PracticeResponse'
        '404':
          description: Practice not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
                
  /practices:
    post:
      summary: Create new practice
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreatePracticeRequest'
      responses:
        '201':
          description: Practice created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PracticeResponse'
        '400':
          description: Validation error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
        '404':
          description: City not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '500':
          description: Internal server error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
```

---

### **Standard HTTP Status Codes für dein Projekt:**

| **Status Code** | **Bedeutung** | **Wann verwenden?** | **Exception** |
|----------------|---------------|---------------------|---------------|
| 200 OK | Success (GET, PUT) | Erfolgreiche Abfrage/Update | - |
| 201 Created | Resource created | Erfolgreiche Erstellung (POST) | - |
| 204 No Content | Success, no body | Erfolgreiche Löschung (DELETE) | - |
| 400 Bad Request | Validation failed | Bean Validation oder Business-Validierung fehlgeschlagen | `MethodArgumentNotValidException`, `ValidationException` |
| 404 Not Found | Resource not found | Entity existiert nicht | `EntityNotFoundException` |
| 409 Conflict | Business rule violated | State-Konflikt, Business-Regel verletzt | `SlotNotAvailableException`, `WorkingHoursOverlapException` |
| 500 Internal Server Error | Unexpected error | Unerwarteter Fehler, Bug | `Exception` (catch-all) |

---

### **OpenAPI Error Schemas - Komplettes Beispiel:**

```yaml
components:
  schemas:
    ErrorResponse:
      type: object
      required:
        - timestamp
        - status
        - error
        - message
        - path
      properties:
        timestamp:
          type: string
          format: date-time
          description: Zeitstempel des Fehlers
        status:
          type: integer
          description: HTTP Status Code
          example: 404
        error:
          type: string
          description: Fehler-Typ
          example: "Not Found"
        message:
          type: string
          description: Fehlermeldung
          example: "Practice mit ID 123 nicht gefunden"
        path:
          type: string
          description: Request-Pfad
          example: "/api/practices/123"
    
    ValidationErrorResponse:
      type: object
      required:
        - timestamp
        - status
        - error
        - message
        - path
        - validationErrors
      properties:
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
          example: 400
        error:
          type: string
          example: "Validation Error"
        message:
          type: string
          example: "Validierungsfehler in der Anfrage"
        path:
          type: string
        validationErrors:
          type: array
          items:
            type: object
            properties:
              field:
                type: string
                example: "email"
              message:
                type: string
                example: "Ungültige Email-Adresse"
```

---

### **Implementierungs-Reihenfolge für Error Handling:**

#### **Phase 2 (OpenAPI):**
1. ✅ Error-Schemas definieren (ErrorResponse, ValidationErrorResponse)
2. ✅ In jedem Endpoint alle Response-Codes definieren (200, 201, 400, 404, 409, 500)
3. ✅ Bean Validation in Request-Schemas definieren (required, minLength, pattern, format)

#### **Phase 3 (Domain):**
1. ✅ Domain Exceptions erstellen (DomainException, ValidationException, etc.)
2. ✅ In Domain Services werfen bei Business-Regel-Verletzung

#### **Phase 5 (Infrastructure):**
1. ✅ GlobalExceptionHandler erstellen
2. ✅ Für jede Exception einen @ExceptionHandler definieren
3. ✅ Mapping zu HTTP Status Codes implementieren

---

### **Beispiel-Flow: Practice nicht gefunden**

**1. Client Request:**
```http
GET /api/practices/999-999-999
```

**2. Controller ruft Use Case auf:**
```java
@GetMapping("/{id}")
public ResponseEntity<PracticeResponse> getPractice(@PathVariable UUID id) {
    return ResponseEntity.ok(managePracticeUseCase.getPracticeById(id));
}
```

**3. Use Case prüft:**
```java
public PracticeResponse getPracticeById(UUID id) {
    Practice practice = practiceRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Practice", id));
    return practiceMapper.toResponse(practice);
}
```

**4. GlobalExceptionHandler fängt:**
```java
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity<ErrorResponse> handleEntityNotFound(
        EntityNotFoundException ex,
        HttpServletRequest request) {
    
    ErrorResponse response = new ErrorResponse(
        LocalDateTime.now(),
        404,
        "Not Found",
        ex.getMessage(),
        request.getRequestURI()
    );
    
    return ResponseEntity.status(404).body(response);
}
```

**5. Client erhält:**
```json
{
    "timestamp": "2026-01-20T21:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Practice mit ID 999-999-999 nicht gefunden",
    "path": "/api/practices/999-999-999"
}
```

---

## 🏛️ **Phase 3: Domain-Layer implementieren**

### Reihenfolge innerhalb des Domain-Layers:

#### **3.1. Exceptions (domain/exception/)**
```
1. DomainException.java (Basis-Klasse)
2. ValidationException.java
3. EntityNotFoundException.java
4. SlotNotAvailableException.java
5. WorkingHoursOverlapException.java
```

**Warum zuerst?**
- Werden von Domain Services verwendet
- Keine Abhängigkeiten

#### **3.2. Domain Services (domain/service/)**
```
Reihenfolge:
1. WorkingHoursDomainService.java
   - validateWorkingHours()
   - checkForOverlaps()
   
2. SlotDomainService.java
   - generateSlots()
   - validateSlotBooking()
   - checkSlotAvailability()
```

**Warum diese Reihenfolge?**
- WorkingHours sind einfacher
- SlotDomainService könnte WorkingHoursDomainService nutzen

---

## 🔌 **Phase 4: Application-Layer (Use Cases & Ports)**

### Reihenfolge:

#### **4.1. Output Ports (application/port/out/) - Interfaces für Repositories**
```
Reihenfolge (von einfach zu komplex):
1. CityRepository.java
2. SpecialityRepository.java
3. PracticeRepository.java
4. DoctorRepository.java
5. WorkingHoursRepository.java
6. SlotRepository.java
```

**Was definieren?**
```java
// Beispiel: PracticeRepository
public interface PracticeRepository {
    Practice save(Practice practice);
    Optional<Practice> findById(UUID id);
    List<Practice> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
```

#### **4.2. Input Ports (application/port/in/) - Interfaces für Use Cases**
```
Reihenfolge:
1. CityQueryService.java
2. SpecialityQueryService.java
3. ManagePracticeUseCase.java
4. ManageDoctorUseCase.java
5. ManageWorkingHoursUseCase.java
6. ManageSlotsUseCase.java
7. SearchSlotsUseCase.java
```

**Was definieren?**
```java
// Beispiel: ManagePracticeUseCase
public interface ManagePracticeUseCase {
    PracticeResponse createPractice(CreatePracticeRequest request);
    PracticeResponse getPracticeById(UUID id);
    List<PracticeResponse> getAllPractices();
    PracticeResponse updatePractice(UUID id, UpdatePracticeRequest request);
    void deletePractice(UUID id);
}
```

#### **4.3. Use Case Implementierungen (application/service/)**
```
Reihenfolge (gleiche wie Input Ports):
1. CityQueryServiceImpl.java
2. SpecialityQueryServiceImpl.java
3. PracticeService.java (implements ManagePracticeUseCase)
4. DoctorService.java (implements ManageDoctorUseCase)
5. WorkingHoursService.java (implements ManageWorkingHoursUseCase)
6. SlotManagementService.java (implements ManageSlotsUseCase)
7. SlotSearchService.java (implements SearchSlotsUseCase)
```

**Wichtig:**
- Use Case Implementierungen nutzen:
  - Output Ports (Repositories)
  - Domain Services
  - Domain Exceptions

---

## 🏗️ **Phase 5: Infrastructure-Layer (Adapters)**

### Reihenfolge:

#### **5.1. Persistence Adapter (infrastructure/adapter/persistence/)**

**Schritt 5.1.1: JPA Entities erstellen**
```
Reihenfolge (von einfach zu komplex):
1. CityEntity.java
2. SpecialityEntity.java
3. PracticeEntity.java
4. DoctorEntity.java
5. DoctorSpecialityEntity.java (Junction Table)
6. WorkingHoursEntity.java
7. SlotEntity.java
```

**Schritt 5.1.2: JPA Repositories erstellen**
```
1. CityJpaRepository.java (extends JpaRepository)
2. SpecialityJpaRepository.java
3. PracticeJpaRepository.java
4. DoctorJpaRepository.java
5. WorkingHoursJpaRepository.java
6. SlotJpaRepository.java
```

**Schritt 5.1.3: Mapper erstellen**
```
1. CityMapper.java
2. SpecialityMapper.java
3. PracticeMapper.java
4. DoctorMapper.java
5. WorkingHoursMapper.java
6. SlotMapper.java
```

**Schritt 5.1.4: Repository Adapter Implementierungen**
```
1. CityRepositoryAdapter.java (implements CityRepository)
2. SpecialityRepositoryAdapter.java
3. PracticeRepositoryAdapter.java
4. DoctorRepositoryAdapter.java
5. WorkingHoursRepositoryAdapter.java
6. SlotRepositoryAdapter.java
```

#### **5.2. REST Adapter (infrastructure/adapter/rest/)**

**Schritt 5.2.1: Exception Handler**
```
1. ErrorResponse.java (DTO)
2. ValidationErrorResponse.java (DTO)
3. GlobalExceptionHandler.java (@ControllerAdvice)
```

**Schritt 5.2.2: Controller Implementierungen**
```
Reihenfolge (gleiche wie API):
1. CityController.java (implements generierte CityApi)
2. SpecialityController.java
3. PracticeController.java
4. DoctorController.java
5. WorkingHoursController.java
6. SlotController.java
```

**Was machen Controller?**
- Implementieren die von OpenAPI generierten API-Interfaces
- Rufen Use Cases (Input Ports) auf
- Geben generierte DTOs zurück

---

## 🧪 **Phase 6: Tests schreiben - PARALLEL zur Entwicklung!**

### ⚠️ **WICHTIG: Test-Driven Development (TDD) Ansatz**

**Tests werden NICHT am Ende geschrieben, sondern PARALLEL zu jeder Feature-Implementierung!**

```
Feature implementieren → Tests schreiben → Weiter zum nächsten Feature
```

### **Test-Strategie für alle Services:**

Für **Doctor-Provider**, **Patient-Customer** UND **Frontend**:
- ✅ **Unit Tests** - Isolierte Domain-Logik (JUnit 5 + Mockito)
- ✅ **Integration Tests** - Mit echter DB (REST Assured + TestContainers)
- ✅ **E2E Tests** - Komplette Flows (REST Assured + TestContainers)

### **Test-Framework Stack:**

```xml
<!-- pom.xml Dependencies für ALLE Backend-Services -->
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- REST Assured für API Tests -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers für PostgreSQL -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito für Unit Tests -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ für bessere Assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### **Test-Pyramide für hexagonale Architektur:**

```
                  E2E Tests (wenige)
                 /                \
    Integration Tests mit REST Assured (mehr)
           /                          \
      Unit Tests (viele)
```

---

### **Die 3 Test-Ebenen:**

#### **6.1. Unit Tests - Domain Layer** 
**Ziel:** Business-Logik isoliert testen (KEINE Datenbank, KEINE Spring Context)

**Was testen?**
- Domain Services
- Domain-Validierungen
- Domain Exceptions

**Test-Framework:**
- JUnit 5
- Mockito (für Mocking)
- AssertJ (für Assertions)

**Reihenfolge:**
```
1. Domain Service Tests:
   - WorkingHoursDomainServiceTest.java
   - SlotDomainServiceTest.java
```

**Beispiel: WorkingHoursDomainServiceTest.java**

```java
package test.doctor_provider.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import test.doctor_provider.domain.enums.Weekday;
import test.doctor_provider.domain.exception.ValidationException;
import test.doctor_provider.domain.exception.WorkingHoursOverlapException;
import test.doctor_provider.domain.model.DoctorWorkingHours;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class WorkingHoursDomainServiceTest {
    
    private WorkingHoursDomainService service;
    
    @BeforeEach
    void setUp() {
        service = new WorkingHoursDomainService();
    }
    
    @Test
    @DisplayName("Sollte Arbeitszeiten validieren: StartTime muss vor EndTime liegen")
    void shouldValidateStartTimeBeforeEndTime() {
        // Given
        DoctorWorkingHours hours = new DoctorWorkingHours(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Weekday.MONDAY,
            LocalTime.of(16, 0),  // StartTime
            LocalTime.of(8, 0)    // EndTime (vor StartTime!)
        );
        
        // When & Then
        assertThatThrownBy(() -> service.validateWorkingHours(hours))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Startzeit muss vor Endzeit liegen");
    }
    
    @Test
    @DisplayName("Sollte Arbeitszeiten validieren: Max 12 Stunden pro Tag")
    void shouldValidateMaxWorkingHours() {
        // Given
        DoctorWorkingHours hours = new DoctorWorkingHours(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Weekday.MONDAY,
            LocalTime.of(6, 0),   // 6:00
            LocalTime.of(22, 0)   // 22:00 (16 Stunden!)
        );
        
        // When & Then
        assertThatThrownBy(() -> service.validateWorkingHours(hours))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("max. 12 Stunden");
    }
    
    @Test
    @DisplayName("Sollte gültige Arbeitszeiten akzeptieren")
    void shouldAcceptValidWorkingHours() {
        // Given
        DoctorWorkingHours hours = new DoctorWorkingHours(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Weekday.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(16, 0)
        );
        
        // When & Then
        assertThatCode(() -> service.validateWorkingHours(hours))
            .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("Sollte Überschneidungen erkennen")
    void shouldDetectOverlappingWorkingHours() {
        // Given
        UUID doctorId = UUID.randomUUID();
        
        DoctorWorkingHours existing = new DoctorWorkingHours(
            UUID.randomUUID(),
            doctorId,
            Weekday.MONDAY,
            LocalTime.of(8, 0),   // 8:00 - 16:00
            LocalTime.of(16, 0)
        );
        
        DoctorWorkingHours newHours = new DoctorWorkingHours(
            UUID.randomUUID(),
            doctorId,
            Weekday.MONDAY,
            LocalTime.of(14, 0),  // 14:00 - 18:00 (überschneidet sich!)
            LocalTime.of(18, 0)
        );
        
        // When & Then
        assertThatThrownBy(() -> 
            service.checkForOverlaps(newHours, Arrays.asList(existing)))
            .isInstanceOf(WorkingHoursOverlapException.class);
    }
    
    @Test
    @DisplayName("Sollte keine Überschneidungen bei unterschiedlichen Wochentagen erkennen")
    void shouldNotDetectOverlapOnDifferentWeekdays() {
        // Given
        UUID doctorId = UUID.randomUUID();
        
        DoctorWorkingHours existing = new DoctorWorkingHours(
            UUID.randomUUID(),
            doctorId,
            Weekday.MONDAY,      // Montag
            LocalTime.of(8, 0),
            LocalTime.of(16, 0)
        );
        
        DoctorWorkingHours newHours = new DoctorWorkingHours(
            UUID.randomUUID(),
            doctorId,
            Weekday.TUESDAY,     // Dienstag
            LocalTime.of(8, 0),
            LocalTime.of(16, 0)
        );
        
        // When & Then
        assertThatCode(() -> 
            service.checkForOverlaps(newHours, Arrays.asList(existing)))
            .doesNotThrowAnyException();
    }
}
```

---

#### **6.2. Integration Tests - Application Layer**
**Ziel:** Use Cases mit echten Repositories testen (MIT Datenbank, MIT Spring Context)

**Was testen?**
- Use Case Implementierungen
- Zusammenspiel von Service + Repository
- Transaktionen
- Exception Handling

**Test-Framework:**
- JUnit 5
- Spring Boot Test (`@SpringBootTest`)
- H2 In-Memory Database (für Tests)
- TestContainers (optional, für PostgreSQL)

**Annotations:**
- `@SpringBootTest` - Lädt den vollen Spring Context
- `@Transactional` - Rollback nach jedem Test
- `@AutoConfigureTestDatabase` - In-Memory DB verwenden

**Reihenfolge:**
```
1. PracticeServiceTest.java
2. DoctorServiceTest.java
3. WorkingHoursServiceTest.java
4. SlotManagementServiceTest.java
```

**Beispiel: PracticeServiceTest.java**

```java
package test.doctor_provider.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import test.doctor_provider.application.port.in.ManagePracticeUseCase;
import test.doctor_provider.domain.exception.EntityNotFoundException;
import test.doctor_provider.infrastructure.adapter.rest.dto.CreatePracticeRequest;
import test.doctor_provider.infrastructure.adapter.rest.dto.PracticeResponse;
import test.doctor_provider.infrastructure.adapter.rest.dto.UpdatePracticeRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")  // Verwendet application-test.properties
@Transactional           // Rollback nach jedem Test
class PracticeServiceTest {
    
    @Autowired
    private ManagePracticeUseCase managePracticeUseCase;
    
    private UUID cityId;
    
    @BeforeEach
    void setUp() {
        // City muss existieren für Practice
        cityId = UUID.randomUUID();  // In echtem Test: City in DB anlegen
    }
    
    @Test
    @DisplayName("Sollte Practice erfolgreich erstellen")
    void shouldCreatePractice() {
        // Given
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setName("Test Practice");
        request.setStreet("Hauptstraße");
        request.setHouseNumber("123");
        request.setPhoneNumber("+49 123 456789");
        request.setEmail("test@practice.com");
        request.setCityId(cityId);
        
        // When
        PracticeResponse response = managePracticeUseCase.createPractice(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Practice");
        assertThat(response.getEmail()).isEqualTo("test@practice.com");
    }
    
    @Test
    @DisplayName("Sollte Practice by ID finden")
    void shouldFindPracticeById() {
        // Given - Practice erstellen
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setName("Find Test");
        request.setStreet("Teststraße");
        request.setHouseNumber("1");
        request.setEmail("find@test.com");
        request.setCityId(cityId);
        
        PracticeResponse created = managePracticeUseCase.createPractice(request);
        
        // When
        PracticeResponse found = managePracticeUseCase.getPracticeById(created.getId());
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Find Test");
    }
    
    @Test
    @DisplayName("Sollte EntityNotFoundException werfen wenn Practice nicht existiert")
    void shouldThrowExceptionWhenPracticeNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        // When & Then
        assertThatThrownBy(() -> 
            managePracticeUseCase.getPracticeById(nonExistentId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Practice");
    }
    
    @Test
    @DisplayName("Sollte Practice aktualisieren")
    void shouldUpdatePractice() {
        // Given - Practice erstellen
        CreatePracticeRequest createRequest = new CreatePracticeRequest();
        createRequest.setName("Old Name");
        createRequest.setStreet("Old Street");
        createRequest.setHouseNumber("1");
        createRequest.setEmail("old@test.com");
        createRequest.setCityId(cityId);
        
        PracticeResponse created = managePracticeUseCase.createPractice(createRequest);
        
        // When - Practice aktualisieren
        UpdatePracticeRequest updateRequest = new UpdatePracticeRequest();
        updateRequest.setName("New Name");
        updateRequest.setEmail("new@test.com");
        
        PracticeResponse updated = managePracticeUseCase.updatePractice(
            created.getId(), 
            updateRequest
        );
        
        // Then
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }
    
    @Test
    @DisplayName("Sollte Practice löschen")
    void shouldDeletePractice() {
        // Given
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setName("To Delete");
        request.setStreet("Delete Street");
        request.setHouseNumber("99");
        request.setEmail("delete@test.com");
        request.setCityId(cityId);
        
        PracticeResponse created = managePracticeUseCase.createPractice(request);
        
        // When
        managePracticeUseCase.deletePractice(created.getId());
        
        // Then - Practice sollte nicht mehr existieren
        assertThatThrownBy(() -> 
            managePracticeUseCase.getPracticeById(created.getId()))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
```

---

#### **6.3. Controller Tests (Web Layer)**
**Ziel:** REST-Endpoints testen (HTTP Requests/Responses, Validierung, Error Handling)

**Was testen?**
- HTTP Status Codes
- Request/Response JSON
- Bean Validation
- Exception Handling (GlobalExceptionHandler)

**Test-Framework:**
- JUnit 5
- MockMvc (`@WebMvcTest`)
- Mockito (für Use Case Mocking)

**Annotations:**
- `@WebMvcTest(ControllerName.class)` - Nur Web Layer laden
- `@MockBean` - Use Cases mocken

**Reihenfolge:**
```
1. CityControllerTest.java
2. SpecialityControllerTest.java
3. PracticeControllerTest.java
4. DoctorControllerTest.java
5. WorkingHoursControllerTest.java
6. SlotControllerTest.java
```

**Beispiel: PracticeControllerTest.java**

```java
package test.doctor_provider.infrastructure.adapter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import test.doctor_provider.application.port.in.ManagePracticeUseCase;
import test.doctor_provider.domain.exception.EntityNotFoundException;
import test.doctor_provider.infrastructure.adapter.rest.dto.CreatePracticeRequest;
import test.doctor_provider.infrastructure.adapter.rest.dto.PracticeResponse;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PracticeController.class)
class PracticeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ManagePracticeUseCase managePracticeUseCase;
    
    @Test
    @DisplayName("POST /practices sollte 201 Created zurückgeben")
    void shouldCreatePractice() throws Exception {
        // Given
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setName("Test Practice");
        request.setStreet("Hauptstraße");
        request.setHouseNumber("123");
        request.setEmail("test@practice.com");
        request.setCityId(UUID.randomUUID());
        
        PracticeResponse response = new PracticeResponse();
        response.setId(UUID.randomUUID());
        response.setName("Test Practice");
        response.setEmail("test@practice.com");
        
        when(managePracticeUseCase.createPractice(any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/practices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Test Practice"))
            .andExpect(jsonPath("$.email").value("test@practice.com"));
    }
    
    @Test
    @DisplayName("POST /practices sollte 400 Bad Request bei Validation Error zurückgeben")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given - Request ohne required field "name"
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setEmail("test@practice.com");
        // name fehlt!
        
        // When & Then
        mockMvc.perform(post("/api/practices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.validationErrors").isArray());
    }
    
    @Test
    @DisplayName("GET /practices/{id} sollte 200 OK zurückgeben")
    void shouldGetPracticeById() throws Exception {
        // Given
        UUID practiceId = UUID.randomUUID();
        
        PracticeResponse response = new PracticeResponse();
        response.setId(practiceId);
        response.setName("Test Practice");
        
        when(managePracticeUseCase.getPracticeById(practiceId))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/practices/{id}", practiceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(practiceId.toString()))
            .andExpect(jsonPath("$.name").value("Test Practice"));
    }
    
    @Test
    @DisplayName("GET /practices/{id} sollte 404 Not Found bei nicht existierender Practice zurückgeben")
    void shouldReturn404WhenPracticeNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        
        when(managePracticeUseCase.getPracticeById(nonExistentId))
            .thenThrow(new EntityNotFoundException("Practice", nonExistentId));
        
        // When & Then
        mockMvc.perform(get("/api/practices/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    @DisplayName("DELETE /practices/{id} sollte 204 No Content zurückgeben")
    void shouldDeletePractice() throws Exception {
        // Given
        UUID practiceId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(delete("/api/practices/{id}", practiceId))
            .andExpect(status().isNoContent());
    }
}
```

---

#### **6.4. End-to-End Tests**
**Ziel:** Komplette User-Flows testen (Über alle Layer hinweg)

**Was testen?**
- Komplette Business-Flows
- Zusammenspiel aller Komponenten
- Realistische Szenarien

**Test-Framework:**
- JUnit 5
- RestAssured (für REST API Testing)
- TestContainers (für echte PostgreSQL DB)

**Reihenfolge:**
```
1. Practice CRUD Flow
2. Doctor CRUD + Speciality Assignment
3. WorkingHours CRUD
4. Slot Generation und Booking Flow
```

**Beispiel: SlotBookingE2ETest.java**

```java
package test.doctor_provider.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalTime;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@Testcontainers
class SlotBookingE2ETest {
    
    @LocalServerPort
    private int port;
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("doctor_provider_test")
        .withUsername("test")
        .withPassword("test");
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }
    
    @Test
    @DisplayName("E2E: Kompletter Slot-Buchungs-Flow")
    void shouldCompleteSlotBookingFlow() {
        // 1. City erstellen (Setup)
        UUID cityId = UUID.randomUUID();
        
        // 2. Practice erstellen
        String practiceJson = """
            {
                "name": "Test Practice",
                "street": "Hauptstraße",
                "houseNumber": "123",
                "email": "practice@test.com",
                "phoneNumber": "+49 123 456789",
                "cityId": "%s"
            }
            """.formatted(cityId);
        
        String practiceId = given()
            .contentType(ContentType.JSON)
            .body(practiceJson)
        .when()
            .post("/practices")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
        
        // 3. Doctor erstellen
        String doctorJson = """
            {
                "firstName": "Dr. Max",
                "lastName": "Mustermann",
                "practiceId": "%s"
            }
            """.formatted(practiceId);
        
        String doctorId = given()
            .contentType(ContentType.JSON)
            .body(doctorJson)
        .when()
            .post("/doctors")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
        
        // 4. Working Hours erstellen
        String workingHoursJson = """
            {
                "weekday": "MONDAY",
                "startTime": "08:00",
                "endTime": "16:00"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(workingHoursJson)
        .when()
            .post("/doctors/{doctorId}/working-hours", doctorId)
        .then()
            .statusCode(201);
        
        // 5. Slots generieren
        String generateSlotsJson = """
            {
                "startDate": "2026-01-27",
                "endDate": "2026-01-27",
                "slotDurationMinutes": 30
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(generateSlotsJson)
        .when()
            .post("/doctors/{doctorId}/slots/generate", doctorId)
        .then()
            .statusCode(200);
        
        // 6. Slots abrufen
        given()
        .when()
            .get("/slots?doctorId={doctorId}&date=2026-01-27", doctorId)
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].status", equalTo("AVAILABLE"));
        
        // 7. Ersten Slot buchen
        String slotId = given()
        .when()
            .get("/slots?doctorId={doctorId}&date=2026-01-27", doctorId)
        .then()
            .extract()
            .path("[0].id");
        
        given()
        .when()
            .post("/slots/{slotId}/book", slotId)
        .then()
            .statusCode(200)
            .body("status", equalTo("BOOKED"));
        
        // 8. Verifizieren dass Slot jetzt BOOKED ist
        given()
        .when()
            .get("/slots/{slotId}", slotId)
        .then()
            .statusCode(200)
            .body("status", equalTo("BOOKED"));
    }
}
```

---

### **Test-Konfiguration:**

#### **application-test.properties** (für Integration Tests)
```properties
# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Logging
logging.level.test.doctor_provider=DEBUG
```

#### **pom.xml - Test Dependencies**
```xml
<dependencies>
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ für bessere Assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- REST Assured für E2E Tests -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- TestContainers für E2E Tests mit PostgreSQL -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- H2 für Integration Tests -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### **Test-Coverage Ziele:**

| **Layer** | **Ziel Coverage** | **Was testen?** |
|-----------|-------------------|-----------------|
| Domain Services | 90-100% | Alle Business-Regeln, Edge Cases |
| Application Services | 80-90% | Happy Path + Error Cases |
| Controllers | 70-80% | Status Codes, Validation, Exceptions |
| E2E | 50-60% | Kritische User-Flows |

---

### **Best Practices für Tests:**

#### **1. Test-Naming Convention:**
```java
@Test
@DisplayName("Sollte EntityNotFoundException werfen wenn Practice nicht existiert")
void shouldThrowExceptionWhenPracticeNotFound() { ... }
```

**Pattern:** `should[ExpectedResult]When[Condition]`

#### **2. AAA Pattern (Arrange-Act-Assert):**
```java
@Test
void testExample() {
    // Given (Arrange) - Setup
    CreatePracticeRequest request = new CreatePracticeRequest();
    
    // When (Act) - Aktion ausführen
    PracticeResponse response = service.createPractice(request);
    
    // Then (Assert) - Verifizieren
    assertThat(response).isNotNull();
}
```

#### **3. Einen Assert pro Test (wenn möglich):**
```java
// ✅ Gut
@Test
void shouldCreatePracticeWithCorrectName() {
    assertThat(response.getName()).isEqualTo("Test Practice");
}

// ❌ Nicht ideal (zu viele Asserts)
@Test
void shouldCreatePractice() {
    assertThat(response.getName()).isEqualTo("Test");
    assertThat(response.getEmail()).isEqualTo("test@test.com");
    assertThat(response.getPhone()).isEqualTo("123");
    // ... 10 weitere Asserts
}
```

#### **4. Test Data Builders verwenden:**
```java
class PracticeTestDataBuilder {
    private String name = "Default Practice";
    private String email = "default@test.com";
    
    public PracticeTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public CreatePracticeRequest build() {
        CreatePracticeRequest request = new CreatePracticeRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }
}

// Verwendung
@Test
void test() {
    CreatePracticeRequest request = new PracticeTestDataBuilder()
        .withName("Custom Name")
        .build();
}
```

---

### **Wann welche Tests schreiben?**

#### **Während der Entwicklung (TDD-Style):**
```
1. Schreibe Unit Test (rot)
   ↓
2. Implementiere Minimum-Code (grün)
   ↓
3. Refactoring (grün bleibt)
   ↓
4. Nächster Test
```

#### **Nach der Implementierung:**
```
1. Feature implementieren
   ↓
2. Unit Tests schreiben
   ↓
3. Integration Tests schreiben
   ↓
4. Controller Tests schreiben
   ↓
5. E2E Test für kritische Flows
```

---

### **Test-Reihenfolge pro Entity:**

**Beispiel: Practice**

```
1. ✅ Domain Tests (wenn Domain Service existiert)
2. ✅ Integration Test: PracticeServiceTest
   - shouldCreatePractice()
   - shouldGetPracticeById()
   - shouldUpdatePractice()
   - shouldDeletePractice()
   - shouldThrowExceptionWhenNotFound()
3. ✅ Controller Test: PracticeControllerTest
   - shouldReturn201OnCreate()
   - shouldReturn200OnGet()
   - shouldReturn400OnValidationError()
   - shouldReturn404WhenNotFound()
4. ✅ E2E Test (optional): Practice CRUD Flow
```

**Dann zur nächsten Entity (Doctor, WorkingHours, Slot)...**

---

## 🚀 **Phase 7: Configuration & Deployment**

### Reihenfolge:

```
1. application.properties konfigurieren
   - Database Connection
   - JPA Settings
   - Server Port
   - Logging

2. Docker Setup (optional)
   - Dockerfile
   - docker-compose.yml (mit PostgreSQL)

3. CI/CD Pipeline (optional)
   - GitHub Actions / GitLab CI
```

---

## 📊 **Zusammenfassung: Die komplette Implementierungs-Reihenfolge**

### **Für jede Entity (Practice, Doctor, WorkingHours, Slot):**

```
1. OpenAPI Schema + Paths definieren
   ↓
2. Code generieren (mvn compile)
   ↓
3. Domain Exceptions erstellen (wenn nötig)
   ↓
4. Domain Service erstellen (wenn Business-Logik nötig)
   ↓
5. Output Port (Repository Interface) definieren
   ↓
6. Input Port (Use Case Interface) definieren
   ↓
7. Use Case Implementierung (Application Service)
   ↓
8. JPA Entity erstellen
   ↓
9. JPA Repository erstellen
   ↓
10. Mapper erstellen
   ↓
11. Repository Adapter Implementierung
   ↓
12. Controller Implementierung
   ↓
13. Tests schreiben
```

---

## 🎯 **Empfohlene Reihenfolge für komplette Features:**

### **Sprint 1: Stammdaten**
1. City (Read-only)
2. Speciality (Read-only)

### **Sprint 2: Practice Management**
1. Practice CRUD komplett durchimplementieren
2. Von OpenAPI bis Controller bis Tests

### **Sprint 3: Doctor Management**
1. Doctor CRUD
2. Doctor-Speciality Assignment

### **Sprint 4: Working Hours**
1. WorkingHours CRUD
2. WorkingHoursDomainService mit Validierung

### **Sprint 5: Slot Management**
1. Slot Generation aus WorkingHours
2. SlotDomainService
3. Slot Search/Filter API

### **Sprint 6: Advanced Features**
1. Slot Booking (Vorbereitung für Patient Service)
2. Slot Block/Unblock
3. Complex Queries

---

## 💡 **Best Practices während der Implementierung:**

### **1. Incrementelles Vorgehen:**
- ✅ Erst eine Entity komplett durchziehen (OpenAPI → Domain → Application → Infrastructure → Tests)
- ❌ NICHT alle Layers gleichzeitig für alle Entities

### **2. Nach jedem Schritt:**
- Code generieren (`mvn clean compile`)
- Kompilieren und Fehler beheben
- Tests laufen lassen
- Git Commit

### **3. Reihenfolge innerhalb einer Entity:**
- Start: OpenAPI Spec
- End: Working Tests
- Nicht zum nächsten Feature, bis Tests grün sind

### **4. Dependency Management:**
- Einfache Entities zuerst (City, Speciality)
- Komplexe Entities zuletzt (Slot)
- Immer Abhängigkeiten berücksichtigen

---

## ✅ **Checkliste für jede Entity:**

```
Entity: _________________

Phase 2: OpenAPI
□ Schemas definiert (Request DTOs, Response DTOs)
□ Paths definiert (POST, GET, PUT, DELETE)
□ Response Codes definiert (200, 201, 400, 404, 409)
□ Code generiert (mvn compile)

Phase 3: Domain
□ Domain Model existiert
□ Domain Exceptions erstellt (wenn nötig)
□ Domain Service erstellt (wenn Business-Logik nötig)

Phase 4: Application
□ Repository Interface (Output Port)
□ Use Case Interface (Input Port)
□ Use Case Implementierung (Service)

Phase 5: Infrastructure - Persistence
□ JPA Entity
□ JPA Repository
□ Mapper (Entity ↔ Domain Model)
□ Repository Adapter Implementierung

Phase 5: Infrastructure - REST
□ Controller Implementierung
□ Exception Handler konfiguriert

Phase 6: Tests
□ Domain Service Tests
□ Application Service Tests
□ Controller Tests
□ Integration Tests

Phase 7: Deployment
□ application.properties konfiguriert
□ Datenbank Migrations (Flyway/Liquibase)
□ Docker Setup (optional)
```

---

## 🎓 **Nächste Schritte für dich:**

### **Jetzt sofort:**
1. Öffne `src/main/resources/openapi/doctor-provider-api.yaml`
2. Beginne mit **City API** (einfachster Fall):
   ```yaml
   /cities:
     get:
       summary: Get all cities
       responses:
         '200':
           content:
             application/json:
               schema:
                 type: array
                 items:
                   $ref: '#/components/schemas/CityResponse'
   ```
3. Dann **Speciality API**
4. Dann **Practice API** (erstes komplettes CRUD)

### **Danach:**
- Für Practice: Den kompletten Flow von OpenAPI bis Tests durchziehen
- Erst wenn Practice komplett fertig ist → Doctor beginnen
- Erst wenn Doctor komplett fertig ist → WorkingHours beginnen
- Usw.

---

## 📚 **Weitere Ressourcen:**

- Hexagonale Architektur: https://alistair.cockburn.us/hexagonal-architecture/
- OpenAPI Spec: https://swagger.io/specification/
- Spring Boot Best Practices: https://spring.io/guides

---

**Viel Erfolg bei der Implementierung! 🚀**

Bei Fragen zur konkreten Implementierung eines Schrittes, frag einfach!
