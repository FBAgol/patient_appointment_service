# ⚙️ Service Guide – Business-Logik in Klassisch & Hexagonal

---

## 1. Was ist ein Service?

Ein Service ist das **Gehirn** deiner Anwendung. Er enthält die **Business-Logik** – also die Regeln, die bestimmen, **was passieren darf und was nicht**.

```
Controller
    │  ruft auf
    ▼
┌──────────────────────────────┐
│          SERVICE             │
│                              │
│  1. Validierungen            │  (Existiert die ID? Duplikat?)
│  2. Business-Regeln          │  (Darf das passieren?)
│  3. Daten holen/speichern    │  (Repository / OutgoingPort)
│  4. Ergebnis zurückgeben     │  (Domain-Objekt)
│                              │
└──────────────────────────────┘
    │  gibt zurück
    ▼
Controller
```

**Der Service macht NICHT:**
- ❌ HTTP-Requests empfangen (das macht der Controller)
- ❌ JSON konvertieren (das macht der Mapper)
- ❌ SQL-Queries schreiben (das macht das Repository)
- ❌ HTTP-Status-Codes setzen (das macht der Controller)

**Der Service macht:**
- ✅ Validierungen (existiert die City-ID? Gibt es schon eine Praxis mit diesem Namen?)
- ✅ Business-Regeln (darf ein Slot gebucht werden, wenn er blockiert ist?)
- ✅ Orchestrierung (mehrere OutgoingPorts aufrufen und Ergebnisse zusammenfügen)

---

## 2. Klassisch vs. Hexagonal

|        Aspekt         |        Klassisch         |         Hexagonal (dein Projekt)         |
|-----------------------|--------------------------|------------------------------------------|
| Service kennt         | Repository direkt        | nur OutgoingPort (Interface)             |
| Wer ruft den Service? | Controller direkt        | Controller über IncomingPort (Interface) |
| Annotations           | `@Service`               | `@Service` (gleich!)                     |
| Austauschbar?         | Schwer (fest verdrahtet) | Leicht (Interfaces tauschen)             |

```
Klassisch:   Controller → Service → Repository → DB
Hexagonal:   Controller → IncomingPort → Service → OutgoingPort → Adapter → Repository → DB
```

**Der Unterschied:** In der hexagonalen Architektur hat der Service **zwei Interfaces** drum herum:
- **IncomingPort** = Was der Service **anbietet** (der Controller ruft das auf)
- **OutgoingPort** = Was der Service **braucht** (Daten lesen/schreiben)

---

## 3. Wichtige Annotationen

### `@Service`

```java
@Service
public class ItemService { ... }
```

**Was macht es?**
- Markiert die Klasse als **Spring Bean** (Spring erstellt automatisch eine Instanz)
- Ist eine **Spezialisierung** von `@Component`
- Spring scannt beim Starten alle `@Service`-Klassen und registriert sie im **Application Context**
- Danach kann die Klasse überall per **Dependency Injection** verwendet werden

**Warum nicht einfach `@Component`?**
- `@Component`, `@Service`, `@Repository`, `@Controller` machen technisch das Gleiche (Bean registrieren)
- Aber sie zeigen dem Leser die **Rolle** der Klasse:

|            Annotation             |                 Rolle                 |      Schicht       |
|-----------------------------------|---------------------------------------|--------------------|
| `@Controller` / `@RestController` | Empfängt HTTP-Requests                | Web/Infrastruktur  |
| `@Service`                        | Business-Logik                        | Application/Domain |
| `@Repository`                     | Datenbank-Zugriff                     | Infrastruktur      |
| `@Component`                      | Allgemein (wenn nichts anderes passt) | Beliebig           |

**Ohne `@Service`:** Spring kennt die Klasse nicht → kann sie nicht injizieren → `No beans of type found`!

---

### `@RequiredArgsConstructor` (Lombok)

```java
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository repository;  // ← Lombok erstellt den Constructor dafür
}
```

**Was macht es?**
- Lombok generiert **automatisch** einen Constructor für alle `final`-Felder
- Das ist **Constructor Injection** – die empfohlene Art der Dependency Injection in Spring

**Was Lombok im Hintergrund generiert:**

