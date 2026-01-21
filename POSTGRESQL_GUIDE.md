# PostgreSQL Management Guide

## 1. PostgreSQL Server Management

### Server starten
```bash
# Via Homebrew (empfohlen)
brew services start postgresql@14

# Für neuere PostgreSQL-Versionen
brew services start postgresql@15
brew services start postgresql@16

# Oder manuell (Intel Mac)
pg_ctl -D /usr/local/var/postgres start

# Oder manuell (Apple Silicon M1/M2/M3)
pg_ctl -D /opt/homebrew/var/postgres start
```

### Server stoppen
```bash
# Via Homebrew
brew services stop postgresql@14

# Oder manuell
pg_ctl -D /usr/local/var/postgres stop

# Oder für neuere Versionen
pg_ctl -D /opt/homebrew/var/postgres stop
```

### Server neustarten
```bash
# Via Homebrew
brew services restart postgresql@14

# Oder manuell
pg_ctl -D /usr/local/var/postgres restart
```

### Server Status prüfen
```bash
# Via Homebrew (zeigt alle Services)
brew services list

# Oder manuell (Intel Mac)
pg_ctl -D /usr/local/var/postgres status

# Oder manuell (Apple Silicon)
pg_ctl -D /opt/homebrew/var/postgres status

# Oder prüfe laufende PostgreSQL-Prozesse
ps aux | grep postgres

# Prüfe, ob PostgreSQL auf Port 5432 läuft
lsof -i :5432
```

---

## 2. Datenbanken verwalten (Terminal)

### Liste aller Datenbanken anzeigen

**Option 1: Ohne psql zu betreten**
```bash
psql -l
```

**Option 2: Innerhalb von psql**
```bash
# PostgreSQL Shell öffnen
psql postgres

# Dann in psql:
\l
# oder
\list
```

**Option 3: Mit SQL-Befehl**
```bash
psql -c "SELECT datname FROM pg_database;"
```

### Datenbank erstellen
```bash
# Direkt via Terminal
createdb doctor_provider_db

# Oder via psql
psql postgres -c "CREATE DATABASE doctor_provider_db;"

# Für beide Services des Projekts
createdb doctor_provider_db
createdb patient_customer_db

# Mit spezifischem Owner
createdb -O dein_benutzer doctor_provider_db
```

### Datenbank löschen
```bash
# Direkt via Terminal
dropdb doctor_provider_db

# Oder via psql
psql postgres -c "DROP DATABASE doctor_provider_db;"
```

### Mit einer Datenbank verbinden
```bash
# Methode 1
psql doctor_provider_db

# Methode 2
psql -d doctor_provider_db

# Methode 3 (mit spezifischem Benutzer)
psql -U dein_benutzer -d doctor_provider_db
```

---

## 3. Datenbankinhalt anzeigen (Terminal)

### Alle Tabellen einer Datenbank anzeigen
```bash
# Verbinde dich mit der Datenbank
psql doctor_provider_db

# Zeige alle Tabellen
\dt

# Zeige Tabellen mit Details
\dt+

# Zeige auch Views und Sequenzen
\d
```

### Schema einer spezifischen Tabelle anzeigen
```sql
-- In psql:
\d speciality
\d+ speciality  -- mit mehr Details
```

### Daten aus einer Tabelle abfragen
```sql
-- In psql oder als Befehl:
SELECT * FROM speciality;

-- Begrenzte Anzahl
SELECT * FROM speciality LIMIT 10;

-- Mit Bedingung
SELECT * FROM speciality WHERE name = 'Kardiologie';
```

### Anzahl der Einträge in einer Tabelle
```sql
SELECT COUNT(*) FROM speciality;
```

### Alle verfügbaren Extensions anzeigen
```sql
-- Installierte Extensions
\dx

-- Verfügbare Extensions
SELECT * FROM pg_available_extensions;
```

---

## 4. Nützliche psql-Befehle

```bash
# PostgreSQL Shell betreten
psql postgres

# Befehle innerhalb von psql:

\?                    # Hilfe für psql-Befehle
\h                    # Hilfe für SQL-Befehle
\h CREATE TABLE       # Hilfe für spezifischen SQL-Befehl

\l                    # Liste aller Datenbanken
\c doctor_provider_db # Mit Datenbank verbinden (connect)
\dt                   # Liste aller Tabellen
\d                    # Liste aller Relationen
\d tablename          # Schema einer Tabelle anzeigen
\du                   # Liste aller Benutzer/Rollen
\dx                   # Liste installierter Extensions

\q                    # psql beenden (quit)
\! clear              # Terminal leeren
```

---

## 5. pgAdmin Management

### pgAdmin starten
1. **Via Launchpad** oder **Applications**
2. Öffne **pgAdmin 4**
3. Gib dein Master-Passwort ein (beim ersten Start festgelegt)

