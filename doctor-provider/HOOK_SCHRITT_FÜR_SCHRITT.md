# 🎯 Git Hook - Schritt-für-Schritt Anleitung

## 📊 Aktueller Status

```
┌─────────────────────────────────────────────────┐
│  ⚠️  Git Hook ist NOCH NICHT aktiv!             │
│                                                  │
│  Was passiert bei 'git commit'?                 │
│  ❌ Keine automatische Formatierung             │
│  ❌ Du musst manuell 'mvn spotless:apply'       │
└─────────────────────────────────────────────────┘
```

---

## ✅ So aktivierst du die automatische Formatierung

### **🖥️ Schritt 1: Terminal öffnen**

**In IntelliJ IDEA:**
1. Klicke unten auf **Terminal** (oder `Alt + F12`)
2. Du solltest im Projekt-Verzeichnis sein

**Oder in macOS Terminal:**
```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
```

---

### **🔧 Schritt 2: Hook aktivieren (EINMALIG!)**

```bash
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

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

### **✅ Schritt 3: Nach der Aktivierung**

```
┌─────────────────────────────────────────────────┐
│  ✅ Git Hook ist JETZT AKTIV!                   │
│                                                  │
│  Was passiert bei 'git commit'?                 │
│  ✅ Automatische Formatierung mit Spotless      │
│  ✅ Du musst NICHTS mehr machen                 │
└─────────────────────────────────────────────────┘
```

---

## 🧪 Test: Funktioniert es?

### **Option 1: Dummy-Commit**

```bash
git commit --allow-empty -m "test: hook aktiviert"
```

**Du solltest sehen:**
```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main abc1234] test: hook aktiviert
```

---

### **Option 2: Echte Änderung**

```bash
# 1. Datei ändern
echo "// Test" >> src/main/java/test/doctor_provider/domain/model/City.java

# 2. Committen
git add .
git commit -m "test: formatierung"
```

**Automatisch:**
```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main xyz1234] test: formatierung
 1 file changed, 1 insertion(+)
```

---

## 📋 Vorher vs. Nachher

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

**Problem:** Leicht zu vergessen! ❌

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

**Vorteil:** Du musst nichts mehr machen! ✅

---

## 🔍 Hook-Status überprüfen

### **Ist der Hook aktiv?**

```bash
ls -la .git/hooks/pre-commit
```

**✅ Hook ist AKTIV:**
```
-rwxr-xr-x  1 user staff 500 Feb 12 15:30 .git/hooks/pre-commit
           ^^^
           Ausführbar (x) = AKTIV!
```

**❌ Hook ist NICHT aktiv:**
```
ls: .git/hooks/pre-commit: No such file or directory
```

**Lösung:**
```bash
./aktiviere-git-hook.sh
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

```
doctor-provider/
└── .git/hooks/pre-commit    ← Hook NUR hier!

patient-customer/
└── .git/hooks/              ← KEIN Hook! ✅

anderes-projekt/
└── .git/hooks/              ← KEIN Hook! ✅
```

---

### **Q: Was, wenn ich den Hook nicht möchte?**

**A:** Einfach nicht aktivieren! Formatiere dann manuell:

```bash
mvn spotless:apply
```

---

## 📝 Checkliste

- [ ] Terminal geöffnet
- [ ] Im Projekt-Verzeichnis (`doctor-provider`)
- [ ] `./aktiviere-git-hook.sh` ausgeführt
- [ ] Erfolgsmeldung gesehen
- [ ] Test-Commit gemacht
- [ ] "🎨 Spotless: Code wird formatiert..." gesehen

**Wenn alle ✅ → Fertig!** 🎉

---

## 🎯 Zusammenfassung

| Schritt | Befehl | Status |
|---------|--------|--------|
| 1. Terminal öffnen | - | ⏸️ |
| 2. Hook aktivieren | `./aktiviere-git-hook.sh` | ⏸️ |
| 3. Testen | `git commit --allow-empty -m "test"` | ⏸️ |
| **Danach** | **Automatische Formatierung!** | ✅ |

---

## 🚀 Los geht's!

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

**Dann bist du fertig!** 🎉

