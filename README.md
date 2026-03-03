# Patient Appointment Service

Microservices für ein Arzttermin-Buchungssystem.

## Architektur-Übersicht

```
┌─────────────────────┐     ┌──────────────────────┐     ┌──────────────────────┐
│   customer-fe       │     │  doctor-provider     │     │  patient-customer    │
│   (Vue.js Frontend) │────▶│  (Spring Boot)       │     │  (Spring Boot)       │
│   Port: 5173        │     │  Port: 8080          │     │  Port: 8081          │
└─────────────────────┘     └──────────┬───────────┘     └──────────┬───────────┘
                                       │                            │
                            ┌──────────▼───────────┐     ┌──────────▼───────────┐
                            │  PostgreSQL          │     │  PostgreSQL          │
                            │  doctor_provider_db  │     │  patient_customer_db │
                            │  Port: 5433          │     │  Port: 5434          │
                            └──────────────────────┘     └──────────────────────┘
```

| Service | Technologie | Port | Datenbank |
|---------|------------|------|-----------|
| **doctor-provider** | Spring Boot 4, Java 25, Hexagonale Architektur | 8080 | `doctor_provider_db` (PostgreSQL) |
| **patient-customer** | Spring Boot 4, Java 25, WebFlux | 8081 | `patient_customer_db` (PostgreSQL) |
| **customer-fe** | Vue.js 3, TypeScript, Vite, Pinia | 5173 | – |

---

## Voraussetzungen

