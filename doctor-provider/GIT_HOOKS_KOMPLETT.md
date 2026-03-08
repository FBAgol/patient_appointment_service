# 🪝 Git Hooks - Vollständige Anleitung

## 📋 Inhaltsverzeichnis

1. [Übersicht](#übersicht)
2. [Setup & Aktivierung](#setup--aktivierung)
3. [Was passiert automatisch?](#was-passiert-automatisch)
4. [Ist der Hook global oder lokal?](#ist-der-hook-global-oder-lokal)
5. [Hook-Dateistruktur](#hook-dateistruktur)
6. [Vorher vs. Nachher](#vorher-vs-nachher)
7. [Hook-Status überprüfen](#hook-status-überprüfen)
8. [Manuelle Kontrolle](#manuelle-kontrolle)
9. [Häufige Fragen](#häufige-fragen)
10. [Troubleshooting](#troubleshooting)
11. [Warum nur Pre-Commit?](#warum-nur-pre-commit)
12. [Hook für Team verteilen](#hook-für-team-verteilen)

---

## 🎯 Übersicht

Ein Git Pre-Commit Hook ermöglicht **automatische Code-Formatierung** bei jedem Commit.

### **Wichtig:**

- ✅ Der Hook ist **NUR für dieses Projekt** (`doctor-provider`)
- ✅ **Andere** Java-Projekte sind **NICHT betroffen**
- ✅ **Keine globale** Git-Konfiguration
- ✅ Wird **einmalig** aktiviert und läuft dann automatisch

### **Status-Übersicht:**

```
┌─────────────────────────────────────────────────┐
│  Vor Aktivierung:                               │
│  ❌ Keine automatische Formatierung             │
│  ❌ Du musst manuell 'mvn spotless:apply'       │
└─────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────┐
│  Nach Aktivierung:                              │
│  ✅ Automatische Formatierung bei git commit    │
│  ✅ Du musst NICHTS mehr machen                 │
└─────────────────────────────────────────────────┘
```

---

## 🚀 Setup & Aktivierung

### **Schritt 1: Terminal öffnen**

**In IntelliJ IDEA:**
- Klicke unten auf **Terminal** (oder `Alt + F12`)

**Oder in macOS Terminal:**

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
```

---

### **Schritt 2: Hook aktivieren (EINMALIG!)**

```bash
chmod +x setup-git-hooks.sh
./setup-git-hooks.sh
```

**Alternative (falls aktiviere-git-hook.sh vorhanden):**

```bash
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

---

### **Schritt 3: Erfolgsmeldung prüfen**

**✅ Erwartete Ausgabe:**

```
🔧 Aktiviere Git Pre-Commit Hook...

✅ Git Pre-Commit Hook wurde aktiviert!

📝 Was passiert jetzt:
   Bei jedem 'git commit' wird dein Code automatisch formatiert!

🔒 Wichtig:
   - Der Hook ist NUR für dieses Projekt (doctor-provider)
   - Andere Java-Projekte sind NICHT betroffen
```

---

### **Schritt 4: Testen (optional)**

```bash
# Test 1: Dummy-Commit
git commit --allow-empty -m "test: hook aktiviert"

# Test 2: Echte Änderung
echo "// Test" >> src/main/java/test/doctor_provider/domain/model/City.java
git add .
git commit -m "test: formatierung"
```

**Du solltest sehen:**

```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main abc1234] test: hook aktiviert
```

**Das war's!** ✅ Hook ist jetzt aktiv!

---

## ⚙️ Was passiert automatisch?

### **Bei `git commit` (Pre-Commit Hook):**

```bash
git add .
git commit -m "feat: add entities"
```

**Automatisch wird ausgeführt:**

1. ✅ `mvn spotless:apply` - Code formatieren
2. ✅ Formatierte Dateien zum Commit hinzufügen (`git add -u`)
3. ✅ Commit durchführen

**Du siehst:**

```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main abc1234] feat: add entities
 2 files changed, 50 insertions(+), 10 deletions(-)
```

### **Was wird formatiert?**

- ✅ **Geänderte Dateien** → werden formatiert
- ✅ **Neue Dateien** → werden formatiert
- ❌ **Unveränderte Dateien** → bleiben wie sie sind

**Wichtig:** Nur Dateien die du änderst und committest werden formatiert!

---

## 🔒 Ist der Hook global oder lokal?

### **Antwort: NUR lokal für dieses Projekt!** ✅

```
/Users/A200151230/Documents/myProjcts/
├── patient_appointment_service/
│   ├── doctor-provider/
│   │   └── .git/hooks/pre-commit    ← Hook NUR hier! ✅
│   └── patient-customer/
│       └── .git/hooks/              ← KEIN Hook hier! ✅
└── anderes-java-projekt/
    └── .git/hooks/                  ← KEIN Hook hier! ✅
```

**Warum?**
- Hooks werden in `.git/hooks/` gespeichert
- `.git/` ist projekt-spezifisch
- Jedes Git-Repository hat sein eigenes `.git/` Verzeichnis
- Hooks werden **NICHT** mit Git versioniert

### **Test: Andere Projekte prüfen**

```bash
# In patient-customer:
cd ../patient-customer
git commit -m "test"
# ❌ KEINE automatische Formatierung! (kein Hook installiert)

# In doctor-provider:
cd ../doctor-provider
git commit -m "test"
# ✅ Automatische Formatierung! (Hook installiert)
```

---

## 📂 Hook-Dateistruktur

```
doctor-provider/
├── .git/                           # Git-Verzeichnis (nicht versioniert)
│   └── hooks/
│       └── pre-commit              # Der Hook (wird bei git commit ausgeführt)
├── setup-git-hooks.sh              # Setup-Script (im Repository)
└── aktiviere-git-hook.sh           # Alternative Setup-Script (im Repository)
```

### **Pre-Commit Hook Inhalt (`.git/hooks/pre-commit`):**

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

## 🎯 Vorher vs. Nachher

### **❌ VORHER (ohne Hook):**

```bash
# 1. Code schreiben
vim DoctorEntity.java

# 2. MANUELL formatieren (leicht zu vergessen!)
mvn spotless:apply

# 3. Commit
git add .
git commit -m "feat: add entity"
```

**Problem:** Du musst daran denken! ❌

---

### **✅ NACHHER (mit Hook):**

```bash
# 1. Code schreiben
vim DoctorEntity.java

# 2. Commit (formatiert automatisch!)
git add .
git commit -m "feat: add entity"
# 🎨 Spotless formatiert automatisch! ✨
```

**Vorteil:** Vollautomatisch! ✅

---

## 🔍 Hook-Status überprüfen

### **Ist der Hook aktiv?**

```bash
ls -la .git/hooks/pre-commit
```

**✅ Hook ist AKTIV:**

```
-rwxr-xr-x  1 A200151230  staff  500 Feb 12 15:30 .git/hooks/pre-commit
           ^^^
           x = ausführbar = AKTIV!
```

**❌ Hook ist NICHT aktiv:**

```
ls: .git/hooks/pre-commit: No such file or directory
```

**Lösung:** Setup-Script ausführen:

```bash
./setup-git-hooks.sh
```

---

### **Hook-Inhalt anzeigen:**

```bash
cat .git/hooks/pre-commit
```

---

## 🛠️ Manuelle Kontrolle

### **Hook temporär überspringen (Notfall!):**

```bash
# Commit ohne Hook
git commit --no-verify -m "WIP: nicht formatiert"
```

**⚠️ Nicht empfohlen!** Nur für Notfälle (z.B. Hook ist defekt).

---

### **Hook deaktivieren:**

```bash
# Pre-Commit komplett entfernen
rm .git/hooks/pre-commit
```

---

### **Hook wieder aktivieren:**

```bash
./setup-git-hooks.sh
```

---

## 🚨 Häufige Fragen

### **Q: Werden auch alte Dateien formatiert?**

**A:** Nur Dateien, die du **änderst** und **committest**!

- ✅ Neue Dateien → werden formatiert
- ✅ Geänderte Dateien → werden formatiert
- ❌ Unveränderte Dateien → bleiben wie sie sind

---

### **Q: Was ist, wenn Spotless einen Fehler hat?**

**A:** Commit wird abgebrochen!

```
❌ Spotless-Formatierung fehlgeschlagen!
Bitte Fehler beheben und erneut committen.
```

**Lösung:**
1. Fehler beheben
2. Erneut committen

**Notfall (Hook überspringen):**

```bash
git commit --no-verify -m "message"
```

---

### **Q: Betrifft das meine anderen Java-Projekte?**

**A:** NEIN! ❌

Der Hook ist **nur** für `doctor-provider` aktiv.

---

### **Q: Was, wenn ich den Hook nicht möchte?**

**A:** Einfach nicht aktivieren! Formatiere dann manuell:

```bash
mvn spotless:apply
```

---

### **Q: Reicht Pre-Commit alleine?**

**A:** JA! ✅ Pre-Commit ist völlig ausreichend!

- ✅ Code wird beim Commit automatisch formatiert
- ✅ Schnell (nur einmal pro Commit)
- ✅ Frühzeitig (bevor Code überhaupt committed wird)

**Pre-Push wäre Overkill:**
- ❌ Zu spät (Code ist schon committed)
- ❌ Langsamer
- ❌ Unnötig, wenn Pre-Commit schon formatiert

---

## 🔧 Troubleshooting

### **Problem: "Permission denied"**

**Lösung:**

```bash
chmod +x .git/hooks/pre-commit
```

---

### **Problem: "Hook funktioniert nicht"**

**Prüfen, ob Hook existiert:**

```bash
ls -la .git/hooks/pre-commit
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

### **Problem: Hook ist zu langsam**

**Lösung 1: Spotless stumm schalten**

Editiere `.git/hooks/pre-commit`:

```bash
# Vorher:
mvn spotless:apply -q

# Nachher (komplett stumm):
mvn spotless:apply -q > /dev/null 2>&1
```

**Lösung 2: Nur geänderte Java-Dateien formatieren**

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

## 📊 Warum nur Pre-Commit?

|      Hook      |       Wann       |                   Vorteil                    |                    Nachteil                     |
|----------------|------------------|----------------------------------------------|-------------------------------------------------|
| **Pre-Commit** | Bei `git commit` | ✅ Frühe Formatierung<br>✅ Schnell<br>✅ Lokal | -                                               |
| **Pre-Push**   | Bei `git push`   | Weniger häufig                               | ❌ Zu spät (Code schon committed)<br>❌ Langsamer |

**Fazit:** Pre-Commit reicht völlig aus! ✅

---

## 🌍 Hook für Team verteilen

### **Problem:** `.git/hooks/` wird nicht mit Git versioniert!

### **Lösung:** Setup-Script im Repository ✅

```bash
# 1. setup-git-hooks.sh ist im Repository ✅
# 2. Team-Mitglied cloned Projekt
git clone <repo>

# 3. Einmalig Hook einrichten
cd doctor-provider
./setup-git-hooks.sh
```

**Jeder im Team muss einmalig das Setup-Script ausführen!**

### **Alternative:** `husky` (npm) für automatisches Setup

Für JavaScript/TypeScript-Projekte:

```bash
npm install --save-dev husky
```

**Für Java-Projekte:** Manuelles Setup mit Script (wie hier) ist Standard! ✅

---

## 📝 Checkliste

- [ ] Terminal geöffnet
- [ ] Im Projekt-Verzeichnis (`doctor-provider`)
- [ ] `./setup-git-hooks.sh` ausgeführt
- [ ] Erfolgsmeldung gesehen
- [ ] Test-Commit gemacht
- [ ] "🎨 Spotless: Code wird formatiert..." gesehen

**Wenn alle ✅ → Fertig!** 🎉

---

## ✅ Zusammenfassung

|             Frage             |             Antwort             |
|-------------------------------|---------------------------------|
| **Reicht Pre-Commit?**        | ✅ JA!                           |
| **Pre-Push nötig?**           | ❌ NEIN!                         |
| **Ist es global?**            | ❌ NEIN, nur für dieses Projekt! |
| **Betrifft andere Projekte?** | ❌ NEIN!                         |
| **Muss ich formatieren?**     | ❌ NEIN, automatisch!            |
| **Einmalig einrichten?**      | ✅ JA!                           |

### **Einmalig ausführen:**

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
chmod +x setup-git-hooks.sh
./setup-git-hooks.sh
```

### **Dann passiert automatisch:**

- ✅ **git commit** → Code wird formatiert
- ✅ Du brauchst nie wieder manuell `mvn spotless:apply` ausführen! 🚀

---

## 📚 Weitere Ressourcen

- [Git Hooks Dokumentation](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks)
- [SPOTLESS_KOMPLETT.md](./SPOTLESS_KOMPLETT.md)
- [README.md](./README.md)

---

🎉 **Viel Erfolg mit automatischer Code-Formatierung!**

