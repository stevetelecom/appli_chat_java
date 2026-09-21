#!/usr/bin/env bash
# Lanceur de ConnectChat : démarre le client graphique.
# Se place dans le dossier du projet, puis lance l'application JavaFX.
cd "$(dirname "$0")" || exit 1
exec java --enable-native-access=ALL-UNNAMED -cp "bin:lib/*" Launcher "$@"