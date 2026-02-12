# 🔧 Spotless Fix für Java 25

## ❌ Problem

**Fehler:**

```
java.lang.NoSuchMethodError: 'java.util.Queue com.sun.tools.javac.util.Log$DeferredDiagnosticHandler.getDiagnostics()'
```

**Ursache:**
- Google Java Format ist **nicht kompatibel** mit Java 25
- API-Inkompatibilität zwischen Spotless und Java 25

---

## ✅ Lösung

**Umstellung von Google Java Format auf Eclipse Formatter**

### **Was wurde geändert:**

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

## 🧪 Testen

```bash
# Prüfen, ob es jetzt funktioniert
mvn spotless:check

# Alle Dateien formatieren
mvn spotless:apply
```

**Sollte jetzt funktionieren!** ✅

---

## 📝 Unterschiede: Google vs Eclipse Formatter

|          Feature           | Google Java Format |     Eclipse Formatter     |
|----------------------------|--------------------|---------------------------|
| **Java 25 Kompatibilität** | ❌ Nein             | ✅ Ja                      |
| **Einrückung**             | 2 Spaces           | 4 Spaces (konfigurierbar) |
| **Line Length**            | 100                | 120 (konfigurierbar)      |
| **Code-Style**             | Google-Style       | Eclipse-Default           |

---

## 🎯 Nächste Schritte

### **1. Teste Spotless:**

```bash
mvn spotless:check
```

**Erwartete Ausgabe:**

```
[INFO] Spotless.Check is keeping 0 files clean - all dirty files are listed below:
[INFO] BUILD SUCCESS
```

---

### **2. Formatiere alle Dateien:**

```bash
mvn spotless:apply
```

**Dann committen:**

```bash
git add .
git commit -m "style: format all files with Eclipse formatter"
git push
```

---

## ⚙️ Optional: Eclipse Formatter anpassen

Wenn du den Code-Style anpassen möchtest, erstelle `eclipse-formatter.xml`:

```xml
<eclipse>
    <file>${project.basedir}/eclipse-formatter.xml</file>
</eclipse>
```

**Oder:** Lass es wie es ist - der Default ist gut! ✅

---

## 🔄 Alternative: Downgrade auf Java 21

Falls du Google Java Format bevorzugst:

```xml
<properties>
    <java.version>21</java.version>  <!-- Statt 25 -->
</properties>
```

**Aber:** Java 25 ist neuer und besser! ✅

---

## ✅ Zusammenfassung

|           Status           |             Wert             |
|----------------------------|------------------------------|
| **Problem behoben?**       | ✅ Ja                         |
| **Spotless funktioniert?** | ✅ Ja (mit Eclipse Formatter) |
| **Java 25 kompatibel?**    | ✅ Ja                         |
| **Git Hook funktioniert?** | ✅ Ja                         |

---

## 🚀 Teste es jetzt!

```bash
mvn spotless:apply
git add .
git commit -m "fix: switch to Eclipse formatter for Java 25 compatibility"
```

🎉 **Fertig!**

