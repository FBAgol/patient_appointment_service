# ⚠️ WICHTIG: Git Hook Aktivierung

## 🔴 Aktueller Status

**Git Hook ist NOCH NICHT aktiv!**

Wenn du jetzt `git commit` machst:
- ❌ Code wird **NICHT** automatisch formatiert
- ❌ Du musst **manuell** `mvn spotless:apply` ausführen

---

## ✅ So aktivierst du die automatische Formatierung

### **Schritt 1: Terminal öffnen**

In IntelliJ: **View** → **Tool Windows** → **Terminal**

---

### **Schritt 2: Zum Projekt navigieren**

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
```

---

### **Schritt 3: Hook aktivieren (EINMALIG!)**

```bash
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

**Ausgabe sollte sein:**

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
# Kleinen Test machen
git commit --allow-empty -m "test: hook aktiviert"
```

**Du solltest sehen:**

```
🎨 Spotless: Code wird formatiert...
✅ Code erfolgreich formatiert!
[main xyz1234] test: hook aktiviert
```

---

## 🎯 Danach: Automatische Formatierung

### **Ab jetzt bei jedem Commit:**

```bash
git add .
git commit -m "feat: neue Funktion"
```

**Automatisch passiert:**
1. ✅ Spotless formatiert deinen Code
2. ✅ Formatierte Dateien werden zum Commit hinzugefügt
3. ✅ Commit wird durchgeführt

**Du brauchst NIE MEHR:**
- ❌ `mvn spotless:apply` manuell ausführen
- ❌ Daran denken zu formatieren

---

## 🔍 Status prüfen

### **Ist der Hook aktiv?**

```bash
ls -l .git/hooks/pre-commit
```

**Sollte ausgeben:**

```
-rwxr-xr-x  1 user staff 500 Feb 12 15:30 .git/hooks/pre-commit
           ^^^
           x = ausführbar = AKTIV! ✅
```

**Wenn Datei nicht existiert:**

```
ls: .git/hooks/pre-commit: No such file or directory
# ❌ Hook ist NICHT aktiv!
# Lösung: ./aktiviere-git-hook.sh ausführen
```

---

## 📝 Zusammenfassung

|        Situation         |                                      Was passiert                                      |
|--------------------------|----------------------------------------------------------------------------------------|
| **Hook NICHT aktiviert** | ❌ Keine automatische Formatierung<br>❌ Du musst manuell `mvn spotless:apply` ausführen |
| **Hook aktiviert**       | ✅ Automatische Formatierung bei `git commit`<br>✅ Du musst NICHTS machen               |

---

## 🚀 Nächster Schritt

**Führe jetzt aus:**

```bash
cd /Users/A200151230/Documents/myProjcts/patient_appointment_service/doctor-provider
chmod +x aktiviere-git-hook.sh
./aktiviere-git-hook.sh
```

**Dann bist du fertig!** 🎉

---

## ⚠️ Notfall: Hook temporär überspringen

Falls der Hook mal Probleme macht:

```bash
git commit --no-verify -m "message"
```

**Nicht empfohlen! Nur für Notfälle.**

---

## 🔒 Wichtig zu verstehen

Der Hook ist:
- ✅ **NUR** für das `doctor-provider` Projekt
- ✅ **NICHT** global für alle Java-Projekte
- ✅ **NICHT** automatisch bei `git clone` dabei (muss jeder im Team einmalig aktivieren)

---

## 📚 Weitere Infos

- [GIT_HOOKS_ZUSAMMENFASSUNG.md](./GIT_HOOKS_ZUSAMMENFASSUNG.md)
- [GIT_HOOKS_GUIDE.md](./GIT_HOOKS_GUIDE.md)
- [SPOTLESS_GUIDE.md](./SPOTLESS_GUIDE.md)

