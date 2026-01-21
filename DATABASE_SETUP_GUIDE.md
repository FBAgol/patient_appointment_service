# 🗄️ Datenbank-Setup: Flyway + JPA/Hibernate

**Für:** Doctor-Provider & Patient-Customer Services

---

## 🎯 Überblick: Die beste Strategie für dein Projekt

### **Empfohlener Ansatz:**

```
Flyway (SQL-Migrationen)  →  erstellt DB-Tabellen
         +
JPA/Hibernate (ORM)       →  Zugriff auf Tabellen aus Java
```

---

## ❓ Warum ORM nutzen?

### **Was ist ORM (Object-Relational Mapping)?**

ORM ist eine Technik, die **Java-Objekte automatisch mit Datenbank-Tabellen verknüpft**.

**Ohne ORM (manuelles JDBC):**
```java
// ❌ Viel Code, fehleranfällig
String sql = "SELECT * FROM practice WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setLong(1, practiceId);
ResultSet rs = stmt.executeQuery();
if (rs.next()) {
    Practice practice = new Practice();
    practice.setId(rs.getLong("id"));
    practice.setName(rs.getString("name"));
    // ... 20 weitere Zeilen ...
}
```

**Mit ORM (JPA/Hibernate):**
```java
// ✅ Simpel, sicher, typsicher
Practice practice = practiceRepository.findById(practiceId);
```

### **Vorteile von JPA/Hibernate:**

✅ **Weniger Code** → Boilerplate weg  
✅ **Typsicher** → Compiler findet Fehler  
✅ **SQL Injection Schutz** → Automatisch  
✅ **Caching** → Performance-Optimierung  
✅ **Lazy Loading** → Nur das laden, was benötigt wird  
✅ **Database-agnostic** → Wechsel zwischen PostgreSQL, MySQL, etc. einfacher  
✅ **Spring Data JPA** → Noch weniger Code (Queries aus Methodennamen generiert)

---

## 🏗️ Die Architektur-Strategie

### **Wichtig für Hexagonale Architektur:**

In deinem Projekt hast du **zwei verschiedene "Modelle"**:

```
1. Domain Models (domain/model/)
   → Reine Business-Objekte
   → KEINE JPA-Annotationen (@Entity, @Table, etc.)
   → KEINE Abhängigkeit zu Spring/Hibernate
   → Business-Logik (Validierung, Berechnungen)

2. JPA Entities (infrastructure/adapter/persistence/)
   → Technische DB-Objekte
   → MIT JPA-Annotationen (@Entity, @Table, @Column, etc.)
   → Mapped auf DB-Tabellen
   → KEINE Business-Logik

3. Mapper (infrastructure/adapter/persistence/)
   → Konvertiert zwischen Domain Model ↔ JPA Entity
```

### **Warum diese Trennung?**

✅ **Domain bleibt unabhängig** → Keine DB-Technologie-Abhängigkeit  
✅ **Austauschbar** → DB-Technologie wechselbar ohne Domain-Änderung  
✅ **Testbar** → Domain Models ohne DB testbar  
✅ **Clean Architecture** → Abhängigkeiten zeigen nach innen

---

## 📦 Setup: Flyway + JPA/Hibernate

### **Schritt 1: Dependencies in pom.xml**

```xml
<dependencies>
    <!-- Spring Data JPA (enthält Hibernate) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Flyway für DB-Migrationen -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- Flyway PostgreSQL Support (ab Flyway 9.x notwendig) -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    
    <!-- Lombok (optional, reduziert Boilerplate) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- MapStruct für Mapper (optional, aber empfohlen) -->
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

---

### **Schritt 2: application.properties konfigurieren**

```properties
# ========================================
# Database Configuration
# ========================================
# WICHTIG: Jeder Service hat seine EIGENE Datenbank!
# doctor-provider → doctor_provider_db
# patient-customer → patient_customer_db
spring.datasource.url=jdbc:postgresql://localhost:5432/doctor_provider_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# ========================================
# JPA/Hibernate Configuration
# ========================================
# WICHTIG: validate, NICHT update/create!
# Flyway erstellt die Tabellen, Hibernate validiert nur!
spring.jpa.hibernate.ddl-auto=validate