### Server verbinden
1. Im linken Panel: **Servers** → **Rechtsklick** → **Register** → **Server**
2. **General Tab:**
   - Name: `Local PostgreSQL`
3. **Connection Tab:**
   - Host: `localhost`
   - Port: `5432`
   - Maintenance database: `postgres`
   - Username: dein PostgreSQL-Benutzer (z.B. `A200151230`)
   - Password: dein Passwort
4. **Save password** aktivieren (optional)
5. Klicke auf **Save**

### Datenbank erstellen (pgAdmin)
1. **Servers** → **PostgreSQL** → **Databases** → **Rechtsklick**
2. Wähle **Create** → **Database**
3. **Database:** `doctor_provider_db`
4. **Owner:** dein Benutzer
5. Klicke auf **Save**

### Tabellen anzeigen (pgAdmin)
1. **Servers** → **PostgreSQL** → **Databases** → **doctor_provider_db**
2. **Schemas** → **public** → **Tables**
3. Rechtsklick auf eine Tabelle → **View/Edit Data** → **All Rows**

### SQL-Query ausführen (pgAdmin)
1. Rechtsklick auf **doctor_provider_db** → **Query Tool**
2. Schreibe deine SQL-Abfrage
3. Klicke auf **Execute** (Play-Button) oder drücke **F5**

### Extensions installieren (pgAdmin)
1. **doctor_provider_db** → **Extensions** → **Rechtsklick**
2. Wähle **Create** → **Extension**
3. Wähle z.B. **uuid-ossp** aus der Dropdown-Liste
4. Klicke auf **Save**

---

## 6. PostgreSQL Benutzerverwaltung

### Aktuellen Benutzernamen anzeigen

**Option 1: Via Terminal (macOS-Benutzer)**
```bash
# Dein macOS-Benutzername ist oft auch dein PostgreSQL-Benutzer
whoami
```

**Option 2: In psql**
```bash
# Verbinde dich mit PostgreSQL
psql postgres

# Zeige aktuellen Benutzer
SELECT current_user;

# Oder
\conninfo
```

**Option 3: Alle PostgreSQL-Benutzer anzeigen**
```bash
# Via Terminal
psql postgres -c "\du"

# Oder in psql
\du
\du+  -- mit mehr Details
```

**Option 4: Mit SQL-Befehl**
```sql
SELECT usename, usesuper, usecreatedb 
FROM pg_user;
```

### Passwort anzeigen

⚠️ **Wichtig:** PostgreSQL speichert Passwörter verschlüsselt (gehashed). Du kannst das **Original-Passwort nicht anzeigen**, nur den Hash.

**Passwort-Hashes anzeigen:**
```bash
# In psql als Superuser
psql postgres

# Zeige Passwort-Hashes
SELECT usename, passwd FROM pg_shadow;

# Oder nur für einen Benutzer
SELECT usename, passwd FROM pg_shadow WHERE usename = 'dein_benutzer';
```

**Passwort prüfen:**
```bash
# Teste die Verbindung mit deinem Passwort
psql -U dein_benutzer -d postgres
# Wenn die Verbindung erfolgreich ist, ist dein Passwort korrekt
```

### Passwort ändern

**Option 1: Via psql (empfohlen)**
```bash
# Verbinde dich als Superuser oder als der Benutzer selbst
psql postgres

# Ändere dein eigenes Passwort
\password

# Oder ändere das Passwort eines anderen Benutzers
\password dein_benutzer
```

**Option 2: Mit SQL-Befehl**
```sql
-- In psql
ALTER USER dein_benutzer WITH PASSWORD 'neues_passwort';

-- Beispiel
ALTER USER A200151230 WITH PASSWORD 'mein_neues_passwort';

-- Für den Benutzer 'postgres'
ALTER USER postgres WITH PASSWORD 'neues_postgres_passwort';
```

**Option 3: Via Terminal (direkt)**
```bash
# Als Superuser
psql postgres -c "ALTER USER dein_benutzer WITH PASSWORD 'neues_passwort';"
```

### Neuen Benutzer erstellen

```bash
# In psql
CREATE USER neuer_benutzer WITH PASSWORD 'passwort';

# Mit Superuser-Rechten
CREATE USER neuer_benutzer WITH SUPERUSER PASSWORD 'passwort';

# Mit spezifischen Rechten
CREATE USER neuer_benutzer WITH 
    PASSWORD 'passwort'
    CREATEDB
    CREATEROLE;
```

**Oder via Terminal:**
```bash
createuser -P neuer_benutzer
# -P fragt nach Passwort
```

### Benutzer löschen

```bash
# In psql
DROP USER benutzer_name;

# Falls der Benutzer Objekte besitzt, erst reassign
REASSIGN OWNED BY benutzer_name TO postgres;
DROP OWNED BY benutzer_name;
DROP USER benutzer_name;
```

