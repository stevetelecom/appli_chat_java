import java.io.Serializable;

/**
 * Message échangé entre le client et le serveur.
 *
 * C'est le "paquet" du réseau : sérialisé en Java puis envoyé sur le
 * socket TCP. Envoyeur et récepteur se comprennent grâce au champ "type".
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /* Différents types de messages du protocole */
    public static final String REGISTER    = "REGISTER";     // le client demande un pseudo
    public static final String REGISTER_OK = "REGISTER_OK";  // le serveur accepte le pseudo
    public static final String REQUEST_LIST = "REQUEST_LIST";// le client demande la liste
    public static final String USER_LIST   = "USER_LIST";    // le serveur renvoie la liste
    public static final String CHAT        = "CHAT";         // un vrai message de discussion
    public static final String ERROR       = "ERROR";        // erreur (pseudo pris, …)
    public static final String INFO        = "INFO";         // petite note système (affichage spécial)

    /* Taille maximale d'un message de discussion (limite anti-abus) */
    public static final int MAX_CONTENT = 2000;

    public String type;     // une des constantes ci-dessus
    public String from;     // pseudo de l'expéditeur
    public String to;       // pseudo du destinataire (ou null)
    public String content;  // le texte utile (pseudo, liste, message…)
    public long time;       // horodatage en millisecondes

    /** Constructeur simple pour créer un paquet à envoyer. */
    public Message(String type, String from, String to, String content) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.content = content;
        this.time = System.currentTimeMillis();
    }

    /**
     * Nettoie un texte libre : supprime les caractères de contrôle et
     * limite la longueur. Protège le serveur des messages absurdes.
     */
    public static String clean(String raw) {
        if (raw == null) return "";
        String t = raw.replaceAll("[\\u0000-\\u001F\\u007F]", "");
        return t.length() <= MAX_CONTENT ? t : t.substring(0, MAX_CONTENT);
    }
}