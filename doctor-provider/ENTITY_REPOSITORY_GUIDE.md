# 🗄️ JPA-Entity & Repository Guide: Hexagonale Architektur

## 📋 Inhaltsverzeichnis

1. [Konzept-Übersicht](#1-konzept-übersicht)
2. [JPA-Entities erstellen](#2-jpa-entities-erstellen)
3. [Spring Data JPA Repositories erstellen](#3-spring-data-jpa-repositories-erstellen)
4. [Persistence-Adapter erstellen](#4-persistence-adapter-erstellen)
5. [Vollständiges Beispiel: City](#5-vollständiges-beispiel-city)
6. [Komplexes Beispiel: Practice (mit Beziehungen)](#6-komplexes-beispiel-practice-mit-beziehungen)
7. [Sehr komplexes Beispiel: Doctor (n:m Beziehungen)](#7-sehr-komplexes-beispiel-doctor-nm-beziehungen)
8. [Beispiel: Slot (mit ZonedDateTime)](#8-beispiel-slot-mit-zoneddatetime)
9. [Best Practices](#9-best-practices)

---

## 1. Konzept-Übersicht

### 1.1 Warum drei verschiedene Modelle?

In der Hexagonalen Architektur trennen wir die Datenbankschicht vom Domain-Modell:

```
┌─────────────────────────────────────────────────────────────────┐
│                     DOMAIN-MODELL (POJO)                        │
│  - Reine Business-Objekte (City, Practice, Doctor)             │
│  - KEINE JPA-Annotationen (@Entity, @Table, etc.)              │
│  - KEINE Abhängigkeit zu Spring/Hibernate                      │
│  - Business-Logik (Validierung, Berechnungen)                  │
│                                                                 │
│  Beispiel: City.java                                            │
│  ├── id: UUID                                                   │
│  ├── name: String                                               │
│  └── postalCode: String                                         │
└─────────────────────────────────────────────────────────────────┘
                              ▲ │
                              │ │ Mapper konvertiert
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                     JPA-ENTITY (@Entity)                        │
│  - Technische DB-Objekte (CityEntity)                          │
│  - MIT JPA-Annotationen (@Entity, @Table, @Column, etc.)       │
│  - Mapped auf DB-Tabellen                                      │
│  - KEINE Business-Logik                                         │
│                                                                 │
│  Beispiel: CityEntity.java                                      │
│  ├── id: UUID (@Id)                                             │
│  ├── name: String (@Column)                                     │
│  └── zipCode: String (@Column(name="zip_code"))  ← anders!     │
└─────────────────────────────────────────────────────────────────┘
                              ▲ │
                              │ │ JPA/Hibernate
                              │ ▼
┌─────────────────────────────────────────────────────────────────┐
│                     DATENBANK-TABELLE                           │
│  - PostgreSQL Tabelle "city"                                    │
│  - Spalten: id, name, zip_code                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Warum diese Trennung?

|      Vorteil       |                     Erklärung                      |
|--------------------|----------------------------------------------------|
| **Unabhängigkeit** | Domain kennt keine Datenbank-Details               |
| **Testbarkeit**    | Domain-Logik ohne DB testbar                       |
| **Flexibilität**   | DB-Schema kann sich ändern, ohne Domain anzufassen |
| **Clean Code**     | Klare Verantwortlichkeiten                         |

---

## 2. JPA-Entities erstellen

### 2.1 Position im Projekt

```
infrastructure/adapter/outgoing/persistence/
├── entity/                    # ⭐ HIER kommen die Entities
│   ├── CityEntity.java
│   ├── SpecialityEntity.java
│   ├── PracticeEntity.java
│   ├── DoctorEntity.java
│   ├── WorkingHoursEntity.java
│   └── SlotEntity.java
├── repository/                # Spring Data JPA Repositories
├── mapper/                    # Entity ↔ Domain Mapper
└── adapter/                   # Persistence Adapter
```

### 2.2 Einfaches Beispiel: CityEntity

#### Schritt 1: Domain-Modell (bereits vorhanden)

```java
package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain-Modell: KEINE JPA-Annotationen!
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class City {
    private UUID id;
    private String name;
    private String postalCode;  // ← Domain-Name
}
```

#### Schritt 2: Flyway-Migration (bereits vorhanden)

```sql
-- V2__Create_city_table.sql
CREATE TABLE city (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL  -- ← DB-Spaltenname (anders als Domain!)
);
```

#### Schritt 3: JPA-Entity erstellen

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA-Entity für die "city"-Tabelle.
 *
 * WICHTIG:
 * - MIT JPA-Annotationen (@Entity, @Table, @Column)
 * - Mapped auf DB-Tabelle
 * - KEINE Business-Logik
 *
 * Unterschiede zum Domain-Modell:
 * - postalCode (Domain) → zipCode (Entity) → zip_code (DB)
 */
@Entity
@Table(name = "city")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityEntity {

    /**
     * Primary Key
     * - UUID-Typ
     * - Wird von der DB generiert (gen_random_uuid())
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Stadt-Name
     * - Pflichtfeld (nullable = false)
     * - Max. 100 Zeichen (siehe DB-Schema)
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Postleitzahl
     * - Pflichtfeld
     * - Feld heißt in DB "zip_code", in Entity "zipCode"
     *
     * ⚠️ WICHTIG: Unterschied zum Domain-Modell (dort "postalCode")
     */
    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;  // ← Anders als im Domain-Modell!
}
```

#### Erklärung der Annotationen:

|        Annotation        |               Zweck                |                     Beispiel                      |
|--------------------------|------------------------------------|---------------------------------------------------|
| `@Entity`                | Markiert die Klasse als JPA-Entity | `@Entity`                                         |
| `@Table(name="...")`     | DB-Tabellenname                    | `@Table(name="city")`                             |
| `@Id`                    | Primary Key                        | `@Id`                                             |
| `@GeneratedValue`        | Auto-generierte ID                 | `@GeneratedValue(strategy = GenerationType.UUID)` |
| `@Column(name="...")`    | DB-Spaltenname                     | `@Column(name="zip_code")`                        |
| `@Column(nullable=...)`  | Pflichtfeld?                       | `@Column(nullable=false)`                         |
| `@Column(length=...)`    | Max. Länge (String)                | `@Column(length=100)`                             |
| `@Column(updatable=...)` | Änderbar?                          | `@Column(updatable=false)` für ID                 |

---

### 2.2.1 Entity mit PostgreSQL ENUM: SpecialityEntity

#### Schritt 1: Domain-Modell mit Java Enum

```java
package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.domain.enums.SpecialityTyp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    private UUID id;
    private SpecialityTyp name;  // ← Java Enum
}
```

```java
package test.doctor_provider.domain.enums;

import lombok.Getter;

@Getter
public enum SpecialityTyp {
    Allgemeinmedizin("allgemeinmedizin"),
    InnereMedizin("inneremedizin"),
    Kardiologie("kardiologe"),
    Dermatologie("dermatologe"),
    // ... weitere Werte
    Zahnmedizin("zahnarzt");

    private final String value;

    SpecialityTyp(String value) {
        this.value = value;
    }
}
```

#### Schritt 2: Flyway-Migration mit PostgreSQL ENUM

```sql
-- V1__Create_speciality_table.sql

-- 1. PostgreSQL ENUM-Typ erstellen
CREATE TYPE speciality_type AS ENUM (
    'allgemeinmedizin',
    'inneremedizin',
    'kardiologe',
    'dermatologe',
    'orthopäde',
    'neurologe',
    'psychiater',
    'gynäkologe',
    'pädiater',
    'urologe',
    'augenarzt',
    'hno',
    'radiologe',
    'anästhesist',
    'zahnarzt'
);

-- 2. Tabelle mit ENUM-Spalte
CREATE TABLE speciality (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name speciality_type NOT NULL UNIQUE  -- ← Verwendet den ENUM-Typ
);
```

#### Schritt 3: JPA-Entity mit ENUM-Mapping

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import test.doctor_provider.domain.enums.SpecialityTyp;

import java.util.UUID;

/**
 * JPA-Entity für die "speciality"-Tabelle.
 *
 * BESONDERHEIT: PostgreSQL ENUM-Typ
 * - DB-Spalte hat Typ "speciality_type" (PostgreSQL ENUM)
 * - Java-Feld hat Typ "SpecialityTyp" (Java Enum)
 * - Hibernate muss zwischen beiden mappen
 */
@Entity
@Table(name = "speciality")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpecialityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * PostgreSQL ENUM-Spalte
     *
     * WICHTIG - Drei Annotationen erforderlich:
     *
     * 1️⃣ @Enumerated(EnumType.STRING)
     * 2️⃣ @JdbcTypeCode(SqlTypes.NAMED_ENUM)
     * 3️⃣ @Column(columnDefinition = "speciality_type")
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "name", nullable = false, columnDefinition = "speciality_type")
    private SpecialityTyp name;
}
```

---

### 🔍 **Was bedeuten die Enum-Annotationen?**

#### **1️⃣ `@Enumerated(EnumType.STRING)`**

**Zweck:** Bestimmt, **WIE** das Java-Enum in der Datenbank gespeichert wird.

**Zwei Optionen:**

|    EnumType     |  Speicherung in DB   |       Beispiel       | Empfohlen? |
|-----------------|----------------------|----------------------|------------|
| **`STRING`** ✅  | Enum-Name als String | `"Allgemeinmedizin"` | ✅ **JA**   |
| **`ORDINAL`** ❌ | Position als Zahl    | `0`, `1`, `2`        | ❌ **NEIN** |

**Warum STRING?**

```java
// Beispiel: Java Enum
public enum SpecialityTyp {
    Allgemeinmedizin,  // Position 0
    InnereMedizin,     // Position 1
    Kardiologie        // Position 2
}

// ❌ ORDINAL (NICHT empfohlen!)
// Speichert: 0, 1, 2
// Problem: Wenn du später "Neurologie" VOR "Kardiologie" einfügst,
//          ändern sich alle Zahlen! → Daten-Chaos!

// ✅ STRING (Empfohlen!)
// Speichert: "Allgemeinmedizin", "InnereMedizin", "Kardiologie"
// Vorteil: Lesbar, sicher bei Änderungen, keine Reihenfolge-Probleme
```

**Ohne `@Enumerated`:**

```java
// ❌ Fehler: Hibernate weiß nicht, wie es das Enum speichern soll
@Column(name = "name")
private SpecialityTyp name;  // Wirft Exception!
```

---

#### **2️⃣ `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` (Hibernate-spezifisch)**

**Zweck:** Teilt Hibernate mit, dass es sich um einen **PostgreSQL NAMED ENUM** handelt.

**Ohne diese Annotation:**

```java
// ❌ Nur @Enumerated(EnumType.STRING)
@Enumerated(EnumType.STRING)
@Column(name = "name", columnDefinition = "speciality_type")
private SpecialityTyp name;

// Problem: Hibernate erstellt VARCHAR statt ENUM!
// SQL: CREATE TABLE speciality (name VARCHAR(255))
//      Aber DB erwartet: name speciality_type
```

**Mit `@JdbcTypeCode`:**

```java
// ✅ Hibernate weiß: "Das ist ein PostgreSQL ENUM"
@Enumerated(EnumType.STRING)
@JdbcTypeCode(SqlTypes.NAMED_ENUM)
@Column(name = "name", columnDefinition = "speciality_type")
private SpecialityTyp name;

// Hibernate generiert korrektes SQL:
// INSERT INTO speciality (name) VALUES ('allgemeinmedizin'::speciality_type)
```

**Import erforderlich:**

```java
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
```

---

#### **3️⃣ `@Column(columnDefinition = "speciality_type")`**

**Zweck:** Explizite Angabe des **PostgreSQL-Datenbanktyps**.

**Warum notwendig?**

```java
// Ohne columnDefinition:
@Column(name = "name")
private SpecialityTyp name;

// Hibernate generiert bei Schema-Erstellung:
// CREATE TABLE speciality (name VARCHAR(255))  ❌

// Mit columnDefinition:
@Column(name = "name", columnDefinition = "speciality_type")
private SpecialityTyp name;

// Hibernate generiert:
// CREATE TABLE speciality (name speciality_type)  ✅
```

**Wichtig:** Der Name `"speciality_type"` muss **exakt** zum `CREATE TYPE` passen!

```sql
-- In der Migration:
CREATE TYPE speciality_type AS ENUM (...);
              ^^^^^^^^^^^^^^^^
              Dieser Name muss in columnDefinition stehen!
```

---

### 📊 **Vergleich: Normale Enums vs. PostgreSQL ENUM**

#### **Variante A: String-basiertes Enum (ohne PostgreSQL ENUM)**

**Migration:**

```sql
CREATE TABLE speciality (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL  -- ← Einfacher String
);
```

**Entity:**

```java
@Entity
@Table(name = "speciality")
public class SpecialityEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)  // ← Nur eine Annotation!
    @Column(name = "name", nullable = false, length = 50)
    private SpecialityTyp name;
}
```

**Vorteil:** ✅ Einfacher
**Nachteil:** ❌ Keine DB-seitige Validierung (du könntest theoretisch "xyz" einfügen)

---

#### **Variante B: PostgreSQL ENUM (empfohlen für feste Werte)**

**Migration:**

```sql
CREATE TYPE speciality_type AS ENUM ('allgemeinmedizin', 'kardiologe', ...);

CREATE TABLE speciality (
    id UUID PRIMARY KEY,
    name speciality_type NOT NULL  -- ← PostgreSQL ENUM
);
```

**Entity:**

```java
@Entity
@Table(name = "speciality")
public class SpecialityEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)           // ← 1. Annotation
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)     // ← 2. Annotation
    @Column(name = "name", columnDefinition = "speciality_type")  // ← 3. Annotation
    private SpecialityTyp name;
}
```

**Vorteil:** ✅ DB-seitige Validierung (PostgreSQL erlaubt nur gültige Werte)
**Nachteil:** ⚠️ Komplexer (aber sicherer!)

---

### 🎯 **Zusammenfassung: Wann welche Annotation?**

|          Szenario           |                                               Annotationen                                               |            Beispiel            |
|-----------------------------|----------------------------------------------------------------------------------------------------------|--------------------------------|
| **Normales Enum → VARCHAR** | `@Enumerated(EnumType.STRING)`                                                                           | SlotStatus (AVAILABLE, BOOKED) |
| **PostgreSQL ENUM**         | `@Enumerated(EnumType.STRING)` <br> `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` <br> `columnDefinition = "..."` | SpecialityTyp                  |

---

### 2.3 Entity mit Beziehungen: PracticeEntity (n:1 zu City)

#### Flyway-Migration

```sql
-- V3__Create_practice_table.sql
CREATE TABLE practice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    house_number VARCHAR(10) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    city_id UUID NOT NULL,  -- FK zu city-Tabelle
    CONSTRAINT fk_practice_city FOREIGN KEY (city_id) REFERENCES city(id)
);
```

#### JPA-Entity

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.CityEntity;

import java.util.UUID;

/**
 * JPA-Entity für die "practice"-Tabelle.
 *
 * Beziehungen:
 * - n:1 zu CityEntity (Viele Praxen → Eine Stadt)
 */
@Entity
@Table(name = "practice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "house_number", nullable = false, length = 10)
    private String houseNumber;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    /**
     * n:1 Beziehung zu CityEntity
     *
     * WICHTIG:
     * - @ManyToOne: Viele Praxen → Eine Stadt
     * - @JoinColumn: FK-Spalte in der practice-Tabelle
     * - fetch = LAZY: Stadt wird nur geladen, wenn benötigt (Performance!)
     * - optional = false: city_id ist Pflicht (NOT NULL)
     *
     * ⚠️ Im Domain-Modell ist das nur eine UUID (cityId)!
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;
}
```

#### Erklärung der Beziehungs-Annotationen:

|        Annotation         |               Bedeutung               |           Beispiel            |
|---------------------------|---------------------------------------|-------------------------------|
| `@ManyToOne`              | Viele → Eine Beziehung                | Viele Praxen → Eine Stadt     |
| `@JoinColumn(name="...")` | FK-Spaltenname in DB                  | `@JoinColumn(name="city_id")` |
| `fetch = LAZY`            | Lazy Loading (erst bei Zugriff laden) | Performance-Optimierung       |
| `fetch = EAGER`           | Eager Loading (sofort mitladen)       | Bei kleinen Objekten          |
| `optional = false`        | Pflichtfeld (NOT NULL)                | Stadt ist erforderlich        |
| `optional = true`         | Optional (NULL erlaubt)               | Feld kann leer sein           |

### 2.4 Entity mit n:m Beziehung: DoctorEntity

#### Flyway-Migrationen

```sql
-- V4__Create_doctor_table.sql
CREATE TABLE doctor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    practice_id UUID,  -- NULLABLE! (Arzt kann ohne Praxis sein)
    CONSTRAINT fk_doctor_practice FOREIGN KEY (practice_id) REFERENCES practice(id)
);

-- V5__Create_doctor_speciality_join_table.sql
CREATE TABLE doctor_speciality (
    doctor_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    PRIMARY KEY (doctor_id, speciality_id),
    CONSTRAINT fk_ds_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    CONSTRAINT fk_ds_speciality FOREIGN KEY (speciality_id) REFERENCES speciality(id) ON DELETE CASCADE
);
```

#### JPA-Entity

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.SpecialityEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA-Entity für die "doctor"-Tabelle.
 *
 * Beziehungen:
 * - n:1 zu PracticeEntity (OPTIONAL - kann null sein)
 * - n:m zu SpecialityEntity (über Join-Tabelle)
 */
@Entity
@Table(name = "doctor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * n:1 Beziehung zu PracticeEntity (OPTIONAL)
     *
     * WICHTIG:
     * - optional = true: practice_id kann NULL sein
     * - fetch = LAZY: Performance-Optimierung
     *
     * ⚠️ Im Domain-Modell: UUID practiceId (kann null sein)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "practice_id", nullable = true)
    private PracticeEntity practice;

    /**
     * n:m Beziehung zu SpecialityEntity
     *
     * WICHTIG:
     * - @ManyToMany: Viele Ärzte ↔ Viele Fachrichtungen
     * - @JoinTable: Join-Tabelle "doctor_speciality"
     * - joinColumns: FK zu doctor (diese Seite)
     * - inverseJoinColumns: FK zu speciality (andere Seite)
     * - fetch = LAZY: Performance!
     * - Set statt List: Keine Duplikate
     *
     * ⚠️ Im Domain-Modell: Set<UUID> specialityIds
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_speciality",                  // Join-Tabelle
            joinColumns = @JoinColumn(name = "doctor_id"),         // FK zu doctor
            inverseJoinColumns = @JoinColumn(name = "speciality_id") // FK zu speciality
    )
    @Builder.Default
    private Set<SpecialityEntity> specialities = new HashSet<>();
}
```

#### Erklärung n:m Beziehung:

```
┌─────────────────┐         ┌─────────────────────┐         ┌─────────────────┐
│  doctor         │         │  doctor_speciality  │         │  speciality     │
├─────────────────┤         ├─────────────────────┤         ├─────────────────┤
│ id (PK)         │◄────────│ doctor_id (FK)      │         │ id (PK)         │
│ first_name      │         │ speciality_id (FK)  │────────►│ name            │
│ last_name       │         │ (Composite PK)      │         └─────────────────┘
│ practice_id (FK)│         └─────────────────────┘
└─────────────────┘
```

---

### ⚠️ **Wichtiger Hinweis: Wann braucht man eine separate Join-Entity?**

#### **Fall 1: Einfache n:m Beziehung (NUR FKs in Join-Tabelle)** ✅

**Datenbank:**

```sql
CREATE TABLE doctor_speciality (
    doctor_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    PRIMARY KEY (doctor_id, speciality_id)  -- ← NUR 2 Fremdschlüssel!
);
```

**Lösung: `@ManyToMany` reicht aus! KEINE separate Entity nötig!**

```java
// DoctorEntity.java - So wie oben gezeigt ✅
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "doctor_speciality",
    joinColumns = @JoinColumn(name = "doctor_id"),
    inverseJoinColumns = @JoinColumn(name = "speciality_id")
)
@Builder.Default
private Set<SpecialityEntity> specialities = new HashSet<>();
```

**Hibernate verwaltet automatisch:**
- ✅ INSERT in `doctor_speciality` beim Hinzufügen
- ✅ DELETE aus `doctor_speciality` beim Entfernen
- ✅ SELECT mit JOIN beim Laden

---

#### **Fall 2: n:m mit Zusatzfeldern (z.B. Zertifizierungsdatum)** ❌

**Datenbank:**

```sql
CREATE TABLE doctor_speciality (
    doctor_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    certified_since DATE,              -- ⚠️ Zusätzliches Feld!
    certification_number VARCHAR(50),  -- ⚠️ Zusätzliches Feld!
    PRIMARY KEY (doctor_id, speciality_id)
);
```

**Lösung: Separate Entity erforderlich!**

```java
// 1. Composite Key (Embedded ID)
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialityId implements Serializable {
    private UUID doctorId;
    private UUID specialityId;
}

// 2. Join-Tabellen-Entity
@Entity
@Table(name = "doctor_speciality")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSpecialityEntity {

    @EmbeddedId
    private DoctorSpecialityId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id")
    private DoctorEntity doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("specialityId")
    @JoinColumn(name = "speciality_id")
    private SpecialityEntity speciality;

    // Zusätzliche Felder
    @Column(name = "certified_since")
    private LocalDate certifiedSince;

    @Column(name = "certification_number", length = 50)
    private String certificationNumber;
}

// 3. DoctorEntity anpassen
@Entity
@Table(name = "doctor")
public class DoctorEntity {
    // ...existing fields...

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DoctorSpecialityEntity> doctorSpecialities = new HashSet<>();
}
```

---

### 🎯 **Entscheidungshilfe:**

|                 Join-Tabelle enthält...                 |             Lösung             |         Separate Entity?          |
|---------------------------------------------------------|--------------------------------|-----------------------------------|
| **Nur 2 Fremdschlüssel** (doctor_id, speciality_id)     | `@ManyToMany`                  | ❌ **NEIN** (wie im Beispiel oben) |
| **2 FKs + zusätzliche Spalten** (certified_since, etc.) | `@OneToMany` + Separate Entity | ✅ **JA**                          |

---

### 2.5 Entity mit Enums: SlotEntity

#### Flyway-Migration

```sql
-- V7__Create_slot_table.sql
CREATE TABLE slot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    working_hours_id UUID NOT NULL,
    start_time TIMESTAMPTZ NOT NULL,  -- Mit Timezone!
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,       -- ENUM als String
    CONSTRAINT fk_slot_working_hours FOREIGN KEY (working_hours_id) REFERENCES working_hours(id)
);

-- Index für schnelle Abfragen
CREATE INDEX idx_slot_working_hours ON slot(working_hours_id);
CREATE INDEX idx_slot_start_time ON slot(start_time);
```

#### JPA-Entity

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.domain.enums.SlotStatus;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.WorkingHoursEntity;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA-Entity für die "slot"-Tabelle.
 *
 * Besonderheiten:
 * - ZonedDateTime für Zeitstempel mit Timezone
 * - Enum-Mapping (@Enumerated)
 * - n:1 Beziehung zu WorkingHoursEntity
 */
@Entity
@Table(name = "slot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * n:1 Beziehung zu WorkingHoursEntity
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "working_hours_id", nullable = false)
    private WorkingHoursEntity workingHours;

    /**
     * Start-Zeitpunkt mit Timezone
     *
     * WICHTIG:
     * - ZonedDateTime für TIMESTAMPTZ in PostgreSQL
     * - Speichert Datum, Zeit UND Timezone
     * - Beispiel: 2026-01-20T10:00:00+01:00[Europe/Berlin]
     */
    @Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime startTime;

    /**
     * End-Zeitpunkt mit Timezone
     */
    @Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private ZonedDateTime endTime;

    /**
     * Slot-Status als Enum
     *
     * WICHTIG:
     * - @Enumerated(EnumType.STRING): Speichert als String ("AVAILABLE")
     * - EnumType.ORDINAL würde als Zahl speichern (0, 1, 2) - NICHT empfohlen!
     *
     * Warum STRING?
     * - Lesbar in DB
     * - Änderungen an Enum-Reihenfolge ändern nicht die DB-Werte
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlotStatus status;
}
```

#### Enum-Mapping Vergleich:

|  EnumType   |   DB-Wert   |    Vorteil     |           Nachteil            |
|-------------|-------------|----------------|-------------------------------|
| **STRING**  | "AVAILABLE" | Lesbar, robust | Mehr Speicher                 |
| **ORDINAL** | 0, 1, 2     | Wenig Speicher | Fehleranfällig bei Änderungen |

**⚠️ Empfehlung:** Immer `EnumType.STRING` verwenden!

---

## 3. Spring Data JPA Repositories erstellen

### 3.1 Position im Projekt

```
infrastructure/adapter/outgoing/persistence/
├── entity/                    # JPA-Entities
├── repository/                # ⭐ HIER kommen die Repositories
│   ├── CityJpaRepository.java
│   ├── SpecialityJpaRepository.java
│   ├── PracticeJpaRepository.java
│   ├── DoctorJpaRepository.java
│   ├── WorkingHoursJpaRepository.java
│   └── SlotJpaRepository.java
├── mapper/                    # Entity ↔ Domain Mapper
└── adapter/                   # Persistence Adapter
```

### 3.2 Einfaches Repository: CityJpaRepository

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.CityEntity;

import java.util.UUID;

/**
 * Spring Data JPA Repository für CityEntity.
 *
 * WICHTIG:
 * - Extends JpaRepository<Entity, ID-Type>
 * - Spring generiert automatisch Implementierung
 * - KEINE Implementierung nötig!
 *
 * Automatisch verfügbare Methoden (von JpaRepository):
 * - save(entity)
 * - findById(id)
 * - findAll()
 * - delete(entity)
 * - count()
 * - existsById(id)
 * - und viele mehr...
 */
@Repository
public interface CityJpaRepository extends JpaRepository<CityEntity, UUID> {

    // ========================================================================
    // Custom Query Methods (Spring generiert automatisch SQL!)
    // ========================================================================

    /**
     * Findet Städte nach Name (Teilstring, case-insensitive)
     *
     * Spring generiert automatisch:
     * SELECT * FROM city WHERE LOWER(name) LIKE LOWER(:name)
     *
     * Naming-Convention: findBy + Feldname + ContainingIgnoreCase
     */
    Page<CityEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Findet Städte nach PLZ (exakte Suche)
     *
     * Spring generiert automatisch:
     * SELECT * FROM city WHERE zip_code = :zipCode
     *
     * Naming-Convention: findBy + Feldname
     */
    Page<CityEntity> findByZipCode(String zipCode, Pageable pageable);

    /**
     * Findet Städte nach Name UND PLZ
     *
     * Spring generiert automatisch:
     * SELECT * FROM city
     * WHERE LOWER(name) LIKE LOWER(:name) AND zip_code = :zipCode
     *
     * Naming-Convention: findBy + Feld1 + And + Feld2
     */
    Page<CityEntity> findByNameContainingIgnoreCaseAndZipCode(
            String name,
            String zipCode,
            Pageable pageable
    );

    // ========================================================================
    // Custom JPQL Queries (für komplexere Abfragen)
    // ========================================================================

    /**
     * Custom Query mit @Query-Annotation
     *
     * JPQL (Java Persistence Query Language):
     * - Arbeitet mit Entity-Namen (CityEntity), nicht Tabellennamen!
     * - :name ist ein Named Parameter
     */
    @Query("SELECT c FROM CityEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<CityEntity> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * Zählt Städte nach PLZ-Präfix
     *
     * JPQL mit COUNT:
     */
    @Query("SELECT COUNT(c) FROM CityEntity c WHERE c.zipCode LIKE :prefix%")
    long countByZipCodePrefix(@Param("prefix") String prefix);
}
```

#### Spring Data JPA Query Methods (Naming-Conventions):

|          Method-Name           |          Generiertes SQL          |             Beispiel             |
|--------------------------------|-----------------------------------|----------------------------------|
| `findBy{Field}`                | `WHERE field = ?`                 | `findByName("Köln")`             |
| `findBy{Field}Containing`      | `WHERE field LIKE %?%`            | `findByNameContaining("Köl")`    |
| `findBy{Field}IgnoreCase`      | `WHERE LOWER(field) = LOWER(?)`   | `findByNameIgnoreCase("KÖLN")`   |
| `findBy{Field1}And{Field2}`    | `WHERE field1 = ? AND field2 = ?` | `findByNameAndZipCode(...)`      |
| `findBy{Field1}Or{Field2}`     | `WHERE field1 = ? OR field2 = ?`  | `findByNameOrZipCode(...)`       |
| `findBy{Field}Between`         | `WHERE field BETWEEN ? AND ?`     | `findByCreatedBetween(...)`      |
| `findBy{Field}LessThan`        | `WHERE field < ?`                 | `findByAgeLessThan(30)`          |
| `findBy{Field}GreaterThan`     | `WHERE field > ?`                 | `findByAgeGreaterThan(18)`       |
| `findBy{Field}IsNull`          | `WHERE field IS NULL`             | `findByEmailIsNull()`            |
| `findBy{Field}IsNotNull`       | `WHERE field IS NOT NULL`         | `findByEmailIsNotNull()`         |
| `findBy{Field}OrderBy{Field2}` | `ORDER BY field2`                 | `findByNameOrderByCreatedDesc()` |

### 3.3 Repository mit Beziehungen: PracticeJpaRepository

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für PracticeEntity.
 *
 * Besonderheiten:
 * - Queries über Beziehungen (city.name, city.id)
 * - JOIN FETCH für Eager Loading (Performance)
 */
@Repository
public interface PracticeJpaRepository extends JpaRepository<PracticeEntity, UUID> {

    // ========================================================================
    // Queries über Beziehungen
    // ========================================================================

    /**
     * Findet Praxen nach Stadt-Name
     *
     * Spring generiert automatisch:
     * SELECT * FROM practice p
     * JOIN city c ON p.city_id = c.id
     * WHERE LOWER(c.name) LIKE LOWER(:cityName)
     *
     * Naming-Convention: findBy + RelatedEntity + _ + Field
     */
    Page<PracticeEntity> findByCity_NameContainingIgnoreCase(String cityName, Pageable pageable);

    /**
     * Findet Praxen nach Stadt-ID
     */
    Page<PracticeEntity> findByCity_Id(UUID cityId, Pageable pageable);

    /**
     * Findet Praxis nach Name (exakte Suche)
     */
    Optional<PracticeEntity> findByName(String name);

    // ========================================================================
    // Custom Queries mit JOIN FETCH (Performance-Optimierung)
    // ========================================================================

    /**
     * Findet Praxis mit Stadt (Eager Loading)
     *
     * WICHTIG:
     * - JOIN FETCH lädt die City-Entity sofort mit
     * - Verhindert N+1 Problem (mehrere DB-Queries)
     * - Nur verwenden, wenn Stadt wirklich benötigt wird!
     */
    @Query("SELECT p FROM PracticeEntity p JOIN FETCH p.city WHERE p.id = :id")
    Optional<PracticeEntity> findByIdWithCity(@Param("id") UUID id);

    /**
     * Findet alle Praxen mit Stadt (Eager Loading)
     *
     * ⚠️ VORSICHT: Bei großen Datenmengen kann das langsam sein!
     */
    @Query("SELECT DISTINCT p FROM PracticeEntity p JOIN FETCH p.city")
    Page<PracticeEntity> findAllWithCity(Pageable pageable);

    /**
     * Sucht Praxen nach Name UND Stadt-Name
     */
    @Query("""
        SELECT p FROM PracticeEntity p
        JOIN p.city c
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :practiceName, '%'))
        AND LOWER(c.name) LIKE LOWER(CONCAT('%', :cityName, '%'))
        """)
    Page<PracticeEntity> searchByPracticeNameAndCityName(
        @Param("practiceName") String practiceName,
        @Param("cityName") String cityName,
        Pageable pageable
    );
}
```

### 3.4 Repository mit komplexen Queries: DoctorJpaRepository

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.DoctorEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository für DoctorEntity.
 *
 * Besonderheiten:
 * - Queries über n:m Beziehungen (specialities)
 * - Optional-Felder (practice kann null sein)
 * - Komplexe Filter-Kombinationen
 */
@Repository
public interface DoctorJpaRepository extends JpaRepository<DoctorEntity, UUID> {

    // ========================================================================
    // Einfache Queries
    // ========================================================================

    /**
     * Findet Ärzte nach Vor- und Nachname
     */
    Page<DoctorEntity> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    /**
     * Findet Ärzte nach Praxis-ID
     *
     * ⚠️ WICHTIG: practice kann NULL sein!
     */
    Page<DoctorEntity> findByPractice_Id(UUID practiceId, Pageable pageable);

    /**
     * Findet Ärzte OHNE Praxis
     */
    Page<DoctorEntity> findByPracticeIsNull(Pageable pageable);

    // ========================================================================
    // Queries über n:m Beziehungen
    // ========================================================================

    /**
     * Findet Ärzte mit bestimmter Fachrichtung
     *
     * Spring generiert automatisch JOIN über doctor_speciality:
     * SELECT d.* FROM doctor d
     * JOIN doctor_speciality ds ON d.id = ds.doctor_id
     * WHERE ds.speciality_id = :specialityId
     */
    @Query("""
            SELECT DISTINCT d FROM DoctorEntity d
            JOIN d.specialities s
            WHERE s.id = :specialityId
            """)
    Page<DoctorEntity> findBySpecialityId(@Param("specialityId") UUID specialityId, Pageable pageable);

    /**
     * Findet Ärzte mit MEHREREN Fachrichtungen (AND-Verknüpfung)
     *
     * Komplexe Query: Arzt muss ALLE angegebenen Fachrichtungen haben
     */
    @Query("""
            SELECT d FROM DoctorEntity d
            WHERE :speciality1Id MEMBER OF d.specialities
            AND :speciality2Id MEMBER OF d.specialities
            """)
    Page<DoctorEntity> findByMultipleSpecialities(
            @Param("speciality1Id") UUID speciality1Id,
            @Param("speciality2Id") UUID speciality2Id,
            Pageable pageable
    );

    // ========================================================================
    // Komplexe Filter-Queries
    // ========================================================================

    /**
     * Kombinierte Suche: Name + Praxis + Fachrichtung
     *
     * Alle Parameter sind OPTIONAL (null wird ignoriert)
     */
    @Query("""
            SELECT DISTINCT d FROM DoctorEntity d
            LEFT JOIN d.practice p
            LEFT JOIN d.specialities s
            WHERE (:firstName IS NULL OR LOWER(d.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
            AND (:lastName IS NULL OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
            AND (:practiceId IS NULL OR p.id = :practiceId)
            AND (:specialityId IS NULL OR s.id = :specialityId)
            """)
    Page<DoctorEntity> searchDoctors(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("practiceId") UUID practiceId,
            @Param("specialityId") UUID specialityId,
            Pageable pageable
    );

    /**
     * Findet Arzt mit allen Beziehungen (Eager Loading)
     *
     * WICHTIG:
     * - JOIN FETCH lädt practice UND specialities sofort
     * - Nur verwenden, wenn wirklich benötigt (Performance!)
     */
    @Query("""
            SELECT DISTINCT d FROM DoctorEntity d
            LEFT JOIN FETCH d.practice
            LEFT JOIN FETCH d.specialities
            WHERE d.id = :id
            """)
    Optional<DoctorEntity> findByIdWithRelations(@Param("id") UUID id);
}
```

### 3.5 Repository mit Datum-Queries: SlotJpaRepository

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.doctor_provider.domain.enums.SlotStatus;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.SlotEntity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository für SlotEntity.
 *
 * Besonderheiten:
 * - Datum/Zeit-Queries (ZonedDateTime)
 * - Enum-Filter (SlotStatus)
 * - Performance-Optimierungen für häufige Abfragen
 */
@Repository
public interface SlotJpaRepository extends JpaRepository<SlotEntity, UUID> {

    // ========================================================================
    // Queries nach Status
    // ========================================================================

    /**
     * Findet Slots nach Status
     *
     * Enum wird automatisch gemappt (STRING in DB)
     */
    Page<SlotEntity> findByStatus(SlotStatus status, Pageable pageable);

    /**
     * Findet verfügbare Slots
     */
    Page<SlotEntity> findByStatusOrderByStartTimeAsc(SlotStatus status, Pageable pageable);

    // ========================================================================
    // Queries nach Datum/Zeit
    // ========================================================================

    /**
     * Findet Slots zwischen zwei Zeitpunkten
     *
     * WICHTIG:
     * - ZonedDateTime vergleicht Datum + Zeit + Timezone
     * - Between ist INKLUSIVE (start <= x <= end)
     */
    List<SlotEntity> findByStartTimeBetween(ZonedDateTime start, ZonedDateTime end);

    /**
     * Findet Slots ab einem bestimmten Zeitpunkt
     */
    List<SlotEntity> findByStartTimeGreaterThanEqual(ZonedDateTime startTime);

    /**
     * Findet verfügbare Slots für einen Tag
     */
    @Query("""
            SELECT s FROM SlotEntity s
            WHERE s.startTime >= :dayStart
            AND s.startTime < :dayEnd
            AND s.status = :status
            ORDER BY s.startTime ASC
            """)
    List<SlotEntity> findAvailableSlotsForDay(
            @Param("dayStart") ZonedDateTime dayStart,
            @Param("dayEnd") ZonedDateTime dayEnd,
            @Param("status") SlotStatus status
    );

    // ========================================================================
    // Queries über WorkingHours → Doctor
    // ========================================================================

    /**
     * Findet Slots für einen bestimmten Arzt
     *
     * Join über: Slot → WorkingHours → Doctor
     */
    @Query("""
            SELECT s FROM SlotEntity s
            JOIN s.workingHours wh
            WHERE wh.doctorId = :doctorId
            AND s.startTime >= :startDate
            AND s.startTime < :endDate
            ORDER BY s.startTime ASC
            """)
    List<SlotEntity> findByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate
    );

    /**
     * Findet verfügbare Slots für einen Arzt
     */
    @Query("""
            SELECT s FROM SlotEntity s
            JOIN s.workingHours wh
            WHERE wh.doctorId = :doctorId
            AND s.startTime >= :startDate
            AND s.startTime < :endDate
            AND s.status = 'AVAILABLE'
            ORDER BY s.startTime ASC
            """)
    List<SlotEntity> findAvailableSlotsByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate
    );

    // ========================================================================
    // Performance-Queries (mit Index-Nutzung)
    // ========================================================================

    /**
     * Zählt Slots nach Status für einen Arzt
     *
     * Nutzt Index: idx_slot_working_hours
     */
    @Query("""
            SELECT COUNT(s) FROM SlotEntity s
            JOIN s.workingHours wh
            WHERE wh.doctorId = :doctorId
            AND s.status = :status
            """)
    long countByDoctorAndStatus(
            @Param("doctorId") UUID doctorId,
            @Param("status") SlotStatus status
    );

    /**
     * Prüft ob Slot-Zeit bereits existiert (für Validierung)
     */
    @Query("""
            SELECT COUNT(s) > 0 FROM SlotEntity s
            WHERE s.workingHours.id = :workingHoursId
            AND s.startTime = :startTime
            """)
    boolean existsByWorkingHoursAndStartTime(
            @Param("workingHoursId") UUID workingHoursId,
            @Param("startTime") ZonedDateTime startTime
    );
}
```

---

## 4. Persistence-Adapter erstellen

### 4.1 Was ist ein Persistence-Adapter?

Der Persistence-Adapter ist die **Brücke** zwischen Domain und Datenbank:

```
┌─────────────────────────────────────────────────────────────────┐
│                  APPLICATION LAYER (Use Case)                   │
│  - CityService ruft CityOutgoingPort auf                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Interface
┌─────────────────────────────────────────────────────────────────┐
│                  CityOutgoingPort (Interface)                   │
│  - findAll(...)                                                 │
│  - findById(...)                                                │
│  - save(...)                                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Implementierung
┌─────────────────────────────────────────────────────────────────┐
│         PERSISTENCE ADAPTER (CityPersistenceAdapter)            │
│  - Implementiert CityOutgoingPort                               │
│  - Verwendet CityJpaRepository                                  │
│  - Verwendet CityEntityMapper                                   │
│  - Konvertiert: Domain ↔ Entity                                 │
└─────────────────────────────────────────────────────────────────┘
                    │                      │
          ┌─────────┴─────────┐   ┌────────┴────────┐
          ▼                   ▼   ▼                 ▼
  CityJpaRepository    CityEntityMapper
```

### 4.2 Beispiel: CityPersistenceAdapter

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import test.doctor_provider.application.port.outgoing.CityOutgoingPort;
import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.CityEntity;
import test.doctor_provider.infrastructure.outgoing.persistence.mapper.CityEntityMapper;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository.CityJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence-Adapter für City.
 *
 * VERANTWORTLICHKEITEN:
 * 1. Implementiert CityOutgoingPort
 * 2. Ruft CityJpaRepository auf
 * 3. Konvertiert Entity ↔ Domain (via Mapper)
 * 4. Mapped Spring Data Page → Domain Page
 */
@Component
@RequiredArgsConstructor
public class CityPersistenceAdapter implements CityOutgoingPort {

    private final CityJpaRepository repository;
    private final CityEntityMapper mapper;

    @Override
    public Page<City> findAll(Optional<String> name, Optional<String> postalCode, int page, int size) {
        // Spring Data PageRequest erstellen
        PageRequest pageRequest = PageRequest.of(page, size);

        // Je nach Filtern unterschiedliche Repository-Methode aufrufen
        org.springframework.data.domain.Page<CityEntity> entityPage;

        if (name.isPresent() && postalCode.isPresent()) {
            // Beide Filter gesetzt
            entityPage = repository.findByNameContainingIgnoreCaseAndZipCode(
                    name.get(),
                    postalCode.get(),
                    pageRequest
            );
        } else if (name.isPresent()) {
            // Nur Name-Filter
            entityPage = repository.findByNameContainingIgnoreCase(
                    name.get(),
                    pageRequest
            );
        } else if (postalCode.isPresent()) {
            // Nur PLZ-Filter
            entityPage = repository.findByZipCode(
                    postalCode.get(),
                    pageRequest
            );
        } else {
            // Keine Filter
            entityPage = repository.findAll(pageRequest);
        }

        // Entity → Domain Konvertierung
        List<City> cities = mapper.toDomainList(entityPage.getContent());

        // Spring Data Page → Domain Page
        return new Page<>(
                cities,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages()
        );
    }

    @Override
    public Optional<City> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);  // Entity → Domain
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
```

### 4.3 Beispiel: PracticePersistenceAdapter (mit save)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import test.doctor_provider.application.port.outgoing.PracticeOutgoingPort;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntity;
import test.doctor_provider.infrastructure.outgoing.persistence.mapper.PracticeEntityMapper;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository.PracticeJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PracticePersistenceAdapter implements PracticeOutgoingPort {

    private final PracticeJpaRepository repository;
    private final PracticeEntityMapper mapper;

    @Override
    public Practice save(Practice practice) {
        // Domain → Entity
        PracticeEntity entity = mapper.toEntity(practice);

        // In DB speichern (INSERT oder UPDATE)
        PracticeEntity savedEntity = repository.save(entity);

        // Entity → Domain
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Practice> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Practice> findByName(String name) {
        return repository.findByName(name)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Practice> findByCity(UUID cityId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        org.springframework.data.domain.Page<PracticeEntity> entityPage =
                repository.findByCity_Id(cityId, pageRequest);

        return new Page<>(
                mapper.toDomainList(entityPage.getContent()),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                entityPage.getTotalPages()
        );
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
```

---

## 5. Vollständiges Beispiel: City

### 5.1 Datei-Struktur

```
doctor-provider/
└── src/main/java/test/doctor_provider/
    ├── domain/
    │   └── model/
    │       └── City.java                          # ✅ Bereits vorhanden
    │
    ├── application/
    │   └── port/
    │       └── outgoing/
    │           └── CityOutgoingPort.java           # ✅ Bereits vorhanden
    │
    └── infrastructure/adapter/outgoing/persistence/
        ├── entity/
        │   └── CityEntity.java                     # ⭐ NEU ERSTELLEN
        ├── repository/
        │   └── CityJpaRepository.java              # ⭐ NEU ERSTELLEN
        ├── mapper/
        │   └── CityEntityMapper.java               # ⭐ NEU ERSTELLEN (siehe MAPPER_GUIDE.md)
        └── adapter/
            └── CityPersistenceAdapter.java         # ⭐ NEU ERSTELLEN
```

### 5.2 Alle Dateien im Überblick

#### ✅ City.java (Domain - bereits vorhanden)

```java
package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class City {
    private UUID id;
    private String name;
    private String postalCode;
}
```

#### ✅ CityOutgoingPort.java (bereits vorhanden)

```java
package test.doctor_provider.application.port.outgoing;

import test.doctor_provider.domain.model.City;
import test.doctor_provider.domain.model.Page;
import java.util.Optional;
import java.util.UUID;

public interface CityOutgoingPort {
    Page<City> findAll(Optional<String> name, Optional<String> postalCode, int page, int size);
    Optional<City> findById(UUID id);
    boolean existsById(UUID id);
}
```

#### ⭐ CityEntity.java (NEU - oben bereits erklärt)

#### ⭐ CityJpaRepository.java (NEU - oben bereits erklärt)

#### ⭐ CityEntityMapper.java (siehe MAPPER_GUIDE.md)

#### ⭐ CityPersistenceAdapter.java (NEU - oben bereits erklärt)

---

## 6. Komplexes Beispiel: Practice (mit Beziehungen)

### 6.1 Practice Domain-Modell

```java
package test.doctor_provider.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Practice {
    private UUID id;
    private String name;
    private String street;
    private String houseNumber;
    private String phone;
    private String email;
    private String postalCode;
    private UUID cityId;         // ← Nur die ID, KEIN City-Objekt!
}
```

### 6.2 PracticeEntity mit Beziehung

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.CityEntity;

import java.util.UUID;

@Entity
@Table(name = "practice")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    /**
     * ⚠️ WICHTIG: Im Entity ist das ein Objekt (CityEntity)
     * Im Domain-Modell ist das nur eine UUID (cityId)
     *
     * Mapper muss zwischen beiden konvertieren!
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;
}
```

---

### 🔍 **Was ist `FetchType`?**

`FetchType` bestimmt **WANN** verknüpfte Daten aus der Datenbank geladen werden.

#### **Zwei Optionen:**

##### **1. `FetchType.LAZY` (Faul/Verzögert)** 🐌

```java
@ManyToOne(fetch = FetchType.LAZY)
private CityEntity city;
```

**Bedeutung:**
- Die `CityEntity` wird **NICHT sofort** geladen
- Wird **erst** geladen, wenn du `practice.getCity()` aufrufst
- **Performance-Vorteil**: Spart Datenbank-Abfragen

**Beispiel:**

```java
PracticeEntity practice = practiceRepository.findById(id);
// Hier ist city NOCH NICHT geladen! ❌

String cityName = practice.getCity().getName();
// JETZT wird city geladen! ✅ (Extra DB-Query)
```

**SQL:**

```sql
-- Erste Query
SELECT * FROM practice WHERE id = '...'

-- Zweite Query (erst beim Zugriff auf getCity())
SELECT * FROM city WHERE id = '...'
```

---

##### **2. `FetchType.EAGER` (Gierig/Sofort)** 🏃

```java
@ManyToOne(fetch = FetchType.EAGER)
private CityEntity city;
```

**Bedeutung:**
- Die `CityEntity` wird **SOFORT** mit der Practice geladen
- **IMMER** geladen, auch wenn du sie nicht brauchst
- **Performance-Nachteil**: Mehr Daten, auch wenn unnötig

**Beispiel:**

```java
PracticeEntity practice = practiceRepository.findById(id);
// Hier ist city BEREITS geladen! ✅ (Ein einziger DB-Query mit JOIN)

String cityName = practice.getCity().getName();
// Keine weitere DB-Abfrage nötig
```

**SQL:**

```sql
-- Nur EINE Query mit JOIN
SELECT p.*, c.*
FROM practice p
JOIN city c ON p.city_id = c.id
WHERE p.id = '...'
```

---

#### **Wann welches verwenden?**

|    Beziehung    |    Empfehlung    |              Grund               |
|-----------------|------------------|----------------------------------|
| **@ManyToOne**  | `LAZY` ✅         | Standard: Lädt nur bei Bedarf    |
| **@OneToMany**  | `LAZY` ✅         | Standard: Verhindert N+1 Problem |
| **@OneToOne**   | `LAZY` / `EAGER` | Abhängig vom Use Case            |
| **@ManyToMany** | `LAZY` ✅         | Standard: Zu viele Daten sonst   |

---

#### **Best Practice: Query-Optimierung mit JOIN FETCH**

Wenn du **manchmal** die City brauchst (aber nicht immer), verwende `LAZY` + **gezieltes Laden**:

```java
public interface PracticeRepository extends JpaRepository<PracticeEntity, UUID> {

    // LAZY: Lädt nur Practice (ohne City)
    Optional<PracticeEntity> findById(UUID id);

    // EAGER: Lädt Practice MIT City in EINER Query
    @Query("SELECT p FROM PracticeEntity p JOIN FETCH p.city WHERE p.id = :id")
    Optional<PracticeEntity> findByIdWithCity(@Param("id") UUID id);

    // EAGER: Lädt alle Practices MIT City
    @Query("SELECT p FROM PracticeEntity p JOIN FETCH p.city")
    List<PracticeEntity> findAllWithCity();
}
```

**Verwendung:**

```java
// Wenn du NUR Practice-Daten brauchst
PracticeEntity practice = repo.findById(id).orElseThrow();
String name = practice.getName(); // ✅ Kein City-Zugriff

// Wenn du AUCH City-Daten brauchst
PracticeEntity practice = repo.findByIdWithCity(id).orElseThrow();
String cityName = practice.getCity().getName(); // ✅ Bereits geladen
```

---

#### **Parameter erklärt:**

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "city_id", nullable = false)
private CityEntity city;
```

| Parameter  |    Wert     |                   Bedeutung                    |
|------------|-------------|------------------------------------------------|
| `fetch`    | `LAZY`      | City wird nur bei Bedarf geladen               |
| `optional` | `false`     | Practice MUSS immer eine City haben (NOT NULL) |
| `name`     | `"city_id"` | DB-Spaltenname für den Foreign Key             |
| `nullable` | `false`     | DB-Constraint: city_id darf nicht NULL sein    |

---

### 6.3 PracticeEntityMapper (MapStruct)

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntity;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PracticeEntityMapper {

    /**
     * Domain → Entity
     * cityId (UUID) → city.id (CityEntity.id)
     */
    @Mapping(source = "cityId", target = "city.id")
    PracticeEntity toEntity(Practice practice);

    /**
     * Entity → Domain
     * city.id → cityId
     */
    @Mapping(source = "city.id", target = "cityId")
    Practice toDomain(PracticeEntity entity);

    List<Practice> toDomainList(List<PracticeEntity> entities);
}
```

---

## 7. Sehr komplexes Beispiel: Doctor (n:m Beziehungen)

### 7.1 Doctor Domain-Modell

```java
package test.doctor_provider.domain.model;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    private UUID id;
    private String firstName;
    private String lastName;
    private UUID practiceId;              // Optional (kann null sein)
    private Set<UUID> specialityIds;      // n:m Beziehung (nur IDs!)
}
```

### 7.2 DoctorEntity (bereits oben erklärt)

### 7.3 DoctorEntityMapper mit Custom-Logik

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.*;
import test.doctor_provider.domain.model.Doctor;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.DoctorEntity;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.SpecialityEntity;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DoctorEntityMapper {

    @Mapping(source = "practiceId", target = "practice.id")
    @Mapping(source = "specialityIds", target = "specialities", qualifiedByName = "idsToEntities")
    DoctorEntity toEntity(Doctor doctor);

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

---

## 8. Beispiel: Slot (mit ZonedDateTime)

### 8.1 Slot Domain-Modell

```java
package test.doctor_provider.domain.model;

