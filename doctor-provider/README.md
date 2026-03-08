# 🏥 Doctor Provider Service

Provider-Service für das Terminbuchungssystem.

## 🚀 Quick Start

### **Projekt bauen:**

```bash
mvn clean install
```

### **Code formatieren (wichtig!):**

```bash
mvn spotless:apply
```

### **Anwendung starten:**

```bash
mvn spring-boot:run
```

---

## 📚 Dokumentation

|                            Dokument                            |             Beschreibung              |
|----------------------------------------------------------------|---------------------------------------|
| [GIT_HOOKS_KOMPLETT.md](./GIT_HOOKS_KOMPLETT.md)               | 🪝 Git Hooks - Vollständige Anleitung |
| [SPOTLESS_KOMPLETT.md](./SPOTLESS_KOMPLETT.md)                 | 🎨 Spotless - Vollständige Anleitung  |
| [ENTITY_REPOSITORY_GUIDE.md](./ENTITY_REPOSITORY_GUIDE.md)     | 🗄️ JPA Entity & Repository Guide     |
| [MAPPER_GUIDE.md](./MAPPER_GUIDE.md)                           | 🔄 MapStruct Mapper Guide             |
| [DATABASE_SETUP_GUIDE.md](./DATABASE_SETUP_GUIDE.md)           | 🗃️ Datenbank Setup Guide             |
| [POSTGRESQL_COMPLETE_GUIDE.md](./POSTGRESQL_COMPLETE_GUIDE.md) | 🐘 PostgreSQL Guide                   |

---

## 🔧 Technologie-Stack

- **Java 25**
- **Spring Boot 4.0.1**
- **Spring WebFlux** (Reactive)
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL 17**
- **Flyway** (Migrations)
- **MapStruct** (Object Mapping)
- **Lombok** (Boilerplate Reduction)
- **OpenAPI 3.0** (API Spec-First)
- **Spotless** (Code Formatting)

---

## 📂 Projekt-Struktur (Hexagonale Architektur)

```
doctor-provider/
├── domain/                    # 🟢 Business-Logik (Kern)
│   ├── model/                # Domain-Modelle (POJOs)
│   ├── enums/                # Domain-Enums
│   └── service/              # Domain-Services (optional)
│
├── application/               # 🔵 Use-Cases
│   ├── port/
│   │   ├── incoming/        # Inbound-Ports (z.B. DoctorIncomingPort)
│   │   └── outgoing/        # Outbound-Ports (z.B. DoctorOutgoingPort)
│   └── service/             # Application-Services (Use-Case-Implementierung)
│
└── infrastructure/            # 🟡 Adapter (Technik)
    └── adapter/
        ├── incoming/         # REST-Controller (Web-Adapter)
        │   └── web/
        └── outgoing/         # Persistence-Adapter
            └── persistence/
                ├── entity/   # JPA-Entities
                ├── repository/ # Spring Data Repositories
                ├── mapper/   # Entity ↔ Domain Mapper
                └── adapter/  # Persistence-Adapter (Port-Implementierung)
```

---

## 🗃️ Datenbank

### **Tabellen:**

1. `city` - Städte (Stammdaten)
2. `speciality` - Fachrichtungen (Stammdaten)
3. `practice` - Praxen
4. `doctor` - Ärzte
5. `doctor_speciality` - n:m Join-Tabelle
6. `doctor_working_hours` - Arbeitszeiten
7. `slot` - Buchbare Termin-Slots

### **Migrations:**

```bash
src/main/resources/db/migration/
├── V1__Create_speciality_table.sql
├── V2__Create_city_table.sql
├── V3__Create_practice_table.sql
├── V4__Create_doctor_table.sql
├── V5__Create_doctor_speciality_table.sql
├── V6__Create_doctor_working_hours_table.sql
└── V7__Create_slot_table.sql
```

---

## 🎨 Code-Formatierung mit Spotless

### **Option 1: Automatisch (empfohlen!) 🚀**

**⚠️ WICHTIG: Erst einmalig aktivieren!**

```bash
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

**Danach:**
- ✅ **Bei `git commit`** → Code wird **automatisch** formatiert
- ✅ Du musst **nie wieder** manuell `mvn spotless:apply` ausführen

**Wichtig:**
- Der Hook ist **nur für dieses Projekt** aktiv, nicht global!
- Details: [HOOK_AKTIVIERUNG.md](./HOOK_AKTIVIERUNG.md)

---

### **Option 2: Manuell (falls Hook nicht gewünscht)**

**Vor jedem Commit:**

```bash
mvn spotless:apply
```

**Details:** [SPOTLESS_QUICKSTART.md](./SPOTLESS_QUICKSTART.md)

---

## 🧪 Tests

```bash
# Alle Tests
mvn test

# Nur Unit Tests
mvn test -Dtest=*Test

# Nur Integration Tests
mvn test -Dtest=*IT
```

---

## 📦 Build & Package

```bash
# Package erstellen
mvn clean package

# JAR ausführen
java -jar target/doctor-provider-0.0.1-SNAPSHOT.jar
```

---

## 🌐 API-Dokumentation

Nach dem Start erreichbar unter:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

---

## ⚙️ Konfiguration

Siehe `src/main/resources/application.properties`

---

## 🤝 Contributing

1. **Code schreiben**
2. **Formatieren:** `mvn spotless:apply`
3. **Testen:** `mvn test`
4. **Bauen:** `mvn clean install`
5. **Commit & Push**

---

## 📄 Lizenz

Copyright (C) 2026 Doctor Provider Team

---

## 👥 Team

Doctor Provider Team

---

## 📞 Support

Bei Fragen: Siehe Dokumentation im `./` Verzeichnis

