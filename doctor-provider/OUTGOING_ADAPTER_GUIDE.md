# 🔌 Outgoing Adapter Guide – Persistence & Externe Services

---

## 1. Was ist ein Outgoing Adapter?

Ein Outgoing Adapter ist die **Ausgangstür** deiner Anwendung. Immer wenn dein Service
**Daten braucht** oder **mit der Außenwelt sprechen** muss, geht das über einen Outgoing Adapter.

```
Service (Business-Logik)
    │  "Ich brauche Daten" / "Speichere das"
    ▼
┌──────────────────────────────┐
│      OUTGOING ADAPTER        │
│                              │
│  1. Domain → Entity/Request  │  (Mapper / Konvertierung)
│  2. Externe Quelle aufrufen  │  (DB, anderer Service, E-Mail, ...)
│  3. Entity/Response → Domain │  (Mapper / Konvertierung)
│  4. Ergebnis zurückgeben     │  (Domain-Objekt)
│                              │
└──────────────────────────────┘
    │  gibt Domain-Objekte zurück
    ▼
Service
```

### Es gibt verschiedene Arten von Outgoing Adaptern:

| Art | Spricht mit... | Beispiel |
|---|---|---|
| **PersistenceAdapter** | Datenbank (PostgreSQL, MySQL, ...) | `CityPersistenceAdapter` |
| **RestClientAdapter** | Anderer Microservice (HTTP) | `PatientServiceAdapter` |
| **EmailAdapter** | E-Mail-Server (SMTP) | `EmailNotificationAdapter` |
| **MessageAdapter** | Message Broker (Kafka, RabbitMQ) | `KafkaNotificationAdapter` |

**Alle** haben das gleiche Prinzip:
- Sie implementieren ein **OutgoingPort** (Interface)
- Der Service kennt **nur das Interface**, nie den Adapter direkt
- Der Adapter erledigt die **technische Arbeit** (SQL, HTTP-Call, SMTP, ...)

---

## 2. Klassisch vs. Hexagonal

| Aspekt | Klassisch | Hexagonal |
|---|---|---|
| Service kennt | Repository / RestTemplate **direkt** | nur OutgoingPort (Interface) |
| Annotation | `@Repository` / `@Component` | `@Component` |
| Austauschbar? | Schwer (fest verdrahtet) | Leicht (Interface tauschen) |
| Wo liegt der Code? | `repository/` Ordner | `infrastructure/outgoing/` Ordner |
| Mapper nötig? | Nein (Entity = Domain oft gleich) | Ja (Entity ≠ Domain, strikt getrennt) |

```
Klassisch:
    Service → Repository → DB
    Service → RestTemplate → Anderer Service

Hexagonal:
    Service → OutgoingPort (Interface) → PersistenceAdapter → Repository → DB
    Service → OutgoingPort (Interface) → RestClientAdapter → RestClient → Anderer Service
```

**Der große Unterschied:** In der klassischen Struktur kennt der Service das Repository direkt.
In der hexagonalen Architektur kennt der Service **nur das Interface** (OutgoingPort).
Dadurch kannst du den Adapter austauschen, ohne den Service zu ändern.

---

## 3. Wichtige Annotationen

### `@Component`

```java
@Component
public class CityPersistenceAdapter implements CityOutgoingPort { ... }
```

**Was macht es?**
- Markiert die Klasse als **Spring Bean** → Spring erstellt automatisch eine Instanz
- Spring scannt beim Start alle `@Component`-Klassen und registriert sie
- Danach kann die Klasse überall per **Dependency Injection** verwendet werden
- Im Service steht `private final CityOutgoingPort port` → Spring sucht automatisch
  eine Bean, die `CityOutgoingPort` implementiert → findet `CityPersistenceAdapter`

**Warum `@Component` und nicht `@Repository` oder `@Service`?**
- `@Component`, `@Service`, `@Repository`, `@Controller` machen **technisch das Gleiche**
  (alle registrieren eine Spring Bean)
- Aber sie zeigen dem Leser die **Rolle** der Klasse:

| Annotation | Rolle | Verwendung |
|---|---|---|
| `@Component` | Allgemeine Komponente | Adapter (Hexagonal) |
| `@Service` | Business-Logik | Service-Klassen |
| `@Repository` | Datenzugriff | JPA Repositories (klassisch) |
| `@Controller` | Web-Eingangspunkt | Controller |