import java.time.ZonedDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import test.doctor_provider.domain.enums.SlotStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Slot {
    private UUID id;
    private UUID workingHoursId;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private SlotStatus status;
}
```

### 8.2 SlotEntity (bereits oben erklärt)

### 8.3 SlotEntityMapper

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import test.doctor_provider.domain.model.Slot;
import test.doctor_provider.infrastructure.outgoing.persistence.entity.SlotEntity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SlotEntityMapper {

    /**
     * Domain → Entity
     *
     * WICHTIG:
     * - workingHoursId → workingHours.id
     * - ZonedDateTime wird automatisch gemappt
     * - SlotStatus (Enum) wird automatisch gemappt
     */
    @Mapping(source = "workingHoursId", target = "workingHours.id")
    SlotEntity toEntity(Slot slot);

    /**
     * Entity → Domain
     */
    @Mapping(source = "workingHours.id", target = "workingHoursId")
    Slot toDomain(SlotEntity entity);

    List<Slot> toDomainList(List<SlotEntity> entities);

    List<SlotEntity> toEntityList(List<Slot> slots);
}
```

---

## 9. Best Practices

### 9.1 Naming-Conventions

|       Komponente        |            Naming            |         Beispiel         |
|-------------------------|------------------------------|--------------------------|
| **Domain-Modell**       | `{Entity}`                   | `City`                   |
| **JPA-Entity**          | `{Entity}Entity`             | `CityEntity`             |
| **Repository**          | `{Entity}JpaRepository`      | `CityJpaRepository`      |
| **Entity-Mapper**       | `{Entity}EntityMapper`       | `CityEntityMapper`       |
| **Persistence-Adapter** | `{Entity}PersistenceAdapter` | `CityPersistenceAdapter` |