### Benutzerrechte anzeigen

```bash
# In psql
\du benutzer_name

# Oder mit SQL
SELECT 
    usename AS username,
    usesuper AS is_superuser,
    usecreatedb AS can_create_db,
    usecreaterole AS can_create_role
FROM pg_user
WHERE usename = 'dein_benutzer';
```

### Datenbank-Besitzer ändern

```sql
-- Ändere den Owner einer Datenbank
ALTER DATABASE doctor_provider_db OWNER TO neuer_benutzer;

-- Zeige alle Datenbanken mit ihren Besitzern
SELECT datname, pg_catalog.pg_get_userbyid(datdba) AS owner
FROM pg_database;
```

### Typische Benutzerkonfiguration für dein Projekt

```bash
# 1. Verbinde dich mit postgres
psql postgres

# 2. Erstelle einen Projektbenutzer (falls noch nicht vorhanden)
CREATE USER doctor_app WITH PASSWORD 'sicheres_passwort';

# 3. Gib dem Benutzer Rechte für die Datenbanken
GRANT ALL PRIVILEGES ON DATABASE doctor_provider_db TO doctor_app;
GRANT ALL PRIVILEGES ON DATABASE patient_customer_db TO doctor_app;

# 4. Ändere den Owner der Datenbanken (optional)
ALTER DATABASE doctor_provider_db OWNER TO doctor_app;
ALTER DATABASE patient_customer_db OWNER TO doctor_app;
```

### Verbindung mit bestimmtem Benutzer testen

```bash
# Teste Verbindung
psql -U benutzer_name -d doctor_provider_db -h localhost -W
# -W fragt nach Passwort

# Oder mit Passwort in einer Zeile (unsicher, nur für Tests!)
PGPASSWORD='dein_passwort' psql -U benutzer_name -d doctor_provider_db
```

### Passwortlose Authentifizierung einrichten (Development)

**⚠️ Nur für lokale Development-Umgebung!**

```bash
# 1. Finde deine pg_hba.conf Datei
psql -c "SHOW hba_file;"

# 2. Bearbeite die Datei (z.B. via nano oder vim)
# Ändere die Methode von 'md5' oder 'scram-sha-256' zu 'trust' für localhost

# Beispiel-Zeile in pg_hba.conf:
# local   all   all   trust
# host    all   all   127.0.0.1/32   trust

# 3. Starte PostgreSQL neu
brew services restart postgresql@14
```

---

## 7. Häufige Problemlösungen

### Problem: "role 'postgres' does not exist"
**Lösung:**
```bash
# Finde deinen aktuellen Benutzer
whoami

# Nutze diesen Benutzer für PostgreSQL
psql -U $(whoami) postgres

# Oder erstelle den Benutzer 'postgres'
psql postgres -c "CREATE USER postgres WITH SUPERUSER PASSWORD 'dein_passwort';"
```

### Problem: "connection refused"
**Lösung:**
```bash
# Prüfe, ob PostgreSQL läuft
brew services list

# Starte PostgreSQL
brew services start postgresql@14

# Prüfe den Port
lsof -i :5432
```

### Problem: "database does not exist"
**Lösung:**
```bash
# Erstelle die Datenbank
createdb doctor_provider_db

# Oder via SQL
psql postgres -c "CREATE DATABASE doctor_provider_db;"
```

### Problem: "Unknown database function 'uuid_generate_v4'" (IntelliJ)
**Lösung:**
```sql
-- Das ist nur eine IDE-Warnung!
-- Die Migration funktioniert zur Laufzeit mit Flyway.
-- Um die Warnung zu beheben:
-- 1. Konfiguriere die Data Source in IntelliJ (siehe Abschnitt 9)
-- 2. Verknüpfe die SQL-Datei mit der Data Source
```

### Problem: Flyway-Migration schlägt fehl
**Lösung:**
```bash
# Prüfe Flyway-Status
psql doctor_provider_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"

# Bei checksum-Problemen (NUR in Development!):
psql doctor_provider_db -c "DELETE FROM flyway_schema_history WHERE script = 'V1__Create_speciality_table.sql';"

# Oder nutze Flyway Repair (empfohlen):
# Füge in application.properties ein: spring.flyway.repair=true
# Starte die App, entferne danach die Zeile wieder
```

---

## 8. Für dein Projekt

### Projektspezifische Befehle

**Datenbanken für alle Services erstellen:**
```bash
createdb doctor_provider_db
createdb patient_customer_db
```

**Verbindung testen:**
```bash
psql doctor_provider_db -c "SELECT version();"
```

**Migration-Status prüfen (nach Spring Boot Start):**
```bash
psql doctor_provider_db -c "SELECT * FROM flyway_schema_history;"
```

