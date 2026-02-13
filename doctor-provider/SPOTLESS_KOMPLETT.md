# 🎨 Spotless Code-Formatierung - Vollständige Anleitung

## 📋 Inhaltsverzeichnis

1. [Was ist Spotless?](#was-ist-spotless)
2. [Schnellstart](#schnellstart)
3. [Installation & Konfiguration](#installation--konfiguration)
4. [Maven-Befehle](#maven-befehle)
5. [IntelliJ IDEA Integration](#intellij-idea-integration)
6. [Java 25 Kompatibilität](#java-25-kompatibilität)
7. [Workflow-Empfehlung](#workflow-empfehlung)
8. [Was wird formatiert?](#was-wird-formatiert)
9. [Formatierungs-Beispiele](#formatierungs-beispiele)
10. [Häufige Probleme](#häufige-probleme)
11. [CI/CD Integration](#cicd-integration)
12. [Eclipse vs. Google Java Format](#eclipse-vs-google-java-format)

---

## 📋 Was ist Spotless?

**Spotless** ist ein Maven/Gradle Plugin für automatische Code-Formatierung.

### ✅ Vorteile:

- ✅ **Konsistente Code-Formatierung** im gesamten Team
- ✅ **Automatisches Formatieren** beim Build
- ✅ **Ungenutzte Imports entfernen**
- ✅ **Import-Reihenfolge standardisieren**
- ✅ **CI/CD Integration** (verhindert unformattierten Code)
- ✅ **Eclipse Formatter** Standard (kompatibel mit Java 25)

---

## ⚡ Schnellstart

### **Code formatieren (vor dem Commit):**

```bash
mvn spotless:apply
```

### **Prüfen, ob Code formatiert ist:**

```bash
mvn spotless:check
```

### **Standard-Workflow:**

```bash
# 1. Code schreiben
# ...

# 2. Formatieren
mvn spotless:apply

# 3. Build
mvn clean install

# 4. Commit
git add .
git commit -m "feat: add entity"
git push
```

---

## 🚀 Installation & Konfiguration

Spotless wurde bereits in der `pom.xml` konfiguriert! ✅

### **Aktuelle Konfiguration in `pom.xml`:**

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>${spotless.version}</version>
    <configuration>
        <java>
            <!-- Eclipse Formatter (kompatibel mit Java 25) -->
            <eclipse>
                <version>4.26.0</version>
            </eclipse>

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

**Wichtig:** Eclipse Formatter wird verwendet (nicht Google Java Format), da **Google Java Format nicht mit Java 25 kompatibel** ist!

---

## 📝 Maven-Befehle

### **1. Code formatieren (automatisch korrigieren)**

```bash
mvn spotless:apply
```

**Was macht das?**
- ✅ Formatiert alle `.java` Dateien
- ✅ Entfernt ungenutzte Imports
- ✅ Sortiert Imports
- ✅ Wendet Eclipse Formatter an

**Wann verwenden?**
- ✅ Vor jedem Commit
- ✅ Nach größeren Code-Änderungen
- ✅ Wenn IntelliJ Warnings anzeigt

**Ausgabe:**

```
[INFO] Spotless.Apply is keeping 0 files clean - 3 files changed
[INFO]     src/main/java/.../DoctorEntity.java
[INFO]     src/main/java/.../City.java
[INFO]     src/main/java/.../Specialty.java
[INFO] BUILD SUCCESS
```

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
- ✅ In CI/CD Pipelines
- ✅ Vor dem Push
- ✅ Um zu testen, ob alle Dateien formatiert sind

**Ausgabe bei Fehlern:**

```
[ERROR] The following files had format violations:
[ERROR]     src/main/java/test/doctor_provider/infrastructure/adapter/outgoing/persistence/entity/DoctorEntity.java
[ERROR] Run 'mvn spotless:apply' to fix
[INFO] BUILD FAILURE
```

**Ausgabe wenn alles OK:**

```
[INFO] Spotless.Check is keeping 0 files clean - all dirty files are listed below:
[INFO] BUILD SUCCESS
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

### **4. Nur kompilieren (ohne Spotless-Check)**

```bash
mvn clean compile -Dspotless.check.skip=true
```

**Nützlich für:** Schnelles Testen während der Entwicklung

---

## 🔧 IntelliJ IDEA Integration

### **Option 1: Eclipse Code Formatter Plugin (empfohlen für Java 25)**

1. `IntelliJ IDEA` → `Settings/Preferences` → `Plugins`
2. Suche: **"Eclipse Code Formatter"**
3. **Install** → **Restart IDE**
4. `Settings` → `Other Settings` → `Eclipse Code Formatter`
   - ✅ **Use Eclipse Code Formatter**
   - ✅ **Eclipse formatter config:** (leer lassen für Default)

**Vorteil:** Code wird automatisch beim Speichern formatiert! ✨

---

### **Option 2: Google Java Format Plugin (nur für Java ≤ 21)**

**⚠️ NICHT kompatibel mit Java 25!**

Falls du Java 21 verwendest:

1. `IntelliJ IDEA` → `Settings` → `Plugins`
2. Suche: **"google-java-format"**
3. **Install** → **Restart IDE**
4. `Settings` → `Other Settings` → `google-java-format Settings`
   - ✅ **Enable google-java-format**
   - ✅ Style: **GOOGLE**

---

### **Option 3: Maven Goals in IntelliJ ausführen**

1. **Maven** Tool Window öffnen (rechts)
2. **doctor-provider** → **Plugins** → **spotless**
3. Doppelklick auf:
   - `spotless:apply` → Code formatieren
   - `spotless:check` → Nur prüfen

**Oder:** Terminal in IntelliJ öffnen und `mvn spotless:apply` ausführen

---

## 🔧 Java 25 Kompatibilität

### ❌ Problem mit Google Java Format

**Fehler:**

```
java.lang.NoSuchMethodError: 'java.util.Queue com.sun.tools.javac.util.Log$DeferredDiagnosticHandler.getDiagnostics()'
```

**Ursache:**
- Google Java Format ist **nicht kompatibel** mit Java 25
- API-Inkompatibilität zwischen Spotless und Java 25

---

### ✅ Lösung: Eclipse Formatter verwenden

**Was wurde geändert:**

#### **Vorher (nicht kompatibel mit Java 25):**

```xml
<googleJavaFormat>
    <version>1.19.2</version>
    <style>GOOGLE</style>
</googleJavaFormat>
```

#### **Nachher (kompatibel mit Java 25):**

```xml
<eclipse>
    <version>4.26.0</version>
</eclipse>
```

---

### 🧪 Test nach Umstellung

```bash
# Prüfen, ob es jetzt funktioniert
mvn spotless:check

# Alle Dateien formatieren
mvn spotless:apply
```

**Sollte jetzt funktionieren!** ✅

---

## 🎯 Workflow-Empfehlung

### **Standard-Workflow (vor dem Commit):**

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

### **Mit Git Hook (automatisch):**

Wenn du den Git Pre-Commit Hook aktiviert hast:

```bash
# 1. Code schreiben
# ...

# 2. Commit (formatiert automatisch!)
git add .
git commit -m "feat: add entity"
# 🎨 Spotless formatiert automatisch! ✨
```

**Siehe:** [GIT_HOOKS_KOMPLETT.md](./GIT_HOOKS_KOMPLETT.md)

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
| `**/*.java`               | ✅ Eclipse Formatter        |
| `pom.xml`                 | ✅ XML Sortierung           |
| `**/*.md`                 | ✅ Markdown Formatierung    |
| `**/target/**`            | ❌ Ausgeschlossen           |
| `**/generated-sources/**` | ❌ Ausgeschlossen (OpenAPI) |

### **Ausschlüsse:**

- `target/` - Build-Artefakte
- `generated-sources/` - Generierter Code (OpenAPI)

**Warum?** Generierter Code sollte nicht manuell formatiert werden!

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
- ✅ Konsistente Einrückung (4 Spaces nach Eclipse Style)
- ✅ Leerzeile nach Package-Deklaration
- ✅ Spacing bei Annotationen (`name = "doctor"`)
- ✅ Geschweifte Klammern korrekt platziert

---

## 🚨 Häufige Probleme

### **Problem 1: "spotless:check failed" im Build**

**Fehler:**

```
[ERROR] The following files had format violations:
[ERROR]     src/main/java/.../DoctorEntity.java
```

**Lösung:**

```bash
mvn spotless:apply
```

---

### **Problem 2: "Cannot find google-java-format" (Java 25)**

**Fehler:**

```
java.lang.NoSuchMethodError: 'java.util.Queue com.sun.tools.javac.util.Log$DeferredDiagnosticHandler.getDiagnostics()'
```

**Ursache:** Google Java Format ist nicht kompatibel mit Java 25

**Lösung:** Eclipse Formatter verwenden (siehe [Java 25 Kompatibilität](#java-25-kompatibilität))

---

### **Problem 3: Maven lädt Dependencies nicht**

**Lösung:**
Maven lädt es automatisch beim ersten Ausführen.

```bash
mvn spotless:apply
# Wartet ab, bis Dependencies geladen sind
```

---

### **Problem 4: Formatierung rückgängig machen**

**Lösung:**

```bash
# Falls Spotless etwas kaputt gemacht hat:
git checkout -- src/main/java/test/doctor_provider/...

# Oder kompletten Branch resetten:
git reset --hard HEAD
```

---

### **Problem 5: "BUILD FAILURE" bei maven-compiler-plugin**

**Fehler:**

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.1:compile
[ERROR] Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag
```

**Ursache:** Inkompatibilität zwischen Maven Compiler Plugin und Java 25

**Lösung:**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version> <!-- Stabile Version für Java 25 -->
</plugin>
```

---

### **Problem 6: Spotless ist zu langsam**

**Lösung 1: Nur geänderte Dateien formatieren**

```bash
# Statt alle Dateien:
mvn spotless:apply

# Nur geänderte:
git diff --name-only | grep '\.java$' | xargs -I {} mvn spotless:apply -DspotlessFiles={}
```

**Lösung 2: Spotless stumm schalten**

```bash
mvn spotless:apply -q
```

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

      - name: Set up JDK 25
        uses: actions/setup-java@v3
        with:
          java-version: '25'
          distribution: 'temurin'

      # Formatierung prüfen
      - name: Check code formatting
        run: mvn spotless:check

      # Build nur, wenn formatiert
      - name: Build with Maven
        run: mvn clean install

      # Optional: Auto-Format und Commit
      - name: Auto-format code
        if: failure()
        run: |
          mvn spotless:apply
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add .
          git commit -m "style: auto-format code"
          git push
```

---

### **GitLab CI Beispiel:**

```yaml
stages:
  - validate
  - build

spotless-check:
  stage: validate
  script:
    - mvn spotless:check
  only:
    - merge_requests

build:
  stage: build
  script:
    - mvn clean install
  only:
    - main
```

---

## 📊 Eclipse vs. Google Java Format

|          Feature           | Google Java Format |     Eclipse Formatter     |
|----------------------------|--------------------|---------------------------|
| **Java 25 Kompatibilität** | ❌ Nein             | ✅ Ja                      |
| **Einrückung**             | 2 Spaces           | 4 Spaces (konfigurierbar) |
| **Line Length**            | 100                | 120 (konfigurierbar)      |
| **Code-Style**             | Google-Style       | Eclipse-Default           |
| **IntelliJ Plugin**        | ✅ Ja               | ✅ Ja                      |
| **Maven Plugin Support**   | ✅ Ja               | ✅ Ja                      |

### **Empfehlung:**

- ✅ **Java 25:** Eclipse Formatter verwenden
- ✅ **Java ≤ 21:** Google Java Format optional

---

## 🔄 Alternative: Downgrade auf Java 21

Falls du Google Java Format bevorzugst:

```xml
<properties>
    <java.version>21</java.version>  <!-- Statt 25 -->
</properties>
```

**Dann:**

```xml
<googleJavaFormat>
    <version>1.19.2</version>
    <style>GOOGLE</style>
</googleJavaFormat>
```

**Aber:** Java 25 ist neuer und besser! ✅

---

## ⚙️ Optional: Eclipse Formatter anpassen

### **Eigene Formatter-Konfiguration verwenden:**

1. Eclipse Formatter XML erstellen:
   - In Eclipse: `Preferences` → `Java` → `Code Style` → `Formatter`
   - `Export...` → `eclipse-formatter.xml` speichern

2. In `pom.xml` referenzieren:

```xml
<eclipse>
    <file>${project.basedir}/eclipse-formatter.xml</file>
</eclipse>
```

**Oder:** Lass es wie es ist - der Default ist gut! ✅

---

## ✅ Zusammenfassung

|        Befehl        |         Zweck          |
|----------------------|------------------------|
| `mvn spotless:apply` | Code formatieren (FIX) |
| `mvn spotless:check` | Nur prüfen (kein FIX)  |
| `mvn clean install`  | Build + Auto-Check     |

### **Best Practice:**

1. ✅ **Vor jedem Commit:** `mvn spotless:apply`
2. ✅ **In CI/CD:** `mvn spotless:check`
3. ✅ **IntelliJ Plugin** installieren für Auto-Formatierung beim Speichern
4. ✅ **Git Hook** verwenden für automatische Formatierung (siehe [GIT_HOOKS_KOMPLETT.md](./GIT_HOOKS_KOMPLETT.md))

### **Status:**

|           Status           |             Wert             |
|----------------------------|------------------------------|
| **Spotless installiert?**  | ✅ Ja (in pom.xml)            |
| **Formatter**              | ✅ Eclipse (Java 25 kompatibel) |
| **Funktioniert?**          | ✅ Ja                         |
| **Git Hook?**              | ⏸️ Optional (siehe andere Datei) |

---

## 📖 Weitere Ressourcen

- **Spotless GitHub:** https://github.com/diffplug/spotless
- **Spotless Maven Plugin:** https://github.com/diffplug/spotless/tree/main/plugin-maven
- **Eclipse Formatter:** https://www.eclipse.org/
- **Git Hooks Guide:** [GIT_HOOKS_KOMPLETT.md](./GIT_HOOKS_KOMPLETT.md)

---

🎉 **Viel Erfolg mit sauberem Code!**