### 9.2 Lazy vs. Eager Loading

| Fetch-Type |       Wann verwenden?       |   Vorteil   |                Nachteil                 |
|------------|-----------------------------|-------------|-----------------------------------------|
| **LAZY**   | Standard (immer verwenden!) | Performance | Kann LazyInitializationException werfen |
| **EAGER**  | Nur wenn wirklich benötigt  | Einfach     | Schlechte Performance                   |

**⚠️ Empfehlung:** Immer `LAZY` verwenden und bei Bedarf `JOIN FETCH` in Queries nutzen!

### 9.3 N+1 Problem vermeiden

**Problem:**

```java
List<PracticeEntity> practices = repository.findAll();  // 1 Query
for (PracticeEntity practice : practices) {
    String cityName = practice.getCity().getName();     // N Queries (für jede Praxis!)
}
// Gesamt: 1 + N Queries (schlecht!)
```

**Lösung 1: JOIN FETCH**

```java
@Query("SELECT p FROM PracticeEntity p JOIN FETCH p.city")
List<PracticeEntity> findAllWithCity();  // 1 Query (gut!)
```

**Lösung 2: DTO-Projection**

```java
@Query("SELECT p.name, c.name FROM PracticeEntity p JOIN p.city c")
List<Object[]> findPracticeWithCityNames();  // 1 Query, nur benötigte Felder
```