- Der PersistenceAdapter ist **kein Repository** (er benutzt ein Repository, aber er IST keins)
- Er ist auch **kein Service** (er hat keine Business-Logik)
- Daher: `@Component` = „Ich bin ein allgemeiner Baustein"

### `@RequiredArgsConstructor` (Lombok)

```java
@RequiredArgsConstructor
public class CityPersistenceAdapter {
    private final CityRepository cityRepository;       // ← wird automatisch injiziert
    private final CityEntityMapper cityEntityMapper;   // ← wird automatisch injiziert
}
```

**Was macht es?**
- Lombok generiert automatisch einen Konstruktor mit **allen `final`-Feldern**
- Spring erkennt den Konstruktor und injiziert automatisch die passenden Beans
- **Ohne Lombok** müsstest du den Konstruktor selbst schreiben:

```java
// Das generiert Lombok automatisch für dich:
public CityPersistenceAdapter(CityRepository cityRepository, CityEntityMapper cityEntityMapper) {
    this.cityRepository = cityRepository;
    this.cityEntityMapper = cityEntityMapper;
}
```

**Wichtig:** Das Feld MUSS `private final` sein – ohne `final` ignoriert Lombok es!

### `@Override`

```java
@Override
public Page<City> findAll(...) { ... }
```

**Was macht es?**
- Sagt dem Compiler: „Diese Methode kommt aus dem Interface (OutgoingPort)"
- Wenn du die Methodensignatur falsch schreibst → **Compile-Error** (Sicherheit!)
- Ohne `@Override`: Wenn du dich vertippst, erstellt Java einfach eine **neue Methode**
  und die Port-Methode bleibt unimplementiert → Fehler erst zur Laufzeit

---

## 4. Allgemeines Beispiel: PersistenceAdapter (mit DB)

### Ordnerstruktur

```
# Klassisch:
src/main/java/com/example/
├── service/
│   └── ItemService.java              ← kennt ItemRepository direkt
├── repository/
│   └── ItemRepository.java
├── entity/
│   └── ItemEntity.java
└── model/
    └── Item.java

# Hexagonal:
src/main/java/com/example/
├── domain/model/
│   └── Item.java                     ← Domain-Modell (rein, keine DB-Abhängigkeit)
├── application/
│   ├── port/outgoing/
│   │   └── ItemOutgoingPort.java     ← Interface: "Was brauche ich?"
│   └── service/
│       └── ItemService.java          ← kennt NUR ItemOutgoingPort
└── infrastructure/outgoing/persistence/
    ├── entity/
    │   └── ItemEntity.java           ← JPA Entity (DB-Tabelle)
    ├── mapper/
    │   └── ItemEntityMapper.java     ← Entity ↔ Domain Konvertierung
    ├── repository/
    │   └── ItemRepository.java       ← JpaRepository (Spring Data)
    └── adapter/
        └── ItemPersistenceAdapter.java  ← implementiert OutgoingPort
```

### Schritt 1: OutgoingPort (Interface)

Das Interface definiert, **was der Service braucht** – ohne zu sagen WIE.

```java
// application/port/outgoing/ItemOutgoingPort.java
public interface ItemOutgoingPort {
    Page<Item> findAll(Optional<String> name, int page, int size);
    Optional<Item> findById(UUID id);
    Item save(Item item);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}
```

### Schritt 2a: PersistenceAdapter – Klassisch

```java
@Service
public class ItemService {

    // ❌ Service kennt das Repository DIREKT
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemEntity findById(UUID id) {
        // ❌ Gibt Entity zurück, keine Domain-Trennung
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item nicht gefunden"));
    }

    public ItemEntity save(ItemEntity entity) {
        return itemRepository.save(entity);
    }
}
```

**Probleme bei klassisch:**
- Service arbeitet direkt mit Entity-Klassen (DB-abhängig)
- Kein Interface dazwischen → schwer austauschbar
- Wenn sich die DB-Struktur ändert, muss der Service auch geändert werden

### Schritt 2b: PersistenceAdapter – Hexagonal

