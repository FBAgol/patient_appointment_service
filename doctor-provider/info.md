# Doctor Provider - Projekt-Informationen

## Architektur-Ansatz: Spec-First (API-First)

Dieses Projekt folgt dem **Spec-First (API-First)** Entwicklungsansatz. Das bedeutet:

### Was ist Spec-First?

Bei diesem Ansatz wird zuerst die API-Spezifikation definiert, bevor der Code implementiert wird. Dies ermöglicht:
- Klare Vertragsgestaltung zwischen Frontend und Backend
- Frühe Abstimmung über API-Endpunkte und Datenmodelle
- Automatische Code-Generierung aus der Spezifikation
- Konsistente API-Dokumentation
- Parallele Entwicklung von Frontend und Backend

### Implementierung in diesem Projekt

#### 1. OpenAPI-Spezifikation

- **Datei:** `src/main/resources/openapi/doctor-provider-api.yaml`
- **Format:** OpenAPI 3.0.3
- **Inhalt:** Vollständige API-Definition mit allen Endpunkten, Datenmodellen und Validierungen

#### 2. Code-Generierung

Das OpenAPI Generator Maven Plugin generiert automatisch:
- **API-Interfaces** im Package `test.doctor_provider.api`
- **DTOs/Models** im Package `test.doctor_provider.api.model`
- **Validierungslogik** basierend auf den OpenAPI-Constraints

#### 3. Controller-Implementierung

Die Controller implementieren die generierten Interfaces:

```java
@RestController
public class DoctorController implements DoctorsApi {
    // Implementierung der API-Methoden
}
```

### Vorteile für dieses Projekt

✅ Automatische API-Dokumentation über Swagger UI  
✅ Type-Safety durch generierte Interfaces  
✅ Konsistente Datenmodelle  
✅ Validierung bereits in der Spezifikation definiert  
✅ Einfache API-Evolution durch Versionierung

---

## 📊 Projekt-Übersicht

### Verwendete Maven Plugins (3)

|             Plugin             |  Version  |                      Zweck                       |
|--------------------------------|-----------|--------------------------------------------------|
| openapi-generator-maven-plugin | 7.10.0    | Generiert API-Interfaces & DTOs aus OpenAPI-Spec |
| maven-compiler-plugin          | (managed) | Kompiliert Java-Code mit Lombok & MapStruct      |
| spring-boot-maven-plugin       | (managed) | Erstellt ausführbare Spring Boot JAR             |

### Verwendete Dependencies (16)

#### Runtime Dependencies (10)

|              Dependency              |  Version  |  Scope  |     Beschreibung     |
|--------------------------------------|-----------|---------|----------------------|
| spring-boot-starter-data-jpa         | (managed) | compile | JPA/Hibernate        |
| spring-boot-starter-validation       | (managed) | compile | Bean Validation      |
| spring-boot-starter-webflux          | (managed) | compile | Reactive Web         |
| springdoc-openapi-starter-webflux-ui | 2.7.0     | compile | Swagger UI           |
| jackson-databind-nullable            | 0.2.6     | compile | Nullable Support     |
| jakarta.validation-api               | (managed) | compile | Validation API       |
| postgresql                           | (managed) | runtime | PostgreSQL Driver    |
| flyway-core                          | (managed) | compile | DB-Migrationen       |
| flyway-database-postgresql           | (managed) | compile | Flyway PostgreSQL    |
| spring-boot-devtools                 | (managed) | runtime | Dev-Tools (optional) |

#### Build-Time Dependencies (3)

|     Dependency      |   Version   |  Scope   |       Beschreibung        |
|---------------------|-------------|----------|---------------------------|
| lombok              | (managed)   | compile  | Code-Generator (optional) |
| mapstruct           | 1.5.5.Final | compile  | Mapper-Generator          |
| mapstruct-processor | 1.5.5.Final | provided | MapStruct Processor       |

#### Test Dependencies (3)

|             Dependency              |  Version  | Scope |   Beschreibung   |
|-------------------------------------|-----------|-------|------------------|
| spring-boot-starter-data-jpa-test   | (managed) | test  | JPA-Tests        |
| spring-boot-starter-validation-test | (managed) | test  | Validation-Tests |
| spring-boot-starter-webflux-test    | (managed) | test  | WebFlux-Tests    |

---

## Projekt-Eigenschaften

### Parent POM

- **Spring Boot Version:** 4.0.1
- **Artifact:** spring-boot-starter-parent

### Projekt-Koordinaten

- **GroupId:** test
- **ArtifactId:** doctor-provider
- **Version:** 0.0.1-SNAPSHOT
- **Name:** doctor-provider
- **Beschreibung:** doctor slots provider project

### Java-Version

- **Java Version:** 25

### Maven-Properties

- **openapi-generator-version:** 7.10.0

---

## Maven Plugins

Folgende Maven-Plugins sind in diesem Projekt installiert:

### 1. openapi-generator-maven-plugin

- **GroupId:** org.openapitools
- **ArtifactId:** openapi-generator-maven-plugin
- **Version:** 7.10.0
- **Funktion:** Generiert API-Interfaces und DTOs aus der OpenAPI-Spezifikation
- **Konfiguration:**
  - Input: `src/main/resources/openapi/doctor-provider-api.yaml`
  - Generator: `spring` (mit Reactive/WebFlux Support)
  - API-Package: `test.doctor_provider.api`
  - Model-Package: `test.doctor_provider.api.model`
  - Reactive: `true` (verwendet Project Reactor)
  - Spring Boot 3: `true`
  - Interface-Only: `true` (generiert nur Interfaces, keine Implementierung)
  - Jakarta EE: `true` (verwendet Jakarta statt javax)

