# 🗄️ Repository & Persistence-Adapter Guide

## Was bauen wir? – Die 2 Teile

In der hexagonalen Architektur brauchst du **2 Dateien** pro Domain-Objekt:

```
infrastructure/adapter/outgoing/persistence/
├── repository/
│   └── PracticeJpaRepository.java     ← Teil 1: Spricht mit der DB
└── adapter/
    └── PracticePersistenceAdapter.java ← Teil 2: Verbindet Domain mit DB
```

### Warum 2 Dateien?

```
Service (Application Layer)
    │
    ▼ ruft auf
OutgoingPort (Interface)          ← "Ich brauche Daten"
    │
    ▼ implementiert
PersistenceAdapter (Klasse)       ← "Ich hole/speichere Daten & konvertiere"
    │
    ├── verwendet: EntityMapper   ← Domain ↔ Entity umwandeln
    │
    ▼ ruft auf
JpaRepository (Interface)         ← "Ich rede mit der Datenbank"
    │
    ▼
PostgreSQL Datenbank
```

---

## Teil 1: JpaRepository

### Was ist ein JpaRepository?

Ein Interface, das du erstellst. **Spring generiert automatisch die Implementierung!**
Du schreibst also **keinen** Code für `save()`, `findById()`, `delete()` etc.

### Grundstruktur

```java
@Repository
public interface PracticeJpaRepository extends JpaRepository<PracticeEntitiy, UUID> {
//                                                           ↑ Entity-Klasse  ↑ ID-Typ
}
```

**Was bedeuten die zwei Parameter in `JpaRepository<Entity, ID-Typ>`?**

| Parameter | Bedeutung | Woher kommt der Wert? |
|---|---|---|
| 1. `PracticeEntitiy` | Die Entity-Klasse | Die `@Entity`-Klasse, die dieses Repository verwaltet |
| 2. `UUID` | Der Typ des Primary Keys | Der Typ des `@Id`-Feldes in der Entity |

Der zweite Parameter muss **immer exakt zum Typ des `@Id`-Feldes** passen:

```java
// Deine Entity:
public class CityEntity {
    @Id
    private UUID id;       // ← Typ ist UUID
}

// Also im Repository:
JpaRepository<CityEntity, UUID>   // ✅ UUID passt zu UUID

// Wäre die ID ein Long:
// @Id private Long id;
// → JpaRepository<CityEntity, Long>

// Wäre die ID ein String:
// @Id private String id;
// → JpaRepository<CityEntity, String>
```

> In deinem Projekt sind **alle IDs `UUID`** → deshalb überall `JpaRepository<..., UUID>`.

**Das war's!** Damit hast du bereits diese Methoden kostenlos:

| Methode | Rückgabetyp | Was sie tut |
|---|---|---|
| `save(entity)` | `Entity` | INSERT (neue ID) oder UPDATE (bestehende ID) |
| `saveAll(List<Entity>)` | `List<Entity>` | Mehrere auf einmal speichern (Bulk) |
| `findById(id)` | `Optional<Entity>` | SELECT WHERE id = ? |
| `findAll()` | `List<Entity>` | SELECT * (alle Einträge) |
| `findAll(Pageable)` | `Page<Entity>` | SELECT * mit Paginierung |
| `findAll(Sort)` | `List<Entity>` | SELECT * sortiert (z.B. nach Name) |
| `findAllById(List<UUID>)` | `List<Entity>` | SELECT WHERE id IN (?, ?, ?) |
| `deleteById(id)` | `void` | DELETE WHERE id = ? |
| `delete(entity)` | `void` | DELETE (anhand des Entity-Objekts) |
| `deleteAll()` | `void` | DELETE * (⚠️ löscht ALLES!) |
| `deleteAllById(List<UUID>)` | `void` | DELETE WHERE id IN (?, ?, ?) |
| `existsById(id)` | `boolean` | Gibt es einen Eintrag mit dieser ID? |
| `count()` | `long` | Wie viele Einträge gibt es insgesamt? |
| `flush()` | `void` | Schreibt alle Änderungen sofort in die DB |
| `getReferenceById(id)` | `Entity` | Gibt einen Proxy zurück (LAZY, kein SELECT!) |

