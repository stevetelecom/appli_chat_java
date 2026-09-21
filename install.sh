#!/usr/bin/env bash
# ------------------------------------------------------------------
#  install.sh : enregistre ConnectChat dans le menu / dock GNOME.
#
#  À lancer sur CHAQUE machine après le clone du projet :
#      ./install.sh
#
#  Ce script :
#   1. copie l'icône dans le dossier d'icônes XDG de l'utilisateur,
#   2. génère ConnectChat.desktop avec le chemin ABSOLU du projet
#      (correct pour cette machine, contrairement au fichier de référence),
#   3. rafraîchit les caches du menu et des icônes.
# ------------------------------------------------------------------
set -e

PROJET="$(cd "$(dirname "$0")" && pwd)"
APPS="$HOME/.local/share/applications"
ICONS="$HOME/.local/share/icons/hicolor"
ICON_NAME="connectchat.png"

# 1. Icônes dans les tailles standard de GNOME.
mkdir -p "$APPS"
mkdir -p "$ICONS/48x48/apps" "$ICONS/64x64/apps" "$ICONS/128x128/apps" "$ICONS/256x256/apps"

for taille in 48x48 64x64 128x128 256x256; do
    cp -f "$PROJET/connectchat_logo_64.png" "$ICONS/$taille/apps/$ICON_NAME"
done

# 2. Fichier .desktop : Icon=connectchat (nom seul, résolu via XDG)
#    et Exec pointant vers le clone de CETTE machine.
cat > "$APPS/ConnectChat.desktop" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=ConnectChat
GenericName=Messagerie locale
Comment=Chat de messagerie en Java (Sockets + JavaFX)
Exec=$PROJET/launch.sh
Icon=$ICON_NAME
Terminal=false
StartupNotify=true
StartupWMClass=App
Categories=Network;InstantMessaging;
Keywords=chat;message;messagerie;java;conversation;
EOF

chmod +x "$PROJET/launch.sh"

# 3. Caches.
update-desktop-database "$APPS" 2>/dev/null || true
if command -v gtk-update-icon-cache >/dev/null 2>&1; then
    gtk-update-icon-cache -f "$HOME/.local/share/icons/hicolor" >/dev/null 2>&1 || true
fi

echo "ConnectChat installé pour '$USER' (projet : $PROJET)."
echo "Si l'icône n'apparaît pas tout de suite : déconnexion puis reconnexion à la session."