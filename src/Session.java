import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session = état partagé de l'application.
 *
 * Toutes les fenêtres (liste principale + fenêtres de chat) consultent
 * cette classe pour connaître la connexion en cours et l'historique
 * des conversations. C'est le "mémoire vive" du client.
 */
public final class Session {

    private Session() { } // classe utilitaire

    /** La connexion réseau actuelle (null si jamais connecté). */
    public static ChatClient client;

    /** Le pseudo enregistré et accepté par le serveur. */
    public static String myName;

    /* Une liste de messages par contact, conservée pendant toute la session. */
    private static final Map<String, ObservableList<Message>> histories =
            new ConcurrentHashMap<>();

    /**
     * Retourne (et crée au besoin) l'historique des messages
     * échangés avec un contact donné.
     */
    public static ObservableList<Message> historyFor(String contact) {
        return histories.computeIfAbsent(contact, k -> FXCollections.observableArrayList());
    }
}