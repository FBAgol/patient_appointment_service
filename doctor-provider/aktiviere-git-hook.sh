#!/bin/bash
# Aktiviert den Git Pre-Commit Hook für automatische Formatierung
# EINMALIG ausführen!

echo "🔧 Aktiviere Git Pre-Commit Hook..."
echo ""

# Prüfe, ob wir im richtigen Verzeichnis sind
if [ ! -f "pom.xml" ]; then
    echo "❌ Fehler: Bitte im doctor-provider Verzeichnis ausführen!"
    exit 1
fi

# Prüfe, ob .git existiert
if [ ! -d ".git" ]; then
    echo "❌ Fehler: Kein Git-Repository gefunden!"
    echo "   Führe zuerst aus: git init"
    exit 1
fi

# Erstelle hooks-Verzeichnis falls nicht vorhanden
mkdir -p .git/hooks

# Erstelle Pre-Commit Hook
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Git Pre-Commit Hook: Automatische Code-Formatierung mit Spotless
# Diese Datei wird automatisch vor jedem Commit ausgeführt

echo "🎨 Spotless: Code wird formatiert..."

# Führe Spotless Apply aus
mvn spotless:apply -q

# Prüfe Exit-Code
if [ $? -eq 0 ]; then
    echo "✅ Code erfolgreich formatiert!"

    # Füge formatierte Dateien zum Commit hinzu
    git add -u

    exit 0
else
    echo "❌ Spotless-Formatierung fehlgeschlagen!"
    echo "Bitte Fehler beheben und erneut committen."
    exit 1
fi
EOF

# Mache Hook ausführbar
chmod +x .git/hooks/pre-commit

# Entferne Pre-Push Hook falls vorhanden (nicht mehr nötig)
rm -f .git/hooks/pre-push

echo "✅ Git Pre-Commit Hook wurde aktiviert!"
echo ""
echo "📝 Was passiert jetzt:"
echo "   Bei jedem 'git commit' wird dein Code automatisch formatiert!"
echo ""
echo "🔒 Wichtig:"
echo "   - Der Hook ist NUR für dieses Projekt (doctor-provider)"
echo "   - Andere Java-Projekte sind NICHT betroffen"
echo ""
echo "🧪 Test:"
echo "   git commit -m 'test: formatierung'"
echo "   # → Code wird automatisch formatiert! ✨"
echo ""
echo "⚠️  Notfall (Hook überspringen):"
echo "   git commit --no-verify -m 'message'"
echo ""

