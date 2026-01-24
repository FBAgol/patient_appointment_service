# 🗄️ PostgreSQL Complete Guide - Für Doctor Provider Service

**Vollständiger Guide für PostgreSQL-Setup, Verwaltung & Integration mit Spring Boot**

---

## 📑 Inhaltsverzeichnis

1. [PostgreSQL Server Management](#1-postgresql-server-management)
2. [Datenbank erstellen, auflisten, löschen](#2-datenbank-erstellen-auflisten-löschen)
3. [Auf Datenbank zugreifen](#3-auf-datenbank-zugreifen)
4. [Benutzerverwaltung (Username & Passwort)](#4-benutzerverwaltung-username--passwort)
5. [Port Management](#5-port-management)
6. [pgAdmin Management Tool](#6-pgadmin-management-tool)
7. [PostgreSQL in Spring Boot nutzen](#7-postgresql-in-spring-boot-nutzen)
8. [Flyway SQL-Migrationen](#8-flyway-sql-migrationen)
9. [IntelliJ Data Source Konfiguration](#9-intellij-data-source-konfiguration)
10. [Problemlösungen & Troubleshooting](#10-problemlösungen--troubleshooting)

---

## 1. PostgreSQL Server Management

### 🚀 Server starten

#### Via Terminal (Homebrew - empfohlen):
```bash
# Für PostgreSQL 14
brew services start postgresql@14

# Für neuere Versionen
brew services start postgresql@15
brew services start postgresql@16

# Automatischer Start bei Systemstart
brew services start postgresql@14
```

#### Manuell starten:
```bash
# Intel Mac
pg_ctl -D /usr/local/var/postgres start

# Apple Silicon (M1/M2/M3)
pg_ctl -D /opt/homebrew/var/postgres start
```

#### Via pgAdmin:
1. pgAdmin öffnen
2. Server in der Liste auswählen
3. Rechtsklick → **Connect Server**
4. Passwort eingeben (falls erforderlich)

---

### 🛑 Server stoppen

#### Via Terminal:
```bash
# Homebrew
brew services stop postgresql@14

# Manuell (Intel)
pg_ctl -D /usr/local/var/postgres stop

# Manuell (Apple Silicon)
pg_ctl -D /opt/homebrew/var/postgres stop
```

---

### 🔄 Server neustarten

```bash
# Homebrew
brew services restart postgresql@14

# Manuell (Intel)
pg_ctl -D /usr/local/var/postgres restart

# Manuell (Apple Silicon)
pg_ctl -D /opt/homebrew/var/postgres restart
```

---

### ✅ Server Status prüfen

```bash
# Alle Homebrew Services anzeigen
brew services list

# Manueller Status-Check
pg_ctl -D /usr/local/var/postgres status       # Intel
pg_ctl -D /opt/homebrew/var/postgres status    # Apple Silicon

# Laufende PostgreSQL-Prozesse anzeigen
ps aux | grep postgres

# Prüfen ob PostgreSQL auf Port läuft
lsof -i :5432
```

**Erwartete Ausgabe:**
```
COMMAND   PID      USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
postgres  1234  username    5u  IPv6  0xabc    0t0  TCP localhost:postgresql (LISTEN)
```

---

## 2. Datenbank erstellen, auflisten, löschen

### 📊 Datenbanken auflisten

#### Via Terminal:

**Option 1 - Schnell:**
```bash
psql -l
# oder
psql --list
```

**Option 2 - In psql Shell:**
```bash
# PostgreSQL Shell öffnen
psql postgres

# In psql:
\l
# oder
\list
# oder
\l+    # mit mehr Details
```

**Option 3 - SQL-Befehl:**
```bash
psql -c "SELECT datname FROM pg_database;"
```

**Erwartete Ausgabe:**
```
                                   List of databases
       Name        |  Owner   | Encoding | Collate | Ctype |   Access privileges   
-------------------+----------+----------+---------+-------+-----------------------
 doctor_provider_db| username | UTF8     | de_DE   | de_DE | 
 patient_customer_db| username| UTF8     | de_DE   | de_DE |
 postgres          | username | UTF8     | de_DE   | de_DE |
 template0         | username | UTF8     | de_DE   | de_DE |
 template1         | username | UTF8     | de_DE   | de_DE |
```

#### Via pgAdmin:
1. pgAdmin öffnen
2. **Servers** → **PostgreSQL** → **Databases**
3. Alle Datenbanken werden in der linken Sidebar angezeigt

---

### ➕ Datenbank erstellen

#### Via Terminal:

**Option 1 - Direkt:**
```bash
createdb doctor_provider_db
createdb patient_customer_db
```

**Option 2 - Mit SQL-Befehl:**
```bash
psql postgres -c "CREATE DATABASE doctor_provider_db;"
psql postgres -c "CREATE DATABASE patient_customer_db;"
```

**Option 3 - Mit spezifischem Owner:**
```bash
createdb -O dein_benutzer doctor_provider_db
```

**Option 4 - In psql Shell:**
```bash
psql postgres

# In psql:
CREATE DATABASE doctor_provider_db;
CREATE DATABASE patient_customer_db;

# Mit Owner und Encoding:
CREATE DATABASE doctor_provider_db
    OWNER = dein_benutzer
    ENCODING = 'UTF8'
    LC_COLLATE = 'de_DE.UTF-8'
    LC_CTYPE = 'de_DE.UTF-8'
    TEMPLATE = template0;
```

#### Via pgAdmin:
1. **Servers** → **PostgreSQL** → **Databases** → **Rechtsklick**
2. **Create** → **Database**
3. **General Tab:**
   - **Database:** `doctor_provider_db`
   - **Owner:** dein Benutzer auswählen
   - **Comment:** (optional) "Doctor Provider Service Database"
4. **Definition Tab:**
   - **Encoding:** UTF8
   - **Template:** template0 oder template1
5. **Save** klicken

---

### 🗑️ Datenbank löschen

#### Via Terminal:

**Option 1 - Direkt:**
```bash
dropdb doctor_provider_db
```

**Option 2 - Mit SQL-Befehl:**
```bash
psql postgres -c "DROP DATABASE doctor_provider_db;"
```

**Option 3 - In psql Shell:**
```bash
psql postgres

# In psql:
DROP DATABASE doctor_provider_db;

# Mit IF EXISTS (verhindert Fehler wenn DB nicht existiert):
DROP DATABASE IF EXISTS doctor_provider_db;
```

⚠️ **Warnung:** Alle Daten werden unwiderruflich gelöscht!

#### Via pgAdmin:
1. **Servers** → **PostgreSQL** → **Databases**
2. **Rechtsklick auf Datenbank** → **Delete/Drop**
3. Bestätige mit **Yes**

---

## 3. Auf Datenbank zugreifen

### 🔌 Mit Datenbank verbinden

#### Via Terminal:

**Option 1 - Einfach:**
```bash
psql doctor_provider_db
```

**Option 2 - Mit -d Flag:**
```bash
psql -d doctor_provider_db
```

**Option 3 - Mit Benutzer:**
```bash
psql -U dein_benutzer -d doctor_provider_db
```

**Option 4 - Mit Host und Port:**
```bash
psql -h localhost -p 5432 -U dein_benutzer -d doctor_provider_db
```

**Option 5 - Mit Passwort-Prompt:**
```bash
psql -U dein_benutzer -d doctor_provider_db -W
# -W fordert Passworteingabe
```

#### In psql wechseln:
```bash
# Erst verbinden mit einer DB
psql postgres

# Dann wechseln:
\c doctor_provider_db
# oder
\connect doctor_provider_db
```

---

### 📋 Datenbankinhalt anzeigen

#### Alle Tabellen anzeigen:
```bash
# Nach Verbindung mit DB:
psql doctor_provider_db

# In psql:
\dt              # Alle Tabellen
\dt+             # Mit Details (Größe, etc.)
\d               # Alle Relationen (Tabellen, Views, Sequenzen)
```

**Erwartete Ausgabe:**
```
                  List of relations
 Schema |          Name           | Type  |  Owner   
--------+-------------------------+-------+----------
 public | city                    | table | username
 public | doctor                  | table | username
 public | doctor_speciality       | table | username
 public | doctor_working_hours    | table | username
 public | flyway_schema_history   | table | username
 public | practice                | table | username
 public | slot                    | table | username
 public | speciality              | table | username
```

#### Schema einer Tabelle anzeigen:
```sql
-- In psql:
\d speciality
\d+ speciality     -- mit mehr Details
```

**Ausgabe-Beispiel:**
```
                         Table "public.speciality"
 Column |     Type      | Collation | Nullable |      Default       
--------+---------------+-----------+----------+--------------------
 id     | uuid          |           | not null | gen_random_uuid()
 name   | speciality_type|          | not null | 
Indexes:
    "speciality_pkey" PRIMARY KEY, btree (id)
    "speciality_name_key" UNIQUE CONSTRAINT, btree (name)
```

#### Daten aus Tabelle anzeigen:
```sql
-- Alle Daten
SELECT * FROM speciality;

-- Begrenzte Anzahl
SELECT * FROM speciality LIMIT 10;

-- Mit Bedingung
SELECT * FROM doctor WHERE last_name = 'Schmidt';

-- Anzahl der Einträge
SELECT COUNT(*) FROM speciality;
```

---

## 4. Benutzerverwaltung (Username & Passwort)

### 👤 Username anzeigen

#### Option 1 - macOS Benutzername:
```bash
whoami
# Oft ist dein macOS-User auch dein PostgreSQL-User
```

#### Option 2 - Aktueller PostgreSQL-Benutzer:
```bash
psql postgres

# In psql:
SELECT current_user;
# oder
\conninfo
```

**Ausgabe:**
```
You are connected to database "postgres" as user "A200151230" via socket in "/tmp" at port "5432".
```

#### Option 3 - Alle Benutzer anzeigen:
```bash
# Via Terminal
psql postgres -c "\du"

# In psql
\du
\du+    # mit mehr Details
```

**Ausgabe:**
```
                                   List of roles
 Role name |                         Attributes                         
-----------+------------------------------------------------------------
 postgres  | Superuser, Create role, Create DB, Replication, Bypass RLS
 username  | Superuser, Create role, Create DB
```

#### Option 4 - Mit SQL:
```sql
SELECT usename, usesuper, usecreatedb, usecreaterole 
FROM pg_user;
```

---

### 🔐 Passwort anzeigen

⚠️ **Wichtig:** PostgreSQL speichert Passwörter **verschlüsselt (gehashed)**. Das Original-Passwort kann **nicht** angezeigt werden!

#### Passwort-Hashes anzeigen (nur als Superuser):
```bash
psql postgres

# In psql:
SELECT usename, passwd FROM pg_shadow;

# Für einen spezifischen Benutzer:
SELECT usename, passwd FROM pg_shadow WHERE usename = 'dein_benutzer';
```

**Ausgabe:**
```
  usename  |                             passwd                              
-----------+----------------------------------------------------------------
 postgres  | SCRAM-SHA-256$4096:abc123...
 username  | SCRAM-SHA-256$4096:def456...
```

#### Passwort testen (Verbindungstest):
```bash
# Versuche dich zu verbinden - wenn erfolgreich, ist das Passwort korrekt
psql -U dein_benutzer -d postgres -W
# -W fordert Passworteingabe
```

---

### 🔑 Passwort ändern

#### Option 1 - Via psql (empfohlen):
```bash
psql postgres

# Eigenes Passwort ändern:
\password

# Passwort eines anderen Benutzers ändern (als Superuser):
\password dein_benutzer
```

#### Option 2 - Mit SQL-Befehl:
```sql
-- In psql:
ALTER USER dein_benutzer WITH PASSWORD 'neues_passwort';

-- Beispiele:
ALTER USER A200151230 WITH PASSWORD 'mein_neues_passwort';
ALTER USER postgres WITH PASSWORD 'neues_postgres_passwort';
```

#### Option 3 - Via Terminal (direkt):
```bash
psql postgres -c "ALTER USER dein_benutzer WITH PASSWORD 'neues_passwort';"
```

---

### ➕ Neuen Benutzer erstellen

#### Via Terminal:
```bash
# Mit Passwort-Prompt
createuser -P neuer_benutzer
# -P fragt nach Passwort

# Mit Superuser-Rechten
createuser -s -P neuer_benutzer
```

#### Via SQL:
```sql
-- In psql:

-- Einfacher Benutzer
CREATE USER neuer_benutzer WITH PASSWORD 'passwort';

-- Mit Superuser-Rechten
CREATE USER neuer_benutzer WITH SUPERUSER PASSWORD 'passwort';

-- Mit spezifischen Rechten
CREATE USER neuer_benutzer WITH 
    PASSWORD 'passwort'
    CREATEDB
    CREATEROLE
    LOGIN;

-- Beispiel für Projektbenutzer:
CREATE USER doctor_app WITH 
    PASSWORD 'sicheres_passwort'
    CREATEDB
    LOGIN;
```

---

### 🗑️ Benutzer löschen

```sql
-- In psql:
DROP USER benutzer_name;

-- Falls der Benutzer Objekte besitzt:
REASSIGN OWNED BY benutzer_name TO postgres;
DROP OWNED BY benutzer_name;
DROP USER benutzer_name;
```

---

### 🔐 Benutzerrechte verwalten

#### Rechte anzeigen:
```bash
# In psql:
\du benutzer_name

# Mit SQL:
SELECT 
    usename AS username,
    usesuper AS is_superuser,
    usecreatedb AS can_create_db,
    usecreaterole AS can_create_role,
    usebypassrls AS bypass_row_security
FROM pg_user
WHERE usename = 'dein_benutzer';
```

#### Rechte auf Datenbank vergeben:
```sql
-- Alle Rechte auf eine Datenbank
GRANT ALL PRIVILEGES ON DATABASE doctor_provider_db TO doctor_app;

-- Lesezugriff
GRANT CONNECT ON DATABASE doctor_provider_db TO readonly_user;

-- Datenbank-Owner ändern
ALTER DATABASE doctor_provider_db OWNER TO neuer_benutzer;

-- Alle Datenbanken mit Besitzern anzeigen
SELECT datname, pg_catalog.pg_get_userbyid(datdba) AS owner
FROM pg_database;
```

---

### 🎯 Typische Konfiguration für dein Projekt:

```bash
# 1. Verbinde dich mit postgres
psql postgres

# 2. Erstelle Projektbenutzer (falls noch nicht vorhanden)
CREATE USER doctor_app WITH PASSWORD 'sicheres_passwort';

# 3. Gib Rechte für die Datenbanken
GRANT ALL PRIVILEGES ON DATABASE doctor_provider_db TO doctor_app;
GRANT ALL PRIVILEGES ON DATABASE patient_customer_db TO doctor_app;

# 4. Ändere Owner (optional)
ALTER DATABASE doctor_provider_db OWNER TO doctor_app;
ALTER DATABASE patient_customer_db OWNER TO doctor_app;

# 5. Teste die Verbindung
\q
psql -U doctor_app -d doctor_provider_db -W
```

---

## 5. Port Management

### 🔍 Aktuellen Port anzeigen

#### Option 1 - Via psql:
```bash
psql postgres -c "SHOW port;"
```

**Ausgabe:**
```
 port 
------
 5432
```

#### Option 2 - Welcher Prozess läuft auf Port:
```bash
lsof -i :5432
```

**Ausgabe:**
```
COMMAND   PID      USER   FD   TYPE DEVICE SIZE/OFF NODE NAME
postgres  1234  username    5u  IPv6  0xabc    0t0  TCP localhost:postgresql (LISTEN)
```

#### Option 3 - Alle PostgreSQL-Verbindungen:
```bash
netstat -an | grep 5432
```

---

### 🔧 Port ändern

#### Via Konfigurationsdatei:

**1. Finde postgresql.conf:**
```bash
psql postgres -c "SHOW config_file;"
```

**Typische Pfade:**
- Intel Mac: `/usr/local/var/postgres/postgresql.conf`
- Apple Silicon: `/opt/homebrew/var/postgres/postgresql.conf`

**2. Bearbeite postgresql.conf:**
```bash
# Mit nano:
nano /opt/homebrew/var/postgres/postgresql.conf

# Mit vim:
vim /opt/homebrew/var/postgres/postgresql.conf

# Oder mit VS Code:
code /opt/homebrew/var/postgres/postgresql.conf
```

**3. Suche und ändere die Zeile:**
```ini
# VORHER:
port = 5432

# NACHHER (z.B. Port 5433):
port = 5433
```

**4. Speichern und PostgreSQL neu starten:**
```bash
brew services restart postgresql@14
```

**5. Teste die neue Port:**
```bash
psql -h localhost -p 5433 -U dein_benutzer -d postgres
```

---

### ⚠️ Port-Konflikte lösen

#### Wenn Port 5432 bereits belegt ist:

**1. Finde welcher Prozess den Port nutzt:**
```bash
lsof -i :5432
```

**2. Stoppe den Prozess:**
```bash
# Finde PID aus obiger Ausgabe (z.B. 1234)
kill -9 1234
```

**3. Oder ändere PostgreSQL Port (siehe oben)**

---

## 6. pgAdmin Management Tool

### 🎯 Wichtige Unterscheidung: Server starten vs. Server registrieren

| Aktion | Was passiert | Wo | Wann |
|--------|--------------|-----|------|
| **PostgreSQL Server starten** | Startet den Datenbank-Dienst | **Terminal** (`brew services start`) | **ZUERST!** Bevor du pgAdmin nutzt |
| **Server in pgAdmin registrieren** | Verbindet pgAdmin mit dem laufenden Server | **pgAdmin** (Register → Server) | **DANACH** Einmalig beim ersten Mal |
| **Server in pgAdmin verbinden** | Aktiviert die Verbindung | **pgAdmin** (Linksklick auf Server) | Bei jedem pgAdmin-Start |

**Merke:**
- ⚠️ **pgAdmin startet NICHT den PostgreSQL-Server!**
- ✅ **pgAdmin ist nur ein Verwaltungs-Tool** (GUI für PostgreSQL)
- ✅ **PostgreSQL muss separat laufen** (via Terminal gestartet)

---

### 🔄 Kompletter Workflow: Vom Start bis zur Datenbank

#### **Phase 1: PostgreSQL Server starten (via Terminal)**

```bash
# Schritt 1: Prüfen ob PostgreSQL bereits läuft
brew services list

# Schritt 2: PostgreSQL starten (falls nicht läuft)
brew services start postgresql@14

# Schritt 3: Verifizieren
lsof -i :5432
# Erwartung: postgres sollte auf Port 5432 laufen
```

**Status nach Phase 1:** ✅ PostgreSQL-Server läuft im Hintergrund

---

#### **Phase 2: pgAdmin starten**

**Via Launchpad/Applications:**
1. **Launchpad** öffnen
2. **pgAdmin 4** suchen und öffnen
3. Master-Passwort eingeben (beim ersten Start festgelegt)

**Via Terminal:**
```bash
open -a pgAdmin\ 4
```

**Status nach Phase 2:** ✅ pgAdmin ist geöffnet (aber noch nicht verbunden)

---

#### **Phase 3: Server in pgAdmin registrieren (nur beim ersten Mal)**

**Dieser Schritt ist nur beim allerersten Mal nötig!**

**Schritt 1: Server registrieren starten**
1. **Servers** → **Rechtsklick** → **Register** → **Server**
2. Oder: **Add New Server** Button oben links

**Schritt 2-6:** Siehe detaillierte Anleitung im nächsten Abschnitt

**Status nach Phase 3:** ✅ Server ist registriert und verbunden (grünes Symbol)

---

#### **Phase 4: Datenbank erstellen**

**Schritt 1: Zum Databases-Ordner**
1. **Servers** → **Dein Server** (z.B. "Local PostgreSQL") → **Databases**

**Schritt 2: Neue DB erstellen**
1. **Rechtsklick auf "Databases"** → **Create** → **Database...**
2. **General Tab:**
   - **Database:** `doctor_provider_db`
   - **Owner:** dein Username
3. **Save** klicken

**Schritt 3: Weitere DBs erstellen (optional)**
- Wiederhole für `patient_customer_db`

**Status nach Phase 4:** ✅ Datenbanken erstellt und einsatzbereit

---

#### **Phase 5: Tabellen erstellen (via Flyway oder SQL)**

**Option A: Via Flyway (empfohlen für dein Projekt)**
1. SQL-Migrations in `src/main/resources/db/migration/` erstellen
2. Spring Boot starten
3. Flyway erstellt automatisch die Tabellen
4. In pgAdmin: Rechtsklick auf DB → **Refresh** → Tabellen erscheinen unter **Schemas** → **public** → **Tables**

**Option B: Via pgAdmin Query Tool**
1. **Rechtsklick auf deine DB** → **Query Tool**
2. SQL-Befehle eingeben (z.B. `CREATE TABLE ...`)
3. **Execute** (F5)

**Status nach Phase 5:** ✅ Tabellen erstellt, Projekt einsatzbereit

---

### 📋 Zusammenfassung des Workflows:

```
1. Terminal: brew services start postgresql@14       [PostgreSQL starten]
                     ↓
2. Launchpad: pgAdmin 4 öffnen                       [pgAdmin öffnen]
                     ↓
3. pgAdmin: Register → Server → Connection eingeben  [Server registrieren]
                     ↓
4. pgAdmin: Databases → Create → Database            [DB erstellen]
                     ↓
5. Flyway: Spring Boot starten                       [Tabellen erstellen]
                     ↓
6. pgAdmin: Refresh → Tabellen anzeigen              [Verifizieren]
```

---

### 🔌 Server in pgAdmin verbinden

⚠️ **WICHTIG:** **"Register Server" startet NICHT den PostgreSQL-Server!**
- Es registriert nur die **Verbindung** zu einem bereits laufenden Server
- PostgreSQL muss **vorher** via Terminal gestartet werden:
  ```bash
  brew services start postgresql@14
  ```

---

**Server-Verbindung in pgAdmin registrieren:**

**Schritt 1: Neuen Server registrieren**
1. In pgAdmin: **Servers** → **Rechtsklick** → **Register** → **Server**
   - Oder: **Add New Server** Button (oben links, Plus-Symbol)

**Schritt 2: General Tab ausfüllen**
- **Name:** `Local PostgreSQL` (oder beliebiger Name, z.B. "Doctor Provider DB")
- **Server Group:** Servers
- **Comments:** (optional) "Lokaler PostgreSQL Server für doctor-provider"

**Schritt 3: Connection Tab ausfüllen**
- **Host name/address:** `localhost` oder `127.0.0.1`
- **Port:** `5432` (Standard, falls nicht geändert)
- **Maintenance database:** `postgres` (Standard-DB für Verbindung)
- **Username:** dein PostgreSQL-Benutzer (z.B. `A200151230` oder `whoami` Ergebnis)
- **Password:** dein PostgreSQL-Passwort
- ✅ **Save password?** aktivieren (optional, für Komfort)

**Schritt 4: SSL Tab** (meist Standard belassen)
- **SSL mode:** Prefer

**Schritt 5: Advanced Tab** (meist leer lassen)

**Schritt 6: Speichern**
- Klicke **Save**

**Bei Erfolg:** 
- ✅ Server erscheint in der linken Sidebar unter **Servers**
- ✅ Grünes Server-Symbol = Verbindung erfolgreich
- ❌ Rotes Server-Symbol = Verbindung fehlgeschlagen (PostgreSQL läuft nicht oder falsche Zugangsdaten)

---

**Bei Fehlern:**

**Fehler: "Connection refused" oder "could not connect to server"**
- **Ursache:** PostgreSQL-Server läuft nicht
- **Lösung:** 
  ```bash
  # Prüfe ob PostgreSQL läuft
  brew services list
  
  # Starte PostgreSQL
  brew services start postgresql@14
  
  # Prüfe Port
  lsof -i :5432
  ```

**Fehler: "password authentication failed"**
- **Ursache:** Falsches Passwort oder Username
- **Lösung:** 
  ```bash
  # Prüfe aktuellen User
  whoami
  
  # Verbinde via Terminal zum Test
  psql postgres -U dein_username
  ```

---

### 📊 Datenbanken in pgAdmin verwalten

#### Datenbank erstellen:

**Voraussetzung:** Server muss registriert und verbunden sein (grünes Symbol)!

**Schritt 1: Zum Databases-Ordner navigieren**
1. In der linken Sidebar:
   - **Servers** → **Dein Server** (z.B. "Local PostgreSQL") → **Databases**

**Schritt 2: Neue Datenbank erstellen**
1. **Rechtsklick auf "Databases"** (der Ordner, nicht auf "postgres"!)
2. **Create** → **Database...**

**Schritt 3: General Tab ausfüllen**
- **Database:** `doctor_provider_db` (Name deiner neuen DB)
- **Owner:** dein Benutzer auswählen (z.B. dein Username)
- **Comment:** (optional) "Doctor Provider Service Database"

**Schritt 4: Definition Tab** (optional)
- **Encoding:** UTF8 (Standard)
- **Template:** template0 oder template1 (Standard)
- **Tablespace:** pg_default (Standard)
- **Collation:** (Standard belassen)
- **Character type:** (Standard belassen)
- **Connection limit:** -1 (unbegrenzt)

**Schritt 5: SQL Tab** (optional, zeigt generierten SQL-Befehl)
- Hier siehst du den SQL-Befehl, der ausgeführt wird:
  ```sql
  CREATE DATABASE doctor_provider_db
      WITH 
      OWNER = dein_username
      ENCODING = 'UTF8'
      CONNECTION LIMIT = -1;
  ```

**Schritt 6: Speichern**
- Klicke **Save**

**Bei Erfolg:**
- ✅ Neue Datenbank erscheint unter **Databases**
- ✅ Du kannst sie erweitern und Schemas/Tabellen sehen

---

**Hinweis zu "postgres" Datenbank:**
- `postgres` ist die **Standard-Maintenance-Datenbank**
- Sie wird für administrative Aufgaben genutzt
- **Lösche sie niemals!**
- Deine App-Datenbanken erstellst du separat (z.B. `doctor_provider_db`)

---

#### Datenbanken anzeigen:
1. **Servers** → **PostgreSQL** → **Databases**
2. Alle Datenbanken werden aufgelistet

#### Tabellen anzeigen:
1. **Servers** → **PostgreSQL** → **Databases** → **doctor_provider_db**
2. **Schemas** → **public** → **Tables**
3. Alle Tabellen werden aufgelistet

#### Tabellendaten anzeigen:
1. Rechtsklick auf **Tabelle** (z.B. `speciality`)
2. **View/Edit Data** → **All Rows**
3. Daten werden im rechten Panel angezeigt

---

### 📝 SQL-Queries in pgAdmin ausführen

1. **Rechtsklick auf Datenbank** (z.B. `doctor_provider_db`)
2. **Query Tool** auswählen
3. SQL-Query schreiben:
   ```sql
   SELECT * FROM speciality;
   ```
4. **Execute** (Play-Button ⏯) klicken oder **F5** drücken
5. Ergebnisse erscheinen unten

---

### 🔌 Extensions in pgAdmin installieren

**UUID Extension aktivieren (optional):**

**Hinweis:** Ab PostgreSQL 13 ist `gen_random_uuid()` standardmäßig verfügbar - du brauchst **keine Extension**!

**Nur wenn du uuid_generate_v4() nutzen willst oder PostgreSQL < 13 hast:**

1. **doctor_provider_db** → **Extensions** → **Rechtsklick**
2. **Create** → **Extension**
3. **Name:** 
   - `uuid-ossp` (für `uuid_generate_v4()`) oder
   - `pgcrypto` (für `gen_random_uuid()` in PostgreSQL < 13)
4. **Save** klicken

**Testen:**
```sql
-- In Query Tool:
SELECT uuid_generate_v4();    -- mit uuid-ossp Extension
-- oder
SELECT gen_random_uuid();     -- PostgreSQL 13+ (ohne Extension!)
```

---

## 7. PostgreSQL in Spring Boot nutzen

### 📦 Dependencies in pom.xml

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
    
    <!-- Flyway PostgreSQL Support (ab Flyway 9.x) -->
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
</dependencies>
```

---

### ⚙️ application.properties Konfiguration

```properties
# ========================================
# Database Configuration
# ========================================
spring.datasource.url=jdbc:postgresql://localhost:5432/doctor_provider_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# ========================================
# JPA/Hibernate Configuration
# ========================================
# WICHTIG: validate, NICHT update/create/create-drop!
# Flyway erstellt die Tabellen, Hibernate validiert nur!
spring.jpa.hibernate.ddl-auto=validate

# Erklärung der Optionen:
# - validate: Hibernate prüft nur, ob Entities mit DB übereinstimmen (RICHTIG für Flyway!)
# - update: Hibernate ändert DB automatisch (GEFÄHRLICH!)
# - create: Hibernate löscht und erstellt DB bei jedem Start (NUR für Tests!)
# - create-drop: Wie create, löscht DB beim Shutdown (NUR für Tests!)
# - none: Hibernate macht nichts (Alternative zu validate)

# SQL-Queries im Log anzeigen (Development)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Dialect (optional, wird automatisch erkannt)
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Naming Strategy (snake_case in DB)
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy

# ========================================
# Flyway Configuration
# ========================================
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-version=0

# Flyway Placeholder (optional)
spring.flyway.placeholder-replacement=true
spring.flyway.placeholders.project=doctor_provider

# ========================================
# Logging (optional)
# ========================================
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.flywaydb=INFO
```

**⚠️ Wichtige Hinweise:**

1. **`spring.jpa.hibernate.ddl-auto=validate` ist Pflicht mit Flyway!**
   - Flyway verwaltet das Datenbank-Schema
   - Hibernate darf nichts ändern, nur validieren
   - Sonst: Konflikte zwischen Flyway und Hibernate

2. **Niemals in Produktion:**
   - `spring.jpa.hibernate.ddl-auto=update` ❌ (kann Daten löschen!)
   - `spring.jpa.hibernate.ddl-auto=create` ❌ (löscht alles!)
   - `spring.jpa.hibernate.ddl-auto=create-drop` ❌ (löscht alles!)

3. **Naming Strategy:**
   - `CamelCaseToUnderscoresNamingStrategy` mappt automatisch:
     - Java: `firstName` → DB: `first_name`
     - Java: `cityId` → DB: `city_id`
   - Daher brauchst du oft keine `@Column(name="...")` Annotation

---

### 🔐 Sensitive Daten auslagern (Best Practice)

**application-local.properties** (nicht in Git!):
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/doctor_provider_db
spring.datasource.username=dein_benutzer
spring.datasource.password=dein_passwort
```

**.gitignore:**
```
application-local.properties
```

**Verwendung:**
```bash
# In IntelliJ: Run Configuration
# Active profiles: local
```

---

## 8. Flyway SQL-Migrationen

### 📂 Wo anlegen?

```
doctor-provider/
└── src/main/resources/
    └── db/
        └── migration/         ← HIER!
            ├── V1__Create_speciality_table.sql
            ├── V2__Create_city_table.sql
            ├── V3__Create_practice_table.sql
            ├── V4__Create_doctor_table.sql
            ├── V5__Create_doctor_speciality_table.sql
            ├── V6__Create_doctor_working_hours_table.sql
            └── V7__Create_slot_table.sql

patient-customer/
└── src/main/resources/
    └── db/
        └── migration/         ← Eigene Migrationen!
            ├── V1__Create_patient_table.sql
            └── V2__Create_appointment_table.sql
```

**Wichtig:**
- ✅ Der Ordner heißt **genau** `db/migration` (nicht `migrations`)
- ✅ Pfad: `src/main/resources/db/migration/`
- ✅ Flyway sucht standardmäßig in `classpath:db/migration`
- ✅ Jeder Service hat seinen eigenen `migration`-Ordner

---

### 📜 Naming Convention (Regeln)

```
V<VERSION>__<DESCRIPTION>.sql

Beispiele:
V1__Create_speciality_table.sql      ✅
V2__Create_city_table.sql             ✅
V10__Add_email_column_to_doctor.sql   ✅
V1.1__Update_speciality.sql           ✅

FALSCH:
v1__create_table.sql                  ❌ (kleines v)
V1_Create_table.sql                   ❌ (nur ein Unterstrich)
V1__create table.sql                  ❌ (Leerzeichen)
Create_table.sql                      ❌ (keine Version)
```

**Regeln:**
1. ✅ Präfix: `V` (großgeschrieben!)
2. ✅ Version: Zahl (z.B. `1`, `2`, `10`) oder `1.0`, `1.1`
   - Wichtig: Zahlen sind fortlaufend, keine Lücken!
   - `V1`, `V2`, `V3` ✅ (richtig)
   - `V1`, `V3`, `V5` ❌ (Lücken vermeiden)
3. ✅ **Zwei** Unterstriche: `__`
4. ✅ Description: Snake_Case oder PascalCase
   - `Create_speciality_table` ✅
   - `Add_email_to_doctor` ✅
   - Sprechende Namen verwenden!
5. ✅ Endung: `.sql`
6. ⚠️ **Niemals** bereits ausgeführte Migrations ändern!
   - Flyway speichert Checksum in `flyway_schema_history`
   - Änderungen führen zu "Checksum mismatch" Fehlern
7. ⚠️ Neue Änderungen = Neue Migration-Datei!
   - Fehler in V1? → Erstelle V8 mit Korrektur, ändere nicht V1!

---

### ⚙️ Wie Flyway funktioniert

**Beim ersten Start der App:**
1. Spring Boot startet
2. Flyway sucht nach `db/migration` Ordner
3. Flyway erstellt Tabelle `flyway_schema_history` (wenn nicht vorhanden)
4. Flyway führt alle `V*__*.sql` Dateien der Reihe nach aus
5. Jede erfolgreiche Migration wird in `flyway_schema_history` gespeichert mit:
   - Version (z.B. `1`)
   - Description (z.B. `Create speciality table`)
   - Checksum (Hash der SQL-Datei)
   - Ausführungsdatum
   - Erfolg/Fehler

**Bei jedem weiteren Start:**
1. Flyway prüft `flyway_schema_history`
2. Nur **neue** Migrations (höhere Version) werden ausgeführt
3. Bereits gelaufene werden übersprungen
4. Falls eine Datei geändert wurde → "Checksum mismatch" Fehler!

**Beispiel `flyway_schema_history` Tabelle:**
```
installed_rank | version | description             | type | script                           | checksum   | installed_on         | success
---------------+---------+-------------------------+------+----------------------------------+------------+----------------------+--------
1              | 1       | Create speciality table | SQL  | V1__Create_speciality_table.sql  | -1234567890| 2026-01-22 10:00:00  | true
2              | 2       | Create city table       | SQL  | V2__Create_city_table.sql        | 987654321  | 2026-01-22 10:00:01  | true
3              | 3       | Create practice table   | SQL  | V3__Create_practice_table.sql    | 123456789  | 2026-01-22 10:00:02  | true
```

---

### 🎯 Flyway Best Practices

1. **✅ Versionsnummern fortlaufend**
   - `V1`, `V2`, `V3`, `V4` ... ✅
   - Keine Lücken, keine Sprünge

2. **✅ Sprechende Dateinamen**
   - `V1__Create_speciality_table.sql` ✅ (gut)
   - `V1__init.sql` ❌ (schlecht)

3. **✅ Eine Datei = Eine logische Änderung**
   - `V1__Create_speciality_table.sql` → Nur speciality Tabelle
   - Nicht: `V1__Create_all_tables.sql` mit 10 Tabellen

4. **✅ Idempotenz beachten (wo möglich)**
   ```sql
   -- Gut:
   CREATE TABLE IF NOT EXISTS speciality (...);
   CREATE INDEX IF NOT EXISTS idx_name ON table(column);
   
   -- Bei Extensions:
   CREATE EXTENSION IF NOT EXISTS pgcrypto;
   ```

5. **✅ Rollback-Strategie**
   - Flyway Free: Keine automatischen Rollbacks!
   - Fehlerhafte Migration → Manuelles Löschen aus `flyway_schema_history` + Tabellen manuell löschen
   - Oder: Neue Migration mit `DROP TABLE` erstellen

6. **✅ Testen vor Deployment**
   - Migrations lokal testen
   - Auf Testdatenbank testen
   - Dann erst auf Produktion

7. **❌ Niemals ändern nach Deployment**
   - Migration gelaufen? → Nicht mehr ändern!
   - Neue Änderung = Neue Migration-Datei

8. **✅ Kommentare in SQL**
   ```sql
   -- =============================================
   -- Purpose: Create speciality table
   -- Author: Dein Name
   -- Date: 2026-01-22
   -- =============================================
   ```

9. **✅ Transaktionen**
   - Flyway führt jede Migration in einer Transaktion aus
   - Bei Fehler: Automatischer Rollback der Migration
   - Aber: Tabelle bleibt in `flyway_schema_history` mit `success = false`

10. **✅ Data Migrations separat**
    ```
    V1__Create_speciality_table.sql      -- Schema
    V2__Insert_default_specialities.sql  -- Daten
    ```

---

### 📝 Flyway SQL-Beispiele mit allen Features

#### **Beispiel 1: V1__Create_speciality_table.sql**

```sql
-- =============================================
-- Speciality Table mit ENUM und UUID
-- =============================================

-- 1. ENUM-Typ erstellen
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

-- 2. Tabelle erstellen
CREATE TABLE speciality (
    -- PRIMARY KEY mit UUID und automatischer Generierung
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- ENUM-Spalte mit UNIQUE constraint
    name speciality_type NOT NULL UNIQUE,
    
    -- Zusätzliche Spalten (optional)
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Index erstellen (für schnellere Suchen)
CREATE INDEX idx_speciality_name ON speciality(name);

-- 4. Kommentar hinzufügen (Dokumentation)
COMMENT ON TABLE speciality IS 'Fachrichtungen der Ärzte';
COMMENT ON COLUMN speciality.name IS 'Name der Fachrichtung (ENUM)';
```

---

#### **Beispiel 2: V2__Create_city_table.sql**

```sql
-- =============================================
-- City Table mit Constraints
-- =============================================

CREATE TABLE city (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'Deutschland',
    
    -- UNIQUE constraint auf Kombination
    CONSTRAINT unique_city_zip UNIQUE (name, zip_code),
    
    -- CHECK constraints für Validierung
    CONSTRAINT city_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT city_zip_code_not_empty CHECK (LENGTH(TRIM(zip_code)) > 0),
    CONSTRAINT city_zip_code_format CHECK (zip_code ~* '^[0-9]{5}$')  -- 5 Ziffern
);

-- Index für häufige Suchen
CREATE INDEX idx_city_name ON city(name);
CREATE INDEX idx_city_zip_code ON city(zip_code);

-- Testdaten einfügen (optional)
INSERT INTO city (name, zip_code, state) VALUES
    ('Berlin', '10115', 'Berlin'),
    ('Hamburg', '20095', 'Hamburg'),
    ('München', '80331', 'Bayern'),
    ('Köln', '50667', 'Nordrhein-Westfalen'),
    ('Frankfurt', '60311', 'Hessen');
```

---

#### **Beispiel 3: V3__Create_practice_table.sql**

```sql
-- =============================================
-- Practice Table mit Foreign Key
-- =============================================

CREATE TABLE practice (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    street VARCHAR(300) NOT NULL,
    house_number VARCHAR(20) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(100),
    website VARCHAR(255),
    
    -- FOREIGN KEY zu city
    city_id UUID NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraint mit CASCADE
    CONSTRAINT fk_practice_city FOREIGN KEY (city_id)
        REFERENCES city(id) ON DELETE CASCADE,
    
    -- Validierungs-Constraints
    CONSTRAINT practice_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT practice_street_not_empty CHECK (LENGTH(TRIM(street)) > 0),
    CONSTRAINT practice_house_number_not_empty CHECK (LENGTH(TRIM(house_number)) > 0),
    
    -- Email-Format Validierung (Regex)
    CONSTRAINT practice_email_format CHECK (
        email IS NULL OR 
        email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'
    ),
    
    -- Phone-Format Validierung
    CONSTRAINT practice_phone_format CHECK (
        phone IS NULL OR 
        phone ~* '^\+?[0-9\s\-\(\)]+$'
    ),
    
    -- Website-Format Validierung
    CONSTRAINT practice_website_format CHECK (
        website IS NULL OR 
        website ~* '^https?://.+'
    )
);

-- Indizes
CREATE INDEX idx_practice_city ON practice(city_id);
CREATE INDEX idx_practice_name ON practice(name);
CREATE INDEX idx_practice_email ON practice(email);

-- Trigger für updated_at (automatisches Update)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_practice_updated_at 
    BEFORE UPDATE ON practice
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

#### **Beispiel 4: V4__Create_doctor_table.sql**

```sql
-- =============================================
-- Doctor Table - Alle Datentypen & Features
-- =============================================

CREATE TABLE doctor (
    -- UUID Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign Key
    practice_id UUID NOT NULL,
    
    -- VARCHAR für Strings
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(50),  -- z.B. "Dr. med.", "Prof. Dr."
    
    -- TEXT für lange Texte
    bio TEXT,
    
    -- INTEGER für Zahlen
    years_of_experience INTEGER CHECK (years_of_experience >= 0),
    
    -- NUMERIC/DECIMAL für Preise/Geld
    consultation_fee NUMERIC(10, 2) CHECK (consultation_fee >= 0),  -- 10 Stellen, 2 Dezimalen
    
    -- BOOLEAN
    is_active BOOLEAN NOT NULL DEFAULT true,
    accepts_new_patients BOOLEAN NOT NULL DEFAULT true,
    
    -- DATE (nur Datum, keine Uhrzeit)
    date_of_birth DATE,
    license_date DATE,
    
    -- TIMESTAMP WITH TIME ZONE (Datum + Uhrzeit + Timezone)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- JSONB für strukturierte Daten (optional)
    contact_preferences JSONB,
    
    -- ARRAY (optional)
    languages TEXT[] DEFAULT ARRAY['Deutsch'],
    
    -- Foreign Key Constraint
    CONSTRAINT fk_doctor_practice FOREIGN KEY (practice_id)
        REFERENCES practice(id) ON DELETE CASCADE,
    
    -- CHECK Constraints
    CONSTRAINT doctor_first_name_not_empty CHECK (LENGTH(TRIM(first_name)) > 0),
    CONSTRAINT doctor_last_name_not_empty CHECK (LENGTH(TRIM(last_name)) > 0),
    CONSTRAINT doctor_age_valid CHECK (
        date_of_birth IS NULL OR 
        date_of_birth < CURRENT_DATE - INTERVAL '18 years'
    )
);

-- Indizes
CREATE INDEX idx_doctor_practice ON doctor(practice_id);
CREATE INDEX idx_doctor_name ON doctor(last_name, first_name);  -- Composite Index
CREATE INDEX idx_doctor_active ON doctor(is_active) WHERE is_active = true;  -- Partial Index
CREATE INDEX idx_doctor_languages ON doctor USING GIN (languages);  -- GIN Index für Arrays

-- Full-text Search Index (optional)
CREATE INDEX idx_doctor_bio_fulltext ON doctor USING GIN (to_tsvector('german', bio));
```

---

#### **Beispiel 5: V5__Create_doctor_speciality_table.sql (M:N)**

```sql
-- =============================================
-- Doctor-Speciality Junction Table (Many-to-Many)
-- =============================================

CREATE TABLE doctor_speciality (
    doctor_id UUID NOT NULL,
    speciality_id UUID NOT NULL,
    
    -- Zusätzliche Felder (optional)
    is_primary BOOLEAN DEFAULT false,  -- Hauptfachrichtung?
    certified_since DATE,
    
    -- Composite Primary Key
    PRIMARY KEY (doctor_id, speciality_id),
    
    -- Foreign Keys mit CASCADE
    CONSTRAINT fk_doctor_speciality_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_doctor_speciality_speciality FOREIGN KEY (speciality_id)
        REFERENCES speciality(id) ON DELETE CASCADE
);

-- Indizes für beide Richtungen
CREATE INDEX idx_doctor_speciality_doctor ON doctor_speciality(doctor_id);
CREATE INDEX idx_doctor_speciality_speciality ON doctor_speciality(speciality_id);
CREATE INDEX idx_doctor_speciality_primary ON doctor_speciality(doctor_id, is_primary) 
    WHERE is_primary = true;
```

---

#### **Beispiel 6: V6__Create_doctor_working_hours_table.sql**

```sql
-- =============================================
-- Doctor Working Hours Table
-- =============================================

CREATE TABLE doctor_working_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    
    -- INTEGER mit CHECK für Wochentag (1=Mo, 7=So)
    weekday INTEGER NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    
    -- TIME (nur Uhrzeit, kein Datum)
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_doctor_working_hours_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    -- CHECK Constraints
    CONSTRAINT chk_start_end_time CHECK (start_time < end_time),
    CONSTRAINT chk_working_hours_duration CHECK (
        (end_time - start_time) >= INTERVAL '30 minutes'
    ),
    
    -- UNIQUE Constraint: Arzt kann nicht zweimal am gleichen Tag zur gleichen Zeit arbeiten
    CONSTRAINT unique_doctor_weekday_time UNIQUE (doctor_id, weekday, start_time, end_time)
);

-- Indizes
CREATE INDEX idx_working_hours_doctor ON doctor_working_hours(doctor_id);
CREATE INDEX idx_working_hours_weekday ON doctor_working_hours(weekday);
```

---

#### **Beispiel 7: V7__Create_slot_table.sql**

```sql
-- =============================================
-- Slot Table mit ENUM Status
-- =============================================

-- 1. ENUM für Slot Status
CREATE TYPE slot_status AS ENUM ('available', 'booked', 'blocked');

-- 2. Tabelle
CREATE TABLE slot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    
    -- TIMESTAMP WITH TIME ZONE für Termine (wichtig für Timezone!)
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- ENUM Status
    status slot_status NOT NULL DEFAULT 'available',
    
    -- Optional: Patient-Info (NULL wenn available)
    patient_id UUID,
    notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id)
        REFERENCES doctor(id) ON DELETE CASCADE,
    
    -- CHECK Constraints
    CONSTRAINT chk_slot_start_end_time CHECK (start_time < end_time),
    CONSTRAINT chk_slot_duration CHECK (
        (end_time - start_time) >= INTERVAL '5 minutes' AND
        (end_time - start_time) <= INTERVAL '4 hours'
    ),
    CONSTRAINT chk_slot_patient CHECK (
        (status = 'booked' AND patient_id IS NOT NULL) OR
        (status != 'booked' AND patient_id IS NULL)
    ),
    
    -- UNIQUE Constraint: Kein Arzt kann zwei Slots zur gleichen Zeit haben
    CONSTRAINT unique_doctor_slot_time UNIQUE (doctor_id, start_time)
);

-- Indizes
CREATE INDEX idx_slot_doctor ON slot(doctor_id);
CREATE INDEX idx_slot_start_time ON slot(start_time);
CREATE INDEX idx_slot_status ON slot(status);
CREATE INDEX idx_slot_doctor_status_time ON slot(doctor_id, status, start_time) 
    WHERE status = 'available';  -- Partial Index für verfügbare Slots

-- Trigger für updated_at
CREATE TRIGGER update_slot_updated_at 
    BEFORE UPDATE ON slot
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

### 📊 Alle PostgreSQL-Datentypen (Übersicht)

| Typ | Beschreibung | Beispiel in SQL |
|-----|--------------|-----------------|
| **UUID** | Universally Unique Identifier | `id UUID PRIMARY KEY DEFAULT gen_random_uuid()` |
| **VARCHAR(n)** | Variable Zeichenkette (max n) | `name VARCHAR(100) NOT NULL` |
| **TEXT** | Unbegrenzte Zeichenkette | `bio TEXT` |
| **INTEGER** | Ganzzahl | `age INTEGER CHECK (age >= 0)` |
| **BIGINT** | Große Ganzzahl | `population BIGINT` |
| **NUMERIC(p,s)** | Dezimalzahl (p=Präzision, s=Nachkomma) | `price NUMERIC(10,2)` |
| **BOOLEAN** | Wahr/Falsch | `is_active BOOLEAN DEFAULT true` |
| **DATE** | Nur Datum | `birth_date DATE` |
| **TIME** | Nur Uhrzeit | `start_time TIME` |
| **TIMESTAMP** | Datum + Uhrzeit (ohne TZ) | `created_at TIMESTAMP` |
| **TIMESTAMPTZ** | Datum + Uhrzeit + Timezone | `start_time TIMESTAMP WITH TIME ZONE` |
| **INTERVAL** | Zeitspanne | `duration INTERVAL` |
| **ENUM** | Aufzählungstyp | `status slot_status` |
| **JSONB** | JSON Binär (performanter) | `metadata JSONB` |
| **ARRAY** | Array | `tags TEXT[]` |

---

### ✅ Flyway Constraints & Validierungen (Checkliste)

```sql
-- PRIMARY KEY
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- FOREIGN KEY
CONSTRAINT fk_name FOREIGN KEY (column) 
    REFERENCES other_table(id) ON DELETE CASCADE

-- UNIQUE
name VARCHAR(100) UNIQUE
-- oder
CONSTRAINT unique_name UNIQUE (column1, column2)

-- NOT NULL
name VARCHAR(100) NOT NULL

-- DEFAULT
is_active BOOLEAN DEFAULT true
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

-- CHECK - Nicht leer
CONSTRAINT name_not_empty CHECK (LENGTH(TRIM(name)) > 0)

-- CHECK - Zahlenbereich
CONSTRAINT age_valid CHECK (age BETWEEN 18 AND 100)

-- CHECK - Regex (Email)
CONSTRAINT email_valid CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')

-- CHECK - Regex (Phone)
CONSTRAINT phone_valid CHECK (phone ~* '^\+?[0-9\s\-\(\)]+$')

-- CHECK - Vergleich
CONSTRAINT start_before_end CHECK (start_time < end_time)

-- CHECK - ENUM
CONSTRAINT status_valid CHECK (status IN ('available', 'booked', 'blocked'))

-- INDEX
CREATE INDEX idx_name ON table(column);

-- UNIQUE INDEX
CREATE UNIQUE INDEX idx_unique_name ON table(column);

-- COMPOSITE INDEX
CREATE INDEX idx_name ON table(col1, col2);

-- PARTIAL INDEX (bedingter Index)
CREATE INDEX idx_name ON table(column) WHERE condition;
```

---

## 9. IntelliJ Data Source Konfiguration

### 🔧 Data Source einrichten

**Schritt 1: Database Tool Window öffnen**
- Shortcut: `⌘` + `Shift` + `D` (Mac) oder `Ctrl` + `Shift` + `D` (Windows)
- Oder: **View** → **Tool Windows** → **Database**

**Schritt 2: Data Source hinzufügen**
1. Klicke auf **"+"** Symbol (oben links)
2. **Data Source** → **PostgreSQL**

**Schritt 3: Verbindungsdaten eingeben**
- **Name:** `doctor_provider_db`
- **Host:** `localhost`
- **Port:** `5432`
- **Database:** `doctor_provider_db`
- **User:** dein PostgreSQL-Benutzer (z.B. `A200151230`)
- **Password:** dein Passwort
- ✅ **Save password** aktivieren (optional)

**Schritt 4: Treiber herunterladen**
- Falls "Missing driver files" angezeigt wird:
- Klicke auf **Download missing driver files**
- Warte bis Download abgeschlossen ist

**Schritt 5: Verbindung testen**
- Klicke auf **Test Connection**
- Bei Erfolg: **"Succeeded"** mit grünem Haken ✅
- Bei Fehler: Prüfe Username, Passwort, Port

**Schritt 6: Speichern**
- Klicke auf **Apply**
- Klicke auf **OK**

---

### 📝 SQL-Datei mit Data Source verknüpfen

**Methode 1: Automatische Erkennung**
1. Öffne deine SQL-Datei (z.B. `V1__Create_speciality_table.sql`)
2. Oben in der Datei erscheint eine gelbe Benachrichtigungsleiste
3. Klicke auf **Assign Data Source**
4. Wähle `doctor_provider_db` aus

**Methode 2: Manuell**
1. Öffne deine SQL-Datei
2. Oben rechts im Editor siehst du eine Dropdown-Liste
3. Klicke auf **<no data source>**
4. Wähle **doctor_provider_db** aus

**Methode 3: Via Context Menu**
1. Rechtsklick in die SQL-Datei
2. **Change Dialect or Data Source**
3. **Data Source:** `doctor_provider_db`
4. **SQL Dialect:** PostgreSQL

---

### 🎯 DDL Data Source konfigurieren

**Wichtig für IntelliJ Code Completion!**

1. Öffne SQL-Datei
2. Oben im Editor: Dropdown **"DDL Data Source"**
3. Wähle `doctor_provider_db`

**Was bringt das?**
- ✅ IntelliJ kennt alle Tabellen
- ✅ Autocomplete für Tabellen- und Spaltennamen
- ✅ "Unable to resolve table 'xxx'" Fehler verschwinden
- ✅ Syntax-Highlighting für deine spezifischen Tabellen

---

### ✅ Troubleshooting IntelliJ Data Source

#### Problem: "No data sources are configured"
**Lösung:** Data Source einrichten (siehe oben)

#### Problem: "SQL dialect is not configured"
**Lösung:**
1. Rechtsklick in SQL-Datei
2. **Change Dialect**
3. **PostgreSQL** wählen

#### Problem: "Unable to resolve table 'xxx'"
**Lösung:**
1. Data Source verbinden (grüner Haken in Database Tool Window)
2. DDL Data Source setzen
3. Eventuell: **Synchronize** klicken (Reload-Icon im Database Tool)

#### Problem: "Unknown database function 'gen_random_uuid'"
**Lösung:**
- Das ist nur eine IDE-Warnung!
- Die Funktion `gen_random_uuid()` ist ab PostgreSQL 13 **standardmäßig verfügbar** (ohne Extension!)
- Flyway wird es zur Laufzeit korrekt ausführen
- Falls du PostgreSQL < 13 nutzt, aktiviere die Extension:
  ```sql
  CREATE EXTENSION IF NOT EXISTS pgcrypto;
  ```

---

## 10. Problemlösungen & Troubleshooting

### ❌ Problem: "role 'postgres' does not exist"

**Ursache:** Der Standardbenutzer `postgres` wurde nicht erstellt.

**Lösung:**
```bash
# Option 1: Nutze deinen macOS-Benutzer
whoami  # z.B. A200151230
psql -U A200151230 postgres

# Option 2: Erstelle den postgres-Benutzer
psql postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'dein_passwort';"
```

---

### ❌ Problem: "connection refused" oder "connection to server failed"

**Ursache:** PostgreSQL läuft nicht.

**Lösung:**
```bash
# Prüfe Status
brew services list

# Starte PostgreSQL
brew services start postgresql@14

# Prüfe Port
lsof -i :5432
```

---

### ❌ Problem: "database does not exist"

**Ursache:** Datenbank wurde noch nicht erstellt.

**Lösung:**
```bash
createdb doctor_provider_db
createdb patient_customer_db
```

---

### ❌ Problem: "Unknown database function 'uuid_generate_v4'"

**Ursache:** uuid-ossp Extension nicht aktiviert (nur für `uuid_generate_v4()`).

**Lösung Option 1 - Nutze `gen_random_uuid()` (PostgreSQL ≥ 13, empfohlen!):**
```sql
-- In deinen Migrations:
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```
**Keine Extension nötig!** Dies ist die moderne und empfohlene Methode.

**Lösung Option 2 - Aktiviere uuid-ossp Extension (für uuid_generate_v4):**
```sql
-- V0__Enable_extensions.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Dann kannst du nutzen:
-- id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
```

**Lösung Option 3 - Aktiviere pgcrypto Extension (PostgreSQL < 13):**
```sql
-- V0__Enable_extensions.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Dann ist gen_random_uuid() verfügbar
```

**Empfehlung:** Nutze `gen_random_uuid()` - es ist modern und braucht keine Extension!

---

### ❌ Problem: Flyway "Checksum mismatch" oder "Migration failed"

**Ursache:** Migration-Datei wurde nach Ausführung geändert.

**Lösung (nur Development!):**
```bash
# Option 1: Flyway History reparieren
psql doctor_provider_db -c "DELETE FROM flyway_schema_history WHERE script = 'V1__Create_speciality_table.sql';"

# Option 2: Gesamte DB neu aufsetzen
dropdb doctor_provider_db
createdb doctor_provider_db
# Spring Boot neu starten → Flyway läuft erneut

# Option 3: Flyway Repair (in application.properties):
spring.flyway.repair=true
# Nach dem Start wieder entfernen!
```

⚠️ **Produktions-Regel:** Niemals bereits ausgeführte Migrations ändern!

---

### ❌ Problem: "Permission denied" beim Zugriff

**Ursache:** Fehlende Berechtigungen.

**Lösung:**
```sql
-- Als Superuser:
psql postgres

-- Rechte vergeben:
GRANT ALL PRIVILEGES ON DATABASE doctor_provider_db TO dein_benutzer;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dein_benutzer;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dein_benutzer;

-- Owner ändern:
ALTER DATABASE doctor_provider_db OWNER TO dein_benutzer;
```

---

### ❌ Problem: Port 5432 bereits belegt

**Ursache:** Anderer Prozess nutzt Port 5432.

**Lösung:**
```bash
# Finde Prozess
lsof -i :5432

# Stoppe Prozess (Vorsicht!)
kill -9 <PID>

# Oder ändere PostgreSQL Port (siehe Kapitel 5)
```

---

### ❌ Problem: pgAdmin Verbindung schlägt fehl

**Checkliste:**
1. ✅ PostgreSQL läuft? → `brew services list`
2. ✅ Richtiger Port? → Standard: 5432
3. ✅ Richtiger Username? → `\du` in psql
4. ✅ Richtiges Passwort? → Test mit `psql -U username -W`
5. ✅ Firewall-Regel? → Localhost sollte immer funktionieren

---

## 🚀 Schnellreferenz (Cheat Sheet)

### PostgreSQL Server
```bash
brew services start postgresql@14     # Starten
brew services stop postgresql@14      # Stoppen
brew services restart postgresql@14   # Neustarten
brew services list                    # Status
lsof -i :5432                         # Port prüfen
```

### Datenbanken
```bash
psql -l                               # Auflisten
createdb doctor_provider_db           # Erstellen
dropdb doctor_provider_db             # Löschen
psql doctor_provider_db               # Verbinden
```

### In psql
```sql
\l                                    -- Datenbanken
\c doctor_provider_db                 -- DB wechseln
\dt                                   -- Tabellen
\d tablename                          -- Schema
\du                                   -- Benutzer
\q                                    -- Beenden
```

### Queries
```sql
SELECT * FROM tablename;              -- Alle Daten
SELECT COUNT(*) FROM tablename;       -- Anzahl
INSERT INTO ...                       -- Einfügen
UPDATE ... SET ... WHERE ...          -- Aktualisieren
DELETE FROM ... WHERE ...             -- Löschen
```

### Benutzer
```bash
\password                             -- Eigenes PW ändern
\du                                   -- Alle Benutzer
CREATE USER name WITH PASSWORD 'pw'; -- Benutzer erstellen
```

---

## ✅ Checkliste: Projekt-Setup

### Initial Setup:
- [ ] PostgreSQL installiert (`brew install postgresql@14`)
- [ ] PostgreSQL gestartet (`brew services start postgresql@14`)
- [ ] Benutzer konfiguriert (`\du`)
- [ ] Passwort gesetzt (`\password`)

### Datenbanken:
- [ ] `doctor_provider_db` erstellt (`createdb doctor_provider_db`)
- [ ] `patient_customer_db` erstellt (`createdb patient_customer_db`)
- [ ] Verbindung getestet (`psql doctor_provider_db`)

### Spring Boot:
- [ ] Dependencies in `pom.xml` (`spring-boot-starter-data-jpa`, `postgresql`, `flyway-core`)
- [ ] `application.properties` konfiguriert
- [ ] Migration-Ordner erstellt (`src/main/resources/db/migration/`)
- [ ] SQL-Dateien erstellt (`V1__*.sql`, `V2__*.sql`, ...)

### IntelliJ:
- [ ] Data Source konfiguriert
- [ ] Data Source verbunden (grüner Haken)
- [ ] SQL-Dateien mit Data Source verknüpft
- [ ] DDL Data Source gesetzt

### pgAdmin:
- [ ] **PostgreSQL Server gestartet** (via Terminal: `brew services start postgresql@14`)
- [ ] pgAdmin 4 installiert und geöffnet
- [ ] **Server in pgAdmin registriert** (Servers → Register → Server → Connection eingeben)
- [ ] Verbindung erfolgreich (grünes Server-Symbol)
- [ ] Datenbank `doctor_provider_db` erstellt (Databases → Create → Database)
- [ ] Datenbank `patient_customer_db` erstellt
- [ ] Extensions aktiviert (optional: `pgcrypto` oder `uuid-ossp`, nur wenn PostgreSQL < 13)

### Test:
- [ ] Spring Boot App gestartet
- [ ] Flyway läuft durch (keine Fehler im Log)
- [ ] Tabellen erstellt (prüfe mit `\dt` oder pgAdmin)
- [ ] `flyway_schema_history` enthält alle Migrations

---

## 🎯 Zusammenfassung

**Du hast jetzt alles, was du für PostgreSQL in deinem Projekt brauchst:**

1. ✅ **Server Management** - Starten, Stoppen, Status prüfen
2. ✅ **Datenbanken** - Erstellen, Auflisten, Löschen, Zugreifen
3. ✅ **Benutzer** - Username/Passwort anzeigen & ändern
4. ✅ **Port** - Anzeigen & ändern
5. ✅ **pgAdmin** - GUI-Tool für komfortable Verwaltung
6. ✅ **Spring Boot Integration** - Dependencies & Configuration
7. ✅ **Flyway Migrations** - SQL-Dateien mit allen Features
8. ✅ **IntelliJ Setup** - Data Source & DDL Configuration
9. ✅ **Troubleshooting** - Lösungen für häufige Probleme

**Die POSTGRESQL_GUIDE.md und DATABASE_SETUP_GUIDE.md bleiben unverändert, damit du vergleichen kannst!**

---

📝 **Erstellt am:** 22. Januar 2026  
🎯 **Für:** Doctor Provider & Patient Customer Services  
✨ **Status:** Production Ready
