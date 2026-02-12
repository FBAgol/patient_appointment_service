# 🎨 Spotless Code-Formatierung Guide

## 📋 Was ist Spotless?

**Spotless** ist ein Maven/Gradle Plugin für automatische Code-Formatierung.

### ✅ Vorteile:

- ✅ **Konsistente Code-Formatierung** im gesamten Team
- ✅ **Automatisches Formatieren** beim Build
- ✅ **Ungenutzte Imports entfernen**
- ✅ **Import-Reihenfolge standardisieren**
- ✅ **CI/CD Integration** (verhindert unformattierten Code)
- ✅ **Google Java Format** Standard (oder Eclipse/Prettier)

---

## 🚀 Installation

Spotless wurde bereits in der `pom.xml` konfiguriert! ✅

### Konfiguration in `pom.xml`:

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>${spotless.version}</version>
    <configuration>
        <java>
            <!-- Google Java Format -->
            <googleJavaFormat>
                <version>1.19.2</version>
                <style>GOOGLE</style>
            </googleJavaFormat>

            <!-- Entfernt ungenutzte Imports -->
            <removeUnusedImports/>

            <!-- Import-Reihenfolge -->
            <importOrder>
                <order>java,jakarta,org,com,test</order>
            </importOrder>

            <!-- Ausschlüsse -->
            <excludes>
                <exclude>**/target/**</exclude>
                <exclude>**/generated-sources/**</exclude>
            </excludes>
        </java>
    </configuration>
</plugin>
```

---

## 📝 Wichtige Maven-Befehle

### **1. Code formatieren (automatisch korrigieren)**

```bash
mvn spotless:apply
```

**Was macht das?**
- ✅ Formatiert alle `.java` Dateien
- ✅ Entfernt ungenutzte Imports
- ✅ Sortiert Imports
- ✅ Wendet Google Java Format an

**Wann verwenden?**
- Vor jedem Commit
- Nach größeren Code-Änderungen
- Wenn IntelliJ Warnings anzeigt

---

### **2. Formatierung prüfen (ohne zu ändern)**

```bash
mvn spotless:check
```

**Was macht das?**
- ❌ **Ändert NICHTS**
- ✅ Prüft nur, ob Code formatiert ist
- ✅ Exit-Code 1, wenn unformatiert (für CI/CD)

**Wann verwenden?**
- In CI/CD Pipelines
- Vor dem Push
- Um zu testen, ob alle Dateien formatiert sind

**Ausgabe bei Fehlern:**

```
[ERROR] The following files had format violations:
[ERROR]     src/main/java/test/doctor_provider/infrastructure/adapter/outgoing/persistence/entity/DoctorEntity.java
[ERROR] Run 'mvn spotless:apply' to fix
```

---

### **3. Formatierung in Maven Build integrieren**

```bash
mvn clean install
```

**Was passiert?**
- ✅ Spotless prüft automatisch beim Build (`validate` Phase)
- ✅ Build **schlägt fehl**, wenn Code nicht formatiert ist
- ⚠️ Um zu fixen: `mvn spotless:apply` ausführen

---

## 🔧 IntelliJ IDEA Integration

### **Option 1: Google Java Format Plugin installieren**

1. **IntelliJ IDEA** → **Settings/Preferences**
2. **Plugins** → Suche nach **"google-java-format"**
3. **Installieren** und **Restart IDE**
4. **Settings** → **Other Settings** → **google-java-format Settings**
   - ✅ Enable google-java-format
   - ✅ Style: **Google**

**Vorteil:** Code wird beim Speichern automatisch formatiert! ✨

---

### **Option 2: Maven Goals in IntelliJ ausführen**

1. **Maven** Tool Window öffnen (rechts)
2. **doctor-provider** → **Plugins** → **spotless**
3. Doppelklick auf:
   - `spotless:apply` → Code formatieren
   - `spotless:check` → Nur prüfen

**Oder:** Terminal in IntelliJ öffnen und `mvn spotless:apply` ausführen

---

## 🎯 Workflow-Empfehlung

### **Vor dem Commit:**

```bash
# 1. Code formatieren
mvn spotless:apply

# 2. Prüfen, ob alles geklappt hat
mvn spotless:check

# 3. Build durchführen
mvn clean install

# 4. Commit & Push
git add .
git commit -m "feat: add DoctorEntity"
git push
```

---

### **Beim Code Review:**

Wenn jemand unformatierten Code pusht:

```bash
# Reviewer:
git checkout feature/xyz
mvn spotless:apply
mvn clean install
# Wenn OK: Merge
```

---

## 📂 Was wird formatiert?

|         Datei-Typ         |        Formatierung        |
|---------------------------|----------------------------|
| `**/*.java`               | ✅ Google Java Format       |
| `pom.xml`                 | ✅ XML Sortierung           |
| `**/*.md`                 | ✅ Markdown Formatierung    |
| `**/target/**`            | ❌ Ausgeschlossen           |
| `**/generated-sources/**` | ❌ Ausgeschlossen (OpenAPI) |

---

## 🚨 Häufige Probleme

### **Problem 1: "spotless:check failed" im Build**

**Lösung:**

```bash
mvn spotless:apply
```

---

### **Problem 2: "Cannot find google-java-format"**

**Lösung:**
Maven lädt es automatisch beim ersten Ausführen.

```bash
mvn spotless:apply
# Wartet ab, bis Dependencies geladen sind
```

---

### **Problem 3: Formatierung rückgängig machen**

**Lösung:**

```bash
# Falls Spotless etwas kaputt gemacht hat:
git checkout -- src/main/java/test/doctor_provider/...
# Oder kompletten Branch resetten:
git reset --hard HEAD
```

---

## 🎨 Formatierungs-Beispiele

### **Vorher (unformatiert):**

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet; // Ungenutzt!

@Entity
@Table(name="doctor")  // Inkonsistente Spacing
@Data
public class DoctorEntity{
private UUID id;
    private String firstName;
  private String lastName; // Inkonsistente Einrückung
}
```

### **Nachher (mit Spotless formatiert):**

```java
package test.doctor_provider.infrastructure.adapter.outgoing.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "doctor")
@Data
public class DoctorEntity {
  private UUID id;
  private String firstName;
  private String lastName;
}
```

**Änderungen:**
- ✅ Imports sortiert (java → jakarta → lombok)
- ✅ Ungenutzter Import (`HashSet`) entfernt
- ✅ Konsistente Einrückung (2 Spaces nach Google Style)
- ✅ Leerzeile nach Package-Deklaration
- ✅ Spacing bei Annotationen (`name = "doctor"`)

---

## 🔗 CI/CD Integration

### **GitHub Actions Beispiel:**

```yaml
name: Build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '25'

      # Formatierung prüfen
      - name: Check code formatting
        run: mvn spotless:check

      # Build nur, wenn formatiert
      - name: Build with Maven
        run: mvn clean install
```

---

## 📖 Weitere Ressourcen

- **Spotless GitHub:** https://github.com/diffplug/spotless
- **Google Java Format:** https://github.com/google/google-java-format
- **Maven Plugin Docs:** https://github.com/diffplug/spotless/tree/main/plugin-maven

---

## ✅ Zusammenfassung

|        Befehl        |         Zweck          |
|----------------------|------------------------|
| `mvn spotless:apply` | Code formatieren (FIX) |
| `mvn spotless:check` | Nur prüfen (kein FIX)  |
| `mvn clean install`  | Build + Auto-Check     |

**Best Practice:**
1. **Vor jedem Commit:** `mvn spotless:apply`
2. **In CI/CD:** `mvn spotless:check`
3. **IntelliJ Plugin** installieren für Auto-Formatierung beim Speichern

🎉 Viel Erfolg mit sauberem Code!