**Alle Tabellen im doctor-provider Service anzeigen:**
```bash
psql doctor_provider_db -c "\dt"
```

**Daten aus Speciality-Tabelle anzeigen:**
```bash
psql doctor_provider_db -c "SELECT * FROM speciality;"
```

---

## 9. IntelliJ Data Source Konfiguration

### PostgreSQL-Verbindung in IntelliJ einrichten

1. **Database Tool Window öffnen:**
   - `⌘` + `Shift` + `D`
   - Oder: **View** → **Tool Windows** → **Database**

2. **Data Source hinzufügen:**
   - Klicke auf **"+"** Symbol
   - Wähle **Data Source** → **PostgreSQL**

3. **Verbindungsdaten eingeben:**
   - **Name:** `doctor_provider_db`
   - **Host:** `localhost`
   - **Port:** `5432`
   - **Database:** `doctor_provider_db`
   - **User:** dein PostgreSQL-Benutzer
   - **Password:** dein Passwort

4. **Treiber herunterladen:**
   - Klicke auf **Download missing driver files**

5. **Verbindung testen:**
   - Klicke auf **Test Connection**
   - Bei Erfolg: **"Succeeded"**
   - Klicke auf **Apply** und **OK**

6. **SQL-Datei mit Data Source verknüpfen:**
   - Öffne deine Migration-Datei (z.B. `V1__Create_speciality_table.sql`)
   - Oben in der Datei siehst du eine Dropdown-Liste
   - Wähle deine Data Source aus: `doctor_provider_db`

---

## 10. Nützliche SQL-Befehle für Entwicklung

### Tabelle leeren (Daten löschen, Struktur behalten)
```sql
TRUNCATE TABLE speciality RESTART IDENTITY CASCADE;
```

### Tabelle löschen
```sql
DROP TABLE IF EXISTS speciality CASCADE;
```

### Testdaten einfügen
```sql
INSERT INTO speciality (name) VALUES 
    ('Allgemeinmedizin'),
    ('Kardiologie'),
    ('Dermatologie'),
    ('Orthopädie');
```

### Spalte hinzufügen
```sql
ALTER TABLE speciality ADD COLUMN description TEXT;
```

### Extension aktivieren (für UUID)
```sql
-- Empfohlen: uuid-ossp Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Alternative: pgcrypto Extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- UUID generieren testen
SELECT uuid_generate_v4();  -- mit uuid-ossp
SELECT gen_random_uuid();   -- mit pgcrypto (PostgreSQL 13+)
```

### Aktive Verbindungen anzeigen
```sql
SELECT * FROM pg_stat_activity WHERE datname = 'doctor_provider_db';
```

---

## 11. Best Practices

### ✅ Empfohlener Workflow

1. **PostgreSQL Server starten:**
   ```bash
   brew services start postgresql@14
   ```

2. **Datenbanken erstellen:**
   ```bash
   createdb doctor_provider_db
   createdb patient_customer_db
   ```

3. **IntelliJ Data Sources konfigurieren:**
   - Siehe Abschnitt 9

4. **Spring Boot App starten:**
   - Flyway führt automatisch Migrationen aus
   - Tabellen werden erstellt

5. **Entwicklung:**
   - Nutze IntelliJ Database Tool für Abfragen
   - Nutze pgAdmin für komplexere Operationen
   - Nutze Terminal für schnelle Checks

### ⚠️ Wichtige Hinweise

- **Niemals Production-Daten** in Development-Umgebung nutzen
- **Backup vor größeren Änderungen:** 
  ```bash
  pg_dump doctor_provider_db > backup.sql
  # Restore: psql doctor_provider_db < backup.sql
  ```
- **Migration-Dateien niemals ändern** nach dem Commit (Flyway-Regel)
- **Flyway-Migrationen reparieren** bei Problemen:
  ```bash
  # In application.properties setzen:
  # spring.flyway.repair=true
  # Nach dem Start wieder entfernen!
  ```
- **Sensible Daten** in `.env` oder `application-local.properties` (nicht in Git)
- **Port 5432** muss frei sein (prüfe mit `lsof -i :5432`)

---

## Schnellreferenz

```bash
# Server
brew services start postgresql@14    # Starten
brew services stop postgresql@14     # Stoppen  
brew services restart postgresql@14  # Neustarten

# Datenbanken
psql -l                              # Liste anzeigen
createdb dbname                      # Erstellen
dropdb dbname                        # Löschen
psql dbname                          # Verbinden

# In psql
\l                                   # Datenbanken
\dt                                  # Tabellen
\d tablename                         # Schema
\q                                   # Beenden

# Abfragen
SELECT * FROM tablename;             # Alle Daten
SELECT COUNT(*) FROM tablename;      # Anzahl
```