### Eigene Methoden hinzufügen

Es gibt **3 Wege**, eigene Queries zu erstellen:

---

#### Weg 1: Derived Query Methods (Spring errät den Query aus dem Methodennamen)

```java
@Repository
public interface PracticeJpaRepository extends JpaRepository<PracticeEntitiy, UUID> {

    // Spring liest den Methodennamen und generiert:
    // SELECT * FROM practice WHERE name = ?
    boolean existsByName(String name);

    // SELECT * FROM practice WHERE name = ? AND id != ?
    boolean existsByNameAndIdNot(String name, UUID id);

    // SELECT * FROM practice WHERE city_id = ?
    // ⚠️ "city" = Feld in PracticeEntitiy, Spring folgt der Beziehung automatisch!
    List<PracticeEntitiy> findByCityId(UUID cityId);
}
```

**Regeln für Methodennamen:**
```
findBy + Feldname          → WHERE feld = ?
existsBy + Feldname        → boolean: gibt es das?
countBy + Feldname          → long: wie viele?
deleteBy + Feldname         → DELETE WHERE feld = ?

Kombinationen:
findByFieldAAndFieldB       → WHERE a = ? AND b = ?
findByNameContainingIgnoreCase → WHERE LOWER(name) LIKE LOWER('%?%')
findByField + Not           → WHERE feld != ?
```

**⚠️ WICHTIG:** Die Feldnamen müssen **exakt** den Java-Feldnamen in der Entity entsprechen!

---

#### Weg 2: @Query mit JPQL (wenn der Methodenname zu lang/komplex wird)

```java
@Repository
public interface WorkingHoursJpaRepository extends JpaRepository<WorkingHoursEntity, UUID> {

    // Alle Working Hours eines Arztes
    List<WorkingHoursEntity> findAllByDoctorId(UUID doctorId);  // ← Weg 1 reicht hier

    // Überlappungs-Prüfung → zu komplex für Methodennamen → @Query verwenden
    @Query("""
        SELECT COUNT(wh) > 0 FROM WorkingHoursEntity wh
        WHERE wh.doctor.id = :doctorId
          AND wh.weekday = :weekday
          AND wh.startTime < :endTime
          AND wh.endTime > :startTime
          AND (:excludeId IS NULL OR wh.id != :excludeId)
        """)
    boolean existsOverlapping(
        @Param("doctorId") UUID doctorId,
        @Param("weekday") Weekday weekday,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") UUID excludeId
    );
}
```

**JPQL vs SQL:**
- JPQL verwendet **Java-Klassen- und Feldnamen**, nicht DB-Tabellennamen!
- `WorkingHoursEntity` statt `doctor_working_hours`
- `wh.doctor.id` statt `wh.doctor_id`
- `wh.weekday` statt `wh.weekday`

---

#### Weg 3: @Query mit nativeQuery = true (echtes SQL)

```java
@Query(value = """
    SELECT * FROM doctor_working_hours
    WHERE doctor_id = :doctorId
    """, nativeQuery = true)
List<WorkingHoursEntity> findByDoctorNative(@Param("doctorId") UUID doctorId);
```

**Wann welchen Weg?**

| Situation | Weg |
|---|---|
| Einfache Abfragen (1-2 Felder) | Weg 1: Methodenname |
| Komplexe Logik (JOINs, OR, Berechnungen) | Weg 2: @Query JPQL |
| DB-spezifische Features (z.B. PostgreSQL-Funktionen) | Weg 3: nativeQuery |

---

### Paginierung im Repository

Wenn dein OutgoingPort `Page<T>` zurückgibt, brauchst du `Pageable` als Parameter:

```java
@Repository
public interface PracticeJpaRepository extends JpaRepository<PracticeEntitiy, UUID> {

    // Alle Praxen, paginiert
    // Spring Data gibt eine org.springframework.data.domain.Page zurück!
    Page<PracticeEntitiy> findAll(Pageable pageable);

    // Mit Filter + Paginierung
    Page<PracticeEntitiy> findByCityId(UUID cityId, Pageable pageable);

    // Mehrere optionale Filter → @Query mit JPQL
    @Query("""
        SELECT p FROM PracticeEntitiy p
        WHERE (:cityId IS NULL OR p.city.id = :cityId)
          AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        """)
    Page<PracticeEntitiy> findAllFiltered(
        @Param("cityId") UUID cityId,
        @Param("name") String name,
        Pageable pageable
    );
}
```

---

### JOIN FETCH (Performance-Optimierung)

#### Schritt 1: Was ist LAZY Loading?

In deinen Entities hast du überall `fetch = FetchType.LAZY`:

```java
public class PracticeEntitiy {
    @ManyToOne(fetch = FetchType.LAZY)   // ← LAZY!
    @JoinColumn(name = "city_id")
    private CityEntity city;
}
```

**LAZY bedeutet:** Wenn du eine Practice aus der DB lädst, wird die City **NICHT** mitgeladen.
Das `city`-Feld ist erstmal ein **leerer Platzhalter** (Hibernate-Proxy).

```java
PracticeEntitiy practice = repository.findById(id).get();

// → JPA führt aus: SELECT * FROM practice WHERE id = ?
// → practice.city ist NICHT geladen (nur ein Proxy-Objekt)

practice.getName();       // ✅ Funktioniert sofort (wurde geladen)
practice.getCity();       // ⚠️ Gibt ein Proxy-Objekt zurück (noch KEINE DB-Abfrage!)
practice.getCity().getId(); // ⚠️ Funktioniert (ID steckt im Proxy)
practice.getCity().getName(); // 🔴 JETZT wird ein ZWEITER SQL-Query ausgeführt!
                              //    → SELECT * FROM city WHERE id = ?
```

**Also:** LAZY = Die Beziehung wird erst geladen, wenn du **auf ein Feld zugreifst** (außer der ID).

---

#### Schritt 2: Was ist das N+1 Problem?

Stell dir vor, du lädst **alle Praxen** und willst für jede die Stadt wissen:

```java
List<PracticeEntitiy> practices = repository.findAll();
// → SQL Query 1: SELECT * FROM practice
// → Ergebnis: 100 Praxen geladen, aber city ist bei allen nur ein Proxy!

for (PracticeEntitiy p : practices) {
    String cityName = p.getCity().getName();  // ← Für JEDE Praxis ein extra Query!
    // → SQL Query 2:  SELECT * FROM city WHERE id = 'city-001'
    // → SQL Query 3:  SELECT * FROM city WHERE id = 'city-002'
    // → SQL Query 4:  SELECT * FROM city WHERE id = 'city-003'
    // → ...
    // → SQL Query 101: SELECT * FROM city WHERE id = 'city-100'
}
```

**Ergebnis: 1 + 100 = 101 SQL-Queries!** Das ist das **N+1 Problem**:
- **1** Query für die Haupttabelle (practice)
- **N** Queries für die Beziehung (city) – eine pro Praxis

Das ist **extrem langsam**, besonders bei vielen Daten.

---

#### Schritt 3: Wie löst JOIN FETCH das Problem?

```java
@Query("SELECT p FROM PracticeEntitiy p JOIN FETCH p.city")
List<PracticeEntitiy> findAllWithCity();
```

**Was passiert?** JPA generiert daraus **einen einzigen** SQL-Query:

```sql
SELECT p.*, c.*
FROM practice p
INNER JOIN city c ON p.city_id = c.id
```

**Ergebnis: NUR 1 Query!** Alle Praxen UND ihre Cities werden in einem Schlag geladen.

```java
List<PracticeEntitiy> practices = repository.findAllWithCity();
// → SQL: SELECT p.*, c.* FROM practice p JOIN city c ON p.city_id = c.id
// → 1 einziger Query! Alle Cities sind sofort da!

for (PracticeEntitiy p : practices) {
    String cityName = p.getCity().getName();  // ✅ KEIN extra Query! Schon geladen!
}
```

---

#### Schritt 4: Wann brauche ich JOIN FETCH in meinem Projekt?