```java
@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemOutgoingPort {

    private final ItemRepository itemRepository;
    private final ItemEntityMapper itemEntityMapper;

    // ────────── LESEN (mit Paginierung) ──────────

    @Override
    public Page<Item> findAll(Optional<String> name, int page, int size) {

        // 1. Repository aufrufen (gibt Spring Page<Entity> zurück)
        org.springframework.data.domain.Page<ItemEntity> entityPage =
                itemRepository.findAllFiltered(
                        name.orElse(null),          // Optional → null (für JPQL)
                        PageRequest.of(page, size)  // int page + int size → Pageable
                );

        // 2. Entity → Domain konvertieren + Domain-Page bauen
        Page<Item> domainPage = new Page<>();
        domainPage.setItems(itemEntityMapper.toDomain(entityPage.getContent()));
        domainPage.setPage(entityPage.getNumber());
        domainPage.setSize(entityPage.getSize());
        domainPage.setTotalElements(entityPage.getTotalElements());
        domainPage.setTotalPages(entityPage.getTotalPages());
        return domainPage;
    }

    // ────────── LESEN (einzeln) ──────────

    @Override
    public Optional<Item> findById(UUID id) {
        // Repository gibt Optional<ItemEntity> zurück
        // .map() konvertiert Entity → Domain (nur wenn vorhanden)
        return itemRepository.findById(id)
                .map(itemEntityMapper::toDomain);
    }

    // ────────── SCHREIBEN ──────────

    @Override
    public Item save(Item item) {
        // 1. Domain → Entity konvertieren
        ItemEntity entity = itemEntityMapper.toEntity(item);

        // 2. In DB speichern (Spring gibt gespeicherte Entity zurück, mit generierter ID)
        ItemEntity savedEntity = itemRepository.save(entity);

        // 3. Entity → Domain konvertieren und zurückgeben
        return itemEntityMapper.toDomain(savedEntity);
    }

    // ────────── PRÜFEN ──────────

    @Override
    public boolean existsById(UUID id) {
        // Direkt ans Repository delegieren (kein Mapping nötig, nur true/false)
        return itemRepository.existsById(id);
    }

    // ────────── LÖSCHEN ──────────

    @Override
    public void deleteById(UUID id) {
        // Direkt ans Repository delegieren (kein Mapping nötig)
        itemRepository.deleteById(id);
    }
}
```

### Warum 3 Schritte bei findAll()?

```
OutgoingPort sagt:    Page<Item>       findAll(Optional<String>, int, int)
Repository erwartet:  Page<ItemEntity> findAllFiltered(String, Pageable)
                      ↑ Spring Page       ↑ Entity       ↑ null    ↑ PageRequest

Du musst also 3 Dinge konvertieren:
1. Optional<String> → String (null)           mit .orElse(null)
2. int page + int size → Pageable             mit PageRequest.of(page, size)
3. Page<ItemEntity> (Spring) → Page<Item> (Domain)  mit Mapper + manuelles Bauen
```

---

## 5. Allgemeines Beispiel: RestClientAdapter (mit anderem Service)

Ein Outgoing Adapter kann auch mit einem **anderen Microservice** kommunizieren.

### Wann brauche ich das?

```
Beispiel: Dein doctor-provider braucht Patienten-Daten vom patient-customer Service

doctor-provider Service
    │  "Existiert der Patient mit dieser ID?"
    ▼
OutgoingPort (Interface)
    │
    ▼
PatientRestClientAdapter            ← Outgoing Adapter
    │  HTTP GET http://patient-service/api/v1/patients/{id}
    ▼
patient-customer Service (anderer Microservice)
```

### Ordnerstruktur

```
# Hexagonal:
infrastructure/outgoing/
├── persistence/           ← Adapter für DB
│   └── adapter/
│       └── ItemPersistenceAdapter.java
└── rest/                  ← Adapter für andere Services (HTTP)
    ├── client/
    │   └── PatientRestClient.java
    ├── mapper/
    │   └── PatientRestMapper.java
    └── adapter/
        └── PatientRestClientAdapter.java
```

### Schritt 1: OutgoingPort

```java
// application/port/outgoing/PatientOutgoingPort.java
public interface PatientOutgoingPort {
    Optional<Patient> findPatientById(UUID patientId);
    boolean patientExists(UUID patientId);
}
```

### Schritt 2: RestClient erstellen