```java
// DAS generiert Lombok automatisch:
public ItemService(ItemRepository repository) {
    this.repository = repository;
}
```

**Warum `final`?**
- `final` = das Feld kann nach der Erstellung nicht mehr geändert werden
- Garantiert, dass die Abhängigkeit immer gesetzt ist (nie `null`)
- Spring ruft den Constructor automatisch auf und übergibt die richtige Bean

---

### `@Transactional`

```java
@Transactional
public Item create(Item item) {
    // Alles hier drin läuft in EINER DB-Transaktion
    repository.save(item);
    otherRepository.save(relatedEntity);
    // Wenn hier eine Exception fliegt → ALLES wird rückgängig gemacht (Rollback)
}
```

**Was macht es?**
- Startet eine **Datenbank-Transaktion** vor der Methode
- Wenn die Methode **erfolgreich** endet → **Commit** (alles wird gespeichert)
- Wenn eine **Exception** fliegt → **Rollback** (alles wird rückgängig gemacht)
- Garantiert **Konsistenz**: Entweder werden ALLE Änderungen gespeichert oder KEINE

**Wann brauchst du `@Transactional`?**

|             Situation             |          `@Transactional` nötig?          |
|-----------------------------------|-------------------------------------------|
| Nur lesen (findAll, findById)     | ❌ Nicht nötig (aber schadet nicht)        |
| Ein einzelnes `save()`            | ⚠️ Optional (JPA macht das intern schon)  |
| Mehrere `save()` in einer Methode | ✅ Ja! Sonst wird nur ein Teil gespeichert |
| Lesen + Schreiben gemischt        | ✅ Ja!                                     |
| Validierung + Speichern zusammen  | ✅ Ja!                                     |

**`@Transactional` auf Klasse vs. Methode:**

```java
// Auf der KLASSE → gilt für ALLE Methoden:
@Service
@Transactional
public class ItemService { ... }

// Auf einer METHODE → gilt nur für DIESE Methode:
@Service
public class ItemService {
    @Transactional
    public Item create(Item item) { ... }

    // Diese Methode hat KEINE Transaktion:
    public Item findById(UUID id) { ... }
}
```

**Empfehlung:** Setze `@Transactional` auf die **Klasse**, wenn die meisten Methoden es brauchen. Oder setze es nur auf die Methoden, die schreiben (create, update, delete).

**Wichtig – `@Transactional(readOnly = true)`:**

```java
@Transactional(readOnly = true)
public Page<Item> findAll(...) { ... }
```

- Sagt Hibernate: "Diese Methode liest nur, schreibt nichts"
- Hibernate kann **optimieren** (kein Dirty-Checking, kein Flush nötig)
- Verhindert **versehentliches Schreiben** in Lese-Methoden

---

## 4. Validierungen im Service – Warum und wie?

Der Service ist der **richtige Ort** für Business-Validierungen. Der Controller prüft nur die Syntax (Format, Pflichtfelder – via `@Valid`), aber der Service prüft die **Logik**.

**Typische Validierungen:**

|      Was?       |                   Beispiel                    |        Exception        |
|-----------------|-----------------------------------------------|-------------------------|
| Existenz prüfen | "Gibt es die City mit dieser ID?"             | `NotFoundException`     |
| Duplikat prüfen | "Gibt es schon eine Praxis mit diesem Namen?" | `ConflictException`     |
| Business-Regel  | "Ist der Slot AVAILABLE (nicht BLOCKED)?"     | `BusinessRuleException` |
| Referenz prüfen | "Gehört die workingHoursId zu diesem Doctor?" | `BadRequestException`   |

**Exceptions werfen – wie?**

Du erstellst eigene Exception-Klassen und wirfst sie im Service. Ein `@ControllerAdvice` (ExceptionHandler) fängt sie ab und konvertiert sie zu HTTP-Status-Codes:

```java
// Im Service:
if (!cityOutgoingPort.existsById(practice.getCityId())) {
    throw new NotFoundException("City not found: " + practice.getCityId());
}

// Im ExceptionHandler (eigene Klasse):
// NotFoundException → 404
// ConflictException → 409
// BadRequestException → 400
```

---

## 5. Vollständiges Beispiel – Klassisch vs. Hexagonal

