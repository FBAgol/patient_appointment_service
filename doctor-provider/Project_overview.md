# Terminbuchungssystem – Fachliche & Technische Dokumentation

## 1. Einordnung des Projekts

Dieses Projekt bildet ein vereinfachtes, aber realistisches
Terminbuchungssystem für Fachärzte ab.

Patienten können:
- sich registrieren und anmelden (JWT-Authentifizierung)
- nach Facharzt und Stadt suchen
- einen Arzt auswählen
- einen einstündigen Termin buchen
- eine Bestätigungs-E-Mail erhalten

Das System ist in **zwei Services** aufgeteilt:

- **Provider-Service**: stellt medizinische Angebote bereit
- **Consumer-Service**: verarbeitet Buchungen und Patienteninteraktionen

Datenbank: **PostgreSQL**  
Architektur: **Hexagonale Architektur**  
API: **OpenAPI (Spec-First)**

---

## 2. Provider-Service – Datenbank & Domänenmodell

Der Provider-Service verwaltet **alle angebotsbezogenen Daten**.
Er kennt **keine Patienten** und verarbeitet **keine Buchungen**.

### 2.1 Tabelle `city`

#### Fachliche Bedeutung

Repräsentiert eine Stadt, in der sich Arztpraxen befinden.
Sie dient als Filterkriterium für die Arztsuche.

#### Technische Erklärung

| Attribut |     Typ      |                  Erklärung                  |
|----------|--------------|---------------------------------------------|
| id       | BIGINT       | Eindeutiger technischer Schlüssel           |
| name     | VARCHAR(255) | Name der Stadt (z. B. „Berlin“)             |
| zip_code | VARCHAR(20)  | Postleitzahl, String wegen führender Nullen |

#### Beziehungen

- Eine Stadt kann **mehrere Praxen** haben
- Beziehung: `city (1) → practice (n)`

---

### 2.2 Tabelle `specialty`

#### Fachliche Bedeutung

Beschreibt eine medizinische Fachrichtung wie:
- Kardiologie
- Dermatologie
- Orthopädie

#### Technische Erklärung

| Attribut |     Typ      |       Erklärung       |
|----------|--------------|-----------------------|
| id       | BIGINT       | Primärschlüssel       |
| name     | VARCHAR(255) | Name der Fachrichtung |

#### Beziehungen

- Ärzte können **mehrere Fachrichtungen** haben
- Beziehung über Join-Tabelle `doctor_specialty`

---

### 2.3 Tabelle `practice`

#### Fachliche Bedeutung

Eine Praxis ist der physische Ort, an dem Ärzte arbeiten.
Mehrere Ärzte können in einer Praxis tätig sein.

#### Technische Erklärung

