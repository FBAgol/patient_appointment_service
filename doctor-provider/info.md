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
  - Verwendet Lombok als Annotation Processor

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
- **spring-boot-devtools** - Entwicklungstools (Hot Reload, etc.)

### OpenAPI & Dokumentation
- **springdoc-openapi-starter-webflux-ui** (2.7.0) - Swagger UI Integration für WebFlux
- **jackson-databind-nullable** (0.2.6) - Support für nullable Felder in generierten Models

### Datenbank
- **postgresql** - PostgreSQL JDBC Driver

### Utilities
- **lombok** - Reduziert Boilerplate-Code (Getters, Setters, etc.)

### Validation
- **jakarta.validation-api** - Jakarta Bean Validation API

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
│   └── controller/
│       ├── DoctorController.java         # Implementiert DoctorsApi
│       └── AppointmentController.java    # Implementiert AppointmentsApi
└── target/generated-sources/openapi/     # Generierte API-Interfaces und Models
    └── test/doctor_provider/
        ├── api/                          # Generierte API-Interfaces
        └── api/model/                    # Generierte DTOs
```

---

## Nächste Schritte

1. **Service-Layer implementieren** - Business-Logik separieren
2. **Repository-Layer** - JPA Entities und Repositories erstellen
3. **Exception Handling** - Globale Error-Handler implementieren
4. **Testing** - Unit- und Integrationstests schreiben
5. **Security** - Authentifizierung und Autorisierung hinzufügen
6. **Database Migration** - Flyway oder Liquibase einrichten

