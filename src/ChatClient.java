import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ChatClient = la connexion TCP du côté utilisateur.
 *
 * Il gère le socket, l'envoi des messages et un thread qui écoute
 * en continu ce que le serveur renvoie (les messages reçus).
 * Chaque événement réseau est transmis au "listener" passé par l'UI.
 */
public class ChatClient implements AutoCloseable {

    /** Délai maximum (en ms) pour établir la connexion avec le serveur. */
    public static final int TIMEOUT_MS = 5000;

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ClientListener listener;
    private volatile boolean running = true;

    /** Interface appelée à chaque événement réseau reçu. */
    public interface ClientListener {
        void onMessage(Message m);
        void onDisconnected(String reason);
    }

    /**
     * Ouvre la connexion vers le serveur et démarre la lecture.
     * Important : l'ObjectOutputStream est créé AVANT l'ObjectInputStream
     * pour éviter un blocage des deux côtés lors du handshake TCP.
     */
    public ChatClient(String host, int port, ClientListener listener) throws IOException {
        this.listener = listener;

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);

        out = new ObjectOutputStream(socket.getOutputStream());
        new Thread(this::readLoop, "lecteur-reseau").start();
    }

    /** Envoie un paquet au serveur (synchronisé pour éviter les écritures croisées). */
    public void send(Message m) {
        try {
            synchronized (out) {
                out.writeObject(m);
                out.flush();
                out.reset(); // évite que Java ne mette en cache des objets obsolètes
            }
        } catch (IOException e) {
            close("Connexion perdue : " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    /** Boucle de lecture : reçoit les messages tant que la connexion est ouverte. */
    private void readLoop() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (running) {
                Object o = in.readObject();
                if (o instanceof Message) {
                    listener.onMessage((Message) o);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // déconnexion ou flux fermé : on termine proprement
        } finally {
            close("Le serveur a fermé la connexion.");
        }
    }

    /** Ferme proprement la connexion et prévient l'interface. */
    @Override
    public void close() {
        close("Connexion fermée.");
    }

    private void close(String reason) {
        if (!running) return;
        running = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        if (listener != null) {
            listener.onDisconnected(reason);
        }
    }
}