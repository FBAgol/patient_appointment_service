# 🚀 Implementierungs-Leitfaden: Patient Appointment Service

**Architektur:** Hexagonale Architektur (Backend) + Pinia (Frontend)  
**Methode:** Spec-First (OpenAPI)  
**Stack:** Spring Boot + PostgreSQL + Vue.js 3

---

## 📋 Teil 1: Service-Implementierungs-Reihenfolge

### **Reihenfolge der Services:**

```
1. Doctor-Provider Service (Backend) ✅ ZUERST
   ↓
2. Patient-Customer Service (Backend) → hängt von Doctor-Provider ab
   ↓
3. Customer-FE (Frontend) → hängt von beiden Backends ab
```

**Begründung:**
- Doctor-Provider muss zuerst fertig sein, da Patient-Customer dessen APIs aufruft
- Frontend benötigt beide Backend-APIs

---

## 🏥 Teil 2: Doctor-Provider Service - Implementierungs-Schritte

### **Schritt 1: Datenbank & Modelle (Woche 1)**
- DB-Schema mit DrawIO definieren (✅ bereits gemacht)
- **Datenbank einrichten:** Flyway + JPA/Hibernate konfigurieren
  - 📖 **Siehe detaillierte Anleitung:** [`DATABASE_SETUP_GUIDE.md`](./DATABASE_SETUP_GUIDE.md)
- Domain Models in `domain/model/` implementieren
- Enums definieren (z.B. `WeekDay`, `SpecialityType`, `SlotStatus`)

### **Schritt 2: Projekt-Struktur (Hexagonal Architecture)**
```
doctor-provider/
├── domain/              # Business-Logik (Ports)
│   ├── model/          # Entities
│   ├── port/           # Interfaces (in/out)
│   └── service/        # Domain Services
├── application/         # Use Cases (Application Services)
│   └── service/
├── infrastructure/      # Adapter (out)
│   ├── adapter/
│   │   ├── persistence/  # JPA Repositories
│   │   └── rest/        # REST Controllers (in)
│   └── config/
└── openapi/            # OpenAPI Spec
```

### **Schritt 3: OpenAPI Spec definieren (Spec-First)**
- `doctor-provider-api.yaml` erstellen
- Alle Endpoints definieren:
  - Practice CRUD
  - Doctor CRUD + Speciality-Zuweisung
  - WorkingHours CRUD
  - Slot-Generation, Booking, Release
- DTOs automatisch generieren lassen (Maven Plugin)

### **Schritt 4: Domain Layer implementieren**
- **Domain Models** (bereits fertig)
- **Domain Services** für Business-Logik:
  - `WorkingHoursDomainService` → Validierung (StartTime < EndTime, Überschneidungen)
  - `SlotDomainService` → Slot-Generierung aus WorkingHours
- **Port Interfaces** definieren:
  - `PracticePort`, `DoctorPort`, `WorkingHoursPort`, `SlotPort`

### **Schritt 5: Application Layer implementieren**
- **Application Services** (Use Cases):
  - `PracticeApplicationService`
  - `DoctorApplicationService`
  - `WorkingHoursApplicationService`
  - `SlotApplicationService`
- Orchestrierung von Domain Services

### **Schritt 6: Infrastructure Layer implementieren**
- **Persistence Adapter:**
  - JPA Entities (von Domain Models trennen!)
  - **JPA Converter** erstellen (WICHTIG!)
    - **Was:** Übersetzen zwischen Datenbank-Typen und Java-Enums
    - **Warum nötig:** Datenbank speichert z.B. INT (1-7) oder PostgreSQL ENUM ('available'), aber Java verwendet type-safe Enums (Weekday.MONDAY, SlotStatus.AVAILABLE)
    - **Beispiele in diesem Projekt:**
      - `WeekdayConverter`: INT (1-7) ↔ Weekday.MONDAY/TUESDAY/...
      - `SlotStatusConverter`: PostgreSQL ENUM 'available' ↔ SlotStatus.AVAILABLE
      - `SpecialityTypeConverter`: PostgreSQL ENUM 'allgemeinmedizin' ↔ SpecialityTyp.Allgemeinmedizin
    - **Implementierung:** `@Converter(autoApply = true)` mit `AttributeConverter<JavaEnum, DBType>`
  - JPA Repositories
  - Mapper (Domain ↔ JPA Entity)
- **REST Adapter:**
  - Controller (aus OpenAPI generiert)
  - ControllerImpl (Business-Logik anbinden)

### **Schritt 7: Tests schreiben (parallel zu jedem Schritt!)**
- **Unit Tests** für Domain Services (JUnit + Mockito)
  - Nach Schritt 4 schreiben