# SQL-Queries im Log anzeigen (Development)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Dialect (optional, wird automatisch erkannt)
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ========================================
# Flyway Configuration
# ========================================
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-version=0
```

**Wichtig:**
- `spring.jpa.hibernate.ddl-auto=validate` → Hibernate darf NICHT die DB ändern!
- Flyway ist die Source of Truth für DB-Schema

---

### **Schritt 3: Flyway Migrations erstellen**

#### **Wo anlegen?**

✅ **Migrations-Ordner DIREKT im jeweiligen Service:**

```
doctor-provider/
└── src/main/resources/
    └── db/
        └── migration/         ← HIER! Im Service selbst
            ├── V1__Create_practice_table.sql
            ├── V2__Create_doctor_table.sql
            └── ...

patient-customer/
└── src/main/resources/
    └── db/
        └── migration/         ← Eigene Migrationen!
            ├── V1__Create_patient_table.sql
            └── V2__Create_appointment_table.sql
```

**Warum?**
- ✅ Jeder Service ist autonom
- ✅ Einfaches Deployment
- ✅ Klare Verantwortlichkeiten
- ✅ Standard-Ansatz in Microservices

**NICHT:** Separates Migration-Repository (nur bei Shared Database Anti-Pattern nötig)

---

#### **Naming Convention:**
```
V<VERSION>__<DESCRIPTION>.sql

