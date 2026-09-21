import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatServer = le "central" du réseau.
 *
 * Il écoute sur un port et garde la liste des utilisateurs connectés.
 * Il relaie les messages privés d'un client à un autre. Aucune
 * interface graphique : il s'exécute en console (terminal).
 */
public class ChatServer {

    public static final int DEFAULT_PORT = 5050;

    /* Tableau des clients connectés : pseudo -> gestionnaire de connexion. */
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide, on utilise " + DEFAULT_PORT);
            }
        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("================================================");
            System.out.println("   ConnectChat - Serveur de messagerie");
            System.out.println("   En écoute sur le port " + port);
            System.out.println("   Prêt à recevoir les clients...");
            System.out.println("================================================");

            /* Boucle principale : accepte chaque nouveau client et lui
               ouvre son propre thread (un par connexion). */
            while (true) {
                try {
                    Socket socket = server.accept();
                    System.out.println("[+] Nouvelle connexion depuis "
                            + socket.getRemoteSocketAddress());
                    new ClientHandler(socket).start();
                } catch (IOException e) {
                    System.out.println("Serveur arrêté.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de démarrer le serveur sur le port "
                    + port + " : " + e.getMessage());
        }
    }

    /** Rend la liste partagée accessible aux gestionnaires. */
    static Map<String, ClientHandler> clientsList() {
        return clients;
    }
}