Beide Beispiele zeigen die **gleichen CRUD-Operationen** – der Unterschied liegt darin, **wen der Service kennt**.

### 5.1 Klassische Architektur

```java
@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;          // ← Direkt das Repository!
  private final CategoryRepository categoryRepository;  // ← Für Validierungen

  // ─── findAll (paginiert + gefiltert) ──────────────────────────────────────
  public Page<Item> findAll(Optional<String> name, Optional<UUID> categoryId,
                            int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);
    return itemRepository.findAllFiltered(
        name.orElse(null),
        categoryId.orElse(null),
        pageRequest
    );
  }

  // ─── create ───────────────────────────────────────────────────────────────
  @Transactional
  public Item create(Item item) {
    // Validierung: Existiert die Kategorie?
    if (!categoryRepository.existsById(item.getCategoryId())) {
        throw new NotFoundException("Category not found: " + item.getCategoryId());
    }

    // Validierung: Name schon vergeben?
    if (itemRepository.existsByName(item.getName())) {
        throw new ConflictException("Item with name '" + item.getName() + "' already exists");
    }

    return itemRepository.save(item);
  }

  // ─── update ───────────────────────────────────────────────────────────────
  @Transactional
  public Item update(UUID id, Item item) {
    // Validierung: Existiert das Item?
    Item existing = itemRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Item not found: " + id));

    // Validierung: Name Duplikat (aber eigener Name ist OK)?
    if (itemRepository.existsByNameAndIdNot(item.getName(), id)) {
        throw new ConflictException("Item with name '" + item.getName() + "' already exists");
    }

    // Felder aktualisieren
    existing.setName(item.getName());
    existing.setCategoryId(item.getCategoryId());
    // ... weitere Felder

    return itemRepository.save(existing);
  }

  // ─── delete ───────────────────────────────────────────────────────────────
  @Transactional
  public void delete(UUID id) {
    // Validierung: Existiert das Item?
    if (!itemRepository.existsById(id)) {
        throw new NotFoundException("Item not found: " + id);
    }

    itemRepository.deleteById(id);
  }
}
```

**Datenfluss:**

```
Controller → Service → Repository → DB
```

### 5.2 Hexagonale Architektur (dein Projekt)

**Schritt 1: IncomingPort definieren (was der Service anbietet)**

```java
public interface ItemIncomingPort {
    Page<Item> findAll(Optional<String> name, Optional<UUID> categoryId, int page, int size);
    Item create(Item item);
    Item update(UUID id, Item item);
    void delete(UUID id);
}
```

**Schritt 2: OutgoingPort definieren (was der Service braucht)**

```java
public interface ItemOutgoingPort {
    Page<Item> findAll(Optional<String> name, Optional<UUID> categoryId, int page, int size);
    Optional<Item> findById(UUID id);
    Item save(Item item);
    void removeById(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID excludeId);
}
```

**Schritt 3: Service implementiert IncomingPort, nutzt OutgoingPort**

```java
@Service
@RequiredArgsConstructor
public class ItemService implements ItemIncomingPort {
//                                  ^^^^^^^^^^^^^^^^
//                    Der Service implementiert den IncomingPort!
//                    Der Controller kennt NUR dieses Interface.

  private final ItemOutgoingPort itemOutgoingPort;          // ← Nur das Interface!
  private final CategoryOutgoingPort categoryOutgoingPort;  // ← Für Validierungen

  // ─── findAll (paginiert + gefiltert) ──────────────────────────────────────
  @Override
  public Page<Item> findAll(Optional<String> name, Optional<UUID> categoryId,
                            int page, int size) {
    return itemOutgoingPort.findAll(name, categoryId, page, size);
  }

  // ─── create ───────────────────────────────────────────────────────────────
  @Override
  @Transactional
  public Item create(Item item) {
    // Validierung: Existiert die Kategorie?
    if (!categoryOutgoingPort.existsById(item.getCategoryId())) {
        throw new NotFoundException("Category not found: " + item.getCategoryId());
    }

    // Validierung: Name schon vergeben?
    if (itemOutgoingPort.existsByName(item.getName())) {
        throw new ConflictException("Item with name '" + item.getName() + "' already exists");
    }

    return itemOutgoingPort.save(item);
  }

  // ─── update ───────────────────────────────────────────────────────────────
  @Override
  @Transactional
  public Item update(UUID id, Item item) {
    // Validierung: Existiert das Item?
    Item existing = itemOutgoingPort.findById(id)
        .orElseThrow(() -> new NotFoundException("Item not found: " + id));

    // Validierung: Name Duplikat (aber eigener Name ist OK)?
    if (itemOutgoingPort.existsByNameAndIdNot(item.getName(), id)) {
        throw new ConflictException("Item with name '" + item.getName() + "' already exists");
    }

    // Felder aktualisieren
    existing.setName(item.getName());
    existing.setCategoryId(item.getCategoryId());
    // ... weitere Felder

    return itemOutgoingPort.save(existing);
  }

  // ─── delete ───────────────────────────────────────────────────────────────
  @Override
  @Transactional
  public void delete(UUID id) {
    // Validierung: Existiert das Item?
    if (!itemOutgoingPort.existsById(id)) {
        throw new NotFoundException("Item not found: " + id);
    }

    itemOutgoingPort.removeById(id);
  }
}
```

