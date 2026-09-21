import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * ClientHandler = le thread qui s'occupe d'UN client connecté.
 *
 * Chaque personne connectée possède son propre thread côté serveur.
 * Il authentifie le pseudo, répond aux demandes de liste et
 * relaie les messages vers le destinataire.
 */
class ClientHandler extends Thread {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String name; // pseudo du client (null tant que non authentifié)
    private volatile boolean abandonne; // vrai si le pseudo a été repris ailleurs

    ClientHandler(Socket socket) {
        this.socket = socket;
        setName("Client-" + socket.getPort());
        try {
            /* Détection plus réactive des coupures réseau. */
            socket.setKeepAlive(true);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        try {
            /* Ordre compatible avec le client : OOS puis OIS. */
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            /* Étape 1 : le client doit choisir un pseudo valide. */
            if (!authenticate()) return;

            /* Étape 2 : on ne fait plus que transporter les demandes. */
            while (true) {
                Object obj = in.readObject();
                if (!(obj instanceof Message)) continue; // paquet inconnu : ignoré

                Message m = (Message) obj;
                if (Message.REQUEST_LIST.equals(m.type)) {
                    sendUserList();
                } else if (Message.CHAT.equals(m.type)) {
                    relay(m);
                }
            }
        } catch (EOFException | SocketException e) {
            // déconnexion normale du client
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[-] Erreur avec " + name + " : " + e.getMessage());
        } finally {
            if (name != null && !abandonne) {
                ChatServer.clientsList().remove(name);
                System.out.println("[-] « " + name + " » s'est déconnecté");
                /* On prévient immédiatement les autres : son pseudo
                   disparaît de leur liste sans qu'ils fassent rien. */
                ChatServer.userListChanged(null);
            }
            closeAll();
        }
    }

    /** Ferme cette session (son pseudo vient d'être repris par une autre connexion). */
    synchronized void reprendre() {
        abandonne = true;
        closeAll();
    }

    /** Attend un pseudo, valide l'unique et l'enregistre dans la liste. */
    private boolean authenticate() throws IOException, ClassNotFoundException {
        while (true) {
            Object obj = in.readObject();
            if (!(obj instanceof Message)) continue;

            Message m = (Message) obj;
            if (!Message.REGISTER.equals(m.type)) continue; // on n'accepte qu'un REGISTER

            String candidate = sanitizeName(m.content);
            if (candidate.length() < 2) {
                send(new Message(Message.ERROR, "Serveur", null,
                        "Le pseudo doit contenir au moins 2 caractères."));
            } else if (ChatServer.clientsList().containsKey(candidate)) {
                /* Le pseudo existe déjà. Il s'agit presque toujours d'une
                   reconnexion après un redémarrage : on reprend la session.
                   L'ancienne connexion est fermée (elle reçoit un avertissement). */
                ClientHandler ancien = ChatServer.clientsList().get(candidate);
                ancien.send(new Message(Message.INFO, "Serveur", candidate,
                        "Ta session a été remplacée par une nouvelle connexion."));
                ChatServer.clientsList().remove(candidate, ancien);
                ancien.reprendre();
                name = candidate;
                ChatServer.clientsList().put(name, this);
                send(new Message(Message.REGISTER_OK, "Serveur", name, null));
                System.out.println("[↻] « " + name + " » reprise (nouvelle connexion)");
                ChatServer.deliverPending(name, this);
                ChatServer.userListChanged(this);
                return true;
            } else {
                name = candidate;
                ChatServer.clientsList().put(name, this);
                send(new Message(Message.REGISTER_OK, "Serveur", name, null));
                System.out.println("[+] « " + name + " » est maintenant connecté");
                /* On livre au passage les messages reçus pendant son absence. */
                ChatServer.deliverPending(name, this);
                /* On prévient les autres : le nouveau pseudo apparaît
                   automatiquement dans leur liste. */
                ChatServer.userListChanged(this);
                return true;
            }
        }
    }

    /** Renvoie au client la liste des utilisateurs connectés (sauf lui-même). */
    void sendUserList() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String who : ChatServer.clientsList().keySet()) {
            if (who.equals(name)) continue; // on ne se liste pas soi-même
            if (!first) sb.append("\n");
            sb.append(who);
            first = false;
        }
        send(new Message(Message.USER_LIST, "Serveur", name, sb.toString()));
    }

    /** Relaie un message privé vers le destinataire indiqué. */
    private void relay(Message m) {
        /* On nettoie le contenu ET on force l'expéditeur :
           personne ne peut usurper l'identité d'un autre. */
        m.content = Message.clean(m.content);
        m.from = name;

        ClientHandler dest = ChatServer.clientsList().get(m.to);

        if (dest == this) {
            send(new Message(Message.ERROR, "Serveur", m.to,
                    "Tu ne peux pas t'envoyer un message à toi-même."));
            return;
        }

        if (dest == null) {
            /* Destinataire hors ligne : on GARDE le message et on prévient
               l'expéditeur au lieu de le perdre silencieusement. */
            List<Message> pending = ChatServer.inboxFor(m.to);
            synchronized (pending) {
                pending.add(m);
                while (pending.size() > Message.MAX_INBOX) pending.remove(0);
            }
            send(new Message(Message.INFO, "Serveur", m.to,
                    "« " + m.to + " » est hors ligne. Ton message a été gardé, "
                    + "il sera délivré à sa prochaine connexion."));
            return;
        }

        dest.send(m);
    }

    /** Envoi sécurisé d'un paquet (synchronisé sur le flux de sortie). */
    void send(Message m) {
        try {
            synchronized (out) {
                out.writeObject(m);
                out.flush();
                out.reset();
            }
        } catch (IOException ignored) {
        }
    }

    private void closeAll() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { socket.close(); } catch (IOException ignored) {}
    }

    /** Nettoie un pseudo : lettres (accents compris), chiffres, _ et espaces. */
    private static String sanitizeName(String raw) {
        if (raw == null) return "";
        String cleaned = raw.trim().replaceAll("[^\\p{L}\\p{N}_ ]", "").trim();
        return cleaned.length() <= 24 ? cleaned : cleaned.substring(0, 24);
    }
}