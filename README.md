# ConnectChat — Application de messagerie en Java

Projet pédagogique réalisé dans le cadre du cours de programmation Java —
Génie informatique (Systèmes Logistiques et Intelligents), niveau 4.

**Réalisé par : Mekontso Olivier Steve && Wome Franck**

## Description de l'application

ConnectChat est une application de **chat/messagerie locale** (réseau local)
qui permet à plusieurs utilisateurs connectés sur le même réseau de discuter
en privé, deux par deux.

Elle est construite avec :
- **JavaFX** pour une interface graphique moderne (couleurs officielles de WhatsApp),
- les **sockets Java (TCP)** pour tout le transport réseau,
- une architecture **client / serveur** simple, robuste et sécurisée.

### Fonctionnalités
- **Première fenêtre** : un message de bienvenue, un champ « pseudo », un
  bouton **Trouver** qui affiche la liste des personnes connectées au réseau,
  dans un tableau à deux colonnes : *Utilisateur* (avec un point vert en ligne)
  et bouton **Écrire**.
- Cliquer sur **Écrire** ouvre la **deuxième fenêtre** : la discussion avec ce
  contact (bulles de messages façon WhatsApp, heure d'envoi, défilement
  automatique, envoi par bouton ou touche Entrée).
- Barre de licence et signature en bas de chaque fenêtre.

## Architecture technique

```
                       ┌────────────┐
   Client A ──────────▶│  Serveur   │◀────────── Client B
  (Steve)   sockets    │ ConnectChat │     sockets   (Frank)
                       └────────────┘
```

- **ChatServer** : le central du réseau. Écoute sur le port 5050, garde la
  liste des utilisateurs connectés et relaie les messages privés. Aucune
  interface graphique, il tourne en console.
- **ChatClient** : la connexion réseau côté utilisateur (envoi + thread de
  lecture continue).
- **Message** : le « paquet » transporté (type, expéditeur, destinataire,
  contenu, heure). Sérialisé en Java puis envoyé sur le socket.
- **Session** : mémoire partagée de l'application (connexion, pseudo,
  historiques des conversations).
- **MainView** : première fenêtre (bienvenue + recherche + tableau).
- **ChatView** : deuxième fenêtre (discussion par bulles).

### Protocole (résumé)
`REGISTER` → `REGISTER_OK` / `ERROR` (pseudo pris) ·
`REQUEST_LIST` → `USER_LIST` · `CHAT` (relayé à l'autre client).

### Sécurité intégrée
- Pseudo unique validé et nettoyé (lettres, chiffres, `_`, espaces, 24 max).
- Message limité à 2000 caractères, caractères de contrôle supprimés.
- L'expéditeur est toujours forcé côté serveur (impossible d'usurper un autre).
- Destinataire absent → message d'erreur renvoyé à l'expéditeur.
- Toute l'interface est mise à jour sur le thread JavaFX uniquement.

## Prérequis
- JDK 17 ou plus récent.
- Les bibliothèques JavaFX sont déjà fournies dans le dossier **lib/**.
- La compilation est automatique (VS Code) : les classes compilées se
  trouvent dans le dossier **bin/**.

## Comment lancer

### 1. Démarrer le serveur (sur la machine qui fait office de central)
```bash
java -cp "bin:lib/*" ChatServer
# port personnalisé :   java -cp "bin:lib/*" ChatServer 6060
```

### 2. Lancer l'interface utilisateur (une par étudiant)
```bash
java --enable-native-access=ALL-UNNAMED -cp "bin:lib/*" Launcher
# vers un autre serveur du réseau :
java --enable-native-access=ALL-UNNAMED -cp "bin:lib/*" Launcher --host=192.168.1.10 --port=5050
```

> Les machines doivent être sur le **même réseau** et le port 5050 ouvert.

### 3. Lancer depuis la barre des tâches (icône + épinglage)
Pour que l'application apparaisse avec **son logo** dans la barre des
tâches (au lieu de l'engrenage Java), un fichier lanceur est fourni :
- `launch.sh` — démarre le client ;
- `ConnectChat.desktop` — le raccourci système (déjà installé dans
  `~/.local/share/applications/`).

Il suffit alors d'ouvrir la vue d'ensemble (touche Super), chercher
**ConnectChat** et épingler l'application à la barre des tâches.

## Structure du dossier
```
src/   →  code source Java
bin/   →  classes compilées (généré automatiquement)
lib/   →  bibliothèques JavaFX 25
```

## Licence
Ce projet est distribué sous **Licence MIT**, à des fins pédagogiques.