|   Attribut   |     Typ      |                    Erklärung                    |
|--------------|--------------|-------------------------------------------------|
| id           | BIGINT       | Primärschlüssel                                 |
| name         | VARCHAR(255) | Name der Praxis                                 |
| street       | VARCHAR(255) | Straßenname (z. B. „Hauptstraße")               |
| house_number | VARCHAR(20)  | Hausnummer inkl. Zusätze (z. B. „42a", „15-17") |
| phone        | VARCHAR(50)  | Telefonnummer                                   |
| city_id      | BIGINT       | Verweis auf Stadt                               |

#### Beziehungen

- Eine Praxis gehört **genau zu einer Stadt**
- Eine Praxis hat **mehrere Ärzte**

#### Adressaufbau

Die vollständige Adresse setzt sich zusammen aus:
- Straße + Hausnummer (z. B. „Hauptstraße 42a")
- PLZ + Stadt (aus verknüpfter `city`-Tabelle)

---

### 2.4 Tabelle `doctor`

#### Fachliche Bedeutung

Repräsentiert einen einzelnen Arzt, der Termine anbietet.

#### Technische Erklärung

|  Attribut   |     Typ      |     Erklärung     |
|-------------|--------------|-------------------|
| id          | BIGINT       | Primärschlüssel   |
| first_name  | VARCHAR(255) | Vorname           |
| last_name   | VARCHAR(255) | Nachname          |
| practice_id | BIGINT       | Zugehörige Praxis |

#### Beziehungen

- Arzt → Praxis (n:1)
- Arzt → Fachrichtungen (n:m)
- Arzt → Arbeitszeiten (1:n)
- Arzt → Slots (1:n)

---

### 2.5 Tabelle `doctor_specialty`

#### Fachliche Bedeutung

Verknüpft Ärzte mit ihren Fachrichtungen.
Ein Arzt kann mehrere Spezialisierungen haben.

#### Technische Erklärung

|   Attribut   |  Typ   |         Erklärung         |
|--------------|--------|---------------------------|
| doctor_id    | BIGINT | Referenz auf Arzt         |
| specialty_id | BIGINT | Referenz auf Fachrichtung |

#### Beziehungen

- n:m Beziehung zwischen Arzt und Fachrichtung
- Keine eigene ID nötig (Composite Key)

---

### 2.6 Tabelle `doctor_working_hours`

#### Fachliche Bedeutung

Definiert, **wann ein Arzt grundsätzlich arbeitet**.
Diese Daten werden verwendet, um Termin-Slots zu generieren.

#### Technische Erklärung

|  Attribut  |  Typ   |       Erklärung        |
|------------|--------|------------------------|
| id         | BIGINT | Primärschlüssel        |
| doctor_id  | BIGINT | Zugehöriger Arzt       |
| weekday    | INT    | 1=Montag bis 7=Sonntag |
| start_time | TIME   | Beginn der Arbeit      |
| end_time   | TIME   | Ende der Arbeit        |

#### Fachliche Logik

- Kein Termin außerhalb dieser Zeiten
- Grundlage für Slot-Generierung

---

### 2.7 Tabelle `slot`

#### Fachliche Bedeutung

Ein Slot ist **ein konkreter, buchbarer Termin**.
Jeder Slot dauert exakt **1 Stunde**.

Slots werden aus den Arbeitszeiten erzeugt.

#### Technische Erklärung

|  Attribut  |     Typ     |            Erklärung            |
|------------|-------------|---------------------------------|
| id         | BIGINT      | Primärschlüssel                 |
| doctor_id  | BIGINT      | Zugehöriger Arzt (FK zu doctor) |
| start_time | TIMESTAMP   | Startzeitpunkt                  |
| end_time   | TIMESTAMP   | Endzeitpunkt                    |
| status     | VARCHAR(20) | FREE, RESERVED, BOOKED          |

#### Beziehungen

- **doctor → slot (1:n)**
- Ein Arzt hat viele Slots
- Foreign Key: `doctor_id` → `doctor(id)` mit CASCADE DELETE

#### Fachliche Logik

- FREE → sichtbar & buchbar
- RESERVED → temporär blockiert (z.B. während Buchungsprozess)
- BOOKED → final vergeben
- Slots müssen innerhalb der `doctor_working_hours` liegen
- Ein Slot wird nur generiert, wenn der Arzt zu dieser Zeit arbeitet

#### Abhängigkeit zwischen Tabellen

```
doctor_working_hours (Template)
         ↓ (generiert)
       slot (konkrete Termine)
```

**Beispiel:**
- Arbeitszeit: Montag 09:00-12:00
- Generierte Slots: 09:00-10:00, 10:00-11:00, 11:00-12:00

---

## 2.8 Beziehungsanalyse: doctor ↔ doctor_working_hours ↔ slot

### Fachliche Beziehungen

```
                ┌─────────────────┐
                │     doctor      │
                │   (1 Arzt)      │
                └────────┬────────┘
                         │
            ┌────────────┴────────────┐
            │                         │
            │ 1:n                     │ 1:n
            │                         │
            ▼                         ▼
┌───────────────────────┐   ┌──────────────────┐
│ doctor_working_hours  │   │      slot        │
│   (Arbeitszeiten)     │   │  (Buchbare       │
│                       │   │   Termine)       │
│ - Wochentag           │   │                  │
│ - Start/Ende (Zeit)   │   │ - Start/Ende     │
│                       │   │   (Timestamp)    │
│ TEMPLATE-Daten        │   │ - Status         │
└───────────────────────┘   │                  │
            │               │ KONKRETE Daten   │
            │               └──────────────────┘
            │
            └───────► (generiert) ───────►
```

## 3. Consumer-Service – Datenbank & Domänenmodell

Der Consumer-Service verwaltet **Patienteninteraktionen**.
Er kennt keine Details über Praxen oder Arbeitszeiten.

---

### 3.1 Tabelle `patient`

#### Fachliche Bedeutung

Patientendatensatz für Authentifizierung und Buchungsverwaltung.

#### Technische Erklärung

|   Attribut    |     Typ      |          Erklärung          |
|---------------|--------------|-----------------------------|
| id            | BIGINT       | Primärschlüssel             |
| email         | VARCHAR(255) | E-Mail-Adresse (UNIQUE)     |
| first_name    | VARCHAR(255) | Vorname                     |
| last_name     | VARCHAR(255) | Nachname                    |
| password_hash | VARCHAR(255) | Gehashtes Passwort (BCrypt) |
| created_at    | TIMESTAMP    | Zeitpunkt der Registrierung |

#### Beziehungen

- Ein Patient kann **mehrere Buchungen** haben
- Beziehung: `patient (1) → booking (n)`

---

### 3.2 Tabelle `booking`

#### Fachliche Bedeutung

Repräsentiert eine Terminbuchung eines Patienten.

#### Technische Erklärung

|  Attribut  |     Typ     |             Erklärung             |
|------------|-------------|-----------------------------------|
| id         | BIGINT      | Primärschlüssel                   |
| patient_id | BIGINT      | Buchender Patient (FK zu patient) |
| doctor_id  | BIGINT      | Arzt-ID aus Provider-Service      |
| slot_id    | BIGINT      | Slot-ID aus Provider-Service      |
| status     | VARCHAR(30) | INITIATED, CONFIRMED, CANCELLED   |
| created_at | TIMESTAMP   | Buchungszeitpunkt                 |

#### Fachliche Logik

- INITIATED → Prozess gestartet
- CONFIRMED → Slot gebucht, Bestätigung versendet
- CANCELLED → Buchung abgebrochen

#### Hinweis zur Microservice-Architektur

`doctor_id` und `slot_id` sind **externe Referenzen** auf Entities im Provider-Service.
Es gibt **keine Foreign Key Constraints** über Service-Grenzen hinweg.
Die Datenkonsistenz wird auf Anwendungsebene sichergestellt.

---

### 3.3 Tabelle `email_log` (optional)

#### Fachliche Bedeutung

Protokolliert versendete Bestätigungs-E-Mails für Nachvollziehbarkeit,
Debugging und Compliance.

#### Technische Erklärung

|    Attribut     |     Typ      |                Erklärung                |
|-----------------|--------------|-----------------------------------------|
| id              | BIGINT       | Primärschlüssel                         |
| booking_id      | BIGINT       | Zugehörige Buchung (FK zu booking)      |
| recipient_email | VARCHAR(255) | Empfänger-Adresse                       |
| subject         | VARCHAR(500) | E-Mail-Betreff                          |
| sent_at         | TIMESTAMP    | Versandzeit (DEFAULT CURRENT_TIMESTAMP) |
| status          | VARCHAR(50)  | SENT, FAILED oder PENDING               |
| error_message   | TEXT         | Fehlermeldung bei Versandproblemen      |

#### Beziehungen

- `booking_id` → Foreign Key zu `booking(id)` mit CASCADE DELETE
- Bei Löschung einer Buchung werden zugehörige E-Mail-Logs mit gelöscht

#### Fachliche Logik

- **PENDING**: E-Mail-Versand wird vorbereitet
- **SENT**: E-Mail erfolgreich versendet
- **FAILED**: Versand fehlgeschlagen (Grund in `error_message`)

#### Indizes für Performance

```sql
CREATE INDEX idx_email_log_booking ON email_log(booking_id);
CREATE INDEX idx_email_log_status ON email_log(status);
```

---

## 4. Authentifizierung & Sicherheit

### 4.1 Authentifizierungskonzept für Testprojekt

Dieses System implementiert eine **einfache JWT-basierte Authentifizierung** ohne E-Mail-Verifikation.

**Wichtiger Hinweis zu E-Mail-Verifikation:**
- ❌ In diesem Testprojekt wird **keine E-Mail-Verifikation** bei der Registrierung durchgeführt
- ❌ Patienten können sich direkt anmelden und Termine buchen
- ✅ Für Produktionssysteme **sollte** E-Mail-Verifikation implementiert werden
- 📖 Siehe `Email_Verification_Guide.md` für eine vollständige Implementierungsanleitung
- 📖 Siehe `Email_Service_Implementation_Guide.md` für E-Mail-Versand nach Buchung
- 📖 Siehe `Email_Service_With_Kafka.md` für produktionsreife Kafka-Architektur

#### Warum keine E-Mail-Verifikation in diesem Testprojekt?

1. Fokus liegt auf Terminbuchungs-Logik
2. Vereinfachter Test-Workflow (keine E-Mail-Bestätigung nötig)
3. Schnellere lokale Entwicklung
4. E-Mail-Verifikation kann später einfach hinzugefügt werden

#### API-Endpoints

**Registrierung:**

```
POST /api/v1/auth/register

Request:
{
  "email": "patient@example.com",
  "password": "securePassword123",
  "firstName": "Max",
  "lastName": "Mustermann"
}

Response (201 Created):
{
  "message": "Registrierung erfolgreich",
  "userId": 1
}
```

**Login:**

```
POST /api/v1/auth/login

Request:
{
  "email": "patient@example.com",
  "password": "securePassword123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "userId": 1
}
```

**Geschützte Endpoints:**

```
GET /api/v1/patient/bookings
Header: Authorization: Bearer {token}

Response (200 OK):
[
  {
    "id": 1,
    "doctorName": "Dr. Müller",
    "startTime": "2026-01-20T10:00:00",
    "status": "CONFIRMED"
  }
]
```

### 4.2 Technische Umsetzung

#### Komponenten:

- **Spring Security**: Basis-Framework für Authentifizierung
- **JWT (JSON Web Token)**: Stateless Token für API-Zugriff
- **BCrypt**: Password-Hashing-Algorithmus
- **Filter Chain**: JWT-Validierung bei jedem Request

#### Ablauf:

1. Patient registriert sich → Passwort wird gehasht und gespeichert
2. Patient meldet sich an → Credentials werden geprüft, JWT wird generiert
3. Patient sendet Anfragen → JWT wird validiert, User-ID wird extrahiert
4. Zugriff nur auf eigene Ressourcen

### 4.3 Sicherheitsmaßnahmen

**Umgesetzt in diesem Projekt:**
- ✅ BCrypt Password-Hashing (automatischer Salt)
- ✅ JWT mit HMAC SHA-256 Signatur
- ✅ Unique Email-Constraint
- ✅ Input-Validierung (Bean Validation)
- ✅ CORS-Konfiguration
- ✅ SQL-Injection Schutz (JPA/Hibernate)

**Nicht implementiert (für Testprojekt nicht erforderlich):**
- ❌ E-Mail-Verifikation (Link-basiert)
- ❌ E-Mail-Verifikation (Code-basiert)
- ❌ Passwort-Zurücksetzen
- ❌ Rate Limiting
- ❌ CAPTCHA
- ❌ 2FA

### 4.4 Alternative: Code-basierte E-Mail-Verifikation

Für Produktionssysteme gibt es zwei Hauptansätze zur E-Mail-Verifikation:

#### Option 1: Link-basierte Verifikation (Token in URL)

**Vorteile:**
- ✅ Ein Klick genügt
- ✅ Benutzerfreundlicher

**Nachteile:**
- ❌ Token kann in Browser-Historie/Logs erscheinen
- ❌ Anfällig für Phishing (Link kann gefälscht werden)

**Implementierung:**

```
E-Mail enthält: https://app.com/verify?token=abc123xyz
```

#### Option 2: Code-basierte Verifikation (6-stelliger Code)

**Vorteile:**
- ✅ Sicherer (Code ist nicht in URL)
- ✅ Kürzere Codes (z.B. 6 Ziffern)
- ✅ Weniger anfällig für Link-Manipulation

**Nachteile:**
- ❌ Benutzer muss Code manuell eingeben
- ❌ Zusätzlicher Eingabeschritt

**Implementierung:**

```
E-Mail enthält: Ihr Verifizierungscode: 492837
Benutzer gibt Code in App ein
```

**Datenbank-Anpassung für Code-Verifikation:**

```sql
ALTER TABLE patient 
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN verification_code VARCHAR(6),
ADD COLUMN verification_code_expiry TIMESTAMP;

CREATE INDEX idx_patient_verification_code ON patient(verification_code);
```

**Workflow:**
1. Patient registriert sich
2. System generiert 6-stelligen Code (z.B. mit `Random`)
3. Code wird per E-Mail versendet
4. Patient gibt Code in App ein
5. System prüft Code und Ablaufzeit
6. `email_verified` wird auf `true` gesetzt

📖 **Detaillierte Implementierung siehe `Email_Verification_Guide.md`**

### 4.5 Datenbank-Migration

**SQL-Script für Patient-Authentifizierung:**

```sql
-- Spalte für Passwort hinzufügen
ALTER TABLE patient ADD COLUMN password_hash VARCHAR(255) NOT NULL;

-- Unique Index auf Email erstellen
CREATE UNIQUE INDEX idx_patient_email ON patient(email);
```

---