Schau dir deinen **Mapper** an. Wenn der Mapper auf die Beziehung zugreift, brauchst du JOIN FETCH:

```java
// PracticeEntityMapper:
@Mapping(source = "city.id", target = "cityId")   // ← Zugriff auf city.id!
Practice toDomain(PracticeEntitiy entity);
```

Hier greift MapStruct auf `entity.getCity().getId()` zu. Das funktioniert zwar auch ohne
JOIN FETCH (weil `.getId()` den Proxy nicht auslöst), aber sobald du `.getName()` oder
andere Felder brauchst, bekommst du das N+1 Problem.

**Faustregel für dein Projekt:**

| Entity | Beziehung | JOIN FETCH nötig? |
|---|---|---|
| `PracticeEntitiy` → `city` | Mapper braucht `city.id` | ⚠️ Empfohlen bei `findAll()` |
| `DoctorEntity` → `practice` | Mapper braucht `practice.id` | ⚠️ Empfohlen bei `findAll()` |
| `DoctorEntity` → `specialities` | Mapper braucht `specialities` (Set) | ✅ Ja, sonst N+1! |
| `WorkingHoursEntity` → `doctor` | Mapper braucht `doctor.id` | ⚠️ Empfohlen bei `findAll()` |
| `SlotEntity` → `workingHours` | Mapper braucht `workingHours.id` | ⚠️ Empfohlen bei `findAll()` |

---

#### Schritt 5: JOIN FETCH mit Paginierung

⚠️ **ACHTUNG:** `JOIN FETCH` + `Pageable` zusammen funktioniert **nicht direkt**!

```java
// ❌ FEHLER: Spring wirft eine Warning/Exception
@Query("SELECT p FROM PracticeEntitiy p JOIN FETCH p.city")
Page<PracticeEntitiy> findAllWithCity(Pageable pageable);
```

**Lösung: `countQuery` separat angeben:**

```java
// ✅ RICHTIG: countQuery ohne JOIN FETCH
@Query(value = "SELECT p FROM PracticeEntitiy p JOIN FETCH p.city",
       countQuery = "SELECT COUNT(p) FROM PracticeEntitiy p")
Page<PracticeEntitiy> findAllWithCity(Pageable pageable);
```

**Warum?** Spring muss für die Paginierung wissen, wie viele Ergebnisse es **insgesamt** gibt.
Dafür macht es einen COUNT-Query. Aber COUNT + JOIN FETCH macht keinen Sinn
(man zählt ja nur, man braucht keine Beziehung zu laden). Deshalb gibst du den
COUNT-Query **ohne** JOIN FETCH separat an.

---

## Teil 2: PersistenceAdapter

### Was ist ein PersistenceAdapter?

Eine Klasse, die:
1. Das **OutgoingPort-Interface** implementiert (aus der Application-Schicht)
2. Das **JpaRepository** aufruft (für DB-Zugriff)
3. Den **EntityMapper** verwendet (Domain ↔ Entity konvertieren)

### Vollständiges Beispiel: PracticePersistenceAdapter

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import test.doctor_provider.application.port.outgoing.PracticeOutgoingPort;
import test.doctor_provider.domain.model.Practice;
import test.doctor_provider.domain.model.Page;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity.PracticeEntitiy;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.mapper.PracticeEntityMapper;
import test.doctor_provider.infrastructure.adapter.outgoing.persistence.repository.PracticeJpaRepository;

import java.util.Optional;
import java.util.UUID;

@Component                    // ← Spring erstellt eine Instanz (Bean)
@RequiredArgsConstructor      // ← Lombok: Constructor mit allen final-Feldern
public class PracticePersistenceAdapter implements PracticeOutgoingPort {
//                                                 ↑ implementiert das OutgoingPort

    private final PracticeJpaRepository repository;  // ← DB-Zugriff
    private final PracticeEntityMapper mapper;        // ← Domain ↔ Entity