- **Integration Tests** für REST APIs (REST Assured + TestContainers)
  - Nach Schritt 6 für jede Entity schreiben
- **E2E Tests** für komplette Flows
  - Am Ende schreiben (3-5 Szenarien)

### **Schritt 8: Error Handling & Validation**
- Global Exception Handler
- Custom Exceptions (ValidationException, NotFoundException)
- Bean Validation (@Valid, @NotNull, etc.)

### **Schritt 9: Security (siehe Teil 4)**

---

## 👤 Teil 3: Patient-Customer Service - Implementierungs-Schritte

### **Schritt 1: Datenbank & Modelle (Woche 3)**
- DB-Schema definieren
- **Datenbank einrichten:** Flyway + JPA/Hibernate konfigurieren
  - 📖 **Siehe detaillierte Anleitung:** [`DATABASE_SETUP_GUIDE.md`](./DATABASE_SETUP_GUIDE.md)
- Tabellen: `patient`, `appointment`
- Domain Models implementieren

### **Schritt 2: Projekt-Struktur (Hexagonal Architecture)**
```
patient-customer/
├── domain/
│   ├── model/          # Patient, Appointment
│   ├── port/
│   └── service/        # Domain Services
├── application/
│   └── service/
├── infrastructure/
│   ├── adapter/
│   │   ├── persistence/
│   │   ├── rest/       # REST Controllers
│   │   └── client/     # Doctor-Provider REST Client (out)
│   └── config/
└── openapi/
```

### **Schritt 3: OpenAPI Spec definieren**
- `patient-customer-api.yaml` erstellen
- Endpoints:
  - Patient CRUD
  - Appointment Booking, List, Cancel

### **Schritt 4: Doctor-Provider Integration**
- **REST Client** (outbound Port):
  - Interface: `DoctorProviderPort`
  - Implementierung: `DoctorProviderClient` (mit RestTemplate/WebClient)
  - Methoden: `getAvailableSlots()`, `bookSlot()`, `releaseSlot()`

### **Schritt 5: Domain Layer**
- Domain Models
- Domain Service: `AppointmentDomainService` (Cancellation Rules)
- Port Interfaces

### **Schritt 6: Application Layer**
- `PatientApplicationService`
- `AppointmentApplicationService` (nutzt DoctorProviderPort)

### **Schritt 7: Infrastructure Layer**
- **Persistence Adapter:**
  - JPA Entities
  - **JPA Converter** (falls Enums verwendet werden - z.B. AppointmentStatus)
  - JPA Repositories
  - Mapper (Domain ↔ JPA Entity)
- **REST Adapter:**
  - REST Controller
- **Doctor-Provider Client Implementierung** (Outbound Adapter)

### **Schritt 8: Tests**
- **Unit Tests** für Domain Services
- **Integration Tests** mit gemocktem Doctor-Provider (WireMock)
- **E2E Tests**

### **Schritt 9: Error Handling & Security**

---

## 🖥️ Teil 4: Customer-FE (Frontend) - Implementierungs-Schritte

### **Schritt 1: Projekt-Setup (Woche 5)**
- Vue 3 + TypeScript + Vite (✅ bereits erstellt)
- Dependencies installieren

### **Schritt 2: Projekt-Struktur**
```
src/
├── api/               # Backend API Clients
├── stores/            # Pinia Stores
├── views/             # Pages/Views
├── components/        # Reusable Components
├── router/            # Vue Router
└── types/             # TypeScript Interfaces
```

### **Schritt 3: API Clients erstellen**
- `doctorProviderApi.ts` → Axios/Fetch für Doctor-Provider
- `patientCustomerApi.ts` → Axios/Fetch für Patient-Customer
- TypeScript Interfaces für DTOs

### **Schritt 4: Pinia Stores**
- `useDoctorStore()` → Doctors, Specialities laden
- `useSlotStore()` → Verfügbare Slots laden
- `useAppointmentStore()` → Appointment buchen/verwalten
- `useAuthStore()` → Login/Logout (wenn Auth implementiert)

### **Schritt 5: Views/Pages implementieren**
1. **DoctorSearchView** → Ärzte nach Speciality/Stadt filtern
2. **SlotSelectionView** → Verfügbare Slots anzeigen
3. **AppointmentBookingView** → Termin buchen
4. **MyAppointmentsView** → Gebuchte Termine anzeigen/stornieren

### **Schritt 6: Components**
- `DoctorCard.vue`
- `SlotSelector.vue`
- `AppointmentCard.vue`

### **Schritt 7: Routing**
- Vue Router konfigurieren
- Navigation Guards (für Auth)

### **Schritt 8: Tests**
- **Component Tests** (Vitest + Vue Test Utils)
- **E2E Tests** (Cypress/Playwright) → optional