Beispiele:
V1__Create_practice_table.sql
V2__Create_doctor_table.sql
V3__Create_speciality_table.sql
```

**Regeln:**
- `V` = Version (großgeschrieben!)
- `__` = Zwei Unterstriche
- Version-Nummern: `V1`, `V2`, `V3`, ... oder `V1.0`, `V1.1`, ...

---

#### **Beispiel: V1__Create_practice_table.sql**

```sql
-- =============================================
-- Practice Table
-- =============================================
CREATE TABLE practice (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT practice_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT practice_email_valid CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Index für häufige Suchen
CREATE INDEX idx_practice_city ON practice(city);
CREATE INDEX idx_practice_name ON practice(name);
```

---

#### **Beispiel: V2__Create_doctor_table.sql**

```sql
-- =============================================
-- Doctor Table
-- =============================================
CREATE TABLE doctor (
    id BIGSERIAL PRIMARY KEY,
    practice_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    license_number VARCHAR(50) NOT NULL UNIQUE,
    years_of_experience INTEGER,
    bio TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_doctor_practice FOREIGN KEY (practice_id) 
        REFERENCES practice(id) ON DELETE CASCADE,
    
    CONSTRAINT doctor_first_name_not_empty CHECK (LENGTH(TRIM(first_name)) > 0),
    CONSTRAINT doctor_last_name_not_empty CHECK (LENGTH(TRIM(last_name)) > 0),
    CONSTRAINT doctor_email_valid CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Indizes
CREATE INDEX idx_doctor_practice ON doctor(practice_id);
CREATE INDEX idx_doctor_name ON doctor(last_name, first_name);
CREATE INDEX idx_doctor_email ON doctor(email);
```

---

#### **Beispiel: V3__Create_speciality_table.sql**

```sql
-- =============================================
-- Speciality Table
-- =============================================
CREATE TABLE speciality (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    speciality_type VARCHAR(50) NOT NULL,
    description TEXT,
    
    CONSTRAINT speciality_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT speciality_type_valid CHECK (speciality_type IN (
        'GENERAL_PRACTICE', 'CARDIOLOGY', 'DERMATOLOGY', 
        'ORTHOPEDICS', 'PEDIATRICS', 'PSYCHIATRY', 
        'RADIOLOGY', 'NEUROLOGY', 'OPHTHALMOLOGY', 
        'ENT', 'GYNECOLOGY', 'UROLOGY'
    ))
);

-- Index
CREATE INDEX idx_speciality_type ON speciality(speciality_type);
```

---

#### **Beispiel: V4__Create_doctor_speciality_table.sql**

```sql
-- =============================================
-- Doctor-Speciality Join Table (Many-to-Many)
-- =============================================
CREATE TABLE doctor_speciality (
    doctor_id BIGINT NOT NULL,
    speciality_id BIGINT NOT NULL,
    
    PRIMARY KEY (doctor_id, speciality_id),
    
    CONSTRAINT fk_doctor_speciality_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_doctor_speciality_speciality FOREIGN KEY (speciality_id) 
        REFERENCES speciality(id) ON DELETE CASCADE
);

-- Indizes für beide Richtungen
CREATE INDEX idx_doctor_speciality_doctor ON doctor_speciality(doctor_id);
CREATE INDEX idx_doctor_speciality_speciality ON doctor_speciality(speciality_id);
```

---

#### **Beispiel: V5__Create_doctor_working_hours_table.sql**

```sql
-- =============================================
-- Doctor Working Hours Table
-- =============================================
CREATE TABLE doctor_working_hours (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    week_day INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,
    
    CONSTRAINT fk_working_hours_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    CONSTRAINT week_day_valid CHECK (week_day BETWEEN 1 AND 7),
    CONSTRAINT start_before_end CHECK (start_time < end_time),
    CONSTRAINT slot_duration_valid CHECK (slot_duration_minutes BETWEEN 5 AND 240),
    
    -- Ein Arzt kann nicht zweimal am gleichen Tag zur gleichen Zeit arbeiten
    CONSTRAINT unique_doctor_day_time UNIQUE (doctor_id, week_day, start_time, end_time)
);

-- Index
CREATE INDEX idx_working_hours_doctor ON doctor_working_hours(doctor_id);
CREATE INDEX idx_working_hours_day ON doctor_working_hours(week_day);
```

---

#### **Beispiel: V6__Create_slot_table.sql**

```sql
-- =============================================
-- Slot Table
-- =============================================
CREATE TABLE slot (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    patient_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    CONSTRAINT slot_start_before_end CHECK (start_time < end_time),
    CONSTRAINT slot_status_valid CHECK (status IN ('AVAILABLE', 'BOOKED', 'BLOCKED')),
    
    -- Ein Arzt kann nicht zwei Slots zur gleichen Zeit haben
    CONSTRAINT unique_doctor_slot_time UNIQUE (doctor_id, start_time)
);

-- Indizes für häufige Abfragen
CREATE INDEX idx_slot_doctor ON slot(doctor_id);
CREATE INDEX idx_slot_start_time ON slot(start_time);
CREATE INDEX idx_slot_status ON slot(status);
CREATE INDEX idx_slot_doctor_status_time ON slot(doctor_id, status, start_time);
```

---

### **Schritt 4: App starten → Flyway läuft automatisch**

Beim ersten Start:
1. Spring Boot startet
2. Flyway läuft automatisch
3. Alle `V*__*.sql` Dateien werden der Reihe nach ausgeführt
4. Tabelle `flyway_schema_history` wird erstellt → speichert, welche Versionen gelaufen sind
5. JPA/Hibernate validiert, ob Entities mit Tabellen übereinstimmen

**Beim nächsten Start:**
- Nur **neue** Migrations werden ausgeführt
- Bereits gelaufene werden übersprungen

---

## 🏗️ JPA Entities erstellen

### **Projekt-Struktur:**

```
doctor-provider/
└── src/main/java/test/doctor_provider/
    ├── domain/
    │   └── model/
    │       ├── Practice.java           # Domain Model (KEINE JPA!)
    │       ├── Doctor.java
    │       ├── Speciality.java
    │       ├── DoctorWorkingHours.java
    │       └── Slot.java
    │
    └── infrastructure/
        └── adapter/
            └── persistence/
                ├── entity/              # JPA Entities
                │   ├── PracticeEntity.java
                │   ├── DoctorEntity.java
                │   ├── SpecialityEntity.java
                │   ├── DoctorWorkingHoursEntity.java
                │   └── SlotEntity.java
                │
                ├── repository/          # Spring Data JPA Repositories
                │   ├── PracticeJpaRepository.java
                │   ├── DoctorJpaRepository.java
                │   ├── SpecialityJpaRepository.java
                │   ├── DoctorWorkingHoursJpaRepository.java
                │   └── SlotJpaRepository.java
                │
                ├── mapper/              # Domain ↔ Entity Mapper
                │   ├── PracticeMapper.java
                │   ├── DoctorMapper.java
                │   ├── SpecialityMapper.java
                │   ├── DoctorWorkingHoursMapper.java
                │   └── SlotMapper.java
                │
                └── adapter/             # Port-Implementierung
                    ├── PracticePersistenceAdapter.java
                    ├── DoctorPersistenceAdapter.java
                    ├── SpecialityPersistenceAdapter.java
                    ├── DoctorWorkingHoursPersistenceAdapter.java
                    └── SlotPersistenceAdapter.java
```

---

### **Beispiel: Practice**

#### **1. Domain Model (domain/model/Practice.java)**

```java
package test.doctor_provider.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain Model - KEINE JPA-Annotationen!
 * Nur Business-Logik
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Practice {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business-Logik Methoden hier (falls nötig)
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Practice name cannot be empty");
        }
        // weitere Validierungen...
    }
}
```

---

#### **2. JPA Entity (infrastructure/adapter/persistence/entity/PracticeEntity.java)**

```java
package test.doctor_provider.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity - MIT JPA-Annotationen!
 * Nur DB-Mapping, KEINE Business-Logik
 */
