# 🔄 MAPPER COMPLETE GUIDE - Von Klassisch bis MapStruct

---

## 🎯 Quick Reference - Mapper-Richtungen

Bevor Sie in die Details einsteigen, hier eine schnelle Übersicht:

### WebMapper (Client ↔ Server)

```
📁 Package: infrastructure/adapter/incoming/web/mapper/

CLIENT                           SERVER
──────────────────────────────────────────────────
CreateDoctorRequest  ──→  Doctor
                   toDomain()

DoctorDto  ←──────────────  Doctor
           toDto()
```

### PersistenceMapper (Server ↔ Datenbank)

```
📁 Package: infrastructure/adapter/outgoing/persistence/mapper/

SERVER                           DATABASE
──────────────────────────────────────────────────
Doctor  ──────────────────→  DoctorEntity
        toEntity()

Doctor  ←──────────────────  DoctorEntity
        toDomain()
```

### Merkhilfe

```
CLIENT ──Request──→ SERVER ──Domain──→ DATABASE
       toDomain()         toEntity()

CLIENT ←──Dto──── SERVER ←──Domain──── DATABASE
       toDto()           toDomain()
```

---

## 📋 Inhaltsverzeichnis

1. [Einführung](#1-einführung)
2. [Klassische Mapper - Manuelle Methode](#2-klassische-mapper---manuelle-methode)
3. [MapStruct - Automatisierte Methode](#3-mapstruct---automatisierte-methode)
4. [MapStruct Annotations - Vollständige Erklärung](#4-mapstruct-annotations---vollständige-erklärung)
5. [Best Practices](#5-best-practices)

---

## 1. Einführung

### 1.1 Was sind Mapper?

Mapper sind **Transformer-Klassen**, die Daten zwischen verschiedenen Schichten einer Anwendung konvertieren. In der Hexagonalen Architektur sorgen sie dafür, dass die Domain-Schicht (Kern) unabhängig von technischen Details bleibt.

### 1.2 Warum benötigen wir Mapper?

Mapper konvertieren Daten zwischen verschiedenen Schichten. Wir haben **zwei Arten von Mappern**:

#### A) **WebMapper** - Zwischen Client und Server

```
CLIENT (Frontend)                    SERVER (Backend)
──────────────────────────────────────────────────────────────
📥 Request                    ─→    📦 Domain Model
   CreateDoctorRequest         toDomain()      Doctor

📤 Dto                        ←─    📦 Domain Model
   DoctorDto                   toDto()         Doctor
```

#### B) **PersistenceMapper** - Zwischen Server und Datenbank

```
SERVER (Backend)                     DATABASE (PostgreSQL)
──────────────────────────────────────────────────────────────
📦 Domain Model               ─→    💾 Entity
   Doctor                     toEntity()      DoctorEntity

📦 Domain Model               ←─    💾 Entity
   Doctor                     toDomain()      DoctorEntity
```

**Vollständiger Datenfluss in unserem Projekt:**

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT (Vue.js Frontend)                                    │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ HTTP POST /api/v1/doctors
                    │ Body: CreateDoctorRequest
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ REST-Controller (DoctorController)                          │
│   - Empfängt CreateDoctorRequest                            │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ WebMapper.toDomain(request)
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ 📦 DOMAIN MODEL (Doctor)                                    │
│   - Geschäftslogik-Objekt (keine JPA-Annotations)          │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ Application Service
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ Persistence Adapter                                         │
│   PersistenceMapper.toEntity(doctor)                        │
└─────────────────────────────────────────────────────────────┘
                    │
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ 💾 DATABASE (PostgreSQL)                                    │
│   - Speichert DoctorEntity                                  │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ PersistenceMapper.toDomain(entity)
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ 📦 DOMAIN MODEL (Doctor)                                    │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ WebMapper.toDto(doctor)
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ REST-Controller                                             │
│   - Gibt DoctorDto zurück                                   │
└─────────────────────────────────────────────────────────────┘
                    │
                    │ HTTP Response 201 Created
                    │ Body: DoctorDto
                    ↓
┌─────────────────────────────────────────────────────────────┐
│ CLIENT (Vue.js Frontend)                                    │
│   - Empfängt DoctorDto                                      │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 Namenskonvention in unserem Projekt

Laut unserer OpenAPI-Spezifikation:

| Begriff | Bedeutung | Beispiel |
|---------|-----------|----------|
| **Request** | Was der Client zum Server sendet | `CreateDoctorRequest`, `UpdateDoctorRequest` |
| **Dto** | Was der Server zum Client zurückgibt | `DoctorDto`, `PracticeDto` |
| **Domain** | Geschäftslogik-Modell im Server | `Doctor`, `Practice` |
| **Entity** | JPA-Datenbank-Entität | `DoctorEntity`, `PracticeEntity` |

---

### 1.4 Übersicht: Mapper-Richtungen und Methoden

#### **WebMapper** - Kommunikation zwischen Client und Server

| Von (Quelle) | Nach (Ziel) | Richtung | Methodenname | Wann? |
|--------------|-------------|----------|--------------|-------|
| **Request** | **Domain** | Client → Server | `toDomain()` | Client sendet Daten (POST, PUT) |
| **Domain** | **Dto** | Server → Client | `toDto()` | Server gibt Daten zurück (Response) |

**Beispiel:**

```java
// WebMapper für Doctor
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    // Client → Server: Request wird zu Domain konvertiert
    Doctor toDomain(CreateDoctorRequest request);

    // Server → Client: Domain wird zu Dto konvertiert
    DoctorDto toDto(Doctor domain);
}
```

---

#### **PersistenceMapper** - Kommunikation zwischen Server und Datenbank

| Von (Quelle) | Nach (Ziel) | Richtung | Methodenname | Wann? |
|--------------|-------------|----------|--------------|-------|
| **Domain** | **Entity** | Server → DB | `toEntity()` | Daten speichern (INSERT, UPDATE) |
| **Entity** | **Domain** | DB → Server | `toDomain()` | Daten laden (SELECT) |

**Beispiel:**

```java
// PersistenceMapper für Doctor
@Mapper(componentModel = "spring")
public interface DoctorEntityMapper {

    // Server → DB: Domain wird zu Entity konvertiert
    DoctorEntity toEntity(Doctor domain);

    // DB → Server: Entity wird zu Domain konvertiert
    Doctor toDomain(DoctorEntity entity);
}
```

---

#### **Visualisierung der Datenfluss-Richtungen**

```
CLIENT                    SERVER                    DATABASE
══════════════════════════════════════════════════════════════

CreateDoctorRequest  ──→  Doctor  ──────────────→  DoctorEntity
(Request)            toDomain()  (Domain)  toEntity()  (Entity)
                     WebMapper                PersistenceMapper

DoctorDto  ←──────────  Doctor  ←──────────────  DoctorEntity
(Dto)      toDto()     (Domain)  toDomain()     (Entity)
           WebMapper              PersistenceMapper
```

---

### 1.5 Zusammenfassung: Welcher Mapper wofür?

| Mapper-Typ | Package | Verantwortlich für | Methoden |
|------------|---------|-------------------|----------|
| **WebMapper** | `infrastructure/adapter/incoming/web/mapper/` | Kommunikation mit Frontend (REST-API) | `toDomain()`, `toDto()` |
| **PersistenceMapper** | `infrastructure/adapter/outgoing/persistence/mapper/` | Kommunikation mit Datenbank (JPA) | `toEntity()`, `toDomain()` |

---

## 2. Klassische Mapper - Manuelle Methode

### 2.1 Was ist ein klassischer Mapper?

Ein klassischer Mapper ist eine **Java-Klasse**, in der Sie **manuell** den Code schreiben, um Daten von einem Objekt zu einem anderen zu kopieren.

### 2.2 Struktur eines klassischen Mappers

```java
package test.doctor_provider.infrastructure.adapter.incoming.web.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.api.model.CreateDoctorRequest;
import test.doctor_provider.api.model.DoctorDto;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Klassischer WebMapper für Doctor
 *
 * Diese Klasse konvertiert zwischen:
 * - CreateDoctorRequest → Doctor (toDomain)
 * - Doctor → DoctorDto (toDto)
 */
@Component
public class DoctorWebMapper {

    // ========================================================================
    // 📥 REQUEST → DOMAIN (Client sendet Daten)
    // ========================================================================

    /**
     * Konvertiert CreateDoctorRequest zu Doctor
     *
     * @param request Das Request-Objekt vom Client (enthält firstName, lastName, practiceId, specialityIds)
     * @return Ein neues Doctor Domain-Modell (ohne ID, da diese vom Service generiert wird)
     */
    public Doctor toDomain(CreateDoctorRequest request) {
        // Erstelle ein neues Doctor-Objekt
        Doctor doctor = new Doctor();

        // Kopiere die Felder vom Request zum Domain-Modell
        // ID wird NICHT gesetzt (wird später vom Service mit UUID.randomUUID() generiert)
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPracticeId(request.getPracticeId());
        doctor.setSpecialityIds(request.getSpecialityIds());

        return doctor;
    }

    /**
     * Konvertiert UpdateDoctorRequest zu Doctor
     *
     * @param request Das Update-Request-Objekt vom Client
     * @param id Die ID des zu aktualisierenden Doctors (aus URL-Path)
     * @return Ein Doctor Domain-Modell mit der übergebenen ID
     */
    public Doctor toDomain(UpdateDoctorRequest request, UUID id) {
        Doctor doctor = new Doctor();

        // Setze die ID aus dem URL-Path
        doctor.setId(id);

        // Kopiere alle Felder vom Request
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPracticeId(request.getPracticeId());
        doctor.setSpecialityIds(request.getSpecialityIds());

        return doctor;
    }

    // ========================================================================
    // 📤 DOMAIN → DTO (Server gibt Daten zurück)
    // ========================================================================

    /**
     * Konvertiert Doctor zu DoctorDto
     *
     * @param domain Das Doctor Domain-Modell (aus dem Service)
     * @return Ein DoctorDto für die HTTP-Response
     */
    public DoctorDto toDto(Doctor domain) {
        // Erstelle ein neues DoctorDto-Objekt
        DoctorDto dto = new DoctorDto();

        // Kopiere alle Felder vom Domain-Modell zum DTO
        dto.setId(domain.getId());
        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setPracticeId(domain.getPracticeId());
        dto.setSpecialityIds(domain.getSpecialityIds());

        return dto;
    }

    /**
     * Konvertiert List<Doctor> zu List<DoctorDto>
     *
     * @param domainList Eine Liste von Doctor Domain-Modellen
     * @return Eine Liste von DoctorDto für die HTTP-Response
     */
    public List<DoctorDto> toDto(List<Doctor> domainList) {
        // Nutze Stream API, um jedes Doctor-Objekt zu DoctorDto zu konvertieren
        return domainList.stream()
                .map(this::toDto)  // Für jedes Doctor-Objekt: rufe toDto() auf
                .collect(Collectors.toList());  // Sammle alle DTOs in einer Liste
    }
}
```

### 2.3 Wichtige Annotations im klassischen Mapper

#### `@Component`

```java
@Component
public class DoctorWebMapper {
    // ...
}
```

**Was macht diese Annotation?**

Die `@Component` Annotation teilt Spring Boot mit, dass diese Klasse eine **Spring Bean** ist. Das bedeutet:

1. **Automatische Instanziierung**: Spring Boot erstellt automatisch eine Instanz dieser Klasse beim Start der Anwendung
2. **Dependency Injection möglich**: Sie können diese Klasse in anderen Klassen (z.B. Controller) mit `@Autowired` injizieren
3. **Singleton-Pattern**: Es gibt nur eine Instanz dieser Klasse in der gesamten Anwendung

**Beispiel für die Verwendung:**

```java
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    // Spring injiziert automatisch die DoctorWebMapper-Instanz
    @Autowired
    private DoctorWebMapper mapper;

    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody CreateDoctorRequest request) {
        // Konvertiere Request zu Domain
        Doctor doctor = mapper.toDomain(request);

        // Speichere den Doctor
        Doctor saved = doctorService.save(doctor);

        // Konvertiere Domain zu DTO
        DoctorDto dto = mapper.toDto(saved);

        return ResponseEntity.status(201).body(dto);
    }
}
```

**Ohne `@Component` (❌ Funktioniert nicht mit Spring):**

```java
// KEINE Spring Bean!
public class DoctorWebMapper {
    // ...
}

// Im Controller:
@RestController
public class DoctorController {

    @Autowired
    private DoctorWebMapper mapper;  // ❌ FEHLER: mapper ist NULL!

    // Sie müssten es manuell instanziieren:
    private DoctorWebMapper mapper = new DoctorWebMapper();  // ❌ Nicht empfohlen!
}
```

### 2.4 Vor- und Nachteile des klassischen Mappers

#### ✅ Vorteile

1. **Einfach zu verstehen**: Keine spezielle Syntax oder Annotations erforderlich
2. **Vollständige Kontrolle**: Sie schreiben jeden Schritt selbst
3. **Debugging einfach**: Sie können Breakpoints in jeder Zeile setzen
4. **Keine zusätzliche Dependency**: Funktioniert mit reinem Java

#### ❌ Nachteile

1. **Viel Boilerplate-Code**: Jedes Feld muss manuell kopiert werden
2. **Fehleranfällig**: Leicht Felder zu vergessen (Compiler warnt nicht)
3. **Wartung aufwendig**: Bei Änderungen am Modell müssen alle Mapper angepasst werden
4. **Performance**: Langsamer als MapStruct (wenn Reflection verwendet wird)
5. **Keine Compile-Zeit-Checks**: Fehler werden erst zur Laufzeit sichtbar

**Beispiel für einen typischen Fehler:**

```java
public Doctor toDomain(CreateDoctorRequest request) {
    Doctor doctor = new Doctor();

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());
    // FEHLER: practiceId wurde vergessen!
    // FEHLER: specialityIds wurde vergessen!

    return doctor;
}

// ❌ Compiler gibt KEINE Warnung!
// ❌ Fehler wird erst zur Laufzeit sichtbar (practiceId ist null)
```

### 2.5 Wann sollten Sie klassische Mapper verwenden?

**Verwenden Sie klassische Mapper, wenn:**

- ✅ Sie sehr einfache Mappings haben (1-2 Felder)
- ✅ Sie komplexe Business-Logik während des Mappings benötigen
- ✅ Sie keine zusätzliche Dependency (MapStruct) hinzufügen möchten

**Beispiel für komplexe Business-Logik:**

```java
public Doctor toDomain(CreateDoctorRequest request) {
    Doctor doctor = new Doctor();

    // Einfaches Mapping
    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    // Komplexe Business-Logik
    if (request.getSpecialityIds() != null && request.getSpecialityIds().size() > 5) {
        // Ein Arzt kann maximal 5 Fachrichtungen haben
        throw new IllegalArgumentException("Ein Arzt kann maximal 5 Fachrichtungen haben");
    }

    // Weitere Validierung
    if (request.getFirstName().contains("Test")) {
        // Testdaten werden markiert
        doctor.setIsTestData(true);
    }

    return doctor;
}
```

**Für die meisten Fälle empfehlen wir jedoch MapStruct!**

---

### 2.6 Beispiel: PersistenceMapper (Klassisch)

Zusätzlich zum WebMapper benötigen Sie auch einen **PersistenceMapper** für die Kommunikation zwischen Server und Datenbank.

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.DoctorEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Klassischer PersistenceMapper für Doctor
 *
 * Diese Klasse konvertiert zwischen:
 * - Doctor → DoctorEntity (toEntity) - Server → Datenbank
 * - DoctorEntity → Doctor (toDomain) - Datenbank → Server
 */
@Component
public class DoctorEntityMapper {

  // ========================================================================
  // 💾 DOMAIN → ENTITY (Server speichert in Datenbank)
  // ========================================================================

  /**
   * Konvertiert Doctor zu DoctorEntity
   *
   * RICHTUNG: Server → Datenbank
   * WANN: Beim Speichern (INSERT/UPDATE)
   *
   * @param domain Das Doctor Domain-Modell
   * @return Ein DoctorEntity für die Datenbank
   */
  public DoctorEntity toEntity(Doctor domain) {
    if (domain == null) {
      return null;
    }

    DoctorEntity entity = new DoctorEntity();

    // Kopiere alle Felder vom Domain-Modell zur Entity
    entity.setId(domain.getId());
    entity.setFirstName(domain.getFirstName());
    entity.setLastName(domain.getLastName());
    entity.setPracticeId(domain.getPracticeId());
    entity.setSpecialityIds(domain.getSpecialityIds());

    return entity;
  }

  // ========================================================================
  // 📦 ENTITY → DOMAIN (Server lädt aus Datenbank)
  // ========================================================================

  /**
   * Konvertiert DoctorEntity zu Doctor
   *
   * RICHTUNG: Datenbank → Server
   * WANN: Beim Laden (SELECT)
   *
   * @param entity Das DoctorEntity aus der Datenbank
   * @return Ein Doctor Domain-Modell
   */
  public Doctor toDomain(DoctorEntity entity) {
    if (entity == null) {
      return null;
    }

    Doctor doctor = new Doctor();

    // Kopiere alle Felder von der Entity zum Domain-Modell
    doctor.setId(entity.getId());
    doctor.setFirstName(entity.getFirstName());
    doctor.setLastName(entity.getLastName());
    doctor.setPracticeId(entity.getPracticeId());
    doctor.setSpecialityIds(entity.getSpecialityIds());

    return doctor;
  }

  /**
   * Konvertiert List<DoctorEntity> zu List<Doctor>
   *
   * @param entityList Eine Liste von DoctorEntity aus der Datenbank
   * @return Eine Liste von Doctor Domain-Modellen
   */
  public List<Doctor> toDomain(List<DoctorEntity> entityList) {
    if (entityList == null) {
      return null;
    }

    return entityList.stream()
      .map(this::toDomain)
      .collect(Collectors.toList());
  }
}
```

**Verwendung im PersistenceAdapter:**

```java
@Component
public class DoctorPersistenceAdapter implements DoctorOutgoingPort {

    @Autowired
    private DoctorJpaRepository repository;

    @Autowired
    private DoctorEntityMapper mapper;

    /**
     * Speichert einen Doctor in der Datenbank
     */
    public Doctor save(Doctor doctor) {
        // 1. Domain → Entity (Server → Datenbank)
        DoctorEntity entity = mapper.toEntity(doctor);

        // 2. Speichern in DB
        DoctorEntity saved = repository.save(entity);

        // 3. Entity → Domain (Datenbank → Server)
        return mapper.toDomain(saved);
    }

    /**
     * Lädt einen Doctor aus der Datenbank
     */
    public Doctor findById(UUID id) {
        // 1. Aus DB laden
        DoctorEntity entity = repository.findById(id).orElseThrow();

        // 2. Entity → Domain (Datenbank → Server)
        return mapper.toDomain(entity);
    }
}
```

---

## 3. MapStruct - Automatisierte Methode

### 3.1 Was ist MapStruct?

MapStruct ist ein **Code-Generator**, der zur **Compile-Zeit** automatisch Mapper-Implementierungen erstellt. Sie schreiben nur ein Interface mit Annotations, und MapStruct generiert den gesamten Mapping-Code für Sie.

### 3.2 Warum MapStruct?

| Aspekt | MapStruct ✅ | Klassisches Mapping |
|--------|-------------|-------------------|
| **Typ** | Interface mit `@Mapper` | Klasse mit manueller Implementierung |
| **Code** | Automatisch generiert | Manuell geschrieben |
| **Fehler** | Compile-Zeit (typsicher) | Runtime (fehleranfällig) |
| **Performance** | Sehr schnell (kein Reflection) | Langsamer (mit Reflection) |
| **Wartbarkeit** | Einfach (nur Annotations) | Aufwendig (viel Boilerplate) |
| **Compile-Zeit-Checks** | ✅ Ja | ❌ Nein |

### 3.3 MapStruct Setup in pom.xml

#### Schritt 1: Dependencies hinzufügen

Öffnen Sie Ihre `pom.xml` und fügen Sie folgende Dependencies hinzu:

```xml
<!-- MapStruct für Mapper (automatische Code-Generierung) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- MapStruct Processor (generiert den Code zur Compile-Zeit) -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>  <!-- Nur zur Compile-Zeit benötigt -->
</dependency>
```

**Erklärung der Dependencies:**

| Dependency | Zweck | Scope |
|------------|-------|-------|
| `mapstruct` | Enthält alle MapStruct-Annotations (`@Mapper`, `@Mapping`, etc.) | `compile` (Standard) |
| `mapstruct-processor` | Generiert zur Compile-Zeit die Mapper-Implementierungen | `provided` (nur zur Compile-Zeit) |

#### Schritt 2: Maven Compiler Plugin konfigurieren (Optional)

Wenn Sie Lombok verwenden, müssen Sie sicherstellen, dass MapStruct und Lombok zusammenarbeiten:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>25</source>
                <target>25</target>
                <annotationProcessorPaths>
                    <!-- Lombok Processor -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <!-- MapStruct Processor -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Wichtig:** Lombok muss **vor** MapStruct in der Liste stehen, damit die generierten Getter/Setter von MapStruct verwendet werden können.

#### Schritt 3: Maven Projekt neu kompilieren

Nach dem Hinzufügen der Dependencies:

```bash
mvn clean install
```

**Was passiert dabei?**

1. Maven lädt die MapStruct-Dependencies herunter
2. Beim Kompilieren generiert MapStruct automatisch Implementierungen für alle `@Mapper` Interfaces
3. Die generierten Implementierungen landen in `target/generated-sources/annotations/`

### 3.4 MapStruct Mapper erstellen - Schritt für Schritt

#### Schritt 1: Interface erstellen

Erstellen Sie ein Interface (keine Klasse!) mit der `@Mapper` Annotation:

```java
package test.doctor_provider.infrastructure.adapter.incoming.web.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DoctorWebMapper {
    // Methoden folgen...
}
```

**Wichtige Punkte:**

- **Interface**, nicht Klasse!
- `@Mapper(componentModel = "spring")` ist **PFLICHT** für Spring Boot
- Package: Legen Sie den Mapper in das entsprechende Adapter-Package

#### Schritt 2: Einfache Mapping-Methode definieren

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Konvertiert Doctor zu DoctorDto
     *
     * MapStruct generiert automatisch folgenden Code:
     *
     * public DoctorDto toDto(Doctor domain) {
     *     if (domain == null) return null;
     *     DoctorDto dto = new DoctorDto();
     *     dto.setId(domain.getId());
     *     dto.setFirstName(domain.getFirstName());
     *     dto.setLastName(domain.getLastName());
     *     dto.setPracticeId(domain.getPracticeId());
     *     dto.setSpecialityIds(domain.getSpecialityIds());
     *     return dto;
     * }
     */
    DoctorDto toDto(Doctor domain);
}
```

**Was passiert hier?**

- MapStruct sieht die Methode `DoctorDto toDto(Doctor domain)`
- MapStruct analysiert beide Klassen (`Doctor` und `DoctorDto`)
- MapStruct findet alle Felder mit **gleichen Namen** und **kompatiblen Typen**
- MapStruct generiert automatisch den Code zum Kopieren dieser Felder

**Voraussetzungen für automatisches Mapping:**

1. **Gleiche Feldnamen**: `doctor.firstName` → `dto.firstName`
2. **Kompatible Typen**: `String` → `String`, `UUID` → `UUID`, `Integer` → `Integer`
3. **Getter/Setter vorhanden**: `getFirstName()` / `setFirstName()`

#### Schritt 3: Mapping mit `@Mapping` Annotation anpassen

Manchmal sind die Feldnamen unterschiedlich oder Sie möchten bestimmte Felder ignorieren:

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Konvertiert CreateDoctorRequest zu Doctor
     *
     * Problem: CreateDoctorRequest hat KEINE id, aber Doctor hat eine id
     * Lösung: Wir ignorieren das id-Feld mit @Mapping(target = "id", ignore = true)
     */
    @Mapping(target = "id", ignore = true)
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Erklärung:**

- `target = "id"`: Das Feld im **Ziel-Objekt** (hier: `Doctor`)
- `ignore = true`: Dieses Feld wird **nicht gemappt** (bleibt null)

#### Schritt 4: Vollständiges Beispiel mit allen Methoden

```java
package test.doctor_provider.infrastructure.adapter.incoming.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.api.model.CreateDoctorRequest;
import test.doctor_provider.api.model.UpdateDoctorRequest;
import test.doctor_provider.api.model.DoctorDto;

import java.util.List;
import java.util.UUID;

/**
 * WebMapper für Doctor - Automatisch generiert durch MapStruct
 *
 * Dieser Mapper konvertiert zwischen:
 * - CreateDoctorRequest → Doctor (toDomain)
 * - UpdateDoctorRequest → Doctor (toDomain)
 * - Doctor → DoctorDto (toDto)
 * - List<Doctor> → List<DoctorDto> (toDto)
 *
 * MapStruct generiert die gesamte Implementierung automatisch zur Compile-Zeit.
 * Die generierte Implementierung finden Sie in:
 * target/generated-sources/annotations/.../DoctorWebMapperImpl.java
 */
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    // ========================================================================
    // 📥 REQUEST → DOMAIN (Client sendet Daten)
    // ========================================================================

    /**
     * Konvertiert CreateDoctorRequest → Doctor
     *
     * @param request Das Request-Objekt vom Client
     * @return Ein neues Doctor Domain-Modell (ohne ID)
     *
     * Automatisches Mapping für:
     * - request.firstName → doctor.firstName
     * - request.lastName → doctor.lastName
     * - request.practiceId → doctor.practiceId
     * - request.specialityIds → doctor.specialityIds
     *
     * Ignoriert:
     * - doctor.id (wird später vom Service generiert)
     */
    @Mapping(target = "id", ignore = true)
    Doctor toDomain(CreateDoctorRequest request);

    /**
     * Konvertiert UpdateDoctorRequest → Doctor
     *
     * @param request Das Update-Request-Objekt vom Client
     * @param id Die ID des zu aktualisierenden Doctors (aus URL-Path)
     * @return Ein Doctor Domain-Modell mit der übergebenen ID
     *
     * Automatisches Mapping für:
     * - request.firstName → doctor.firstName
     * - request.lastName → doctor.lastName
     * - request.practiceId → doctor.practiceId
     * - request.specialityIds → doctor.specialityIds
     * - id → doctor.id (explizit gemappt)
     */
    @Mapping(target = "id", source = "id")
    Doctor toDomain(UpdateDoctorRequest request, UUID id);

    // ========================================================================
    // 📤 DOMAIN → DTO (Server gibt Daten zurück)
    // ========================================================================

    /**
     * Konvertiert Doctor → DoctorDto
     *
     * @param domain Das Doctor Domain-Modell
     * @return Ein DoctorDto für die HTTP-Response
     *
     * Automatisches Mapping für:
     * - domain.id → dto.id
     * - domain.firstName → dto.firstName
     * - domain.lastName → dto.lastName
     * - domain.practiceId → dto.practiceId
     * - domain.specialityIds → dto.specialityIds
     */
    DoctorDto toDto(Doctor domain);

    /**
     * Konvertiert List<Doctor> → List<DoctorDto>
     *
     * @param domainList Eine Liste von Doctor Domain-Modellen
     * @return Eine Liste von DoctorDto für die HTTP-Response
     *
     * MapStruct generiert automatisch eine Schleife:
     * - Für jedes Doctor-Objekt: rufe toDto() auf
     * - Sammle alle DTOs in einer neuen Liste
     */
    List<DoctorDto> toDto(List<Doctor> domainList);
}
```

#### Schritt 5: Generierte Implementierung anschauen (Optional)

Nach `mvn clean install` finden Sie die generierte Implementierung in:

```
target/generated-sources/annotations/test/doctor_provider/infrastructure/adapter/incoming/web/mapper/DoctorWebMapperImpl.java
```

**Beispiel der generierten Klasse:**

```java
@Component
public class DoctorWebMapperImpl implements DoctorWebMapper {

    @Override
    public Doctor toDomain(CreateDoctorRequest request) {
        if (request == null) {
            return null;
        }

        Doctor doctor = new Doctor();

        // id wird ignoriert (bleibt null)
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setPracticeId(request.getPracticeId());
        doctor.setSpecialityIds(request.getSpecialityIds());

        return doctor;
    }

    @Override
    public Doctor toDomain(UpdateDoctorRequest request, UUID id) {
        if (request == null && id == null) {
            return null;
        }

        Doctor doctor = new Doctor();

        // id aus Parameter setzen
        doctor.setId(id);

        if (request != null) {
            doctor.setFirstName(request.getFirstName());
            doctor.setLastName(request.getLastName());
            doctor.setPracticeId(request.getPracticeId());
            doctor.setSpecialityIds(request.getSpecialityIds());
        }

        return doctor;
    }

    @Override
    public DoctorDto toDto(Doctor domain) {
        if (domain == null) {
            return null;
        }

        DoctorDto dto = new DoctorDto();

        dto.setId(domain.getId());
        dto.setFirstName(domain.getFirstName());
        dto.setLastName(domain.getLastName());
        dto.setPracticeId(domain.getPracticeId());
        dto.setSpecialityIds(domain.getSpecialityIds());

        return dto;
    }

    @Override
    public List<DoctorDto> toDto(List<Doctor> domainList) {
        if (domainList == null) {
            return null;
        }

        List<DoctorDto> list = new ArrayList<>(domainList.size());
        for (Doctor doctor : domainList) {
            list.add(toDto(doctor));
        }

        return list;
    }
}
```

**Vorteile:**

- ✅ Kein Boilerplate-Code
- ✅ Null-Checks automatisch
- ✅ Typsicher zur Compile-Zeit
- ✅ Kein Reflection → sehr schnell

### 3.5 Verwendung im Controller

```java
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    // MapStruct-Mapper wird automatisch von Spring injiziert
    @Autowired
    private DoctorWebMapper mapper;

    @Autowired
    private DoctorService doctorService;

    /**
     * POST /api/v1/doctors
     * Erstellt einen neuen Arzt
     */
    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody CreateDoctorRequest request) {
        // 1. Request → Domain
        Doctor doctor = mapper.toDomain(request);

        // 2. Business-Logik (Service)
        Doctor saved = doctorService.save(doctor);

        // 3. Domain → DTO
        DoctorDto dto = mapper.toDto(saved);

        return ResponseEntity.status(201).body(dto);
    }

    /**
     * GET /api/v1/doctors
     * Gibt alle Ärzte zurück
     */
    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        // 1. Lade alle Doctors
        List<Doctor> doctors = doctorService.findAll();

        // 2. List<Doctor> → List<DoctorDto>
        List<DoctorDto> dtos = mapper.toDto(doctors);

        return ResponseEntity.ok(dtos);
    }
}
```

---

### 3.6 MapStruct PersistenceMapper - Vollständiges Beispiel

Zusätzlich zum WebMapper benötigen Sie auch einen **PersistenceMapper** für die Datenbank-Kommunikation.

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.DoctorEntity;

import java.util.List;

/**
 * PersistenceMapper für Doctor - Automatisch generiert durch MapStruct
 *
 * Dieser Mapper konvertiert zwischen:
 * - Doctor → DoctorEntity (toEntity) - Server → Datenbank
 * - DoctorEntity → Doctor (toDomain) - Datenbank → Server
 *
 * MapStruct generiert die gesamte Implementierung automatisch zur Compile-Zeit.
 */
@Mapper(componentModel = "spring")
public interface DoctorEntityMapper {

  // ========================================================================
  // 💾 DOMAIN → ENTITY (Server speichert in Datenbank)
  // ========================================================================

  /**
   * Konvertiert Doctor → DoctorEntity
   *
   * RICHTUNG: Server → Datenbank
   * WANN: Beim Speichern (INSERT/UPDATE)
   *
   * @param domain Das Doctor Domain-Modell
   * @return Ein DoctorEntity für die Datenbank
   *
   * Automatisches Mapping für:
   * - domain.id → entity.id
   * - domain.firstName → entity.firstName
   * - domain.lastName → entity.lastName
   * - domain.practiceId → entity.practiceId
   * - domain.specialityIds → entity.specialityIds
   */
  DoctorEntity toEntity(Doctor domain);

  // ========================================================================
  // 📦 ENTITY → DOMAIN (Server lädt aus Datenbank)
  // ========================================================================

  /**
   * Konvertiert DoctorEntity → Doctor
   *
   * RICHTUNG: Datenbank → Server
   * WANN: Beim Laden (SELECT)
   *
   * @param entity Das DoctorEntity aus der Datenbank
   * @return Ein Doctor Domain-Modell
   *
   * Automatisches Mapping für:
   * - entity.id → domain.id
   * - entity.firstName → domain.firstName
   * - entity.lastName → domain.lastName
   * - entity.practiceId → domain.practiceId
   * - entity.specialityIds → domain.specialityIds
   */
  Doctor toDomain(DoctorEntity entity);

  /**
   * Konvertiert List<DoctorEntity> → List<Doctor>
   *
   * RICHTUNG: Datenbank → Server
   * WANN: Beim Laden mehrerer Datensätze
   *
   * @param entityList Eine Liste von DoctorEntity aus der Datenbank
   * @return Eine Liste von Doctor Domain-Modellen
   */
  List<Doctor> toDomain(List<DoctorEntity> entityList);
}
```

**Verwendung im PersistenceAdapter:**

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import test.doctor_provider.application.port.outgoing.DoctorOutgoingPort;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.DoctorEntity;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository.DoctorJpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Persistence Adapter für Doctor
 *
 * Verantwortlich für:
 * - Speichern von Doctor in der Datenbank
 * - Laden von Doctor aus der Datenbank
 * - Konvertierung: Domain ↔ Entity (mit DoctorEntityMapper)
 */
@Component
public class DoctorPersistenceAdapter implements DoctorOutgoingPort {

  @Autowired
  private DoctorJpaRepository repository;

  @Autowired
  private DoctorEntityMapper mapper;  // ← MapStruct-Mapper

  /**
   * Speichert einen Doctor in der Datenbank
   *
   * Datenfluss:
   * 1. Domain → Entity (Server → Datenbank)
   * 2. Entity in DB speichern
   * 3. Entity → Domain (Datenbank → Server)
   */
  @Override
  public Doctor save(Doctor doctor) {
    // 1. Domain → Entity (Server → Datenbank)
    DoctorEntity entity = mapper.toEntity(doctor);

    // 2. Speichern in DB
    DoctorEntity saved = repository.save(entity);

    // 3. Entity → Domain (Datenbank → Server)
    return mapper.toDomain(saved);
  }

  /**
   * Lädt einen Doctor aus der Datenbank
   *
   * Datenfluss:
   * 1. Entity aus DB laden
   * 2. Entity → Domain (Datenbank → Server)
   */
  @Override
  public Doctor findById(UUID id) {
    // 1. Aus DB laden
    DoctorEntity entity = repository.findById(id)
      .orElseThrow(() -> new RuntimeException("Doctor not found"));

    // 2. Entity → Domain (Datenbank → Server)
    return mapper.toDomain(entity);
  }

  /**
   * Lädt alle Doctors aus der Datenbank
   */
  @Override
  public List<Doctor> findAll() {
    // 1. Alle Entities aus DB laden
    List<DoctorEntity> entities = repository.findAll();

    // 2. List<Entity> → List<Domain> (Datenbank → Server)
    return mapper.toDomain(entities);
  }
}
```

---

### 3.7 Vollständiger Datenfluss: Client → Server → Datenbank → Client

Hier sehen Sie den **kompletten Datenfluss** mit beiden Mappern:

```java
@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    @Autowired
    private DoctorWebMapper webMapper;  // ← Für Client ↔ Server

    @Autowired
    private DoctorService doctorService;

    /**
     * POST /api/v1/doctors
     * Erstellt einen neuen Arzt
     *
     * Datenfluss:
     * Client → Server: Request → Domain (webMapper.toDomain)
     * Server → DB:     Domain → Entity (entityMapper.toEntity)
     * DB → Server:     Entity → Domain (entityMapper.toDomain)
     * Server → Client: Domain → Dto    (webMapper.toDto)
     */
    @PostMapping
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody CreateDoctorRequest request) {
        // 1. Client → Server: Request → Domain
        Doctor doctor = webMapper.toDomain(request);

        // 2. Business-Logik + Speichern
        //    Intern: Domain → Entity (PersistenceMapper.toEntity)
        //    Intern: Entity → Domain (PersistenceMapper.toDomain)
        Doctor saved = doctorService.save(doctor);

        // 3. Server → Client: Domain → Dto
        DoctorDto dto = webMapper.toDto(saved);

        return ResponseEntity.status(201).body(dto);
    }

    /**
     * GET /api/v1/doctors/{id}
     * Lädt einen Arzt
     *
     * Datenfluss:
     * DB → Server:     Entity → Domain (entityMapper.toDomain)
     * Server → Client: Domain → Dto    (webMapper.toDto)
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getDoctor(@PathVariable UUID id) {
        // 1. Laden aus DB
        //    Intern: Entity → Domain (PersistenceMapper.toDomain)
        Doctor doctor = doctorService.findById(id);

        // 2. Server → Client: Domain → Dto
        DoctorDto dto = webMapper.toDto(doctor);

        return ResponseEntity.ok(dto);
    }
}
```

---

## 4. MapStruct Annotations - Vollständige Erklärung

In diesem Kapitel erkläre ich **alle** MapStruct-Annotations und Attribute, die Sie benötigen.

### 4.1 `@Mapper` - Hauptannotation

Die `@Mapper` Annotation markiert ein Interface als MapStruct-Mapper. MapStruct generiert automatisch eine Implementierung dieses Interfaces.

#### Attribute von `@Mapper`

| Attribut | Typ | Zweck | Beispiel |
|----------|-----|-------|----------|
| `componentModel` | String | Definiert, wie der Mapper instanziiert wird | `"spring"`, `"cdi"`, `"default"` |
| `uses` | Class[] | Andere Mapper, die wiederverwendet werden sollen | `uses = CityWebMapper.class` |
| `imports` | Class[] | Java-Klassen, die in `expression` verwendet werden | `imports = {UUID.class, LocalDateTime.class}` |
| `unmappedTargetPolicy` | ReportingPolicy | Wie soll mit nicht gemappten Feldern umgegangen werden | `ReportingPolicy.ERROR` |
| `nullValuePropertyMappingStrategy` | NullValuePropertyMappingStrategy | Wie soll mit null-Werten umgegangen werden | `NullValuePropertyMappingStrategy.IGNORE` |

#### `componentModel` - Wie wird der Mapper instanziiert?

**Zweck:**
Definiert, wie MapStruct die Mapper-Instanz erstellt und wie Sie diese in Ihrer Anwendung verwenden können.

**Mögliche Werte:**

| Wert | Beschreibung | Verwendung |
|------|--------------|------------|
| `"spring"` | Mapper wird als Spring Bean erstellt (`@Component`) | **Empfohlen für Spring Boot!** Sie können `@Autowired` verwenden |
| `"cdi"` | Mapper wird als CDI Bean erstellt (`@ApplicationScoped`) | Für Java EE / Jakarta EE |
| `"default"` | Keine Dependency Injection, manuelle Instanziierung | Sie müssen `Mappers.getMapper(...)` aufrufen |

**Beispiel mit `componentModel = "spring"`:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {
    DoctorDto toDto(Doctor domain);
}

// Verwendung im Controller:
@RestController
public class DoctorController {

    @Autowired  // ✅ Funktioniert nur mit componentModel = "spring"
    private DoctorWebMapper mapper;

    @GetMapping("/doctors/{id}")
    public DoctorDto getDoctor(@PathVariable UUID id) {
        Doctor doctor = doctorService.findById(id);
        return mapper.toDto(doctor);  // ✅ Mapper ist automatisch injiziert
    }
}
```

**Beispiel mit `componentModel = "default"` (❌ Nicht empfohlen für Spring Boot):**

```java
@Mapper  // KEIN componentModel!
public interface DoctorWebMapper {
    DoctorDto toDto(Doctor domain);
}

// Verwendung im Controller:
@RestController
public class DoctorController {

    // ❌ @Autowired funktioniert NICHT!
    // @Autowired
    // private DoctorWebMapper mapper;

    // ✅ Manuelle Instanziierung erforderlich:
    private final DoctorWebMapper mapper = Mappers.getMapper(DoctorWebMapper.class);

    @GetMapping("/doctors/{id}")
    public DoctorDto getDoctor(@PathVariable UUID id) {
        Doctor doctor = doctorService.findById(id);
        return mapper.toDto(doctor);
    }
}
```

**Empfehlung:** Verwenden Sie **IMMER** `componentModel = "spring"` in Spring Boot-Projekten!

#### `uses` - Andere Mapper wiederverwenden

**Zweck:**
Wenn ein Mapper-Feld gemappt werden muss, für das bereits ein anderer Mapper existiert, kann MapStruct diesen automatisch verwenden.

**Beispiel:**

```java
// CityWebMapper.java
@Mapper(componentModel = "spring")
public interface CityWebMapper {
    CityDto toDto(City domain);
}

// PracticeWebMapper.java
@Mapper(
    componentModel = "spring",
    uses = CityWebMapper.class  // ← Verwende CityWebMapper für city-Feld
)
public interface PracticeWebMapper {

    /**
     * MapStruct erkennt automatisch, dass Practice ein city-Feld hat
     * und ruft cityWebMapper.toDto(practice.getCity()) auf
     */
    PracticeDto toDto(Practice domain);
}
```

**Was MapStruct generiert:**

```java
@Component
public class PracticeWebMapperImpl implements PracticeWebMapper {

    @Autowired  // MapStruct injiziert CityWebMapper automatisch
    private CityWebMapper cityWebMapper;

    @Override
    public PracticeDto toDto(Practice domain) {
        if (domain == null) {
            return null;
        }

        PracticeDto dto = new PracticeDto();

        dto.setId(domain.getId());
        dto.setName(domain.getName());

        // ✅ Nutzt CityWebMapper für city-Feld
        dto.setCity(cityWebMapper.toDto(domain.getCity()));

        return dto;
    }
}
```

**Vorteile:**

- ✅ Code-Wiederverwendung
- ✅ Kein doppelter Mapping-Code
- ✅ Änderungen am CityWebMapper werden automatisch übernommen

#### `imports` - Java-Klassen für `expression` importieren

**Zweck:**
Wenn Sie in `@Mapping(expression = "...")` Java-Code schreiben, müssen Sie die verwendeten Klassen explizit importieren.

**Beispiel:**

```java
@Mapper(
    componentModel = "spring",
    imports = {UUID.class, LocalDateTime.class}  // ← Importiert UUID und LocalDateTime
)
public interface DoctorWebMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Was MapStruct generiert:**

```java
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DoctorWebMapperImpl implements DoctorWebMapper {

    @Override
    public Doctor toDomain(CreateDoctorRequest request) {
        if (request == null) {
            return null;
        }

        Doctor doctor = new Doctor();

        // ✅ UUID.randomUUID() funktioniert, weil UUID importiert wurde
        doctor.setId(UUID.randomUUID());

        // ✅ LocalDateTime.now() funktioniert, weil LocalDateTime importiert wurde
        doctor.setCreatedAt(LocalDateTime.now());

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());

        return doctor;
    }
}
```

**Ohne `imports` (❌ Compile-Fehler):**

```java
@Mapper(componentModel = "spring")  // KEIN imports!
public interface DoctorWebMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")  // ❌ Compile-Fehler: UUID nicht gefunden
    Doctor toDomain(CreateDoctorRequest request);
}
```

#### `unmappedTargetPolicy` - Umgang mit nicht gemappten Feldern

**Zweck:**
Definiert, wie MapStruct reagieren soll, wenn ein Feld im Ziel-Objekt nicht gemappt wurde.

**Mögliche Werte:**

| Wert | Beschreibung | Empfehlung |
|------|--------------|------------|
| `ReportingPolicy.IGNORE` | Keine Meldung, Feld bleibt null | Nicht empfohlen (Fehler werden übersehen) |
| `ReportingPolicy.WARN` | Gelbe Warnung beim Kompilieren | **Standard**, gut für Entwicklung |
| `ReportingPolicy.ERROR` | Roter Fehler, Build schlägt fehl | **Empfohlen für Production** |

**Beispiel mit `ReportingPolicy.ERROR`:**

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR  // ← Build schlägt fehl, wenn Felder fehlen
)
public interface DoctorWebMapper {

    // ❌ FEHLER: "Unmapped target property: createdAt"
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Lösung: Felder explizit ignorieren:**

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DoctorWebMapper {

    @Mapping(target = "id", ignore = true)  // ✅ Explizit ignoriert
    @Mapping(target = "createdAt", ignore = true)  // ✅ Explizit ignoriert
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Vorteile von `ReportingPolicy.ERROR`:**

- ✅ Zwingt Sie, alle Felder explizit zu mappen oder zu ignorieren
- ✅ Verhindert vergessene Felder
- ✅ Compile-Zeit-Sicherheit

#### `nullValuePropertyMappingStrategy` - Umgang mit null-Werten

**Zweck:**
Definiert, wie MapStruct mit null-Werten beim Mapping umgehen soll. **Besonders wichtig für UPDATE-Operationen!**

**Mögliche Werte:**

| Wert | Beschreibung | Verwendung |
|------|--------------|------------|
| `NullValuePropertyMappingStrategy.SET_TO_NULL` | null-Werte werden übernommen (Ziel wird auf null gesetzt) | **Standard**, gut für CREATE |
| `NullValuePropertyMappingStrategy.IGNORE` | null-Werte werden ignoriert (Ziel bleibt unverändert) | **Empfohlen für UPDATE** |

**Problem ohne `IGNORE` (UPDATE-Operation):**

```java
// Bestehender Doctor in der DB:
Doctor existing = {
    id = "123",
    firstName = "Hans",
    lastName = "Müller",
    practiceId = "456"
}

// Update-Request vom Client (nur lastName soll geändert werden):
UpdateDoctorRequest request = {
    firstName = null,      // Client sendet null (= keine Änderung gewünscht)
    lastName = "Schmidt",  // Soll geändert werden
    practiceId = null      // Client sendet null (= keine Änderung gewünscht)
}

// Mapper OHNE nullValuePropertyMappingStrategy.IGNORE:
Doctor updated = mapper.toDomain(request);
// → updated = { firstName = null, lastName = "Schmidt", practiceId = null }
// ❌ DATENVERLUST! firstName und practiceId wurden auf null gesetzt!
```

**Lösung mit `IGNORE`:**

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // ← null-Werte ignorieren
)
public interface DoctorWebMapper {

    /**
     * Update-Methode: Nur nicht-null Werte werden übernommen
     */
    void updateDomain(@MappingTarget Doctor target, UpdateDoctorRequest source);
}

// Verwendung im Service:
public Doctor update(UUID id, UpdateDoctorRequest request) {
    // 1. Bestehenden Doctor laden
    Doctor existing = doctorRepository.findById(id).orElseThrow();
    // existing = { firstName = "Hans", lastName = "Müller", practiceId = "456" }

    // 2. Nur nicht-null Werte werden übernommen
    mapper.updateDomain(existing, request);
    // existing = { firstName = "Hans", lastName = "Schmidt", practiceId = "456" }
    // ✅ firstName und practiceId blieben unverändert!

    // 3. Speichern
    return doctorRepository.save(existing);
}
```

**Was MapStruct generiert:**

```java
@Override
public void updateDomain(Doctor target, UpdateDoctorRequest source) {
    if (source == null) {
        return;
    }

    // ✅ Nur wenn firstName nicht null ist:
    if (source.getFirstName() != null) {
        target.setFirstName(source.getFirstName());
    }

    // ✅ Nur wenn lastName nicht null ist:
    if (source.getLastName() != null) {
        target.setLastName(source.getLastName());
    }

    // ✅ Nur wenn practiceId nicht null ist:
    if (source.getPracticeId() != null) {
        target.setPracticeId(source.getPracticeId());
    }
}
```

**Empfehlung:**

- **CREATE**: `nullValuePropertyMappingStrategy` NICHT setzen (Standard = `SET_TO_NULL`)
- **UPDATE**: `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE`

### 4.2 `@Mapping` - Feld-Mapping konfigurieren

Die `@Mapping` Annotation konfiguriert, wie ein einzelnes Feld gemappt werden soll.

#### Attribute von `@Mapping`

| Attribut | Typ | Zweck | Beispiel |
|----------|-----|-------|----------|
| `target` | String | Name des Feldes im **Ziel-Objekt** (PFLICHT) | `target = "firstName"` |
| `source` | String | Name des Feldes im **Quell-Objekt** | `source = "name"` |
| `ignore` | boolean | Feld wird nicht gemappt (bleibt null oder unverändert) | `ignore = true` |
| `constant` | String | Fester Wert, der **immer** gesetzt wird | `constant = "ACTIVE"` |
| `defaultValue` | String | Wert, der gesetzt wird, wenn Quelle null ist | `defaultValue = "N/A"` |
| `expression` | String | Java-Code, der ausgeführt wird | `expression = "java(UUID.randomUUID())"` |
| `qualifiedByName` | String | Name einer benutzerdefinierten Mapping-Methode | `qualifiedByName = "toUpperCase"` |

#### `target` - Ziel-Feld (PFLICHT)

**Zweck:**
Gibt an, welches Feld im **Ziel-Objekt** gemappt werden soll.

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Mappt firstName vom Request zum Doctor
     */
    @Mapping(target = "id", ignore = true)  // target = "id" → Doctor.id
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Verschachtelte Felder:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Mappt verschachtelte Felder
     */
    @Mapping(target = "practice.city.name", source = "cityName")
    // doctor.getPractice().getCity().setName(request.getCityName())
    Doctor toDomain(CreateDoctorRequest request);
}
```

#### `source` - Quell-Feld (Woher kommen die Daten?)

**Zweck:**
`source` sagt MapStruct: **"Nimm den Wert aus DIESEM Feld des Quell-Objekts"**

Das `source` Attribut gibt an, welches Feld im **Quell-Objekt** als Quelle verwendet werden soll.
**Optional**, wenn Quell- und Ziel-Feld denselben Namen haben (dann macht MapStruct es automatisch).

---

**Beispiel 1: Einfaches Feld - Unterschiedliche Feldnamen**

```java
// Quell-Objekt (Request)
public class DoctorRequest {
    private String vorname;      // ← Hier lesen wir (deutscher Name)
    private String nachname;     // ← Hier lesen wir
    private String emailAdresse; // ← Hier lesen wir
}

// Ziel-Objekt (Domain)
public class Doctor {
    private String firstName;    // ← Hier schreiben wir (englischer Name)
    private String lastName;     // ← Hier schreiben wir
    private String email;        // ← Hier schreiben wir
}

// Mapper
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(source = "vorname", target = "firstName")
    @Mapping(source = "nachname", target = "lastName")
    @Mapping(source = "emailAdresse", target = "email")
    Doctor toDomain(DoctorRequest request);
}

// Was passiert:
// 1. Lies request.vorname      → Schreibe in doctor.firstName
// 2. Lies request.nachname     → Schreibe in doctor.lastName
// 3. Lies request.emailAdresse → Schreibe in doctor.email
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(DoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // source = "vorname" → target = "firstName"
    doctor.setFirstName(request.getVorname());

    // source = "nachname" → target = "lastName"
    doctor.setLastName(request.getNachname());

    // source = "emailAdresse" → target = "email"
    doctor.setEmail(request.getEmailAdresse());

    return doctor;
}
```

---

**Beispiel 2: Verschachtelte Objekte (Nested Objects) - Eine Ebene tief**

Das ist der **wichtigste Use-Case** für `source`!

```java
// Quell-Objekt (Request)
public class AppointmentRequest {
    private Long doctorId;        // ← Nur die ID! (einfacher Typ)
    private Long patientId;       // ← Nur die ID!
    private String appointmentDate;
}

// Ziel-Objekt (Domain)
public class Appointment {
    private Doctor doctor;        // ← Ganzes Objekt! (komplexer Typ)
    private Patient patient;      // ← Ganzes Objekt!
    private LocalDateTime appointmentDate;
}

public class Doctor {
    private Long id;              // ← Hier soll die doctorId hin!
    private String firstName;
}

public class Patient {
    private Long id;              // ← Hier soll die patientId hin!
    private String name;
}

// Mapper
@Mapper(componentModel = "spring")
public interface AppointmentWebMapper {

    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "patientId", target = "patient.id")
    Appointment toDomain(AppointmentRequest request);
}

// Was bedeutet "doctor.id"?
// Der Punkt (.) bedeutet: "Gehe eine Ebene tiefer ins Objekt"
// target = "doctor.id" bedeutet: appointment.doctor.id
```

**Was passiert Schritt für Schritt:**

```java
// Client schickt:
AppointmentRequest request = new AppointmentRequest();
request.setDoctorId(123L);
request.setPatientId(456L);
request.setAppointmentDate("2026-03-15T10:00");

Appointment appointment = appointmentMapper.toDomain(request);

// MapStruct macht:
// 1. Lies request.doctorId (= 123L)
// 2. Erstelle ein neues Doctor-Objekt
// 3. Setze doctor.id = 123L
// 4. Setze appointment.doctor = dieses Doctor-Objekt

// 5. Lies request.patientId (= 456L)
// 6. Erstelle ein neues Patient-Objekt
// 7. Setze patient.id = 456L
// 8. Setze appointment.patient = dieses Patient-Objekt

// Ergebnis:
appointment.getDoctor().getId();  // → 123L
appointment.getPatient().getId(); // → 456L
```

**Was MapStruct generiert:**

```java
@Override
public Appointment toDomain(AppointmentRequest request) {
    if (request == null) {
        return null;
    }

    Appointment appointment = new Appointment();

    // source = "doctorId" → target = "doctor.id"
    if (request.getDoctorId() != null) {
        Doctor doctor = new Doctor();
        doctor.setId(request.getDoctorId());  // ← doctorId wird in doctor.id geschrieben
        appointment.setDoctor(doctor);
    }

    // source = "patientId" → target = "patient.id"
    if (request.getPatientId() != null) {
        Patient patient = new Patient();
        patient.setId(request.getPatientId());  // ← patientId wird in patient.id geschrieben
        appointment.setPatient(patient);
    }

    return appointment;
}
```

---

**Beispiel 3: Mehrere Ebenen tief (Deep Nesting) - Drei Ebenen**

Wenn Objekte SEHR tief verschachtelt sind:

```java
// Quell-Objekt (Request)
public class DoctorRequest {
    private String city;           // ← Einfacher String direkt
    private String street;
    private String postalCode;
}

// Ziel-Objekt (Domain) - SEHR verschachtelt!
public class Doctor {
    private Practice practice;     // ← Erste Ebene
}

public class Practice {
    private Address address;       // ← Zweite Ebene
}

public class Address {
    private City city;             // ← Dritte Ebene
    private String street;
    private String postalCode;
}

public class City {
    private String name;           // ← HIER soll der String-Wert hin! (Vierte Ebene)
}

// Mapper
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(source = "city", target = "practice.address.city.name")
    @Mapping(source = "street", target = "practice.address.street")
    @Mapping(source = "postalCode", target = "practice.address.postalCode")
    Doctor toDomain(DoctorRequest request);
}

// Was bedeutet "practice.address.city.name"?
// → Gehe 4 Ebenen tief:
//   doctor.practice.address.city.name
```

**Was passiert Schritt für Schritt:**

```java
// Client schickt:
DoctorRequest request = new DoctorRequest();
request.setCity("Berlin");
request.setStreet("Hauptstraße 1");
request.setPostalCode("10115");

Doctor doctor = doctorMapper.toDomain(request);

// MapStruct macht bei source = "city" → target = "practice.address.city.name":
// 1. Lies request.city (= "Berlin")
// 2. Erstelle: Practice practice = new Practice();
// 3. Erstelle: Address address = new Address();
// 4. Erstelle: City city = new City();
// 5. Setze city.name = "Berlin"           // ← Hier landet der Wert!
// 6. Setze address.city = city;
// 7. Setze practice.address = address;
// 8. Setze doctor.practice = practice;

// Ergebnis (4 Ebenen tief!):
doctor.getPractice()
      .getAddress()
      .getCity()
      .getName();  // → "Berlin"
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(DoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // source = "city" → target = "practice.address.city.name"
    if (request.getCity() != null) {
        Practice practice = new Practice();
        Address address = new Address();
        City city = new City();
        city.setName(request.getCity());    // ← "Berlin" landet hier!
        address.setCity(city);
        practice.setAddress(address);
        doctor.setPractice(practice);
    }

    // source = "street" → target = "practice.address.street"
    if (request.getStreet() != null) {
        if (doctor.getPractice() == null) {
            doctor.setPractice(new Practice());
        }
        if (doctor.getPractice().getAddress() == null) {
            doctor.getPractice().setAddress(new Address());
        }
        doctor.getPractice().getAddress().setStreet(request.getStreet());
    }

    // Gleiches für postalCode...

    return doctor;
}
```

---

**Beispiel 4: Umgekehrte Richtung (Domain → DTO) - Flach machen**

Verschachtelte Objekte → Flache Struktur:

```java
// Quell-Objekt (Domain) - verschachtelt
public class Doctor {
    private Practice practice;
}

public class Practice {
    private Address address;
}

public class Address {
    private City city;
}

public class City {
    private String name;  // ← Wert ist hier tief versteckt!
}

// Ziel-Objekt (DTO) - flach
public class DoctorDto {
    private String cityName;  // ← Einfacher String!
}

// Mapper
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(source = "practice.address.city.name", target = "cityName")
    DoctorDto toDto(Doctor doctor);
}

// Was passiert:
// source = "practice.address.city.name" → Lies aus doctor.practice.address.city.name
// target = "cityName"                   → Schreibe in dto.cityName
```

**Was MapStruct generiert:**

```java
@Override
public DoctorDto toDto(Doctor doctor) {
    if (doctor == null) {
        return null;
    }

    DoctorDto dto = new DoctorDto();

    // Lies tief verschachtelten Wert
    if (doctor.getPractice() != null
        && doctor.getPractice().getAddress() != null
        && doctor.getPractice().getAddress().getCity() != null) {

        String cityName = doctor.getPractice()
                                .getAddress()
                                .getCity()
                                .getName();  // ← "München"
        dto.setCityName(cityName);           // ← Setze flach
    }

    return dto;
}
```

---

**Beispiel 5: Mehrere Parameter - source aus verschiedenen Quellen**

Wenn deine Mapper-Methode **mehrere Parameter** hat:

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Zwei Parameter: request UND id
     * source kann aus beiden kommen!
     */
    @Mapping(source = "id", target = "id")                    // ← Aus dem 2. Parameter
    @Mapping(source = "request.firstName", target = "firstName")  // ← Aus dem 1. Parameter
    @Mapping(source = "request.lastName", target = "lastName")
    Doctor toDomain(UpdateDoctorRequest request, UUID id);
}

// WICHTIG: Bei mehreren Parametern musst du "request." oder "id." voranstellen!
// Ohne Präfix nimmt MapStruct automatisch den ersten Parameter.
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(UpdateDoctorRequest request, UUID id) {
    if (request == null && id == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // source = "id" → Aus dem zweiten Parameter
    doctor.setId(id);

    if (request != null) {
        // source = "request.firstName" → Aus dem ersten Parameter
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
    }

    return doctor;
}
```

---

**Zusammenfassung: source verstehen**

```java
@Mapping(source = "feldImQuellObjekt", target = "feldImZielObjekt")

// Beispiele:

// Einfach:
@Mapping(source = "vorname", target = "firstName")
         ↑                      ↑
         |                      |
    Lies aus Request        Schreibe in Domain
    request.vorname         doctor.firstName


// Eine Ebene tief:
@Mapping(source = "doctorId", target = "doctor.id")
         ↑                      ↑
         |                      |
    request.doctorId       domain.doctor.id
    (Long)                 (Doctor-Objekt mit id-Feld)


// Drei Ebenen tief:
@Mapping(source = "city", target = "practice.address.city.name")
         ↑                      ↑
         |                      |____________________________
         |                                                    |
    request.city          domain.practice.address.city.name
    (String)              (String im City-Objekt, 4 Ebenen tief)


// Mehrere Parameter:
@Mapping(source = "request.firstName", target = "firstName")
@Mapping(source = "id", target = "id")
         ↑                      ↑
         |                      |
    Aus Parameter 2        In Domain schreiben
```

---

**Häufige Fehler mit `source`**

❌ **Falsch:**
```java
@Mapping(source = "request.doctorId", target = "doctor.id")
// "request." ist FALSCH bei nur einem Parameter!
// MapStruct weiß schon, dass es das Request-Objekt ist
```

✅ **Richtig:**
```java
@Mapping(source = "doctorId", target = "doctor.id")
// Bei nur einem Parameter: KEIN Präfix!
```

❌ **Falsch:**
```java
@Mapping(source = "firstName", target = "id")
// Typ-Fehler: String kann nicht zu UUID konvertiert werden!
```

✅ **Richtig:**
```java
@Mapping(source = "firstName", target = "firstName")
// Typen müssen kompatibel sein!
```

---

**Wann brauchst du KEIN `source`?**

Wenn Quell- und Ziel-Feld **den gleichen Namen** haben:

```java
// Request
public class DoctorRequest {
    private String firstName;  // ← Gleicher Name
    private String lastName;   // ← Gleicher Name
}

// Domain
public class Doctor {
    private String firstName;  // ← Gleicher Name
    private String lastName;   // ← Gleicher Name
}

// Mapper - KEIN source nötig!
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {
    Doctor toDomain(DoctorRequest request);
    // MapStruct mapped automatisch:
    // request.firstName → doctor.firstName
    // request.lastName → doctor.lastName
}
```

#### `ignore` - Feld ignorieren

**Zweck:**
Das Ziel-Feld wird **nicht gemappt** und bleibt null (bei CREATE) oder unverändert (bei UPDATE).

**Wann verwenden?**

- ID wird vom Service/Datenbank generiert
- Berechnete Felder (z.B. `fullName` wird aus `firstName` + `lastName` berechnet)
- Felder, die später manuell gesetzt werden

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(target = "id", ignore = true)          // ID wird vom Service generiert
    @Mapping(target = "createdAt", ignore = true)   // Wird vom Service gesetzt
    @Mapping(target = "updatedAt", ignore = true)   // Wird vom Service gesetzt
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(CreateDoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ id bleibt null (wird ignoriert)
    // ✅ createdAt bleibt null (wird ignoriert)
    // ✅ updatedAt bleibt null (wird ignoriert)

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    return doctor;
}
```

#### `constant` - Fester Wert

**Zweck:**
Setzt ein Feld **immer** auf einen festen Wert, unabhängig von der Quelle.

**Wann verwenden?**

- Status-Felder (z.B. `status = "ACTIVE"` für neue Objekte)
- Versions-Felder (z.B. `version = "1"`)
- Default-Werte, die **nie** null sein dürfen

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(target = "status", constant = "ACTIVE")      // Immer "ACTIVE"
    @Mapping(target = "version", constant = "1")           // Immer 1
    @Mapping(target = "deleted", constant = "false")       // Immer false
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(CreateDoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ Feste Werte
    doctor.setStatus("ACTIVE");
    doctor.setVersion(1);
    doctor.setDeleted(false);

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    return doctor;
}
```

**Unterschied zu `defaultValue`:**

| Attribut | Verhalten | Wann verwenden |
|----------|-----------|----------------|
| `constant` | **Immer** dieser Wert, auch wenn Quelle nicht null ist | Status-Felder, die immer gleich sein sollen |
| `defaultValue` | **Nur** wenn Quelle null ist | Optional-Felder mit Fallback-Wert |

**Beispiel Unterschied:**

```java
// Request hat: status = "INACTIVE"

// Mit constant:
@Mapping(target = "status", constant = "ACTIVE")
// → doctor.status = "ACTIVE"  (request.status wird ignoriert!)

// Mit defaultValue:
@Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
// → doctor.status = "INACTIVE"  (request.status wird verwendet)
```

#### `defaultValue` - Fallback-Wert bei null

**Zweck:**
Setzt ein Feld auf einen Fallback-Wert, **nur** wenn die Quelle null ist.

**Wann verwenden?**

- Optional-Felder, die einen sinnvollen Default-Wert haben sollen
- Felder, die nie null sein dürfen (z.B. Listen → `[]`)

**Beispiel 1: Einfache Felder (String, Integer, Boolean)**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    @Mapping(target = "description", source = "description", defaultValue = "N/A")
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(CreateDoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ Wenn request.status null ist → "ACTIVE", sonst request.status
    if (request.getStatus() != null) {
        doctor.setStatus(request.getStatus());
    } else {
        doctor.setStatus("ACTIVE");
    }

    // ✅ Wenn request.description null ist → "N/A", sonst request.description
    if (request.getDescription() != null) {
        doctor.setDescription(request.getDescription());
    } else {
        doctor.setDescription("N/A");
    }

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    return doctor;
}
```

---

**Beispiel 2: Listen/Collections mit `defaultValue`**

Für Listen, Sets oder andere komplexe Objekte brauchst du die `expression`-Syntax mit `java(...)`:

```java
// Request
public class DoctorRequest {
    private List<String> specialities;  // Kann null sein!
}

// Domain
public class Doctor {
    private List<String> specialities;
}

// Mapper
@Mapper(componentModel = "spring")
public interface DoctorMapper {

    @Mapping(source = "specialities", target = "specialities", defaultValue = "java(new java.util.ArrayList<>())")
    Doctor toDomain(DoctorRequest request);
}

// WICHTIG: Für Listen/Objekte brauchst du expression-Syntax!
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(DoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ Wenn request.specialities null ist → neue leere ArrayList
    if (request.getSpecialities() != null) {
        List<String> list = new ArrayList<>(request.getSpecialities());
        doctor.setSpecialities(list);
    } else {
        doctor.setSpecialities(new java.util.ArrayList<>());
    }

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    return doctor;
}
```

**Test:**

```java
// Client schickt keine Liste:
DoctorRequest request = new DoctorRequest();
request.setSpecialities(null);  // ← null

Doctor doctor = mapper.toDomain(request);

// Ergebnis:
doctor.getSpecialities();  // → [] (leere Liste statt null!)


// Client schickt Liste:
DoctorRequest request2 = new DoctorRequest();
request2.setSpecialities(List.of("CARDIOLOGY", "DERMATOLOGY"));

Doctor doctor2 = mapper.toDomain(request2);

// Ergebnis:
doctor2.getSpecialities();  // → ["CARDIOLOGY", "DERMATOLOGY"] (Client-Wert!)
```

---

#### `expression` - Java-Code ausführen

**Zweck:**
Führt beliebigen Java-Code aus, um den Wert eines Feldes zu berechnen.

**Wann verwenden?**

- UUID generieren
- Aktuelles Datum setzen
- Berechnungen (z.B. Preis * 1.19)
- Komplexe Transformationen

**Wichtig:** Verwendete Klassen müssen in `imports` deklariert werden!

**Beispiel:**

```java
@Mapper(
    componentModel = "spring",
    imports = {UUID.class, LocalDateTime.class}  // ← WICHTIG!
)
public interface DoctorWebMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "fullName", expression = "java(request.getFirstName() + \" \" + request.getLastName())")
    Doctor toDomain(CreateDoctorRequest request);
}
```

**Was MapStruct generiert:**

```java
import java.time.LocalDateTime;
import java.util.UUID;

@Override
public Doctor toDomain(CreateDoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ Expression wird ausgeführt
    doctor.setId(UUID.randomUUID());
    doctor.setCreatedAt(LocalDateTime.now());
    doctor.setFullName(request.getFirstName() + " " + request.getLastName());

    doctor.setFirstName(request.getFirstName());
    doctor.setLastName(request.getLastName());

    return doctor;
}
```

**Syntax:**

```java
expression = "java(JAVA_CODE_HIER)"
```

**Beispiele:**

```java
// UUID generieren
@Mapping(target = "id", expression = "java(UUID.randomUUID())")

// Aktuelles Datum
@Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")

// Berechnung
@Mapping(target = "priceWithTax", expression = "java(source.getPrice() * 1.19)")

// String-Konkatenation
@Mapping(target = "fullName", expression = "java(source.getFirstName() + \" \" + source.getLastName())")

// Ternärer Operator
@Mapping(target = "status", expression = "java(source.isActive() ? \"ACTIVE\" : \"INACTIVE\")")
```

#### `qualifiedByName` - Benutzerdefinierte Mapping-Methode

**Zweck:**
Ruft eine benutzerdefinierte Methode auf, um komplexe Transformationen durchzuführen.

**Wann verwenden?**

- Komplexe Business-Logik
- Wiederverwendbare Transformationen
- Validierungen während des Mappings

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Verwendet die Methode "toUpperCase" für firstName
     */
    @Mapping(target = "firstName", source = "firstName", qualifiedByName = "toUpperCase")
    Doctor toDomain(CreateDoctorRequest request);

    /**
     * Benutzerdefinierte Mapping-Methode
     *
     * @Named markiert die Methode, damit sie von @Mapping gefunden werden kann
     */
    @Named("toUpperCase")
    default String toUpperCase(String value) {
        if (value == null) {
            return null;
        }
        return value.toUpperCase();
    }
}
```

**Was MapStruct generiert:**

```java
@Override
public Doctor toDomain(CreateDoctorRequest request) {
    if (request == null) {
        return null;
    }

    Doctor doctor = new Doctor();

    // ✅ Ruft toUpperCase() auf
    doctor.setFirstName(toUpperCase(request.getFirstName()));
    doctor.setLastName(request.getLastName());

    return doctor;
}

@Override
public String toUpperCase(String value) {
    if (value == null) {
        return null;
    }
    return value.toUpperCase();
}
```

**Beispiel mit Parametern:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(target = "specialityIds", qualifiedByName = "filterInvalidIds")
    Doctor toDomain(CreateDoctorRequest request);

    @Named("filterInvalidIds")
    default List<UUID> filterInvalidIds(List<UUID> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        // Filtere null-Werte
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
```

### 4.3 `@MappingTarget` - Bestehendes Objekt updaten

Die `@MappingTarget` Annotation markiert ein Methoden-Parameter als Ziel-Objekt, das **aktualisiert** werden soll (statt ein neues Objekt zu erstellen).

**Zweck:**
Für **UPDATE-Operationen**: Ändern Sie ein bestehendes Objekt, statt ein neues zu erstellen.

**Beispiel:**

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // ← WICHTIG für Updates!
)
public interface DoctorWebMapper {

    /**
     * UPDATE: Ändert ein bestehendes Doctor-Objekt
     *
     * @param target Das bestehende Doctor-Objekt (wird geändert!)
     * @param source Das Update-Request mit den neuen Werten
     */
    void updateDomain(@MappingTarget Doctor target, UpdateDoctorRequest source);
}
```

**Verwendung im Service:**

```java
@Service
public class DoctorService {

    @Autowired
    private DoctorWebMapper mapper;

    @Autowired
    private DoctorRepository repository;

    public Doctor update(UUID id, UpdateDoctorRequest request) {
        // 1. Bestehenden Doctor laden
        Doctor existing = repository.findById(id).orElseThrow();
        // existing = { id="123", firstName="Hans", lastName="Müller" }

        // 2. Nur nicht-null Felder werden geändert
        mapper.updateDomain(existing, request);
        // existing = { id="123", firstName="Hans", lastName="Schmidt" }
        // ✅ existing wurde geändert (kein neues Objekt!)

        // 3. Speichern
        return repository.save(existing);
    }
}
```

**Was MapStruct generiert:**

```java
@Override
public void updateDomain(Doctor target, UpdateDoctorRequest source) {
    if (source == null) {
        return;  // ← target bleibt unverändert
    }

    // ✅ Nur nicht-null Werte werden übernommen (wegen nullValuePropertyMappingStrategy.IGNORE)
    if (source.getFirstName() != null) {
        target.setFirstName(source.getFirstName());
    }

    if (source.getLastName() != null) {
        target.setLastName(source.getLastName());
    }

    if (source.getPracticeId() != null) {
        target.setPracticeId(source.getPracticeId());
    }
}
```

**Unterschied zu normalem Mapping:**

| Methode | Rückgabe | Verhalten |
|---------|----------|-----------|
| `Doctor toDomain(Request)` | **Neues** Doctor-Objekt | CREATE-Operation |
| `void updateDomain(@MappingTarget Doctor, Request)` | **void** (ändert das übergebene Objekt) | UPDATE-Operation |

### 4.4 `@Named` - Benutzerdefinierte Methode markieren

Die `@Named` Annotation markiert eine Methode, damit sie von `@Mapping(qualifiedByName = "...")` gefunden werden kann.

**Beispiel:**

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    @Mapping(target = "firstName", qualifiedByName = "toUpperCase")
    Doctor toDomain(CreateDoctorRequest request);

    @Named("toUpperCase")  // ← Macht die Methode für qualifiedByName sichtbar
    default String toUpperCase(String value) {
        return value != null ? value.toUpperCase() : null;
    }
}
```

### 4.5 Zusammenfassung aller wichtigen Annotations

| Annotation | Wo verwendet | Zweck |
|------------|--------------|-------|
| `@Mapper` | Interface | Markiert Interface als MapStruct-Mapper |
| `@Mapping` | Methode | Konfiguriert Mapping eines Feldes |
| `@MappingTarget` | Parameter | Markiert Parameter als Ziel-Objekt für UPDATE |
| `@Named` | Methode | Macht Methode für `qualifiedByName` sichtbar |

---

## 5. Best Practices

### 5.1 Namenskonvention

**Mapper-Klassennamen:**

| Mapper-Typ | Namenskonvention | Beispiel |
|------------|------------------|----------|
| WebMapper | `<Entity>WebMapper` | `DoctorWebMapper`, `PracticeWebMapper` |
| PersistenceMapper | `<Entity>EntityMapper` | `DoctorEntityMapper`, `PracticeEntityMapper` |

**Methodennamen mit Richtungen:**

#### WebMapper (Client ↔ Server)

| Von | Nach | Richtung | Methodenname | Beispiel |
|-----|------|----------|--------------|----------|
| Request | Domain | Client → Server | `toDomain(Request)` | `toDomain(CreateDoctorRequest)` |
| Domain | Dto | Server → Client | `toDto(Domain)` | `toDto(Doctor)` |

#### PersistenceMapper (Server ↔ Datenbank)

| Von | Nach | Richtung | Methodenname | Beispiel |
|-----|------|----------|--------------|----------|
| Domain | Entity | Server → DB | `toEntity(Domain)` | `toEntity(Doctor)` |
| Entity | Domain | DB → Server | `toDomain(Entity)` | `toDomain(DoctorEntity)` |

**Merkhilfe:**

```
CLIENT ──Request──→ SERVER ──Domain──→ DATABASE
       toDomain()         toEntity()

CLIENT ←──Dto──── SERVER ←──Domain──── DATABASE
       toDto()           toDomain()
```

### 5.2 Package-Struktur

```
infrastructure/adapter/
├── incoming/
│   └── web/
│       ├── controller/
│       │   ├── DoctorController.java
│       │   ├── PracticeController.java
│       │   └── SlotController.java
│       └── mapper/                          # WebMapper (Client ↔ Server)
│           ├── DoctorWebMapper.java         # Request ↔ Domain
│           ├── PracticeWebMapper.java
│           └── SlotWebMapper.java
└── outgoing/
    └── persistence/
        ├── DoctorPersistenceAdapter.java
        ├── PracticePersistenceAdapter.java
        ├── entity/
        │   ├── DoctorEntity.java
        │   ├── PracticeEntity.java
        │   └── SlotEntity.java
        ├── repository/
        │   ├── DoctorJpaRepository.java
        │   ├── PracticeJpaRepository.java
        │   └── SlotJpaRepository.java
        └── mapper/                           # PersistenceMapper (Server ↔ DB)
            ├── DoctorEntityMapper.java       # Entity ↔ Domain
            ├── PracticeEntityMapper.java
            └── SlotEntityMapper.java
```

### 5.3 Immer `componentModel = "spring"` verwenden

```java
@Mapper(componentModel = "spring")  // ← IMMER für Spring Boot!
public interface DoctorWebMapper {
    // ...
}
```

### 5.4 Verwenden Sie `unmappedTargetPolicy = ERROR` für Sicherheit

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR  // ← Zwingt explizites Mapping
)
public interface DoctorWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Doctor toDomain(CreateDoctorRequest request);
}
```

### 5.5 UPDATE-Mapper mit `nullValuePropertyMappingStrategy = IGNORE`

```java
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DoctorWebMapper {

    void updateDomain(@MappingTarget Doctor target, UpdateDoctorRequest source);
}
```

### 5.6 Wiederverwendung mit `uses`

```java
@Mapper(
    componentModel = "spring",
    uses = {CityWebMapper.class, SpecialityWebMapper.class}
)
public interface PracticeWebMapper {
    PracticeDto toDto(Practice domain);
}
```

### 5.7 Dokumentation in Mapper-Interfaces

```java
@Mapper(componentModel = "spring")
public interface DoctorWebMapper {

    /**
     * Konvertiert CreateDoctorRequest zu Doctor
     *
     * @param request Das Request-Objekt vom Client
     * @return Ein neues Doctor Domain-Modell (ohne ID)
     *
     * Automatisches Mapping:
     * - request.firstName → doctor.firstName
     * - request.lastName → doctor.lastName
     *
     * Ignoriert:
     * - doctor.id (wird vom Service generiert)
     */
    @Mapping(target = "id", ignore = true)
    Doctor toDomain(CreateDoctorRequest request);
}
```

---

## 🎉 Fazit

Sie haben jetzt gelernt:

✅ **Klassische Mapper**: Manuelle Methode mit vollem Verständnis
✅ **MapStruct Setup**: Wie Sie MapStruct in Ihr Projekt integrieren
✅ **MapStruct Mapper erstellen**: Schritt-für-Schritt Anleitung
✅ **Alle Annotations**: Vollständige Erklärung mit Beispielen
✅ **Best Practices**: Konventionen für Production-Code
✅ **Mapper-Richtungen**: Klare Unterscheidung zwischen WebMapper und PersistenceMapper

---

### 📊 Übersichtsgrafik: Datenfluss mit Mappern

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT (Vue.js)                             │
│                     CreateDoctorRequest / DoctorDto                 │
└─────────────────────────────────────────────────────────────────────┘
                              ↕
                    ┌─────────────────────┐
                    │   HTTP REST API     │
                    └─────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────────┐
│                    WEB LAYER (Controller)                           │
│                                                                     │
│  📥 Request empfangen                                               │
│      ↓                                                              │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  WebMapper.toDomain(CreateDoctorRequest)                      ║ │
│  ║  RICHTUNG: Client → Server                                    ║ │
│  ║  ERGEBNIS: Doctor (Domain Model)                              ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│      ↓                                                              │
│  📦 Domain Model an Service übergeben                               │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER (Service)                      │
│                                                                     │
│  Business-Logik ausführen                                           │
│      ↓                                                              │
│  📦 Domain Model an Persistence Adapter übergeben                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│               PERSISTENCE LAYER (Adapter + Repository)              │
│                                                                     │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  PersistenceMapper.toEntity(Doctor)                           ║ │
│  ║  RICHTUNG: Server → Datenbank                                 ║ │
│  ║  ERGEBNIS: DoctorEntity (JPA Entity)                          ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│      ↓                                                              │
│  💾 Entity in Datenbank speichern                                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                      DATABASE (PostgreSQL)                          │
│                         DoctorEntity                                │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
                     (Daten gespeichert)
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│               PERSISTENCE LAYER (Adapter + Repository)              │
│                                                                     │
│  💾 Entity aus Datenbank laden                                      │
│      ↓                                                              │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  PersistenceMapper.toDomain(DoctorEntity)                     ║ │
│  ║  RICHTUNG: Datenbank → Server                                 ║ │
│  ║  ERGEBNIS: Doctor (Domain Model)                              ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│      ↓                                                              │
│  📦 Domain Model an Service zurückgeben                             │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER (Service)                      │
│                                                                     │
│  📦 Domain Model an Controller zurückgeben                          │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                    WEB LAYER (Controller)                           │
│                                                                     │
│  ╔═══════════════════════════════════════════════════════════════╗ │
│  ║  WebMapper.toDto(Doctor)                                      ║ │
│  ║  RICHTUNG: Server → Client                                    ║ │
│  ║  ERGEBNIS: DoctorDto                                          ║ │
│  ╚═══════════════════════════════════════════════════════════════╝ │
│      ↓                                                              │
│  📤 Response senden                                                 │
└─────────────────────────────────────────────────────────────────────┘
                              ↕
                    ┌─────────────────────┐
                    │   HTTP REST API     │
                    └─────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT (Vue.js)                             │
│                           DoctorDto                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

### 📝 Zusammenfassung: Welcher Mapper wird wann verwendet?

| Schritt | Aktion | Mapper | Methode | Richtung |
|---------|--------|--------|---------|----------|
| 1 | Client sendet Request | **WebMapper** | `toDomain()` | Request → Domain |
| 2 | Service verarbeitet Domain | - | - | - |
| 3 | Domain wird in DB gespeichert | **PersistenceMapper** | `toEntity()` | Domain → Entity |
| 4 | Entity wird aus DB geladen | **PersistenceMapper** | `toDomain()` | Entity → Domain |
| 5 | Service gibt Domain zurück | - | - | - |
| 6 | Server sendet Response | **WebMapper** | `toDto()` | Domain → Dto |

---

**Empfehlung:** Verwenden Sie **MapStruct** für alle Mapper in Ihrem Projekt! Es ist typsicher, performant und wartbar.

**Nächste Schritte:**

1. ✅ Erstellen Sie `DoctorWebMapper` mit MapStruct (Client ↔ Server)
2. ✅ Erstellen Sie `DoctorEntityMapper` mit MapStruct (Server ↔ Datenbank)
3. ✅ Testen Sie die Mapper in Ihrem Controller
4. ✅ Erstellen Sie weitere Mapper (`PracticeWebMapper`, `SlotWebMapper`)

---

**Dokumentversion:** 1.1
**Letzte Aktualisierung:** 15. Februar 2026
**Autor:** GitHub Copilot

