# 🌐 Controller Guide – Incoming/Web (Spec-First + Hexagonal + Spring MVC)

---

## 1. Was ist ein Controller?

Ein Controller ist die **Eingangstür** deiner Anwendung:

```
Client (Browser/Postman)
    │  HTTP Request (z.B. POST /api/v1/doctor)
    ▼
┌──────────────────────────────┐
│       CONTROLLER             │
│  1. Request → Domain         │  (WebMapper.toDomain)
│  2. Port aufrufen            │  (Business-Logik)
│  3. Domain → DTO             │  (WebMapper.toDto)
│  4. ResponseEntity erstellen │  (HTTP Status + Body)
└──────────────────────────────┘
    │  HTTP Response (z.B. 201 Created + JSON)
    ▼
Client
```

---

## 2. Klassisch vs. Hexagonal (kurz)

| Aspekt | Klassisch | Hexagonal (dein Projekt) |
|---|---|---|
| Abhängigkeit | `private final PracticeService service` | `private final PracticeIncomingPort port` |
| Controller kennt | konkreten Service | nur das Interface (Port) |
| Annotations | `@GetMapping`, `@PostMapping`, etc. | Kommen vom generierten Interface |
| Austauschbar? | Schwer | Leicht (Interface tauschen) |

```
Klassisch:   Controller → Service → Repository → DB
Hexagonal:   Controller → IncomingPort (Interface) → Service → OutgoingPort → Adapter → Repository → DB
```

---

## 3. Dein Projekt-Setup

```
1. Spec-First:   OpenAPI YAML → OpenAPI Generator → API-Interfaces (CitiesApi, DoctorsApi, ...)
2. Hexagonal:    Controller → IncomingPort → Service → OutgoingPort → Adapter → Repository
3. Spring MVC:   ResponseEntity<T> – synchron, blockierend, einfach
```

**Das bedeutet:**
- Du schreibst **KEINE** `@GetMapping`, `@PostMapping`, etc. – die kommen vom generierten Interface!
- Du implementierst (`implements`) das generierte Interface und überschreibst (`@Override`) die Methoden.
- Du arbeitest mit `ResponseEntity<T>` direkt (kein Mono, kein Flux).

---

## 4. Wichtige Annotationen (Kurzreferenz)

### `@RestController`
Kombiniert `@Controller` + `@ResponseBody` → Spring erkennt die Klasse als Web-Controller, Rückgabewerte werden automatisch als JSON serialisiert.

### `@RequestMapping` / `@GetMapping` / `@PostMapping` / ...
Verknüpft eine Methode mit einem HTTP-Endpoint. **⚠️ In deinem Projekt nicht nötig** – kommt vom generierten Interface!

### `@PathVariable`
Extrahiert einen Wert aus dem URL-Pfad: `/api/v1/practice/{id}` → `@PathVariable("id") UUID id`

### `@RequestParam`
Extrahiert Query-Parameter: `/api/v1/practices?cityId=abc&page=0` → `@RequestParam("cityId") UUID cityId`
- `required = false` → Parameter ist optional
- `defaultValue = "0"` → Standardwert wenn Parameter fehlt

### `@RequestBody`
Liest den HTTP-Body (JSON) und konvertiert ihn automatisch in ein Java-Objekt (via Jackson).

### `@Valid`
Aktiviert Bean-Validation (`@NotNull`, `@Size`, etc.) → bei Fehler automatisch HTTP 400.

### `ResponseEntity<T>`
Volle Kontrolle über die HTTP-Response (Status Code + Body):
```java
ResponseEntity.ok(dto)                              // 200 OK + Body
ResponseEntity.status(HttpStatus.CREATED).body(dto)  // 201 Created + Body
ResponseEntity.noContent().build()                   // 204 No Content
ResponseEntity.notFound().build()                    // 404 Not Found
```

---

## 5. Paginierung – Was ist `setPage`, `setSize`, `setTotalElements`, `setTotalPages`?

Wenn du eine Liste abrufst, willst du **nicht alle 10.000 auf einmal** zurückgeben. Stattdessen teilst du die Ergebnisse in **Seiten (Pages)** auf.

```
📖 150 Ärzte aufgeteilt in Seiten (size=20):
   ├── Seite 0: Arzt 1-20
   ├── Seite 1: Arzt 21-40
   ├── ...
   └── Seite 7: Arzt 141-150   → totalPages = 8
```