```java
// infrastructure/outgoing/rest/client/PatientRestClient.java
@Component
public class PatientRestClient {

    private final RestClient restClient;

    public PatientRestClient(RestClient.Builder builder,
                             @Value("${services.patient.url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Optional<PatientResponse> findById(UUID id) {
        try {
            PatientResponse response = restClient.get()
                    .uri("/api/v1/patients/{id}", id)
                    .retrieve()
                    .body(PatientResponse.class);
            return Optional.ofNullable(response);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
```

### Neue Annotationen:

#### `@Value`

```java
@Value("${services.patient.url}")
private String baseUrl;
```

**Was macht es?**
- Liest einen Wert aus `application.properties` (oder `application.yml`)
- In `application.properties` steht z.B.: `services.patient.url=http://localhost:8081`
- Spring ersetzt `${services.patient.url}` automatisch durch den Wert
- **Vorteil:** URL ist konfigurierbar, nicht hardcoded

#### `RestClient`

```java
private final RestClient restClient;
```

**Was ist das?**
- Spring Boot 3.2+ bietet `RestClient` als **modernen HTTP-Client**
- Ersetzt `RestTemplate` (alt) und `WebClient` (reaktiv, komplexer)
- Synchron und blockierend – passt perfekt zu Spring MVC
- Kann GET, POST, PUT, DELETE Requests an andere Services senden

### Schritt 3: Adapter implementieren

```java
@Component
@RequiredArgsConstructor
public class PatientRestClientAdapter implements PatientOutgoingPort {

    private final PatientRestClient patientRestClient;
    private final PatientRestMapper patientRestMapper;

    @Override
    public Optional<Patient> findPatientById(UUID patientId) {
        // 1. HTTP-Call an patient-customer Service
        // 2. Response → Domain konvertieren
        return patientRestClient.findById(patientId)
                .map(patientRestMapper::toDomain);
    }

    @Override
    public boolean patientExists(UUID patientId) {
        return patientRestClient.findById(patientId).isPresent();
    }
}
```

### Vergleich: PersistenceAdapter vs. RestClientAdapter

| Aspekt | PersistenceAdapter | RestClientAdapter |
|---|---|---|
| Spricht mit | Datenbank | Anderer Microservice |
| Benutzt intern | `JpaRepository` | `RestClient` |
| Konvertiert | Entity ↔ Domain | Response-DTO ↔ Domain |
| Annotation | `@Component` | `@Component` |
| Implementiert | OutgoingPort | OutgoingPort |
| Konfiguration | `application.properties` (DB-URL) | `application.properties` (Service-URL) |

**Das Muster ist IMMER gleich:**
1. Implementiere das OutgoingPort-Interface
2. Benutze intern die technische Bibliothek (JPA, RestClient, Kafka, ...)
3. Konvertiere zwischen Domain und externer Darstellung (Entity, DTO, ...)

---

## 6. application.properties Konfiguration

```properties
# ── Für PersistenceAdapter (DB) ──
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=secret

# ── Für RestClientAdapter (anderer Service) ──
services.patient.url=http://localhost:8081
services.notification.url=http://localhost:8082
```

---

## 7. Zusammenfassung – Wann verwende ich was?

| Situation | Adapter-Typ | Intern |
|---|---|---|
| Daten in meiner DB lesen/schreiben | PersistenceAdapter | JpaRepository + EntityMapper |
| Daten von anderem Service holen | RestClientAdapter | RestClient + RestMapper |
| E-Mail senden | EmailAdapter | JavaMailSender |
| Nachricht an Kafka senden | KafkaAdapter | KafkaTemplate |

**Alle** folgen dem gleichen Prinzip:

```
Service
    │  ruft auf
    ▼
OutgoingPort (Interface)        ← "WAS brauche ich?"
    │  implementiert
    ▼
Adapter (@Component)            ← "WIE mache ich das technisch?"
    │  benutzt intern
    ▼
Technische Bibliothek           ← JpaRepository / RestClient / KafkaTemplate / ...
```

**Merke:** Der Service weiß NIE, ob seine Daten aus einer DB, einem HTTP-Call oder
einer Kafka-Queue kommen. Er kennt nur das OutgoingPort-Interface. Das ist der
Kern der hexagonalen Architektur! 🎯

