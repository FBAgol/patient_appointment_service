#!/bin/bash
# Setup-Script für Git Hooks
# Einmalig ausführen, um automatische Formatierung zu aktivieren

echo "🔧 Git Hook wird eingerichtet..."

# Navigiere zum Projekt-Verzeichnis
cd "$(dirname "$0")"

# Mache Hook ausführbar
chmod +x .git/hooks/pre-commit

# Entferne Pre-Push Hook falls vorhanden (nicht mehr nötig)
rm -f .git/hooks/pre-push

echo ""
echo "✅ Git Hook erfolgreich eingerichtet!"
echo ""
echo "Was passiert jetzt automatisch:"
echo "  📝 Bei git commit → Code wird automatisch formatiert ✨"
echo ""
echo "Hinweis: Der Hook ist NUR für dieses Projekt aktiv!"
echo "         Andere Projekte sind NICHT betroffen."
echo ""
echo "Test:"
echo "  git commit -m 'test'  # Formatiert automatisch!"
echo ""