| Feld | Typ | Beschreibung | Beispiel |
|---|---|---|---|
| `items` | `List<XxxDto>` | Elemente **auf dieser Seite** | 20 Ärzte |
| `page` | `int` | Welche Seite? (0-basiert) | `0` = erste Seite |
| `size` | `int` | Elemente pro Seite | `20` |
| `totalElements` | `long` | Elemente insgesamt (alle Seiten) | `150` |
| `totalPages` | `int` | Seiten insgesamt | `8` (= 150 ÷ 20) |

**Warum?** Der Client braucht diese Infos für Paginierungs-Buttons:

```
┌──────────────────────────────────────────────────────┐
│  Dr. Müller    │ Kardiologie  │ Berlin               │
│  Dr. Schmidt   │ Neurologie   │ München              │
│  ... (20 Ärzte auf dieser Seite)                     │
├──────────────────────────────────────────────────────┤
│  ◀ Zurück   Seite 1 von 8   Weiter ▶               │  ← page, totalPages
│  150 Ergebnisse, 20 pro Seite                        │  ← totalElements, size
└──────────────────────────────────────────────────────┘
```

**Woher kommen die Werte?**
```
DB → Repository (Spring Data Page) → PersistenceAdapter (Domain Page) → Controller (Response-Objekt)
```

Im Controller kopierst du die Werte aus dem Domain-Page in das generierte Response-Objekt:
```java
FindAllXxx200Response response = new FindAllXxx200Response();
response.setItems(mapper.toDto(result.getItems()));       // Domain-Liste → DTO-Liste
response.setPage(result.getPage());                        // z.B. 0
response.setSize(result.getSize());                        // z.B. 20
response.setTotalElements(result.getTotalElements());      // z.B. 150
response.setTotalPages(result.getTotalPages());            // z.B. 8
```

---

## 6. Vollständiges Beispiel – Klassisch vs. Hexagonal

Beide Beispiele zeigen die **gleichen CRUD-Operationen** – der Unterschied liegt nur darin, **wen der Controller kennt** und **woher die Endpoint-Annotations kommen**.

### 6.1 Klassische Architektur

```java
@RestController
@RequestMapping("/api/v1/items")    // ← DU definierst die Basis-URL
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;  // ← Direkt den konkreten Service!
  private final ItemMapper mapper;

  // ─── GET (paginiert + gefiltert) ──────────────────────────────────────────
  @GetMapping                        // ← DU schreibst die HTTP-Methode
  public ResponseEntity<ItemPageResponse> findAll(
    @RequestParam(required = false) String name,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
  ) {
    Page<Item> result = itemService.findAll(Optional.ofNullable(name), page, size);

    ItemPageResponse response = new ItemPageResponse();
    response.setItems(mapper.toDto(result.getItems()));
    response.setPage(result.getPage());
    response.setSize(result.getSize());
    response.setTotalElements(result.getTotalElements());
    response.setTotalPages(result.getTotalPages());

    return ResponseEntity.ok(response);
  }

  // ─── POST (erstellen) ────────────────────────────────────────────────────
  @PostMapping
  public ResponseEntity<ItemDto> create(@Valid @RequestBody CreateItemRequest request) {
    Item item = mapper.toDomain(request);
    Item saved = itemService.create(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
  }

  // ─── PUT (aktualisieren) ─────────────────────────────────────────────────
  @PutMapping("/{id}")
  public ResponseEntity<ItemDto> update(
    @PathVariable UUID id, @Valid @RequestBody UpdateItemRequest request
  ) {
    Item item = mapper.toDomain(id, request);
    Item updated = itemService.update(id, item);
    return ResponseEntity.ok(mapper.toDto(updated));
  }

  // ─── DELETE (löschen) ────────────────────────────────────────────────────
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    itemService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

**Datenfluss:**
```
Client → Controller → Service → Repository → DB
                                    ↓
Client ← Controller ← Service ← Repository ← DB
```

### 6.2 Hexagonale Architektur + Spec-First (dein Projekt)

```java
@RestController                     // ← Nur das! Kein @RequestMapping nötig!
@RequiredArgsConstructor
public class ItemController implements ItemsApi {
//                                     ^^^^^^^^
//                    Generiertes Interface → enthält alle @RequestMapping etc.
//                    Du musst NUR @Override die Methoden!

  private final ItemIncomingPort port;  // ← Nur das Interface (Port), NICHT den Service!
  private final ItemWebMapper mapper;

