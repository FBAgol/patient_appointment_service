# 🪝 Git Hook für automatische Code-Formatierung

## 🎯 Übersicht

Ein Git Pre-Commit Hook ermöglicht **automatische Code-Formatierung** bei jedem Commit.

**Wichtig:**
- ✅ Der Hook ist **NUR für dieses Projekt** (`doctor-provider`)
- ✅ **Andere** Java-Projekte sind **NICHT betroffen**
- ✅ **Keine globale** Git-Konfiguration

---

## 🚀 Setup (einmalig)

```bash
chmod +x setup-git-hooks.sh
./setup-git-hooks.sh
```

**Das war's!** ✅

---

## ⚙️ Was passiert automatisch?

### **Bei `git commit` (Pre-Commit Hook)**

```bash
git add .
git commit -m "feat: add entities"
```

**Automatisch wird:**
1. ✅ `mvn spotless:apply` ausgeführt
2. ✅ Code formatiert
3. ✅ Formatierte Dateien zum Commit hinzugefügt
4. ✅ Commit durchgeführt

**Du siehst:**

```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main abc1234] feat: add entities
 2 files changed, 50 insertions(+), 10 deletions(-)
```

---

## 🔒 Ist der Hook global oder lokal?

### **Antwort: Nur lokal für dieses Projekt!** ✅

```
/Users/A200151230/Documents/myProjcts/
├── patient_appointment_service/
│   ├── doctor-provider/
│   │   └── .git/hooks/pre-commit    ← Hook NUR hier!
│   └── patient-customer/
│       └── .git/hooks/              ← KEIN Hook hier! ✅
└── anderes-projekt/
    └── .git/hooks/                  ← KEIN Hook hier! ✅
```

**Warum?**
- Hooks werden in `.git/hooks/` gespeichert
- `.git/` ist projekt-spezifisch
- Jedes Git-Repository hat sein eigenes `.git/` Verzeichnis

---

## 🛠️ Manuelle Kontrolle

### **Hook temporär überspringen:**

```bash
# Commit ohne Hook (Notfall!)
git commit --no-verify -m "WIP: nicht formatiert"
```

**⚠️ Nicht empfohlen!** Nur für Notfälle.

---

### **Hook deaktivieren:**

```bash
# Pre-Commit deaktivieren
rm .git/hooks/pre-commit
```

### **Hook wieder aktivieren:**

```bash
./setup-git-hooks.sh
```

---

## 📂 Hook-Dateien

```
doctor-provider/
├── .git/
│   └── hooks/
│       └── pre-commit      # Formatiert bei Commit
└── setup-git-hooks.sh      # Setup-Script
```

**Nicht versioniert:** `.git/hooks/` ist NICHT im Git-Repository!

---

## 🔍 Was macht der Hook genau?

### **Pre-Commit Hook (`.git/hooks/pre-commit`):**

```bash
#!/bin/bash
# Wird VOR jedem Commit ausgeführt

echo "🎨 Spotless: Code wird formatiert..."

# Code formatieren
mvn spotless:apply -q

# Exit-Code prüfen
if [ $? -eq 0 ]; then
    echo "✅ Code erfolgreich formatiert!"

    # Formatierte Dateien zum Commit hinzufügen
    git add -u

    exit 0
else
    echo "❌ Spotless-Formatierung fehlgeschlagen!"
    echo "Bitte Fehler beheben und erneut committen."
    exit 1
fi
```

**Was macht `git add -u`?**
- Fügt **nur bereits getrackte Dateien** zum Commit hinzu
- Neue Dateien (untracked) werden NICHT hinzugefügt
- Perfekt für formatierte Änderungen

---

## 🎯 Workflow mit Hook

### **Ohne Hook (manuell):**

```bash
# 1. Code schreiben
vim DoctorEntity.java

# 2. Manuell formatieren (leicht zu vergessen!)
mvn spotless:apply

# 3. Commit
git add .
git commit -m "feat: add entity"
```