### 9.4 Indexes nutzen

**In Flyway-Migration:**

```sql
CREATE INDEX idx_practice_city_id ON practice(city_id);
CREATE INDEX idx_practice_name ON practice(name);
CREATE INDEX idx_slot_start_time ON slot(start_time);
CREATE INDEX idx_slot_working_hours ON slot(working_hours_id);
```

**In Repository-Query:**

```java
// Spring nutzt automatisch den Index idx_practice_city_id
Page<PracticeEntity> findByCity_Id(UUID cityId, Pageable pageable);
```

### 9.5 Transaktionen

**Im Service-Layer (nicht im Adapter!):**

```java
@Service
@Transactional
public class PracticeService {

    @Transactional(readOnly = true)  // ← Performance-Optimierung für Lese-Operationen
    public Page<Practice> getAllPractices(...) {
        // ...
    }

    @Transactional  // ← Schreib-Transaktion
    public Practice createPractice(Practice practice) {
        // Validierung...
        return practicePort.save(practice);
    }
}
```

### 9.6 Pagination Best Practices

**Immer Paginierung verwenden:**

```java
// ❌ SCHLECHT: Lädt ALLE Datensätze
List<CityEntity> findAll();

// ✅ GUT: Paginiert
Page<CityEntity> findAll(Pageable pageable);
```