  // ─── GET (paginiert + gefiltert) ──────────────────────────────────────────
  // Kein @GetMapping nötig – kommt vom generierten Interface!
  @Override
  public ResponseEntity<FindAllItems200Response> findAllItems(
    String name, Integer page, Integer size
  ) {
    Page<Item> result = port.findAll(Optional.ofNullable(name), page, size);

    FindAllItems200Response response = new FindAllItems200Response();
    response.setItems(mapper.toDto(result.getItems()));
    response.setPage(result.getPage());
    response.setSize(result.getSize());
    response.setTotalElements(result.getTotalElements());
    response.setTotalPages(result.getTotalPages());

    return ResponseEntity.ok(response);
  }

  // ─── POST (erstellen) ────────────────────────────────────────────────────
  @Override
  public ResponseEntity<ItemDto> registerItem(CreateItemRequest request) {
    Item item = mapper.toDomain(request);
    Item saved = port.create(item);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
  }

  // ─── PUT (aktualisieren) ─────────────────────────────────────────────────
  @Override
  public ResponseEntity<ItemDto> modifyItem(UUID id, UpdateItemRequest request) {
    Item item = mapper.toDomain(id, request);
    Item updated = port.update(id, item);
    return ResponseEntity.ok(mapper.toDto(updated));
  }

  // ─── DELETE (löschen) ────────────────────────────────────────────────────
  @Override
  public ResponseEntity<Void> removeItem(UUID id) {
    port.delete(id);
    return ResponseEntity.noContent().build();
  }
}
```

**Datenfluss:**
```
Client → Controller → Port (Interface) → Service → OutPort → Adapter → Repository → DB
                                                                            ↓
Client ← Controller ← Port (Interface) ← Service ← OutPort ← Adapter ← Repository ← DB
```

### 6.3 Unterschiede auf einen Blick

| | Klassisch | Hexagonal + Spec-First |
|---|---|---|
| **Klasse** | `class ItemController` | `class ItemController implements ItemsApi` |
| **Basis-URL** | `@RequestMapping("/api/v1/items")` | Kommt vom generierten Interface |
| **Methoden-Annotations** | `@GetMapping`, `@PostMapping`, etc. | Nicht nötig (`@Override` reicht) |
| **Abhängigkeit** | `ItemService service` (konkrete Klasse) | `ItemIncomingPort port` (Interface) |
| **Response-Objekte** | Eigene DTOs | Generiert aus OpenAPI YAML |
| **Wann nutzen?** | Kleine/einfache Projekte | Größere Projekte, Microservices |

---

## 7. Schritt-für-Schritt: Neuen Controller erstellen

### Klassisch

1. Erstelle eine Klasse mit `@RestController` + `@RequestMapping("/api/v1/xxx")`
2. Injiziere den **Service** direkt (`private final XxxService service`)
3. Schreibe jede Methode mit `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
4. Definiere Parameter mit `@PathVariable`, `@RequestParam`, `@RequestBody`

### Hexagonal + Spec-First (dein Projekt)

1. Finde das generierte Interface in `target/generated-sources/openapi/.../api/`
2. Erstelle eine Klasse mit `@RestController` + `implements XxxApi`
3. Injiziere den **Port** (`private final XxxIncomingPort port`) – nie den Service direkt!
4. Lass die IDE die `@Override`-Methoden generieren
5. Implementiere die Logik (Annotations kommen vom Interface – du brauchst keine!)

**Welcher HTTP-Status für welche Operation?**

| Operation | HTTP-Methode | Status | ResponseEntity |
|---|---|---|---|
| Liste abrufen | GET | 200 OK | `ResponseEntity.ok(response)` |
| Erstellen | POST | 201 Created | `ResponseEntity.status(HttpStatus.CREATED).body(dto)` |
| Aktualisieren | PUT | 200 OK | `ResponseEntity.ok(dto)` |
| Löschen | DELETE | 204 No Content | `ResponseEntity.noContent().build()` |
| Status ändern | PUT (ohne Body) | 200 OK | `ResponseEntity.ok(dto)` |

---

## 8. Optional.ofNullable() – Warum?

Deine Ports erwarten `Optional<UUID>`, aber die generierten Methoden geben `UUID` (nullable):

```java
// Generiertes Interface:          Dein Port:
UUID cityId          ───────→      Optional<UUID> cityId

// Lösung: Optional.ofNullable() als Brücke
port.getAllPractices(
    Optional.ofNullable(cityId),       // null → Optional.empty()
    Optional.ofNullable(practiceName), // "xyz" → Optional.of("xyz")
    page, size
);
```

---

## 9. Übersicht aller Controller in deinem Projekt