**Datenfluss:**

```
Controller → IncomingPort → Service → OutgoingPort → Adapter → Repository → DB
```

### 5.3 Unterschiede auf einen Blick

|                     |         Klassisch          |                    Hexagonal                    |
|---------------------|----------------------------|-------------------------------------------------|
| **Klasse**          | `class ItemService`        | `class ItemService implements ItemIncomingPort` |
| **Abhängigkeit**    | `ItemRepository` (konkret) | `ItemOutgoingPort` (Interface)                  |
| **Methoden**        | Eigene Methoden            | `@Override` vom IncomingPort                    |
| **Controller ruft** | `service.create(item)`     | `port.create(item)` (selbes Interface)          |
| **Testbar?**        | Repository mocken          | OutgoingPort mocken (einfacher!)                |
| **Wann nutzen?**    | Kleine/einfache Projekte   | Größere Projekte, Microservices                 |

---

## 6. Einfacher Service (nur Weiterleitung)

Nicht jeder Service braucht komplexe Validierungen. Manche Services leiten nur weiter:

```java
@Service
@RequiredArgsConstructor
public class CityService implements CityIncomingPort {

  private final CityOutgoingPort cityOutgoingPort;

  @Override
  public Page<City> getAllCities(Optional<String> name, Optional<String> postalCode,
                                 int page, int size) {
    // Keine Validierung nötig – einfach weiterleiten
    return cityOutgoingPort.findAll(name, postalCode, page, size);
  }
}
```

**Warum trotzdem einen Service?**
- Konsistenz: Alle Use-Cases gehen über den Service-Layer
- Erweiterbarkeit: Später kann man Validierungen hinzufügen, ohne den Controller zu ändern
- In der hexagonalen Architektur: Der Controller kennt nur den Port – der Service MUSS existieren

---

## 7. Schritt-für-Schritt: Neuen Service erstellen

### Klassisch

1. Erstelle eine Klasse mit `@Service` + `@RequiredArgsConstructor`
2. Injiziere die **Repositories** direkt (als `final`-Felder)
3. Schreibe die Methoden (findAll, create, update, delete)
4. Füge `@Transactional` hinzu bei schreibenden Methoden
5. Füge Validierungen hinzu und wirf Exceptions

### Hexagonal (dein Projekt)

1. **IncomingPort** existiert bereits (Interface mit den Use-Case-Methoden)
2. **OutgoingPort** existiert bereits (Interface für Datenzugriff)
3. Erstelle eine Klasse mit `@Service` + `@RequiredArgsConstructor` + `implements XxxIncomingPort`
4. Injiziere die **OutgoingPorts** (als `final`-Felder) – NIE die Repositories direkt!
5. Implementiere alle `@Override`-Methoden vom IncomingPort
6. Füge `@Transactional` hinzu bei schreibenden Methoden
7. Füge Validierungen hinzu und wirf Exceptions

**Merke:** Der Service kennt in der hexagonalen Architektur **nur Interfaces** (Ports), **nie** konkrete Klassen (Repository, Adapter).

---

