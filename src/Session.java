import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Session = état partagé de l'application.
 *
 * Toutes les fenêtres (liste principale + fenêtres de chat) consultent
 * cette classe pour connaître la connexion en cours et l'historique
 * des conversations.
 *
 * L'historique est EN PLUS sauvegardé sur disque (dossier ~/.connectchat)
 * : il survit donc aux fermetures et aux redémarrages de l'application,
 * contrairement à une simple carte en mémoire. La sauvegarde se fait en
 * tâche de fond pour ne jamais ralentir l'interface.
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

    /* Dossier de sauvegarde de l'historique, dans le dossier de l'utilisateur. */
    private static final Path DOSSIER =
            Path.of(System.getProperty("user.home"), ".connectchat");

    /* Un seul fil de sauvegarde en arrière-plan (daemon : ne bloque pas la fermeture). */
    private static final ExecutorService SAUVEGARDE =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "sauvegarde-historique");
                t.setDaemon(true);
                return t;
            });

    /**
     * Retourne (et crée au besoin) l'historique des messages
     * échangés avec un contact donné.
     */
    public static ObservableList<Message> historyFor(String contact) {
        return histories.computeIfAbsent(contact, Session::charger);
    }

    /**
     * Sauvegarde en tâche de fond l'historique d'un contact.
     * À appeler sur le fil de l'interface après chaque ajout.
     */
    public static void sauver(String contact) {
        ObservableList<Message> liste = histories.get(contact);
        if (liste == null) return;
        List<Message> copie = List.copyOf(liste); // copie réalisée sur le fil de l'UI
        SAUVEGARDE.execute(() -> ecrire(contact, copie));
    }

    /** Recharge un historique depuis le disque (ou une liste vide au premier lancement). */
    private static ObservableList<Message> charger(String contact) {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(fichier(contact)))) {
            Object lu = in.readObject();
            @SuppressWarnings("unchecked")
            List<Message> liste = (List<Message>) lu;
            return liste == null
                    ? FXCollections.observableArrayList()
                    : FXCollections.observableArrayList(liste);
        } catch (Exception e) {
            /* Fichier absent ou corrompu : on repart d'un historique vide,
               jamais de crash au démarrage. */
            return FXCollections.observableArrayList();
        }
    }

    /** Écrit un historique sur disque. */
    private static void ecrire(String contact, List<Message> liste) {
        try {
            Files.createDirectories(DOSSIER);
            try (ObjectOutputStream out =
                         new ObjectOutputStream(new FileOutputStream(fichier(contact)))) {
                out.writeObject(liste);
            }
        } catch (IOException ignored) {
            // pas grave : on réessaiera au prochain ajout
        }
    }

    /** Le fichier d'un contact (le pseudo est nettoyé pour servir de nom de fichier). */
    private static File fichier(String contact) {
        String nomPropre = contact.replaceAll("[^\\p{L}\\p{N}._-]", "_");
        return DOSSIER.resolve(nomPropre + ".dat").toFile();
    }
}