@Entity
@Table(name = "practice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "address", nullable = false, length = 500)
    private String address;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

#### **3. Spring Data JPA Repository (infrastructure/adapter/persistence/repository/PracticeJpaRepository.java)**

```java
package test.doctor_provider.infrastructure.adapter.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.doctor_provider.infrastructure.adapter.persistence.entity.PracticeEntity;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository
 * Spring generiert automatisch die Implementierung!
 */
@Repository
public interface PracticeJpaRepository extends JpaRepository<PracticeEntity, Long> {
    
    // Spring Data JPA generiert automatisch Queries aus Methodennamen!
    
    Optional<PracticeEntity> findByEmail(String email);
    
    List<PracticeEntity> findByCity(String city);
    
    List<PracticeEntity> findByCityAndNameContainingIgnoreCase(String city, String name);
    
    boolean existsByEmail(String email);
}
```

**Spring Data Magic:**
- `findByEmail` → `SELECT * FROM practice WHERE email = ?`
- `findByCity` → `SELECT * FROM practice WHERE city = ?`
- Keine Implementierung nötig!

---

#### **4. Mapper (infrastructure/adapter/persistence/mapper/PracticeMapper.java)**

**Option A: Manuell (ohne MapStruct)**

```java
package test.doctor_provider.infrastructure.adapter.persistence.mapper;

import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.adapter.persistence.entity.PracticeEntity;

@Component
public class PracticeMapper {
    
    public PracticeEntity toEntity(Practice domain) {
        if (domain == null) return null;
        
        return PracticeEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .address(domain.getAddress())
                .city(domain.getCity())
                .postalCode(domain.getPostalCode())
                .phoneNumber(domain.getPhoneNumber())
                .email(domain.getEmail())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    public Practice toDomain(PracticeEntity entity) {
        if (entity == null) return null;
        
        return Practice.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .postalCode(entity.getPostalCode())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
```

**Option B: Mit MapStruct (empfohlen, weniger Code!)**

```java
package test.doctor_provider.infrastructure.adapter.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.adapter.persistence.entity.PracticeEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PracticeMapper {
    
    PracticeEntity toEntity(Practice domain);
    
    Practice toDomain(PracticeEntity entity);
}
```

→ MapStruct generiert automatisch die Implementierung!

---

#### **5. Persistence Adapter (infrastructure/adapter/persistence/adapter/PracticePersistenceAdapter.java)**

```java
package test.doctor_provider.infrastructure.adapter.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.domain.port.out.PracticePort;
import test.doctor_provider.infrastructure.adapter.persistence.entity.PracticeEntity;
import test.doctor_provider.infrastructure.adapter.persistence.mapper.PracticeMapper;
import test.doctor_provider.infrastructure.adapter.persistence.repository.PracticeJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementierung des Outbound Ports
 * Verbindet Domain mit JPA
 */
@Component
@RequiredArgsConstructor
public class PracticePersistenceAdapter implements PracticePort {
    
    private final PracticeJpaRepository repository;
    private final PracticeMapper mapper;
    
    @Override
    public Practice save(Practice practice) {
        PracticeEntity entity = mapper.toEntity(practice);
        PracticeEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Practice> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Practice> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Practice> findByCity(String city) {
        return repository.findByCity(city).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
```

---

## 🔁 Der komplette Flow

### **Beispiel: Practice erstellen**

```
1. REST Controller (Infrastructure In)
   ↓
2. Application Service (Application Layer)
   ↓
3. Domain Service (Domain Layer) → Business-Logik
   ↓
4. PracticePort Interface (Domain Port Out)
   ↓
5. PracticePersistenceAdapter (Infrastructure Out)
   ↓
6. PracticeMapper → Domain → Entity
   ↓
7. PracticeJpaRepository (Spring Data JPA)
   ↓
8. Hibernate/JPA → SQL Query
   ↓
9. PostgreSQL Database
```

---

## 📋 Checkliste: DB-Setup

### **Für jeden Service (Doctor-Provider, Patient-Customer):**

- [ ] **Dependencies hinzufügen** (pom.xml)
  - spring-boot-starter-data-jpa
  - postgresql
  - flyway-core
  - flyway-database-postgresql
  - lombok (optional)
  - mapstruct (optional)

- [ ] **application.properties konfigurieren**
  - Datasource (URL, Username, Password)
  - `spring.jpa.hibernate.ddl-auto=validate`
  - Flyway enabled

- [ ] **Flyway Migrations erstellen**
  - Ordner: `src/main/resources/db/migration/`
  - `V1__Create_xxx_table.sql` für jede Tabelle
  - Constraints, Indizes definieren

- [ ] **Domain Models** (OHNE JPA)
  - In `domain/model/`
  - Business-Logik

- [ ] **JPA Entities** (MIT JPA)
  - In `infrastructure/adapter/persistence/entity/`
  - `@Entity`, `@Table`, `@Column`, etc.

- [ ] **Spring Data Repositories**
  - In `infrastructure/adapter/persistence/repository/`
  - Interface extends `JpaRepository`

- [ ] **Mapper**
  - Manuell oder MapStruct
  - Domain ↔ Entity Konvertierung

- [ ] **Persistence Adapter**
  - Implementiert Port Interface
  - Nutzt Repository + Mapper

- [ ] **App starten & testen**
  - Flyway läuft → Tabellen erstellt
  - Hibernate validiert → Entities passen zu Tabellen

---

## ✅ Zusammenfassung

### **Was du machen sollst:**

1. ✅ **Flyway für DB-Schema** → SQL-Dateien erstellen Tabellen
2. ✅ **JPA/Hibernate für Zugriff** → Domain Models + JPA Entities
3. ✅ **Trennung Domain ↔ Persistence** → Hexagonale Architektur
4. ✅ **Spring Data JPA** → Wenig Code, viel Funktionalität
5. ✅ **MapStruct (optional)** → Automatisches Mapping

### **Was du NICHT machen sollst:**

❌ Manuelles JDBC → Zu viel Code  
❌ Hibernate `ddl-auto=update` → Unkontrolliert  
❌ JPA-Annotationen in Domain Models → Verletzt Hexagonale Architektur  
❌ DB manuell über UI Tool verwalten → Nicht versioniert

---

**Du hast jetzt alles, was du für die DB brauchst!** 🎯