## 8. Update-Logik: Felder aktualisieren

Beim Update musst du entscheiden: **Alle Felder überschreiben oder nur die gesendeten?**

### Variante A: Alle Felder überschreiben (PUT-Semantik)

```java
@Override
@Transactional
public Item update(UUID id, Item item) {
    Item existing = outPort.findById(id)
        .orElseThrow(() -> new NotFoundException("Item not found: " + id));

    // ALLE Felder überschreiben:
    existing.setName(item.getName());
    existing.setDescription(item.getDescription());
    existing.setCategoryId(item.getCategoryId());

    return outPort.save(existing);
}
```

### Variante B: Nur gesendete Felder überschreiben (PATCH-Semantik mit PUT)

Wenn dein UpdateRequest optionale Felder hat (z.B. mit `JsonNullable` aus OpenAPI):

```java
@Override
@Transactional
public Item update(UUID id, Item item) {
    Item existing = outPort.findById(id)
        .orElseThrow(() -> new NotFoundException("Item not found: " + id));

    // Nur überschreiben, wenn der Wert gesendet wurde (nicht null):
    if (item.getName() != null) {
        existing.setName(item.getName());
    }
    if (item.getDescription() != null) {
        existing.setDescription(item.getDescription());
    }
    if (item.getCategoryId() != null) {
        existing.setCategoryId(item.getCategoryId());
    }

    return outPort.save(existing);
}
```

---

## 9. Übersicht aller Services in deinem Projekt

|        Service        | Implementiert (IncomingPort) |                         Nutzt (OutgoingPorts)                          |
|-----------------------|------------------------------|------------------------------------------------------------------------|
| `CityService`         | `CityIncomingPort`           | `CityOutgoingPort`                                                     |
| `SpecialityService`   | `SpecialityIncomingPort`     | `SpecialityOutgoingPort`                                               |
| `PracticeService`     | `PracticeIncomingPort`       | `PracticeOutgoingPort`, `CityOutgoingPort`                             |
| `DoctorService`       | `DoctorIncomingPort`         | `DoctorOutgoingPort`, `PracticeOutgoingPort`, `SpecialityOutgoingPort` |
| `WorkingHoursService` | `WorkingHoursIncomingPort`   | `WorkingHoursOutgoingPort`, `DoctorOutgoingPort`                       |
| `SlotService`         | `SlotIncomingPort`           | `SlotOutgoingPort`, `WorkingHoursOutgoingPort`                         |

**Warum nutzen manche Services MEHRERE OutgoingPorts?**
- `PracticeService` braucht `CityOutgoingPort` → um zu prüfen ob die `cityId` existiert
- `DoctorService` braucht `PracticeOutgoingPort` → um zu prüfen ob die `practiceId` existiert
- `DoctorService` braucht `SpecialityOutgoingPort` → um zu prüfen ob die `specialityIds` existieren

---

## 10. Zusammenfassung der Regeln

**Für beide Ansätze:**

```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL 1: @Service auf der Klasse                                 │
│ REGEL 2: @RequiredArgsConstructor (Lombok Constructor Injection) │
│ REGEL 3: @Transactional bei schreibenden Methoden                │
│ REGEL 4: Validierungen im Service (nicht im Controller!)         │
│ REGEL 5: Exceptions werfen bei Fehlern (NotFoundException etc.)  │
│ REGEL 6: Nur Domain-Objekte rein und raus (keine DTOs, Entities!)│
└──────────────────────────────────────────────────────────────────┘
```

**Zusätzlich bei Klassisch:**

```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL K1: Repositories direkt injizieren                         │
│ REGEL K2: Eigene Methoden definieren                             │
└──────────────────────────────────────────────────────────────────┘
```

**Zusätzlich bei Hexagonal (dein Projekt):**

```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL H1: implements XxxIncomingPort                             │
│ REGEL H2: Nur OutgoingPorts injizieren (NIE Repository direkt!) │
│ REGEL H3: @Override für alle Interface-Methoden                  │
│ REGEL H4: Andere OutgoingPorts für Validierungen nutzen          │
│           (z.B. CityOutgoingPort.existsById() in PracticeService)│
└──────────────────────────────────────────────────────────────────┘
```