    // ┌─────────────────────────────────────────────────────────────┐
    // │ MUSTER 1: save() - Domain → Entity → DB → Entity → Domain │
    // └─────────────────────────────────────────────────────────────┘
    @Override
    public Practice save(Practice practice) {
        // Schritt 1: Domain → Entity (Mapper)
        PracticeEntitiy entity = mapper.toEntity(practice);

        // Schritt 2: Entity in DB speichern (Repository)
        //            → JPA generiert die ID und gibt die gespeicherte Entity zurück
        PracticeEntitiy savedEntity = repository.save(entity);

        // Schritt 3: Entity → Domain (Mapper) und zurückgeben
        return mapper.toDomain(savedEntity);
    }

    // ┌─────────────────────────────────────────────────────────────┐
    // │ MUSTER 2: findById() - DB → Entity → Domain               │
    // └─────────────────────────────────────────────────────────────┘
    @Override
    public Optional<Practice> findById(UUID id) {
        // repository.findById() gibt Optional<PracticeEntitiy> zurück
        // .map(mapper::toDomain) konvertiert das Entity (falls vorhanden) zu Domain
        return repository.findById(id)
                .map(mapper::toDomain);
    }
    // ⚠️ Was ist .map(mapper::toDomain)?
    //    Kurzform für: .map(entity -> mapper.toDomain(entity))
    //    Wenn Optional LEER ist → gibt Optional.empty() zurück (kein Mapper-Aufruf)
    //    Wenn Optional VOLL ist → ruft mapper.toDomain(entity) auf

    // ┌─────────────────────────────────────────────────────────────┐
    // │ MUSTER 3: findAll() mit Paginierung + Filtern              │
    // └─────────────────────────────────────────────────────────────┘
    @Override
    public Page<Practice> findAll(Optional<UUID> cityId, Optional<String> practiceName,
                                   int page, int size) {
        // Schritt 1: PageRequest erstellen (Spring Data braucht das)
        PageRequest pageRequest = PageRequest.of(page, size);

        // Schritt 2: Repository aufrufen (gibt Spring Data Page zurück)
        //            Hier: Filter-Parameter an Repository weitergeben
        //            .orElse(null) → Wenn Optional leer ist, wird null übergeben
        //            → In der @Query: ":param IS NULL OR ..." fängt das ab
        org.springframework.data.domain.Page<PracticeEntitiy> entityPage =
            repository.findAllFiltered(
                cityId.orElse(null),
                practiceName.orElse(null),
                pageRequest
            );

        // Schritt 3: Spring Data Page → Domain Page konvertieren
        return new Page<>(
            mapper.toDomain(entityPage.getContent()),  // List<Entity> → List<Domain>
            entityPage.getNumber(),                     // aktuelle Seite
            entityPage.getSize(),                       // Elemente pro Seite
            entityPage.getTotalElements(),              // Gesamtanzahl
            entityPage.getTotalPages()                  // Gesamtseiten
        );
    }
    // ⚠️ WICHTIG: Wir haben ZWEI verschiedene "Page"-Klassen:
    //    - org.springframework.data.domain.Page (Spring Data) → vom Repository
    //    - test.doctor_provider.domain.model.Page (Domain)     → unser eigenes
    //    Der Adapter konvertiert zwischen beiden!

    // ┌─────────────────────────────────────────────────────────────┐
    // │ MUSTER 4: Einfache Methoden (direkt durchreichen)         │
    // └─────────────────────────────────────────────────────────────┘
    @Override
    public void removeById(UUID id) {
        repository.deleteById(id);  // ← Direkt durchreichen, kein Mapper nötig
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);  // ← Direkt durchreichen
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);  // ← Direkt durchreichen
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        return repository.existsByNameAndIdNot(name, excludeId);
    }

    // ┌─────────────────────────────────────────────────────────────┐
    // │ MUSTER 5: update/modify (gleich wie save!)                │
    // └─────────────────────────────────────────────────────────────┘
    @Override
    public Practice remove(Practice practice) {
        // ⚠️ save() macht sowohl INSERT als auch UPDATE!
        // Wenn die Entity eine ID hat → UPDATE
        // Wenn die Entity keine ID hat → INSERT
        PracticeEntitiy entity = mapper.toEntity(practice);
        PracticeEntitiy savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
```