**PageRequest erstellen:**

```java
// Seite 0 (erste Seite), 20 Elemente pro Seite
PageRequest pageRequest = PageRequest.of(0, 20);

// Mit Sortierung
PageRequest pageRequest = PageRequest.of(0, 20, Sort.by("name").ascending());
```

### 9.7 Optional vs. Null

**In Repositories:**

```java
// ✅ GUT: Optional für Single-Results
Optional<CityEntity> findById(UUID id);
Optional<CityEntity> findByName(String name);

// ✅ GUT: List für Multi-Results (niemals null, sondern leere Liste)
List<CityEntity> findByNameContaining(String name);
```

**In Persistence-Adaptern:**

```java
@Override
public Optional<City> findById(UUID id) {
    return repository.findById(id)
            .map(mapper::toDomain);  // Optional bleibt Optional
}
```

### 9.8 Testing

**Repository-Test:**

```java
@DataJpaTest
class CityJpaRepositoryTest {

    @Autowired
    private CityJpaRepository repository;

    @Test
    void testFindByName() {
        CityEntity city = new CityEntity();
        city.setName("Köln");
        city.setZipCode("50667");
        repository.save(city);

        Page<CityEntity> result = repository.findByNameContainingIgnoreCase(
            "köln",
            PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Köln");
    }
}
```

