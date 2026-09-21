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
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    /**
     * Appelé à la fermeture de la fenêtre : on termine proprement les
     * sauvegardes restées en file pour ne jamais perdre les derniers
     * messages quand on quitte l'application.
     */
    public static void fermer() {
        SAUVEGARDE.shutdown();
        try {
            SAUVEGARDE.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            /* Fichier absent ou corrompu : on met de côté le fichier
               suspect (sauvegarde de secours) avant de reprendre un
               historique vide. Ainsi on ne l'écrase JAMAIS par mégarde
               et on peut récupérer les données à la main. */
            protegerFichierSuspect(fichier(contact));
            return FXCollections.observableArrayList();
        }
    }

    /**
     * Écrit un historique sur disque de façon ATOMIQUE : on écrit d'abord
     * un fichier temporaire, puis on le renomme d'un coup. Si l'application
     * est tuée en plein milieu, soit l'ancien fichier est intact, soit le
     * nouveau est complet — jamais un mélange corrompu des deux.
     */
    private static void ecrire(String contact, List<Message> liste) {
        try {
            Files.createDirectories(DOSSIER);
            Path cible = fichier(contact).toPath();
            Path temp = cible.resolveSibling(cible.getFileName() + ".tmp");
            try (ObjectOutputStream out =
                         new ObjectOutputStream(new FileOutputStream(temp.toFile()))) {
                out.writeObject(liste);
                out.flush();
            }
            try {
                Files.move(temp, cible, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                /* Certains systèmes de fichiers n'acceptent pas ATOMIC_MOVE :
                   on se rabat sur un remplacement ordinaire. */
                Files.move(temp, cible, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // pas grave : on réessaiera au prochain ajout
        }
    }

    /** Met à l'abri un fichier non lisible avant tout réécriture. */
    private static void protegerFichierSuspect(File fichier) {
        if (!fichier.isFile() || fichier.length() == 0) return;
        File secours = new File(fichier.getPath() + ".corrompu");
        try {
            Files.move(fichier.toPath(), secours.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    /** Le fichier d'un contact (le pseudo est nettoyé pour servir de nom de fichier). */
    private static File fichier(String contact) {
        String nomPropre = contact.replaceAll("[^\\p{L}\\p{N}._-]", "_");
        return DOSSIER.resolve(nomPropre + ".dat").toFile();
    }
}