- **Docker** & **Docker Compose** → [Docker installieren](https://docs.docker.com/get-docker/)
- **Java 25** → nur nötig, wenn du die Services lokal (ohne Docker) starten willst
- **Node.js ≥ 20.19** → nur für das Frontend nötig

---

## ⚙️ Ersteinrichtung (.env)

Sensible Daten (DB-Passwörter, Ports etc.) werden in einer `.env`-Datei gespeichert, die **nicht in Git** committet wird.

```bash
# .env aus der Vorlage erstellen
cp .env.example .env
```

Dann `.env` öffnen und deine Werte eintragen:

```dotenv
# Doctor-Provider Datenbank
DOCTOR_DB_NAME=doctor_provider_db
DOCTOR_DB_USER=doctor_user
DOCTOR_DB_PASSWORD=doctor_pass
DOCTOR_DB_PORT=5433

# Doctor-Provider App
DOCTOR_APP_PORT=8080
```

> **⚠️ Warum Port `5433` und nicht `5432`?**
>
> Wenn du PostgreSQL auch lokal auf deinem Mac installiert hast (z.B. über Homebrew oder Postgres.app),
> belegt es bereits Port `5432`. Der Docker-Container kann dann denselben Port nicht nutzen.
> Deshalb verwenden wir **`5433`** für die Docker-DB → kein Konflikt.
>
> In pgAdmin gibst du dann Port **`5433`** an, um den Docker-Container zu erreichen.

> **Wichtig:** `.env` ist in `.gitignore` eingetragen und wird nicht committed. Die Datei `.env.example` dient als Vorlage für andere Entwickler.

---

## 🚀 Services starten

### Option 1: Nur Datenbanken mit Docker, Apps lokal (empfohlen zum Entwickeln)

Das ist die beste Option für die Entwicklung, weil du IntelliJ nutzen kannst und trotzdem eine saubere DB hast.

**Schritt 1 – Datenbanken starten:**

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service

docker compose up doctor-provider-db -d
```

**Schritt 2 – doctor-provider starten (IntelliJ oder Terminal):**

```bash
cd doctor-provider
./mvnw spring-boot:run
```

> Die App startet auf **http://localhost:8080**
> Flyway erstellt automatisch alle Tabellen beim ersten Start.

**Schritt 3 – Frontend starten:**

```bash
cd customer-fe/patient_appointment_booking_fe
npm install
npm run dev
```

> Das Frontend startet auf **http://localhost:5173**

---

### Option 2: Alles mit Docker (DB + Backend)

Wenn du alles in Docker starten möchtest, ohne Java lokal installiert zu haben:

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service

# Schritt 1: Image neu bauen (nur nötig nach Code-Änderungen)
docker compose build

# Schritt 2: Starten OHNE -d (Logs direkt im Terminal sichtbar!)
docker compose up
```

> **Tipp:** Ohne `-d` siehst du alle Logs live im Terminal. Mit `Ctrl+C` stoppst du alles.
>
> Falls du im Hintergrund starten willst:
> ```bash
> docker compose up -d
> docker compose logs -f          # Logs aller Services
> docker compose logs -f doctor-provider-app  # Nur App-Logs
> ```

> **Hinweis:** Das Frontend muss trotzdem separat mit `npm run dev` gestartet werden.

---

## 🛑 Services stoppen

```bash
# Alles stoppen (Daten bleiben erhalten)
docker compose down

# Alles stoppen + Datenbanken komplett löschen (frischer Start)
docker compose down -v
```

---

## 🔄 Datenbank zurücksetzen

Wenn du die Datenbank komplett neu aufsetzen willst (z.B. nach Flyway-Änderungen):

```bash
docker compose down -v
docker compose up doctor-provider-db -d
```

> Docker erstellt die DB automatisch neu. Beim nächsten App-Start führt Flyway alle Migrationen erneut aus.

---

## 📋 Nützliche Befehle

| Befehl | Beschreibung |
|--------|-------------|
| `docker compose up doctor-provider-db -d` | Nur die DB starten |
| `docker compose build` | Image neu bauen (nach Code-Änderungen) |
| `docker compose up` | Starten + Logs live im Terminal |
| `docker compose up -d` | Starten im Hintergrund |
| `docker compose down` | Alles stoppen |
| `docker compose down -v` | Alles stoppen + Daten löschen |
| `docker compose logs -f` | Logs aller Services |
| `docker compose logs -f doctor-provider-app` | Nur App-Logs |
| `docker compose logs -f doctor-provider-db` | Nur DB-Logs |
| `docker compose ps` | Status aller Container anzeigen |
| `docker exec -it doctor-provider-db psql -U doctor_user -d doctor_provider_db` | Direkt in die DB verbinden |

---

## 🔧 Datenbank-Zugangsdaten

Alle Zugangsdaten werden zentral in der `.env`-Datei verwaltet:

| Variable | Beschreibung | Standardwert |
|----------|-------------|-------------|
| `DOCTOR_DB_NAME` | Datenbank-Name | `doctor_provider_db` |
| `DOCTOR_DB_USER` | Datenbank-User | `doctor_user` |
| `DOCTOR_DB_PASSWORD` | Datenbank-Passwort | `doctor_pass` |
| `DOCTOR_DB_PORT` | Externer DB-Port | `5433` |
| `DOCTOR_APP_PORT` | Externer App-Port | `8080` |

> `docker-compose.yml` und `application.properties` lesen diese Werte automatisch aus der `.env`-Datei.

---

## 🗄️ Datenbank in pgAdmin anzeigen

Wir nutzen die **pgAdmin Desktop-App** (lokal auf dem Mac installiert), um die PostgreSQL-Datenbank zu verwalten.

> **Voraussetzung:** pgAdmin muss installiert sein → [pgAdmin Download](https://www.pgadmin.org/download/pgadmin-4-macos/)

### Schritt 1 – Datenbank starten (frisch!)

Wenn du zum **ersten Mal** startest oder die Meldung `Skipping initialization` siehst, musst du das alte Volume löschen,
damit PostgreSQL den User und die Datenbank korrekt erstellt:

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service

# ⚠️ Altes Volume löschen + DB frisch starten
docker compose down -v
docker compose up doctor-provider-db -d
```

> **Warum `-v`?** PostgreSQL erstellt User und Datenbank nur beim **allerersten Start**.
> Wenn ein altes Volume existiert (z.B. mit dem User `postgres`), überspringt PostgreSQL die Initialisierung
> und der neue User `doctor_user` wird nie erstellt. Mit `-v` wird das Volume gelöscht → frischer Start.

Prüfe in den Logs, dass die Initialisierung diesmal **nicht** übersprungen wird:

```bash
docker compose logs doctor-provider-db
```

✅ **Gut** – du solltest sehen:
```
database system is ready to accept connections
```

❌ **Schlecht** – wenn du das siehst, wurde das Volume nicht gelöscht:
```
PostgreSQL Database directory appears to contain a database; Skipping initialization
```

### Schritt 2 – Spring Boot App starten (für Tabellen!)

> **⚠️ Wichtig:** Die Datenbank allein hat **keine Tabellen!**
> Die Tabellen werden erst durch **Flyway** erstellt, wenn die **Spring Boot App** startet.

```bash
cd doctor-provider
./mvnw spring-boot:run
```

Erst danach siehst du in pgAdmin die Tabellen (`city`, `doctor`, `practice`, etc.).

### Schritt 3 – pgAdmin Desktop-App öffnen

Öffne die **pgAdmin**-App auf deinem Mac (z.B. über Spotlight: `Cmd + Space` → „pgAdmin" eingeben).

### Schritt 4 – Datenbankserver registrieren

Einmalig musst du deinen PostgreSQL-Server hinzufügen:

1. **Rechtsklick** auf „Servers" (links im Baum) → **Register** → **Server…**

2. Im Tab **General**:

   | Feld | Wert |
   |------|------|
   | **Name** | `doctor-provider` (frei wählbar) |

3. Im Tab **Connection**:

   | Feld | Wert | Erklärung |
   |------|------|-----------|
   | **Host name/address** | `localhost` | Weil pgAdmin lokal auf deinem Mac läuft, erreichst du die DB über `localhost`. Docker leitet den Port an den Container weiter. |
   | **Port** | `5433` | = `DOCTOR_DB_PORT` aus deiner `.env`-Datei. ⚠️ **Nicht `5432`!** Port 5432 ist dein lokaler PostgreSQL. |
   | **Maintenance database** | `doctor_provider_db` | = `DOCTOR_DB_NAME` |
   | **Username** | `doctor_user` | = `DOCTOR_DB_USER` |
   | **Password** | `doctor_pass` | = `DOCTOR_DB_PASSWORD` |

   > ✅ Setze den Haken bei **Save password**, damit du dich nicht jedes Mal neu einloggen musst.

4. Klicke auf **Save**.

### Schritt 5 – Datenbank durchstöbern

Nach dem Verbinden siehst du im Baum links:

```
Servers
└── doctor-provider
    └── Databases
        └── doctor_provider_db
            └── Schemas
                └── public
                    └── Tables
                        ├── city
                        ├── doctor
                        ├── practice
                        ├── speciality
                        ├── working_hours
                        ├── slot
                        └── ...
```

- **Rechtsklick auf eine Tabelle** → **View/Edit Data** → **All Rows** → zeigt dir alle Daten
- **Tools** → **Query Tool** → hier kannst du SQL-Queries direkt ausführen, z.B.:
  ```sql
  SELECT * FROM doctor;
  ```

### ❓ Warum `localhost` und nicht den Container-Namen?

| Situation | Host | Port |
|-----------|------|------|
| **pgAdmin läuft lokal** (Desktop-App – unser Setup) | `localhost` | `DOCTOR_DB_PORT` aus `.env` (z.B. `5433`) |
| **pgAdmin läuft in Docker** (Web-Version) | Container-Name (z.B. `doctor-provider-db`) | `5432` (interner Port) |

> Weil die pgAdmin Desktop-App **außerhalb** von Docker läuft, erreichst du die DB über `localhost` + den Port, den Docker nach außen weiterleitet (`DOCTOR_DB_PORT`). Der Container-Name funktioniert nur innerhalb des Docker-Netzwerks.

---

## 📂 Projektstruktur

```
patient_appointment_service/
├── .env                            ← Sensible Daten (NICHT in Git!)
├── .env.example                    ← Vorlage für .env (in Git)
├── .gitignore                      ← Schließt .env von Git aus
├── docker-compose.yml              ← Docker-Konfiguration für alle Services
├── README.md                       ← Diese Datei
│
├── doctor-provider/                ← Backend: Arzt/Praxis-Verwaltung
│   ├── Dockerfile                  ← Docker-Build für doctor-provider
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/               ← Hexagonale Architektur
│           └── resources/
│               ├── application.properties
│               ├── application-docker.properties
│               ├── db/migration/   ← Flyway SQL-Migrationen
│               └── openapi/        ← API-Spezifikation
│
├── patient-customer/               ← Backend: Patienten-Verwaltung
│   ├── pom.xml
│   └── src/
│
└── customer-fe/                    ← Frontend: Vue.js App
    └── patient_appointment_booking_fe/
        ├── package.json
        └── src/
```