| Controller | Interface | Port | Mapper |
|---|---|---|---|
| `CityController` | `CitiesApi` | `CityIncomingPort` | `CityWebMapper` |
| `SpecialityController` | `SpecialitiesApi` | `SpecialityIncomingPort` | `SpecialityWebMapper` |
| `PracticeController` | `PracticesApi` | `PracticeIncomingPort` | `PracticeWebMapper` |
| `DoctorController` | `DoctorsApi` | `DoctorIncomingPort` | `DoctorWebMapper` |
| `WorkingHoursController` | `WorkingHoursApi` | `WorkingHoursIncomingPort` | `WorkingHoursWebMapper` |
| `SlotController` | `SlotsApi` | `SlotIncomingPort` | `SlotWebMapper` |

---

## 10. Hintergrundwissen: Spring MVC vs. Spring WebFlux

> **Dein Projekt nutzt Spring MVC.** Dieser Abschnitt ist nur Wissen – damit du den Unterschied verstehst, falls du es mal in einem anderen Projekt siehst.

| | Spring MVC (dein Projekt) | Spring WebFlux |
|---|---|---|
| **Dependency** | `spring-boot-starter-web` | `spring-boot-starter-webflux` |
| **Server** | Tomcat (Servlet-basiert) | Netty (Non-Blocking) |
| **Programmierung** | Synchron (blockierend) | Asynchron (nicht-blockierend) |
| **Return-Typen** | `ResponseEntity<T>` | `Mono<ResponseEntity<T>>` |
| **Listen** | `List<T>` | `Flux<T>` |
| **DB-Zugriff** | JPA/Hibernate ✅ | R2DBC nötig ⚠️ |
| **Einfachheit** | ✅ Einfach | ❌ Komplex |

**Restaurant-Analogie:**
- **MVC** = Ein Kellner pro Tisch: Kellner wartet bis Essen fertig → bringt es. Bei 1000 Tischen brauchst du 1000 Kellner (Threads).
- **WebFlux** = Ein Kellner für ALLE Tische: Nimmt Bestellungen auf → geht sofort weiter → wird benachrichtigt wenn Essen fertig. 1 Kellner kann 1000 Tische bedienen.

**Warum MVC für dein Projekt richtig ist:**
- `spring-boot-starter-web` + `spring-boot-starter-data-jpa` = perfektes Paar (beides synchron)
- Kein Mono/Flux nötig, kein `fromCallable()`, kein `ServerWebExchange`
- Einfacher zu debuggen, mehr Tutorials, weniger Fehlerquellen

**Wie würde der gleiche Code in WebFlux aussehen?**

```java
// MVC (dein Projekt):
public ResponseEntity<PracticeDto> registerPractice(CreatePracticeRequest request) { ... }

// WebFlux:
public Mono<ResponseEntity<PracticeDto>> registerPractice(
    Mono<CreatePracticeRequest> request, ServerWebExchange exchange) {
    return request.map(req -> { ... });
}
```

In WebFlux müsste man zusätzlich blockierenden JPA-Code in `Mono.fromCallable()` verpacken – unnötig komplex für dein Setup.

---

## 11. Zusammenfassung der Regeln

**Für beide Ansätze:**
```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL 1: @RestController auf der Klasse                          │
│ REGEL 2: WebMapper für DTO ↔ Domain verwenden                   │
│ REGEL 3: ResponseEntity direkt zurückgeben (kein Mono/Flux!)     │
│ REGEL 4: Richtiger HTTP-Status: 200 OK, 201 Created, 204 None   │
└──────────────────────────────────────────────────────────────────┘
```

**Zusätzlich bei Klassisch:**
```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL K1: @RequestMapping für Basis-URL                          │
│ REGEL K2: @GetMapping/@PostMapping/... für jede Methode          │
│ REGEL K3: @PathVariable, @RequestParam, @RequestBody selbst      │
│           an die Parameter schreiben                             │
└──────────────────────────────────────────────────────────────────┘
```

**Zusätzlich bei Hexagonal + Spec-First (dein Projekt):**
```
┌──────────────────────────────────────────────────────────────────┐
│ REGEL H1: implements XxxApi (generiertes Interface)              │
│ REGEL H2: Nur IncomingPort injizieren (nie den Service direkt!)  │
│ REGEL H3: @Override für alle Interface-Methoden                  │
│ REGEL H4: Keine @GetMapping etc. nötig – kommt vom Interface!    │
│ REGEL H5: Optional.ofNullable() für optionale Query-Parameter    │
└──────────────────────────────────────────────────────────────────┘
```