### 2. maven-compiler-plugin

- **GroupId:** org.apache.maven.plugins
- **ArtifactId:** maven-compiler-plugin
- **Funktion:** Kompiliert den Java-Quellcode
- **Konfiguration:**
  - **Annotation Processors:**
    - **Lombok** (1.18.30) - ZUERST (damit MapStruct auf Lombok-generierte Getter/Setter zugreifen kann)
    - **MapStruct Processor** (1.5.5.Final) - DANACH
  - **⚠️ WICHTIG:** Reihenfolge ist entscheidend! Lombok muss VOR MapStruct stehen

### 3. spring-boot-maven-plugin

- **GroupId:** org.springframework.boot
- **ArtifactId:** spring-boot-maven-plugin
- **Funktion:** Erstellt ausführbare Spring Boot JAR/WAR-Dateien
- **Konfiguration:**
  - Schließt Lombok aus dem finalen Build aus

---

## Dependencies

### Spring Boot Dependencies

- **spring-boot-starter-data-jpa** - JPA/Hibernate für Datenbank-Zugriff
- **spring-boot-starter-validation** - Bean Validation Support
- **spring-boot-starter-webflux** - Reactive Web Framework
- **spring-boot-devtools** (runtime, optional) - Entwicklungstools (Hot Reload, etc.)

### OpenAPI & Dokumentation

- **springdoc-openapi-starter-webflux-ui** (2.7.0) - Swagger UI Integration für WebFlux
- **jackson-databind-nullable** (0.2.6) - Support für nullable Felder in generierten Models

### Datenbank

- **postgresql** (runtime) - PostgreSQL JDBC Driver
- **flyway-core** - Flyway Core für Datenbank-Migrationen
- **flyway-database-postgresql** - Flyway PostgreSQL Support (ab Flyway 9.x erforderlich)

### Utilities

- **lombok** (optional) - Reduziert Boilerplate-Code (Getters, Setters, Constructors, Builder, etc.)
- **mapstruct** (1.5.5.Final) - Code-Generator für Type-Safe Mapper (Compile-Zeit)
- **mapstruct-processor** (1.5.5.Final, provided) - Annotation Processor für MapStruct

### Validation

- **jakarta.validation-api** - Jakarta Bean Validation API

### Test Dependencies

- **spring-boot-starter-data-jpa-test** (test scope) - Test-Support für Spring Data JPA
- **spring-boot-starter-validation-test** (test scope) - Test-Support für Validation
- **spring-boot-starter-webflux-test** (test scope) - Test-Support für WebFlux (WebTestClient, etc.)

---

## Build & Code-Generierung

### API-Code generieren

```bash
mvn clean generate-sources
```

Dieser Befehl:
1. Liest die OpenAPI-Spezifikation aus `src/main/resources/openapi/doctor-provider-api.yaml`
2. Generiert API-Interfaces in `target/generated-sources/openapi/`
3. Generiert DTOs/Models mit Jakarta Validation Annotationen
4. Die generierten Dateien werden automatisch zum Classpath hinzugefügt

### MapStruct-Mapper generieren

```bash
mvn clean compile
```

Dieser Befehl:
1. Kompiliert alle Java-Quellen
2. **MapStruct Annotation Processor** generiert Mapper-Implementierungen
3. Generierte Mapper landen in `target/generated-sources/annotations/`
4. **⚠️ WICHTIG:** Bei Mapper-Änderungen immer `mvn clean compile` ausführen!

### Projekt bauen

```bash
mvn clean install
```

### Anwendung starten

```bash
mvn spring-boot:run
```

### Swagger UI aufrufen

Nach dem Start der Anwendung ist die API-Dokumentation verfügbar unter:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Projekt-Struktur

```
doctor-provider/
├── src/main/resources/openapi/
│   └── doctor-provider-api.yaml          # OpenAPI-Spezifikation (Spec-First!)
├── src/main/java/test/doctor_provider/
│   ├── DoctorProviderApplication.java    # Spring Boot Main-Klasse
│   ├── domain/                           # Domain-Modelle & Enums
│   │   ├── model/
│   │   └── enums/
│   ├── application/                      # Use Cases & Ports
│   │   ├── port/
│   │   │   ├── incoming/
│   │   │   └── outgoing/
│   │   └── service/
│   └── infrastructure/                   # Adapter (Web & Persistence)
│       └── adapter/
│           ├── incomming/                # REST-Controller (TODO)
│           │   └── web/
│           │       └── mapper/           # ⭐ Web-Mapper (API-DTO ↔ Domain)
│           └── outgoing/
│               └── persistence/
│                   ├── entity/           # JPA-Entities (TODO)
│                   ├── repository/       # Spring Data Repositories (TODO)
│                   ├── mapper/           # ⭐ Entity-Mapper (Domain ↔ Entity)
│                   └── adapter/          # Persistence-Adapter (TODO)
└── target/generated-sources/
    ├── openapi/                          # Generierte API-Interfaces & DTOs
    └── annotations/                      # Generierte MapStruct-Implementierungen
```

---

## Nächste Schritte

1. **Service-Layer implementieren** - Business-Logik separieren
2. **Repository-Layer** - JPA Entities und Repositories erstellen
3. **Exception Handling** - Globale Error-Handler implementieren
4. **Testing** - Unit- und Integrationstests schreiben
5. **Security** - Authentifizierung und Autorisierung hinzufügen
6. **Database Migration** - Flyway oder Liquibase einrichten

