#!/usr/bin/env bash
# Lanceur de ConnectChat : démarre le client graphique.
# Se place dans le dossier du projet, puis lance l'application JavaFX.
cd "$(dirname "$0")" || exit 1

# Le drapeau --enable-native-access n'existe qu'à partir du JDK 24.
# On ne l'ajoute que si le JDK est assez récent, pour que l'application
# tourne aussi sur les JDK 17-23 (ex. machine de l'enseignant).
JAVA=java
VERSION=$("$JAVA" -version 2>&1 | head -1 | grep -oE '"[0-9]+' | tr -d '"')
OPTS=""
if [ -n "$VERSION" ] && [ "$VERSION" -ge 24 ]; then
    OPTS="--enable-native-access=ALL-UNNAMED"
fi

exec "$JAVA" $OPTS -cp "bin:lib/*" Launcher "$@"