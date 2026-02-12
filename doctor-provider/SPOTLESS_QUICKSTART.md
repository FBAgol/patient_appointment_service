# 🚀 Spotless Quick Start

## ⚡ Schnellstart

### **Code formatieren (vor dem Commit):**

```bash
mvn spotless:apply
```

### **Prüfen, ob Code formatiert ist:**

```bash
mvn spotless:check
```

---

## 🔧 IntelliJ Setup (einmalig)

### **Option 1: Google Java Format Plugin (empfohlen)**

1. `IntelliJ IDEA` → `Settings` → `Plugins`
2. Suche: **"google-java-format"**
3. **Install** → **Restart IDE**
4. `Settings` → `Other Settings` → `google-java-format Settings`
   - ✅ **Enable google-java-format**
   - ✅ Style: **GOOGLE**

**Vorteil:** Code wird automatisch beim Speichern formatiert! ✨

---

### **Option 2: Maven Goal in IntelliJ**

**Rechtsklick auf `pom.xml`** → `Add as Maven Project`

**Dann:**
1. Maven Tool Window (rechts) öffnen
2. `doctor-provider` → `Plugins` → `spotless`
3. Doppelklick auf `spotless:apply`

---

## 📝 Workflow

```bash
# 1. Code schreiben
# ...

# 2. Vor dem Commit: Formatieren
mvn spotless:apply

# 3. Build
mvn clean install

# 4. Commit
git add .
git commit -m "feat: add entity"
git push
```

---

## ❌ Fehler beheben

### **"spotless:check failed"**

```bash
# Lösung: Einfach formatieren
mvn spotless:apply
```

---

## 📖 Mehr Details

Siehe: [SPOTLESS_GUIDE.md](./SPOTLESS_GUIDE.md)

---

✅ **Spotless ist jetzt aktiviert!**

Vor jedem Commit: `mvn spotless:apply` ausführen!

