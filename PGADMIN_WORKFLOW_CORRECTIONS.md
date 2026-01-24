# pgAdmin Workflow - Korrekturen & Klarstellungen

**Datum:** 22. Januar 2026, 22:15 Uhr

---

## 🔍 Deine ursprünglichen Aussagen:

### ❌ Aussage 1: TEILWEISE FALSCH

**Du sagtest:**
> "Server starten: Rechtsklick auf server → register → server → Connection Tab → hostname, port, username, password → so wird der psql server gestartet"

**Was ist falsch:**
- ❌ **"Register Server" startet NICHT den PostgreSQL-Server!**
- ❌ Es startet nur die Verbindung zwischen pgAdmin und dem Server

**Richtig ist:**
- ✅ **PostgreSQL-Server muss VORHER via Terminal gestartet werden:**
  ```bash
  brew services start postgresql@14
  ```
- ✅ **"Register Server" in pgAdmin registriert nur die Verbindung**
- ✅ Es ist eine **einmalige Konfiguration** beim ersten Mal

---

### ✅ Aussage 2: FAST RICHTIG (kleine Korrektur)

**Du sagtest:**
> "DB erstellen: Server gestartet → Linksklick auf Server → Rechtsklick auf postgres → create → database → Name und Elemente definieren"

**Kleine Korrektur:**
- ⚠️ Nicht "Rechtsklick auf **postgres**" (das ist eine Datenbank)
- ✅ Sondern "Rechtsklick auf **Databases**" (das ist der Ordner)

**Richtig ist:**
1. ✅ Linksklick auf **Server** (zum Erweitern)
2. ✅ Linksklick auf **Databases** (zum Erweitern)
3. ✅ **Rechtsklick auf "Databases"** (der Ordner)
4. ✅ **Create** → **Database...**
5. ✅ Name und Elemente definieren
6. ✅ **Save** klicken

**Hinweis zu "postgres":**
- `postgres` ist die Standard-Maintenance-Datenbank
- Du erstellst deine eigenen DBs (z.B. `doctor_provider_db`) daneben
- Lösche niemals die `postgres` Datenbank!

---

## ✅ Korrigierter kompletter Workflow:

### Phase 1: PostgreSQL Server starten (Terminal)
```bash
# WICHTIG: Zuerst den Server starten!
brew services start postgresql@14

# Verifizieren
lsof -i :5432
```

### Phase 2: pgAdmin öffnen
```bash
# Via Terminal
open -a pgAdmin\ 4

# Oder via Launchpad
# Launchpad → pgAdmin 4 → Master-Passwort eingeben
```

### Phase 3: Server in pgAdmin registrieren (nur 1x beim ersten Mal)
1. **Servers** → **Rechtsklick** → **Register** → **Server**
2. **General Tab:**
   - Name: `Local PostgreSQL`
3. **Connection Tab:**
   - Host: `localhost`
   - Port: `5432`
   - Username: dein Username (z.B. aus `whoami`)
   - Password: dein Passwort
   - ✅ Save password aktivieren
4. **Save** klicken

**Ergebnis:** Server erscheint mit **grünem Symbol** (verbunden)

### Phase 4: Datenbank erstellen
1. **Servers** → **Local PostgreSQL** → **Databases**
2. **Rechtsklick auf "Databases"** (der Ordner!)
3. **Create** → **Database...**
4. **General Tab:**
   - Database: `doctor_provider_db`
   - Owner: dein Username
5. **Save** klicken

**Ergebnis:** Neue DB erscheint unter Databases

### Phase 5: Tabellen erstellen (via Flyway)
1. SQL-Migrations in `src/main/resources/db/migration/` erstellen
2. Spring Boot starten
3. Flyway erstellt Tabellen automatisch
4. In pgAdmin: **Refresh** → Tabellen erscheinen unter **Tables**

---

## 📊 Wichtige Unterscheidungen:

| Begriff | Was es ist | Was es NICHT ist |
|---------|-----------|------------------|
| **PostgreSQL Server** | Der Datenbank-Dienst (läuft im Hintergrund) | Nicht pgAdmin! |
| **pgAdmin** | GUI-Tool zur Verwaltung | Startet NICHT den Server! |
| **Register Server** | Verbindung zu laufendem Server konfigurieren | Startet NICHT den Server! |
| **postgres** | Standard-Maintenance-Datenbank | Nicht der Ordner für neue DBs! |
| **Databases** | Ordner für alle Datenbanken | Hier neue DBs erstellen! |

---

## 🎯 Wichtigste Erkenntnisse:

### 1. **pgAdmin startet NICHT den PostgreSQL-Server!**
- pgAdmin ist nur ein **Verwaltungs-Tool** (GUI)
- PostgreSQL muss **separat via Terminal gestartet** werden
- Vergleich: pgAdmin = Fernbedienung, PostgreSQL = Fernseher
  - Die Fernbedienung kann den Fernseher nicht einschalten, wenn er keinen Strom hat!

### 2. **"Register Server" ist eine einmalige Konfiguration**
- Beim ersten Mal musst du die Verbindung einrichten
- Danach bleibt die Konfiguration gespeichert
- Bei jedem pgAdmin-Start verbindet es automatisch (wenn Server läuft)

### 3. **Neue Datenbanken erstellt man im "Databases"-Ordner**
- Nicht auf "postgres" rechtsklicken
- Sondern auf den "Databases"-Ordner
- "postgres" ist selbst eine Datenbank, nicht der Container

### 4. **Typischer Fehler:**
- ❌ pgAdmin öffnen → "Connection refused" Fehler
- **Ursache:** PostgreSQL läuft nicht!
- **Lösung:** Erst `brew services start postgresql@14` im Terminal

---