---

## 10. Zusammenfassung

### 10.1 Checkliste: Entity & Repository erstellen

- [ ] **Flyway-Migration erstellt** (Tabelle in DB)
- [ ] **Domain-Modell vorhanden** (POJO ohne JPA)
- [ ] **JPA-Entity erstellt** (mit `@Entity`, `@Table`, `@Column`, etc.)
- [ ] **Repository-Interface erstellt** (extends `JpaRepository`)
- [ ] **Entity-Mapper erstellt** (MapStruct - siehe MAPPER_GUIDE.md)
- [ ] **Persistence-Adapter erstellt** (implementiert OutgoingPort)
- [ ] **Tests geschrieben** (`@DataJpaTest` für Repository)

### 10.2 Reihenfolge beim Erstellen

1. **Flyway-Migration** → Tabelle in DB
2. **Domain-Modell** → Business-Objekt
3. **JPA-Entity** → DB-Mapping
4. **Repository** → Datenzugriff
5. **Entity-Mapper** → Konvertierung
6. **Persistence-Adapter** → Port-Implementierung

### 10.3 Wichtige Punkte

✅ **Domain-Modell** kennt KEINE Datenbank
✅ **JPA-Entity** kennt KEINE Business-Logik
✅ **Mapper** konvertiert zwischen beiden
✅ **Persistence-Adapter** ist die einzige Verbindung zur DB
✅ **Repository** nutzt Spring Data JPA (wenig Code!)
✅ **Lazy Loading** als Standard verwenden
✅ **JOIN FETCH** für N+1 Problem
✅ **Paginierung** immer verwenden

---

**Viel Erfolg beim Implementieren! 🚀**

