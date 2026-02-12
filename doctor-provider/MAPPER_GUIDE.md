# 🔄 Mapper-Guide: Hexagonale Architektur mit MapStruct

## 📋 Inhaltsverzeichnis
1. [Projektanalyse](#1-projektanalyse)
2. [Mapper-Positionen in der Architektur](#2-mapper-positionen-in-der-architektur)
3. [Was wird zu was gemappt?](#3-was-wird-zu-was-gemappt)
4. [Klassische Mapper (Manuell)](#4-klassische-mapper-manuell)
5. [MapStruct-Mapper](#5-mapstruct-mapper)
6. [Best Practices](#6-best-practices)

---

## 1. Projektanalyse

### 1.1 Aktuelle Projektstruktur

```
doctor-provider/
└── src/main/java/test/doctor_provider/
    ├── DoctorProviderApplication.java
    │
    ├── domain/                              # 🟢 Kern - Business-Logik
    │   ├── model/                           # Domain-Modelle (POJO, keine Annotations)
    │   │   ├── City.java
    │   │   ├── Speciality.java
    │   │   ├── Practice.java
    │   │   ├── Doctor.java
    │   │   ├── DoctorWorkingHours.java
    │   │   ├── Slot.java
    │   │   └── Page.java
    │   ├── enums/
    │   │   ├── SpecialityTyp.java
    │   │   ├── Weekday.java
    │   │   └── SlotStatus.java
    │   └── service/                         # Domain-Services (Business-Logik)
    │
    ├── application/                         # 🔵 Use Cases
    │   ├── port/
    │   │   ├── incoming/                    # Eingehende Ports (API)
    │   │   │   ├── CityIncomingPort.java
    │   │   │   ├── SpecialityIncomingPort.java
    │   │   │   ├── PracticeIncomingPort.java
    │   │   │   ├── DoctorIncomingPort.java
    │   │   │   ├── WorkingHoursIncomingPort.java
    │   │   │   └── SlotIncomingPort.java
    │   │   └── outgoing/                    # Ausgehende Ports (Persistence)
    │   │       ├── CityOutgoingPort.java
    │   │       ├── SpecialityOutgoingPort.java
    │   │       ├── PracticeOutgoingPort.java
    │   │       ├── DoctorOutgoingPort.java
    │   │       ├── WorkingHoursOutgoingPort.java
    │   │       └── SlotOutgoingPort.java
    │   └── service/                         # Application Services (Use Case Implementierung)
    │
    └── infrastructure/                      # 🟡 Adapter - Technische Details
        ├── adapter/
        │   ├── incomming/                   # ⚠️ Typo: sollte "incoming" sein
        │   │   └── web/                     # REST-Controller (NOCH NICHT VORHANDEN)
        │   │       ├── CityController.java
        │   │       ├── SpecialityController.java
        │   │       ├── PracticeController.java
        │   │       ├── DoctorController.java
        │   │       ├── WorkingHoursController.java
        │   │       ├── SlotController.java
        │   │       └── mapper/              # ⭐ WEB-MAPPER (API-DTOs ↔ Domain)
        │   │           ├── CityWebMapper.java
        │   │           ├── SpecialityWebMapper.java
        │   │           ├── PracticeWebMapper.java
        │   │           ├── DoctorWebMapper.java
        │   │           ├── WorkingHoursWebMapper.java
        │   │           ├── SlotWebMapper.java
        │   │           └── PageWebMapper.java
        │   │
        │   └── outgoing/
        │       └── persistence/             # JPA-Adapter (NOCH NICHT VORHANDEN)
        │           ├── CityPersistenceAdapter.java
        │           ├── SpecialityPersistenceAdapter.java
        │           ├── PracticePersistenceAdapter.java
        │           ├── DoctorPersistenceAdapter.java
        │           ├── WorkingHoursPersistenceAdapter.java
        │           ├── SlotPersistenceAdapter.java
        │           ├── entity/              # JPA-Entities
        │           │   ├── CityEntity.java
        │           │   ├── SpecialityEntity.java
        │           │   ├── PracticeEntity.java
        │           │   ├── DoctorEntity.java
        │           │   ├── WorkingHoursEntity.java
        │           │   └── SlotEntity.java
        │           ├── repository/          # Spring Data JPA Repositories
        │           │   ├── CityJpaRepository.java
        │           │   ├── SpecialityJpaRepository.java
        │           │   ├── PracticeJpaRepository.java
        │           │   ├── DoctorJpaRepository.java
        │           │   ├── WorkingHoursJpaRepository.java
        │           │   └── SlotJpaRepository.java
        │           └── mapper/              # ⭐ PERSISTENCE-MAPPER (JPA-Entity ↔ Domain)
        │               ├── CityEntityMapper.java
        │               ├── SpecialityEntityMapper.java
        │               ├── PracticeEntityMapper.java
        │               ├── DoctorEntityMapper.java
        │               ├── WorkingHoursEntityMapper.java
        │               ├── SlotEntityMapper.java
        │               └── PageEntityMapper.java
        │
        └── persistence/                     # LEER (sollte gelöscht werden)
```

### 1.2 OpenAPI-Generierung (Spec-First)

**Generierte API-Interfaces und DTOs:**
```
target/generated-sources/openapi/
└── test/doctor_provider/
    ├── api/                                 # Generierte REST-Interfaces
    │   ├── CitiesApi.java
    │   ├── SpecialitiesApi.java
    │   ├── PracticesApi.java
    │   ├── DoctorsApi.java
    │   ├── WorkingHoursApi.java
    │   └── SlotsApi.java
    └── api/model/                           # Generierte API-DTOs
        ├── CityDto.java
        ├── SpecialityDto.java
        ├── PracticeDto.java
        ├── CreatePracticeRequest.java
        ├── UpdatePracticeRequest.java
        ├── DoctorDto.java
        ├── CreateDoctorRequest.java
        ├── UpdateDoctorRequest.java
        ├── WorkingHoursDto.java
        ├── CreateWorkingHoursRequest.java
        ├── UpdateWorkingHoursRequest.java
        ├── SlotDto.java
        ├── GenerateSlotsRequest.java
        ├── UpdateSlotRequest.java
        ├── PageResponse.java
        └── ErrorResponse.java
```

---

## 2. Mapper-Positionen in der Architektur

### 2.1 Übersicht: Zwei Arten von Mappern

In der Hexagonalen Architektur benötigst du **ZWEI verschiedene Mapper-Typen**:

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND (Vue.js)                        │
└─────────────────────────────────────────────────────────────────┘
                              ▲ │
                    JSON      │ │ JSON
                    (HTTP)    │ │ (HTTP)
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│           🟡 INFRASTRUCTURE - Incoming Adapter (Web)            │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  REST-Controller (CitiesApi implementiert)               │  │
│  │  - CityController.java                                    │  │
│  │  - PracticeController.java                                │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ▲ │                                │
│              ⭐ MAPPER 1     │ │                                │
│              (API-DTO ↔ Domain-Modell)                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  📁 infrastructure/adapter/incomming/web/mapper/          │  │
│  │  - CityWebMapper.java                                     │  │
│  │  - PracticeWebMapper.java                                 │  │
│  │  - DoctorWebMapper.java                                   │  │
│  │  - SlotWebMapper.java                                     │  │
│  │  - PageWebMapper.java                                     │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                  🔵 APPLICATION - Use Cases                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Application Services (Orchestrierung)                    │  │
│  │  - CityService.java                                       │  │
│  │  - PracticeService.java                                   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                 🟢 DOMAIN - Business-Logik (Kern)               │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Domain-Modelle (POJOs, keine Annotations)               │  │
│  │  - City.java                                              │  │
│  │  - Practice.java                                          │  │
│  │  - Doctor.java                                            │  │
│  │  - Slot.java                                              │  │
│  │  - Page<T>.java                                           │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                  🔵 APPLICATION - Ports                         │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Outgoing Ports (Interfaces für Persistence)             │  │
│  │  - CityOutgoingPort.java                                  │  │
│  │  - PracticeOutgoingPort.java                              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│           🟡 INFRASTRUCTURE - Outgoing Adapter (DB)             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Persistence Adapter (OutgoingPort implementiert)         │  │
│  │  - CityPersistenceAdapter.java                            │  │
│  │  - PracticePersistenceAdapter.java                        │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ▲ │                                │
│              ⭐ MAPPER 2     │ │                                │
│              (Domain-Modell ↔ JPA-Entity)                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  📁 infrastructure/adapter/outgoing/persistence/mapper/   │  │
│  │  - CityEntityMapper.java                                  │  │
│  │  - PracticeEntityMapper.java                              │  │
│  │  - DoctorEntityMapper.java                                │  │
│  │  - SlotEntityMapper.java                                  │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ▲ │                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  JPA Entities (mit @Entity, @Table, etc.)                │  │
│  │  - CityEntity.java                                        │  │
│  │  - PracticeEntity.java                                    │  │
│  └───────────────────────────────────────────────────────────┘  │
│                              ▲ │                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Spring Data JPA Repositories                             │  │
│  │  - CityJpaRepository.java                                 │  │
│  │  - PracticeJpaRepository.java                             │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                 DATABASE (PostgreSQL + Flyway)                  │
│  - city (Tabelle)                                               │
│  - practice (Tabelle)                                           │
│  - doctor (Tabelle)                                             │
│  - slot (Tabelle)                                               │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Wo werden Mapper erstellt?

#### ⭐ MAPPER 1: Web-Mapper (API-Layer)
**Position:** `infrastructure/adapter/incomming/web/mapper/`

**Zweck:** Konvertierung zwischen **API-DTOs** (generiert von OpenAPI) und **Domain-Modellen**

**Dateien:**
```
infrastructure/adapter/incomming/web/mapper/
├── CityWebMapper.java              # CityDto ↔ City
├── SpecialityWebMapper.java        # SpecialityDto ↔ Speciality
├── PracticeWebMapper.java          # PracticeDto/CreatePracticeRequest ↔ Practice
├── DoctorWebMapper.java            # DoctorDto/CreateDoctorRequest ↔ Doctor
├── WorkingHoursWebMapper.java      # WorkingHoursDto ↔ DoctorWorkingHours
├── SlotWebMapper.java              # SlotDto ↔ Slot
└── PageWebMapper.java              # PageResponse ↔ Page<T>
```

#### ⭐ MAPPER 2: Entity-Mapper (Persistence-Layer)
**Position:** `infrastructure/adapter/outgoing/persistence/mapper/`

**Zweck:** Konvertierung zwischen **Domain-Modellen** und **JPA-Entities**

**Dateien:**
```
infrastructure/adapter/outgoing/persistence/mapper/
├── CityEntityMapper.java           # City ↔ CityEntity
├── SpecialityEntityMapper.java     # Speciality ↔ SpecialityEntity
├── PracticeEntityMapper.java       # Practice ↔ PracticeEntity
├── DoctorEntityMapper.java         # Doctor ↔ DoctorEntity
├── WorkingHoursEntityMapper.java   # DoctorWorkingHours ↔ WorkingHoursEntity
└── SlotEntityMapper.java           # Slot ↔ SlotEntity
```

---

## 3. Was wird zu was gemappt?

### 3.1 Mapping-Übersicht: City (Beispiel)

```
┌──────────────────────┐         ┌──────────────────────┐         ┌──────────────────────┐
│    API-DTO           │         │   Domain-Modell      │         │    JPA-Entity        │
│  (OpenAPI-generiert) │         │   (Business-Logik)   │         │   (Datenbank-Layer)  │
├──────────────────────┤         ├──────────────────────┤         ├──────────────────────┤
│ CityDto              │◄───────►│ City                 │◄───────►│ CityEntity           │
│ - id: UUID           │   Web   │ - id: UUID           │  Entity │ - id: UUID           │
│ - name: String       │  Mapper │ - name: String       │  Mapper │ - name: String       │
│ - postalCode: String │         │ - postalCode: String │         │ - zipCode: String    │
└──────────────────────┘         └──────────────────────┘         └──────────────────────┘
                                                                    │ + @Entity            │
                                                                    │ + @Table(name="city")│
                                                                    │ + @Column(...)       │
                                                                    └──────────────────────┘
```

**Mapping-Regeln:**

| Mapper-Typ | Von | Nach | Besonderheiten |
|------------|-----|------|----------------|
| **CityWebMapper** | `CityDto` | `City` | - Einfaches 1:1 Mapping<br>- `postalCode` → `postalCode` |
| **CityEntityMapper** | `City` | `CityEntity` | - **Feld-Umbenennung:** `postalCode` → `zipCode`<br>- JPA-Annotations ignorieren |

### 3.2 Mapping-Übersicht: Practice (komplexeres Beispiel)

```
┌────────────────────────────┐      ┌──────────────────────┐      ┌────────────────────────────┐
│ CreatePracticeRequest      │      │   Practice           │      │    PracticeEntity          │
│ (API INPUT)                │      │   (Domain)           │      │    (JPA)                   │
├────────────────────────────┤      ├──────────────────────┤      ├────────────────────────────┤
│ - name: String             │──┐   │ - id: UUID           │      │ - id: UUID                 │
│ - street: String           │  │   │ - name: String       │      │ - name: String             │
│ - houseNumber: String      │  └──►│ - street: String     │◄────►│ - street: String           │
│ - phone: String            │      │ - houseNumber: String│      │ - houseNumber: String      │
│ - email: String (email)    │      │ - phone: String      │      │ - phone: String            │
│ - postalCode: String       │      │ - email: String      │      │ - email: String            │
│ - cityId: UUID             │      │ - postalCode: String │      │ - postalCode: String       │
└────────────────────────────┘      │ - cityId: UUID       │      │ - city: CityEntity (@ManyToOne) │
                                    └──────────────────────┘      └────────────────────────────┘
┌────────────────────────────┐                                    
│ PracticeDto                │                                    
│ (API OUTPUT)               │                                    
├────────────────────────────┤                                    
│ - id: UUID                 │                                    
│ - name: String             │◄───── Wird vom Domain-Modell       
│ - street: String           │       generiert (WebMapper)        
│ - houseNumber: String      │                                    
│ - phone: String            │                                    
│ - email: String            │                                    
│ - postalCode: String       │                                    
│ - cityId: UUID             │                                    
└────────────────────────────┘                                    
```

**Mapping-Regeln:**

| Mapper-Typ | Von | Nach | Besonderheiten |
|------------|-----|------|----------------|
| **PracticeWebMapper** | `CreatePracticeRequest` | `Practice` | - Kein `id` (wird von DB generiert)<br>- Email-Validierung (bereits in OpenAPI) |
| **PracticeWebMapper** | `Practice` | `PracticeDto` | - Alle Felder 1:1<br>- `id` ist vorhanden |
| **PracticeEntityMapper** | `Practice` | `PracticeEntity` | - **Beziehungs-Mapping:**<br>`cityId` (UUID) → `city` (CityEntity-Objekt)<br>- Lazy Loading beachten |
| **PracticeEntityMapper** | `PracticeEntity` | `Practice` | - **Beziehungs-Auflösung:**<br>`city.getId()` → `cityId` |

### 3.3 Mapping-Übersicht: Doctor (n:m Beziehung)

```
┌────────────────────────────┐      ┌──────────────────────────┐      ┌────────────────────────────┐
│ CreateDoctorRequest        │      │   Doctor                 │      │    DoctorEntity            │
│ (API INPUT)                │      │   (Domain)               │      │    (JPA)                   │
├────────────────────────────┤      ├──────────────────────────┤      ├────────────────────────────┤
│ - firstName: String        │──┐   │ - id: UUID               │      │ - id: UUID                 │
│ - lastName: String         │  └──►│ - firstName: String      │◄────►│ - firstName: String        │
│ - practiceId: UUID (opt)   │      │ - lastName: String       │      │ - lastName: String         │
│ - specialityIds: [UUID]    │      │ - practiceId: UUID       │      │ - practice: PracticeEntity │
│   (optional)               │      │ - specialityIds: Set<UUID>│     │   (@ManyToOne, optional)   │
└────────────────────────────┘      └──────────────────────────┘      │ - specialities: Set<       │
                                                                        │     SpecialityEntity>      │
                                                                        │   (@ManyToMany)            │
                                                                        │ - doctorSpecialities:      │
                                                                        │     Join-Table             │
                                                                        └────────────────────────────┘
```

**Mapping-Regeln:**

| Mapper-Typ | Von | Nach | Besonderheiten |
|------------|-----|------|----------------|
| **DoctorWebMapper** | `CreateDoctorRequest` | `Doctor` | - `specialityIds` kann `null` oder leer sein<br>- `practiceId` kann `null` sein |
| **DoctorEntityMapper** | `Doctor` | `DoctorEntity` | - **n:1:** `practiceId` → `practice` (Entity laden oder lazy)<br>- **n:m:** `specialityIds` → `specialities` (Set von Entities) |
| **DoctorEntityMapper** | `DoctorEntity` | `Doctor` | - `practice?.getId()` → `practiceId`<br>- `specialities.map(s → s.getId())` → `specialityIds` |

### 3.4 Mapping-Übersicht: Slot (mit Datum/Zeit)

```
┌────────────────────────────┐      ┌──────────────────────────┐      ┌────────────────────────────┐
│ SlotDto                    │      │   Slot                   │      │    SlotEntity              │
│ (API)                      │      │   (Domain)               │      │    (JPA)                   │
├────────────────────────────┤      ├──────────────────────────┤      ├────────────────────────────┤
│ - id: UUID                 │      │ - id: UUID               │      │ - id: UUID                 │
│ - workingHoursId: UUID     │◄────►│ - workingHoursId: UUID   │◄────►│ - workingHours: Working    │
│ - startTime: ZonedDateTime │      │ - startTime: ZonedDateTime│     │     HoursEntity            │
│ - endTime: ZonedDateTime   │      │ - endTime: ZonedDateTime │      │   (@ManyToOne)             │
│ - status: SlotStatus       │      │ - status: SlotStatus     │      │ - startTime: ZonedDateTime │
│   (ENUM)                   │      │   (ENUM)                 │      │ - endTime: ZonedDateTime   │
└────────────────────────────┘      └──────────────────────────┘      │ - status: String (@Enum)   │
                                                                        └────────────────────────────┘
```

**Mapping-Regeln:**

| Mapper-Typ | Von | Nach | Besonderheiten |
|------------|-----|------|----------------|
| **SlotWebMapper** | `SlotDto` | `Slot` | - Enum-Mapping (direkt)<br>- ZonedDateTime bleibt gleich |
| **SlotEntityMapper** | `Slot` | `SlotEntity` | - `workingHoursId` → `workingHours` (Entity)<br>- `SlotStatus` (Enum) → `@Enumerated(STRING)` |

### 3.5 Mapping-Übersicht: Page (Generisches Mapping)

```
┌────────────────────────────┐      ┌──────────────────────────┐
│ PageResponse               │      │   Page<T>                │
│ (API, generiert)           │      │   (Domain)               │
├────────────────────────────┤      ├──────────────────────────┤
│ - items: List<?>           │◄────►│ - items: List<T>         │
│ - page: int                │      │ - page: int              │
│ - size: int                │      │ - size: int              │
│ - totalElements: long      │      │ - totalElements: long    │
│ - totalPages: int          │      │ - totalPages: int        │
└────────────────────────────┘      └──────────────────────────┘
```

**Besonderheit:** Generisches Mapping mit Type-Parameter!

---

## 4. Klassische Mapper (Manuell)

### 4.1 Schritt-für-Schritt: CityWebMapper (Manuell)

**Schritt 1: Interface erstellen**

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import test.doctor_provider.api.model.CityDto;
import test.doctor_provider.domain.model.City;

import java.util.List;

/**
 * Manueller Mapper: API-DTO ↔ Domain-Modell
 * 
 * ZWECK:
 * - Konvertiert zwischen OpenAPI-generierten DTOs und Domain-Modellen
 * - Wird in REST-Controllern verwendet
 * 
 * RICHTUNG:
 * - CityDto → City (für INPUT, falls benötigt - hier READ-ONLY)
 * - City → CityDto (für OUTPUT - Hauptverwendung)
 */
public interface CityWebMapper {
    
    /**
     * Domain → API-DTO
     * Wird verwendet in: GET /api/v1/cities (Response)
     */
    CityDto toDto(City city);
    
    /**
     * Bulk-Konvertierung für Listen
     */
    List<CityDto> toDtoList(List<City> cities);
}
```

**Schritt 2: Implementierung erstellen**

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.api.model.CityDto;
import test.doctor_provider.domain.model.City;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manuelle Implementierung des CityWebMappers.
 * 
 * ⚠️ WICHTIG: @Component für Spring Dependency Injection
 */
@Component
public class CityWebMapperImpl implements CityWebMapper {

    @Override
    public CityDto toDto(City city) {
        if (city == null) {
            return null;
        }
        
        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setPostalCode(city.getPostalCode());
        
        return dto;
    }

    @Override
    public List<CityDto> toDtoList(List<City> cities) {
        if (cities == null) {
            return null;
        }
        
        return cities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
```

### 4.2 Schritt-für-Schritt: PracticeWebMapper (komplexer)

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.api.model.CreatePracticeRequest;
import test.doctor_provider.api.model.PracticeDto;
import test.doctor_provider.api.model.UpdatePracticeRequest;
import test.doctor_provider.domain.model.Practice;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PracticeWebMapperImpl implements PracticeWebMapper {

    /**
     * CreateRequest → Domain (für POST)
     * ⚠️ Kein ID-Mapping (wird von DB generiert)
     */
    @Override
    public Practice toDomain(CreatePracticeRequest request) {
        if (request == null) {
            return null;
        }
        
        Practice practice = new Practice();
        // Kein ID! (wird von DB generiert)
        practice.setName(request.getName());
        practice.setStreet(request.getStreet());
        practice.setHouseNumber(request.getHouseNumber());
        practice.setPhone(request.getPhone());
        practice.setEmail(request.getEmail());
        practice.setPostalCode(request.getPostalCode());
        practice.setCityId(request.getCityId());
        
        return practice;
    }

    /**
     * UpdateRequest → Domain (für PUT)
     * ⚠️ ID wird separat übergeben (aus Path-Parameter)
     */
    @Override
    public Practice toDomain(UpdatePracticeRequest request, UUID id) {
        if (request == null) {
            return null;
        }
        
        Practice practice = new Practice();
        practice.setId(id);  // ← ID aus Path-Parameter
        practice.setName(request.getName());
        practice.setStreet(request.getStreet());
        practice.setHouseNumber(request.getHouseNumber());
        practice.setPhone(request.getPhone());
        practice.setEmail(request.getEmail());
        practice.setPostalCode(request.getPostalCode());
        practice.setCityId(request.getCityId());
        
        return practice;
    }

    /**
     * Domain → DTO (für GET/POST/PUT Response)
     */
    @Override
    public PracticeDto toDto(Practice practice) {
        if (practice == null) {
            return null;
        }
        
        PracticeDto dto = new PracticeDto();
        dto.setId(practice.getId());
        dto.setName(practice.getName());
        dto.setStreet(practice.getStreet());
        dto.setHouseNumber(practice.getHouseNumber());
        dto.setPhone(practice.getPhone());
        dto.setEmail(practice.getEmail());
        dto.setPostalCode(practice.getPostalCode());
        dto.setCityId(practice.getCityId());
        
        return dto;
    }

    @Override
    public List<PracticeDto> toDtoList(List<Practice> practices) {
        if (practices == null) {
            return null;
        }
        
        return practices.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
```

### 4.3 Schritt-für-Schritt: CityEntityMapper (Persistence)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.City;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manueller Entity-Mapper: Domain ↔ JPA-Entity
 *
 * WICHTIG:
 * - Feld-Umbenennung: postalCode ↔ zipCode
 * - JPA-Annotationen ignorieren
 */
@Component
public class CityEntityMapperImpl implements CityEntityMapper {

    /**
     * Domain → Entity (für save/update)
     */
    @Override
    public CityEntity toEntity(City city) {
        if (city == null) {
            return null;
        }

        CityEntity entity = new CityEntity();
        entity.setId(city.getId());
        entity.setName(city.getName());
        entity.setZipCode(city.getPostalCode());  // ← Feld-Umbenennung!

        return entity;
    }

    /**
     * Entity → Domain (für find/load)
     */
    @Override
    public City toDomain(CityEntity entity) {
        if (entity == null) {
            return null;
        }

        City city = new City();
        city.setId(entity.getId());
        city.setName(entity.getName());
        city.setPostalCode(entity.getZipCode());  // ← Feld-Umbenennung!

        return city;
    }

    @Override
    public List<City> toDomainList(List<CityEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityEntity> toEntityList(List<City> cities) {
        if (cities == null) {
            return null;
        }

        return cities.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
```

### 4.4 Schritt-für-Schritt: PracticeEntityMapper (mit Beziehungen)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity-Mapper mit Beziehungen.
 *
 * WICHTIG:
 * - cityId (UUID) → city (CityEntity-Objekt)
 * - Lazy Loading vs. Eager Loading beachten
 */
@Component
public class PracticeEntityMapperImpl implements PracticeEntityMapper {

    /**
     * Domain → Entity
     * ⚠️ cityId wird zu CityEntity-Referenz
     */
    @Override
    public PracticeEntity toEntity(Practice practice) {
        if (practice == null) {
            return null;
        }

        PracticeEntity entity = new PracticeEntity();
        entity.setId(practice.getId());
        entity.setName(practice.getName());
        entity.setStreet(practice.getStreet());
        entity.setHouseNumber(practice.getHouseNumber());
        entity.setPhone(practice.getPhone());
        entity.setEmail(practice.getEmail());
        entity.setPostalCode(practice.getPostalCode());

        // ⚠️ WICHTIG: CityEntity-Referenz erstellen
        if (practice.getCityId() != null) {
            CityEntity cityEntity = new CityEntity();
            cityEntity.setId(practice.getCityId());
            entity.setCity(cityEntity);  // JPA wird die Referenz auflösen
        }

        return entity;
    }

    /**
     * Entity → Domain
     * ⚠️ CityEntity → cityId (nur ID extrahieren)
     */
    @Override
    public Practice toDomain(PracticeEntity entity) {
        if (entity == null) {
            return null;
        }

        Practice practice = new Practice();
        practice.setId(entity.getId());
        practice.setName(entity.getName());
        practice.setStreet(entity.getStreet());
        practice.setHouseNumber(entity.getHouseNumber());
        practice.setPhone(entity.getPhone());
        practice.setEmail(entity.getEmail());
        practice.setPostalCode(entity.getPostalCode());

        // ⚠️ WICHTIG: Aus CityEntity nur die ID extrahieren
        if (entity.getCity() != null) {
            practice.setCityId(entity.getCity().getId());
        }

        return practice;
    }

    @Override
    public List<Practice> toDomainList(List<PracticeEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
```

### 4.5 PageWebMapper (Generisch)

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.api.model.PageResponse;
import test.doctor_provider.domain.model.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Generischer Page-Mapper.
 * 
 * VERWENDUNG:
 * Page<City> → PageResponse mit CityDto items
 * 
 * ⚠️ WICHTIG: Benötigt Item-Mapper als Parameter!
 */
@Component
public class PageWebMapperImpl implements PageWebMapper {

    /**
     * Konvertiert Page<T> → PageResponse
     * 
     * @param page Domain-Page
     * @param itemMapper Funktion zum Konvertieren der Items (z.B. city -> cityDto)
     * @param <T> Domain-Typ (z.B. City)
     * @param <D> DTO-Typ (z.B. CityDto)
     * @return PageResponse mit konvertierten Items
     */
    @Override
    public <T, D> PageResponse toPageResponse(Page<T> page, Function<T, D> itemMapper) {
        if (page == null) {
            return null;
        }
        
        PageResponse response = new PageResponse();
        
        // Metadaten kopieren
        response.setPage(page.getPage());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        
        // Items konvertieren
        List<D> dtoItems = page.getItems().stream()
                .map(itemMapper)
                .toList();
        response.setItems(dtoItems);
        
        return response;
    }
}
```

**Verwendung im Controller:**

```java
@RestController
public class CityController implements CitiesApi {
    
    private final CityIncomingPort cityPort;
    private final CityWebMapper cityMapper;
    private final PageWebMapper pageMapper;
    
    @Override
    public Mono<ResponseEntity<PageResponse>> findAllCities(...) {
        Page<City> cityPage = cityPort.getAllCities(name, postalCode, page, size);
        
        // Generisches Page-Mapping mit Item-Mapper
        PageResponse response = pageMapper.toPageResponse(
            cityPage,
            cityMapper::toDto  // ← Methoden-Referenz als Funktion
        );
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
```

---

## 5. MapStruct-Mapper

### 5.1 Maven-Konfiguration

**Bereits in `pom.xml` vorhanden:**

```xml
<dependencies>
    <!-- MapStruct für Mapper -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**⚠️ WICHTIG: Compiler-Plugin aktualisieren!**

Aktuell fehlt MapStruct im `annotationProcessorPaths`. Muss ergänzt werden:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <!-- Lombok ZUERST -->
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.30</version>
            </path>
            <!-- MapStruct DANACH -->
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

**⚠️ REIHENFOLGE:** Lombok VOR MapStruct! Sonst funktioniert Lombok in MapStruct-Mappern nicht.

### 5.2 CityWebMapper mit MapStruct

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.api.model.CityDto;
import test.doctor_provider.domain.model.City;

import java.util.List;

/**
 * MapStruct-Mapper: API-DTO ↔ Domain
 * 
 * ⚠️ WICHTIG:
 * - componentModel = "spring" → Generiert @Component (Spring Bean)
 * - Interface OHNE Implementierung
 * - MapStruct generiert Code zur Compile-Zeit
 */
@Mapper(componentModel = "spring")
public interface CityWebMapper {
    
    /**
     * Domain → DTO
     * ⚠️ Automatisches 1:1 Mapping (gleiche Feldnamen)
     */
    CityDto toDto(City city);
    
    /**
     * Bulk-Konvertierung
     * ⚠️ MapStruct generiert automatisch die Implementierung
     */
    List<CityDto> toDtoList(List<City> cities);
}
```

**Generierter Code (automatisch in `target/generated-sources/annotations/`):**

```java
@Component
public class CityWebMapperImpl implements CityWebMapper {
    
    @Override
    public CityDto toDto(City city) {
        if (city == null) {
            return null;
        }
        
        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setPostalCode(city.getPostalCode());
        
        return dto;
    }
    
    @Override
    public List<CityDto> toDtoList(List<City> cities) {
        if (cities == null) {
            return null;
        }
        
        List<CityDto> list = new ArrayList<>(cities.size());
        for (City city : cities) {
            list.add(toDto(city));
        }
        return list;
    }
}
```

### 5.3 CityEntityMapper mit MapStruct (Feld-Umbenennung)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.domain.model.City;

import java.util.List;

/**
 * MapStruct Entity-Mapper mit Feld-Umbenennung.
 *
 * ⚠️ @Mapping für unterschiedliche Feldnamen:
 * - postalCode (Domain) ↔ zipCode (Entity)
 */
@Mapper(componentModel = "spring")
public interface CityEntityMapper {

    /**
     * Domain → Entity
     * ⚠️ postalCode → zipCode (Feld-Umbenennung)
     */
    @Mapping(source = "postalCode", target = "zipCode")
    CityEntity toEntity(City city);

    /**
     * Entity → Domain
     * ⚠️ zipCode → postalCode (Feld-Umbenennung)
     */
    @Mapping(source = "zipCode", target = "postalCode")
    City toDomain(CityEntity entity);

    List<City> toDomainList(List<CityEntity> entities);

    List<CityEntity> toEntityList(List<City> cities);
}
```

### 5.4 PracticeWebMapper mit MapStruct

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.api.model.CreatePracticeRequest;
import test.doctor_provider.api.model.PracticeDto;
import test.doctor_provider.api.model.UpdatePracticeRequest;
import test.doctor_provider.domain.model.Practice;

import java.util.List;
import java.util.UUID;

/**
 * MapStruct Web-Mapper für Practice.
 * 
 * Besonderheiten:
 * - CreateRequest hat kein ID (wird ignoriert)
 * - UpdateRequest benötigt ID als separaten Parameter
 */
@Mapper(componentModel = "spring")
public interface PracticeWebMapper {
    
    /**
     * CreateRequest → Domain
     * ⚠️ ID wird ignoriert (wird von DB generiert)
     */
    @Mapping(target = "id", ignore = true)
    Practice toDomain(CreatePracticeRequest request);
    
    /**
     * UpdateRequest → Domain
     * ⚠️ ID wird aus separatem Parameter gesetzt
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "request.name", target = "name")
    @Mapping(source = "request.street", target = "street")
    @Mapping(source = "request.houseNumber", target = "houseNumber")
    @Mapping(source = "request.phone", target = "phone")
    @Mapping(source = "request.email", target = "email")
    @Mapping(source = "request.postalCode", target = "postalCode")
    @Mapping(source = "request.cityId", target = "cityId")
    Practice toDomain(UpdatePracticeRequest request, UUID id);
    
    /**
     * Domain → DTO
     */
    PracticeDto toDto(Practice practice);
    
    List<PracticeDto> toDtoList(List<Practice> practices);
}
```

### 5.5 PracticeEntityMapper mit MapStruct (Beziehungen)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntity;

import java.util.List;

/**
 * MapStruct Entity-Mapper mit Beziehungen.
 * 
 * WICHTIG:
 * - cityId (UUID) ↔ city.id (CityEntity)
 * - MapStruct kann Beziehungen NICHT automatisch auflösen
 * - Benötigt @AfterMapping für manuelle Beziehungs-Logik
 */
@Mapper(componentModel = "spring")
public interface PracticeEntityMapper {
    
    /**
     * Domain → Entity
     * ⚠️ cityId → city.id (Beziehungs-Mapping)
     */
    @Mapping(source = "cityId", target = "city.id")
    PracticeEntity toEntity(Practice practice);
    
    /**
     * Entity → Domain
     * ⚠️ city.id → cityId (ID-Extraktion)
     */
    @Mapping(source = "city.id", target = "cityId")
    Practice toDomain(PracticeEntity entity);
    
    List<Practice> toDomainList(List<PracticeEntity> entities);
    List<PracticeEntity> toEntityList(List<Practice> practices);
}
```

### 5.6 DoctorEntityMapper (n:m Beziehung mit MapStruct)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.DoctorEntity;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.SpecialityEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MapStruct Entity-Mapper mit n:m Beziehung.
 * 
 * Besonderheiten:
 * - specialityIds (Set<UUID>) ↔ specialities (Set<SpecialityEntity>)
 * - practiceId (UUID) ↔ practice (PracticeEntity)
 * - Custom Mappings mit @Named-Methoden
 */
@Mapper(componentModel = "spring")
public interface DoctorEntityMapper {
    
    /**
     * Domain → Entity
     */
    @Mapping(source = "practiceId", target = "practice.id")
    @Mapping(source = "specialityIds", target = "specialities", qualifiedByName = "idsToEntities")
    DoctorEntity toEntity(Doctor doctor);
    
    /**
     * Entity → Domain
     */
    @Mapping(source = "practice.id", target = "practiceId")
    @Mapping(source = "specialities", target = "specialityIds", qualifiedByName = "entitiesToIds")
    Doctor toDomain(DoctorEntity entity);
    
    List<Doctor> toDomainList(List<DoctorEntity> entities);
    
    /**
     * Custom Mapping: Set<UUID> → Set<SpecialityEntity>
     */
    @Named("idsToEntities")
    default Set<SpecialityEntity> idsToEntities(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }
        return ids.stream()
                .map(id -> {
                    SpecialityEntity entity = new SpecialityEntity();
                    entity.setId(id);
                    return entity;
                })
                .collect(Collectors.toSet());
    }
    
    /**
     * Custom Mapping: Set<SpecialityEntity> → Set<UUID>
     */
    @Named("entitiesToIds")
    default Set<UUID> entitiesToIds(Set<SpecialityEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptySet();
        }
        return entities.stream()
                .map(SpecialityEntity::getId)
                .collect(Collectors.toSet());
    }
}
```

### 5.7 SlotWebMapper mit MapStruct (Enums)

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.api.model.SlotDto;
import test.doctor_provider.domain.model.Slot;

import java.util.List;

/**
 * MapStruct Slot-Mapper.
 * 
 * ⚠️ Enums werden automatisch gemappt (gleicher Name)
 * ⚠️ ZonedDateTime wird automatisch kopiert
 */
@Mapper(componentModel = "spring")
public interface SlotWebMapper {
    
    SlotDto toDto(Slot slot);
    Slot toDomain(SlotDto dto);
    
    List<SlotDto> toDtoList(List<Slot> slots);
}
```

### 5.8 PageWebMapper mit MapStruct (Generisch)

```java
package test.doctor_provider.infrastructure.adapter.incomming.web.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import test.doctor_provider.api.model.PageResponse;
import test.doctor_provider.domain.model.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Page-Mapper für MapStruct.
 * 
 * ⚠️ WICHTIG: MapStruct kann KEINE generischen Mappings
 * → Muss als @Component mit default-Methode implementiert werden
 */
@Component
public class PageWebMapper {
    
    /**
     * Generisches Page-Mapping
     * 
     * @param page Domain-Page
     * @param itemMapper Funktion für Item-Konvertierung
     * @return PageResponse mit konvertierten Items
     */
    public <T, D> PageResponse toPageResponse(Page<T> page, Function<T, D> itemMapper) {
        if (page == null) {
            return null;
        }
        
        PageResponse response = new PageResponse();
        response.setPage(page.getPage());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        
        List<D> items = page.getItems().stream()
                .map(itemMapper)
                .toList();
        response.setItems(items);
        
        return response;
    }
}
```

---

## 6. Best Practices

### 6.1 Naming Conventions

| Mapper-Typ | Package | Naming | Beispiel |
|------------|---------|--------|----------|
| **Web-Mapper** | `infrastructure/adapter/incomming/web/mapper/` | `{Entity}WebMapper` | `CityWebMapper` |
| **Entity-Mapper** | `infrastructure/adapter/outgoing/persistence/mapper/` | `{Entity}EntityMapper` | `CityEntityMapper` |
| **Page-Mapper** | `infrastructure/adapter/incomming/web/mapper/` | `PageWebMapper` | `PageWebMapper` |

### 6.2 Dependency Injection

**Verwendung in Controllern:**

```java
@RestController
@RequiredArgsConstructor  // Lombok Constructor Injection
public class CityController implements CitiesApi {
    
    private final CityIncomingPort cityPort;
    private final CityWebMapper cityMapper;      // ← MapStruct generiert @Component
    private final PageWebMapper pageMapper;
    
    @Override
    public Mono<ResponseEntity<PageResponse>> findAllCities(...) {
        Page<City> cityPage = cityPort.getAllCities(name, postalCode, page, size);
        PageResponse response = pageMapper.toPageResponse(cityPage, cityMapper::toDto);
        return Mono.just(ResponseEntity.ok(response));
    }
}
```

**Verwendung in Persistence-Adaptern:**

```java
@Component
@RequiredArgsConstructor
public class CityPersistenceAdapter implements CityOutgoingPort {
    
    private final CityJpaRepository repository;
    private final CityEntityMapper mapper;  // ← MapStruct generiert @Component
    
    @Override
    public Page<City> findAll(Optional<String> name, Optional<String> postalCode, int page, int size) {
        // JPA-Query...
        org.springframework.data.domain.Page<CityEntity> entityPage = repository.findAll(...);
        
        List<City> cities = mapper.toDomainList(entityPage.getContent());
        
        return new Page<>(
            cities,
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalElements(),
            entityPage.getTotalPages()
        );
    }
}
```

### 6.3 Null-Handling

**MapStruct Default:** Automatisches Null-Handling

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PracticeWebMapper {
    // ...
}
```

**Optionen:**
- `IGNORE` – Null-Felder werden nicht gesetzt (behält Default-Werte)
- `SET_TO_NULL` – Null-Felder werden explizit auf null gesetzt
- `SET_TO_DEFAULT` – Null-Felder bekommen Default-Werte

### 6.4 Collections

**MapStruct erkennt automatisch:**
- `List<T>` ↔ `List<D>`
- `Set<T>` ↔ `Set<D>`
- `Map<K,V>` ↔ `Map<K2,V2>`

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface SpecialityWebMapper {
    SpecialityDto toDto(Speciality speciality);
    
    // ⚠️ Automatisch generiert:
    List<SpecialityDto> toDtoList(List<Speciality> specialities);
    Set<SpecialityDto> toDtoSet(Set<Speciality> specialities);
}
```

### 6.5 Fehlerbehandlung

**MapStruct-Exceptions:**

Wenn Mapping fehlschlägt (z.B. Type-Inkompatibilität), gibt es **Compile-Fehler**, KEINE Runtime-Exceptions!

**Beispiel (funktioniert NICHT):**

```java
@Mapper(componentModel = "spring")
public interface BadMapper {
    @Mapping(source = "name", target = "id")  // ❌ String → UUID geht nicht
    CityDto toDto(City city);
}
```

**Compiler-Fehler:**
```
Can't map property "String name" to "UUID id". Consider to declare/implement a mapping method.
```

### 6.6 Performance

**MapStruct ist schneller als manuelle Mapper:**

| Methode | Performance |
|---------|-------------|
| MapStruct | ⭐⭐⭐⭐⭐ (Compile-Zeit Code-Generierung) |
| Manuell | ⭐⭐⭐⭐ (Optimiert, aber mehr Boilerplate) |
| ModelMapper | ⭐⭐ (Reflection zur Runtime) |
| Dozer | ⭐ (Langsam durch XML-Konfiguration) |

### 6.7 Testing

**MapStruct-Mapper testen:**

```java
@SpringBootTest
class CityWebMapperTest {
    
    @Autowired
    private CityWebMapper mapper;
    
    @Test
    void testToDomain() {
        City city = new City(UUID.randomUUID(), "Köln", "50667");
        CityDto dto = mapper.toDto(city);
        
        assertThat(dto.getId()).isEqualTo(city.getId());
        assertThat(dto.getName()).isEqualTo(city.getName());
        assertThat(dto.getPostalCode()).isEqualTo(city.getPostalCode());
    }
    
    @Test
    void testNullHandling() {
        CityDto dto = mapper.toDto(null);
        assertThat(dto).isNull();
    }
}
```

---

## 7. Zusammenfassung

### 7.1 Checkliste: Mapper erstellen

- [ ] **Schritt 1:** POM.xml korrekt konfigurieren (Lombok VOR MapStruct)
- [ ] **Schritt 2:** Entity-Klassen erstellen (JPA-Entities)
- [ ] **Schritt 3:** Entity-Mapper erstellen (Domain ↔ Entity)
- [ ] **Schritt 4:** Web-Mapper erstellen (API-DTO ↔ Domain)
- [ ] **Schritt 5:** Mapper in Controllern verwenden
- [ ] **Schritt 6:** Mapper in Persistence-Adaptern verwenden
- [ ] **Schritt 7:** Tests schreiben
- [ ] **Schritt 8:** `mvn clean compile` ausführen (generiert Mapper-Implementierungen)

### 7.2 Vorteile MapStruct vs. Manuell

| Feature | MapStruct | Manuell |
|---------|-----------|---------|
| **Boilerplate** | ✅ Sehr wenig | ❌ Viel Code |
| **Type-Safety** | ✅ Compile-Zeit | ⚠️ Nur bei Tests |
| **Performance** | ✅ Schnell | ✅ Schnell |
| **Flexibilität** | ⚠️ Begrenzt (Custom Methods) | ✅ Volle Kontrolle |
| **Wartbarkeit** | ✅ Sehr gut | ⚠️ Mehr Code = mehr Wartung |
| **Lernkurve** | ⚠️ Annotations lernen | ✅ Einfach |

**Empfehlung:** **MapStruct verwenden** für:
- Einfache 1:1 Mappings
- Standard-Mappings mit wenigen Custom-Logiken
- Große Projekte mit vielen Entities

**Manuell verwenden** für:
- Sehr komplexe Mapping-Logiken
- Wenn MapStruct nicht ausreicht

### 7.3 Nächste Schritte

1. **POM.xml aktualisieren** (Compiler-Plugin mit MapStruct)
2. **JPA-Entities erstellen** (falls noch nicht vorhanden)
3. **Entity-Mapper erstellen** (CityEntityMapper, PracticeEntityMapper, etc.)
4. **Web-Mapper erstellen** (CityWebMapper, PracticeWebMapper, etc.)
5. **Controller implementieren** (Mapper verwenden)
6. **Persistence-Adapter implementieren** (Mapper verwenden)
7. **Tests schreiben**

---

## 8. Anhang: Datei-Übersicht

### 8.1 Zu erstellende Web-Mapper

```
infrastructure/adapter/incomming/web/mapper/
├── CityWebMapper.java
├── SpecialityWebMapper.java
├── PracticeWebMapper.java
├── DoctorWebMapper.java
├── WorkingHoursWebMapper.java
├── SlotWebMapper.java
└── PageWebMapper.java
```

### 8.2 Zu erstellende Entity-Mapper

```
infrastructure/adapter/outgoing/persistence/mapper/
├── CityEntityMapper.java
├── SpecialityEntityMapper.java
├── PracticeEntityMapper.java
├── DoctorEntityMapper.java
├── WorkingHoursEntityMapper.java
└── SlotEntityMapper.java
```

---

**Viel Erfolg beim Implementieren! 🚀**

