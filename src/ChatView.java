import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * ChatView = deuxième fenêtre : la discussion avec un contact.
 *
 * Elle affiche les bulles de messages (style WhatsApp) dans une
 * ListView, une zone de saisie en bas, et la même signature.
 * Les messages envoyés partent à droite (bulle verte), ceux reçus
 * arrivent à gauche (bulle blanche).
 */
public class ChatView extends BorderPane {

    private static final DateTimeFormatter HEURE =
            DateTimeFormatter.ofPattern("HH:mm");

    private final String contact;
    private final ObservableList<Message> history;
    private final ListView<Message> list = new ListView<>();
    private final TextField input = new TextField();

    public ChatView(String contact) {
        this.contact = contact;
        this.history = Session.historyFor(contact);

        setStyle("-fx-background-color:" + Colors.BG + ";");
        setTop(buildHeader());
        setCenter(buildList());
        setBottom(buildBottom());

        /* La liste observe l'historique partagé : toute nouvelle bulle
           déclenche un défilement automatique vers le bas. */
        history.addListener((ListChangeListener<Message>) c -> scrollToEnd());
        scrollToEnd();
    }

    /* ------------------------------------------------------------------ */
    /*                          Construction de l'UI                       */
    /* ------------------------------------------------------------------ */

    private VBox buildHeader() {
        /* Petit avatar rond avec l'initiale du contact. */
        Label initial = new Label(contact.substring(0, 1).toUpperCase());
        initial.setFont(Font.font("System", FontWeight.BOLD, 18));
        initial.setTextFill(Color.WHITE);
        Circle cercle = new Circle(22, Color.web(Colors.TEAL_MED));
        cercle.setStroke(Color.web(Colors.GREEN));
        cercle.setStrokeWidth(2);
        StackPane avatar = new StackPane(cercle, initial);

        Label title = new Label(contact);
        title.setFont(Font.font("System", FontWeight.BOLD, 17));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("En ligne sur le réseau local");
        subtitle.setFont(Font.font("System", 11));
        subtitle.setTextFill(Color.web(Colors.CLOUD));

        VBox infos = new VBox(1, title, subtitle);
        HBox top = new HBox(12, avatar, infos);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox header = new VBox(top);
        header.setPadding(new Insets(14, 18, 12, 18));
        header.setStyle("-fx-background-color:" + Colors.TEAL_DARK + ";");
        return header;
    }

    private ListView<Message> buildList() {
        list.setItems(history);
        list.setFocusTraversable(false);

        /* Chaque message est rendu comme une bulle arrondie. */
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message m, boolean empty) {
                super.updateItem(m, empty);
                setStyle("-fx-background-color: transparent;");
                setText(null);

                if (empty || m == null) {
                    setGraphic(null);
                    return;
                }

                /* Note système (contact hors ligne...) : centrée, grise, italique. */
                if (Message.INFO.equals(m.type)) {
                    Label note = new Label(m.content);
                    note.setStyle("-fx-text-fill:#667781; -fx-font-style:italic;");
                    note.setFont(Font.font("System", 11));
                    HBox ligne = new HBox(note);
                    ligne.setAlignment(Pos.CENTER);
                    ligne.setPadding(new Insets(3, 8, 3, 8));
                    setGraphic(ligne);
                    return;
                }

                boolean mine = Session.myName != null && Session.myName.equals(m.from);

                Label bulle = new Label(m.content);
                bulle.setWrapText(true);
                bulle.setMaxWidth(340);
                bulle.setPadding(new Insets(9, 12, 9, 12));
                bulle.setFont(Font.font("System", 13));
                bulle.setTextFill(Color.web(Colors.TEXT));
                bulle.setStyle(mine
                        ? "-fx-background-color:" + Colors.BUBBLE_SENT
                        + "; -fx-background-radius:16 16 4 16;"
                        : "-fx-background-color:" + Colors.WHITE
                        + "; -fx-background-radius:16 16 16 4;");

                /* Petite ligne d'information (heure [+ auteur si reçu]). */
                Label meta = new Label((mine ? "" : m.from + "  -  ")
                        + HEURE.format(Instant.ofEpochMilli(m.time)
                        .atZone(ZoneId.systemDefault())));
                meta.setStyle("-fx-text-fill:" + Colors.MUTED + "; -fx-font-size:10;");

                VBox cell = new VBox(2, bulle, meta);
                cell.setAlignment(mine ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);

                /* Un espace flexible pousse la bulle à droite (envoyé) ou à gauche. */
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox box = mine
                        ? new HBox(spacer, cell)
                        : new HBox(cell, spacer);
                box.setPadding(new Insets(4, 10, 4, 10));

                setGraphic(box);
            }
        });
        return list;
    }

    private VBox buildBottom() {
        input.setPromptText("Écrivez votre message...");
        input.setStyle("-fx-background-color:" + Colors.WHITE + ";"
                + "-fx-background-radius:22;"
                + "-fx-padding:9 14 9 14;");

        Button sendButton = new Button("Envoyer");
        sendButton.setStyle("-fx-background-color:" + Colors.TEAL_MED + ";"
                + "-fx-text-fill:" + Colors.WHITE + ";"
                + "-fx-background-radius:20;"
                + "-fx-padding:9 20 9 20;"
                + "-fx-font-weight:bold;"
                + "-fx-cursor:hand;");

        input.setOnAction(e -> send());
        sendButton.setOnAction(e -> send());

        HBox compose = new HBox(10, input, sendButton);
        HBox.setHgrow(input, Priority.ALWAYS);
        compose.setPadding(new Insets(10, 16, 10, 16));
        compose.setStyle("-fx-background-color:" + Colors.BG + ";");

        Label signature = new Label(
                "© 2026 ConnectChat - Réalisé par Mekontso Olivier Steve && Wome Franck");
        signature.setFont(Font.font("System", 10));
        signature.setTextFill(Color.web(Colors.MUTED));
        signature.setPadding(new Insets(0, 16, 6, 16));
        signature.setStyle("-fx-background-color:" + Colors.BG + ";");

        return new VBox(compose, signature);
    }

    /** Fait défiler la liste jusqu'au dernier message. */
    private void scrollToEnd() {
        Platform.runLater(() -> {
            if (!history.isEmpty()) {
                list.scrollTo(history.size() - 1);
            }
        });
    }

    /** Envoie le message saisi à l'attention de "contact". */
    private void send() {
        if (Session.client == null || !Session.client.isRunning()) {
            addInfo("Connexion au serveur perdue. Clique sur « Trouver » pour te reconnecter.");
            return;
        }
        if (Session.myName == null) {
            addInfo("Reconnecte-toi du menu principal, puis réessaie.");
            return;
        }

        String texte = Message.clean(input.getText());
        if (texte.isEmpty()) {
            return;
        }

        Message m = new Message(Message.CHAT, Session.myName, contact, texte);
        Session.client.send(m);

        /* On affiche immédiatement la bulle côté expéditeur. */
        history.add(m);
        input.clear();
        scrollToEnd();
    }

    /** Ajoute une note système au fil de la conversation. */
    private void addInfo(String texte) {
        history.add(new Message(Message.INFO, "Serveur", contact, texte));
        scrollToEnd();
    }
}