### **Schritt 9: Security (siehe Teil 5)**

---

## 🔒 Teil 5: Sicherheitsmaßnahmen

### **Backend (Doctor-Provider & Patient-Customer):**

#### **1. Authentication & Authorization**
- **JWT-Token** für sichere API-Zugriffe
- Spring Security konfigurieren
- Rollen: `ROLE_DOCTOR`, `ROLE_PATIENT`, `ROLE_ADMIN`
- Endpoints absichern:
  ```
  /api/slots/** → Öffentlich (lesend)
  /api/slots/{id}/book → Nur authentifizierte Patienten
  /api/doctors/** → Nur Admins/Doctors (schreibend)
  ```

#### **2. Input Validation**
- **Bean Validation** (@Valid, @NotNull, @Email, @Size)
- Custom Validators für Business-Rules
- SQL Injection Prevention (durch JPA/Hibernate)

#### **3. CORS Configuration**
- Nur Frontend-Domain erlauben
- Credentials erlauben (wenn Cookies genutzt werden)

#### **4. Rate Limiting**
- Bucket4j für API Rate Limiting
- Schutz vor Brute-Force (z.B. Login, Booking)

#### **5. HTTPS**
- SSL/TLS für alle APIs
- Produktionsumgebung: Zertifikate (Let's Encrypt)

#### **6. Secrets Management**
- **NIEMALS** Secrets in `application.properties` committen
- Umgebungsvariablen nutzen
- Spring Cloud Config oder Vault für Produktion

#### **7. Error Handling**
- **KEINE** sensiblen Daten in Error Messages
- Stack Traces nur im Development Mode

#### **8. Logging & Monitoring**
- Audit Logs für kritische Aktionen (Booking, Cancellation)
- PII (Personally Identifiable Information) anonymisieren

---

### **Frontend (Customer-FE):**

#### **1. Authentication**
- JWT-Token sicher speichern:
  - **httpOnly Cookies** (bevorzugt) ODER
  - LocalStorage (nur wenn httpOnly nicht möglich)
- Token bei jedem API-Call im Header senden

#### **2. CSRF Protection**
- CSRF-Token bei State-Changing Requests
- Spring Security CSRF aktivieren

#### **3. XSS Prevention**
- Vue.js schützt automatisch vor XSS (Template Escaping)
- **NIEMALS** `v-html` mit User-Input nutzen
- Content Security Policy (CSP) Header setzen

#### **4. Input Validation (Client-Seite)**
- Vuelidate/VeeValidate für Formular-Validierung
- **ABER:** Backend-Validierung ist Pflicht! (Client-Validierung nur UX)

#### **5. Secure API Calls**
- Axios Interceptors für Token-Handling
- Timeout setzen
- Error Handling (Netzwerkfehler, 401, 403)

#### **6. Sensitive Data**
- **KEINE** API-Keys im Frontend-Code
- Environment Variables für Base URLs

#### **7. HTTPS**
- Frontend nur über HTTPS ausliefern

#### **8. Dependencies**
- Regelmäßig `npm audit` ausführen
- Vulnerabilities fixen

---

## ✅ Test-Strategie (in jedem Service)

### **Wann testen?**
```
Feature implementieren → Tests schreiben → Nächstes Feature
```
**NICHT:** Alles implementieren, dann Tests schreiben!

### **Test-Ebenen:**

#### **1. Unit Tests**
- **Was:** Domain Services, Business-Logik
- **Tools:** JUnit 5 + Mockito + AssertJ
- **Wann:** Nach Domain Layer (Schritt 4/5)

#### **2. Integration Tests**
- **Was:** REST APIs mit echter Datenbank
- **Tools:** REST Assured + TestContainers
- **Wann:** Nach Infrastructure Layer (Schritt 6/7) - für jede Entity sofort!

#### **3. E2E Tests**
- **Was:** Komplette User-Flows
- **Tools:** REST Assured (Backend), Cypress (Frontend)
- **Wann:** Am Ende, wenn alle Entities fertig

### **Test-Reihenfolge pro Entity:**
```
1. Implementation (Domain + Application + Infrastructure)
2. Unit Tests (falls Domain Service vorhanden)
3. Integration Tests (REST APIs)
4. Nächste Entity
```

### **Coverage-Ziele:**
- Domain Services: 90-100%
- Application Services: 80-90%
- REST APIs: 80-90%
- E2E: 60-70% (kritische Flows)

---

## 📅 Zeitplan-Übersicht

| Woche | Service | Phase |
|-------|---------|-------|
| 1-2 | Doctor-Provider | DB, Domain, Application, Infrastructure |
| 2-3 | Doctor-Provider | Tests, Error Handling, Security |
| 3-4 | Patient-Customer | DB, Domain, Application, Infrastructure, Integration |
| 4 | Patient-Customer | Tests, Security |
| 5-6 | Customer-FE | Setup, API Clients, Pinia, Views |
| 6 | Customer-FE | Tests, Security |
| 7 | Alle | E2E Tests, Integration Testing, Deployment |

---

## 🎯 Wichtige Hinweise

### **Spec-First Vorteile:**
- OpenAPI Spec → DTOs automatisch generieren
- Konsistenz zwischen API-Doku und Code
- Contract Testing möglich

### **Hexagonale Architektur Prinzipien:**
- **Domain** kennt NICHTS außer sich selbst (keine Spring, keine JPA)
- **Application** orchestriert Domain Services
- **Infrastructure** implementiert Ports (Adapter)
- **Abhängigkeitsrichtung:** Immer nach innen (Infrastructure → Application → Domain)

### **Pinia Best Practices:**
- Ein Store pro Domain-Konzept (Doctor, Slot, Appointment)
- Actions für async Calls
- Getters für derived State
- State nicht direkt mutieren (außer in Actions)

---

## 🔧 Zusätzliche Tools & Bibliotheken

### **Backend:**
- **Flyway** → DB-Migrationen (EMPFOHLEN! Einfacher als Liquibase)
- Lombok → Boilerplate reduzieren
- MapStruct → Domain ↔ DTO Mapping
- Bucket4j → Rate Limiting
- Resilience4j → Circuit Breaker (für Service-Integration)

### **Frontend:**
- Axios → HTTP Client
- Vuelidate/VeeValidate → Form Validation
- TailwindCSS/Vuetify → UI Framework
- Date-fns/Day.js → Datum-Handling

---

## 📝 Checkliste pro Service

### **Doctor-Provider:**
- [ ] DB-Schema erstellt
- [ ] Domain Models implementiert
- [ ] OpenAPI Spec definiert
- [ ] Domain Services mit Tests
- [ ] Application Services
- [ ] JPA Entities erstellt
- [ ] JPA Converter erstellt (Weekday, SlotStatus, SpecialityType)
- [ ] JPA Repositories implementiert
- [ ] Persistence Adapter
- [ ] REST Controller
- [ ] Integration Tests
- [ ] E2E Tests
- [ ] Security implementiert
- [ ] Error Handling
- [ ] Dokumentation

### **Patient-Customer:**
- [ ] DB-Schema erstellt
- [ ] Domain Models implementiert
- [ ] OpenAPI Spec definiert
- [ ] Doctor-Provider Client
- [ ] Domain Services mit Tests
- [ ] Application Services
- [ ] JPA Entities erstellt
- [ ] JPA Converter erstellt (falls Enums vorhanden)
- [ ] JPA Repositories implementiert
- [ ] Persistence Adapter
- [ ] REST Controller
- [ ] Integration Tests (mit gemocktem Doctor-Provider)
- [ ] E2E Tests
- [ ] Security implementiert
- [ ] Error Handling
- [ ] Dokumentation

### **Customer-FE:**
- [ ] Projekt-Setup
- [ ] API Clients
- [ ] Pinia Stores
- [ ] Views/Pages
- [ ] Components
- [ ] Routing
- [ ] Tests
- [ ] Security (Token-Handling)
- [ ] Error Handling
- [ ] Responsive Design

---

## 🔧 Quick Reference: JPA Converter

**Wann brauche ich einen Converter?**
- Wenn Datenbank und Java unterschiedliche Typen verwenden
- Häufigster Fall: PostgreSQL ENUM oder INT ↔ Java Enum

**Code-Template:**
```java
@Converter(autoApply = true)
public class MyEnumConverter implements AttributeConverter<JavaEnum, DbType> {
    
    @Override
    public DbType convertToDatabaseColumn(JavaEnum javaEnum) {
        return javaEnum == null ? null : javaEnum.getValue();
    }
    
    @Override
    public JavaEnum convertToEntityAttribute(DbType dbValue) {
        return dbValue == null ? null : JavaEnum.fromValue(dbValue);
    }
}
```

**Verwendung in JPA Entity:**
```java
@Entity
public class MyEntity {
    @Convert(converter = MyEnumConverter.class)  // Optional bei autoApply=true
    private JavaEnum myField;
}
```

**Beispiele aus diesem Projekt:**
1. **WeekdayConverter**: `INT (1-7)` ↔ `Weekday.MONDAY`
2. **SlotStatusConverter**: `String 'available'` ↔ `SlotStatus.AVAILABLE`
3. **SpecialityTypeConverter**: `String 'allgemeinmedizin'` ↔ `SpecialityTyp.Allgemeinmedizin`

---

**Ende des Leitfadens**