---

### **Mit Hook (automatisch):**

```bash
# 1. Code schreiben
vim DoctorEntity.java

# 2. Commit (Formatierung passiert automatisch!)
git add .
git commit -m "feat: add entity"
# 🎨 Spotless formatiert automatisch! ✨
```

**Viel einfacher!** 🎉

---

## 🚨 Troubleshooting

### **Problem: "Permission denied"**

```bash
chmod +x .git/hooks/pre-commit
```

---

### **Problem: "Hook funktioniert nicht"**

**Prüfen, ob Hook existiert:**

```bash
ls -la .git/hooks/pre-commit
```

**Ausgabe sollte sein:**

```
-rwxr-xr-x  1 user staff 256 Feb 12 10:00 .git/hooks/pre-commit
           ^^^
           Muss ausführbar sein (x)
```

**Neu einrichten:**

```bash
./setup-git-hooks.sh
```

---

### **Problem: "mvn: command not found"**

**Maven installieren:**

```bash
brew install maven  # macOS
```

**Oder Hook temporär überspringen:**

```bash
git commit --no-verify -m "message"
```

---

## ⚙️ Hook anpassen

### **Pre-Commit weniger verbos:**

Editiere `.git/hooks/pre-commit`:

```bash
# Vorher:
mvn spotless:apply -q

# Nachher (komplett stumm):
mvn spotless:apply -q > /dev/null 2>&1
```

---

### **Nur bestimmte Dateien formatieren:**

Editiere `.git/hooks/pre-commit`:

```bash
#!/bin/bash

# Nur Java-Dateien, die geändert wurden
git diff --cached --name-only --diff-filter=ACM | grep '\.java$' > /dev/null

if [ $? -eq 0 ]; then
    echo "🎨 Spotless: Formatiere Java-Dateien..."
    mvn spotless:apply -q
    git add -u
fi

exit 0
```

---

## 📊 Warum nur Pre-Commit (kein Pre-Push)?

|      Hook      |       Wann       |                   Vorteil                    |                    Nachteil                     |
|----------------|------------------|----------------------------------------------|-------------------------------------------------|
| **Pre-Commit** | Bei `git commit` | ✅ Frühe Formatierung<br>✅ Schnell<br>✅ Lokal | -                                               |
| **Pre-Push**   | Bei `git push`   | Weniger häufig                               | ❌ Zu spät (Code schon committed)<br>❌ Langsamer |

**Fazit:** Pre-Commit reicht völlig aus! ✅

---

## 🌍 Hook für Team-Mitglieder verteilen

### **Problem:** `.git/hooks/` wird nicht mit Git versioniert!

### **Lösung:** Setup-Script im Repository

```bash
# 1. setup-git-hooks.sh ist im Repository ✅
# 2. Team-Mitglied cloned Projekt
git clone <repo>

# 3. Einmalig Hook einrichten
./setup-git-hooks.sh
```

**Alternative:** `husky` (npm) für automatisches Setup

```bash
# Für JavaScript/TypeScript-Projekte
npm install --save-dev husky
```

**Für Java-Projekte:** Manuelles Setup mit Script (wie hier) ist Standard! ✅

---

## 🎉 Zusammenfassung

### **Einmalig ausführen:**

```bash
./setup-git-hooks.sh
```

### **Dann passiert automatisch:**

- ✅ **git commit** → Code wird formatiert

### **Gültig für:**

- ✅ NUR dieses Projekt (`doctor-provider`)
- ❌ NICHT für andere Projekte

**Du musst nie wieder manuell `mvn spotless:apply` ausführen!** 🚀

---

## 📚 Weitere Ressourcen

- [Git Hooks Dokumentation](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks)
- [SPOTLESS_GUIDE.md](./SPOTLESS_GUIDE.md)
- [README.md](./README.md)

