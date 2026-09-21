import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * MainView = première fenêtre de l'application.
 *
 * Elle contient :
 *   - un message de bienvenue (vert WhatsApp),
 *   - un champ "pseudo" + le bouton "Trouver" qui liste les personnes
 *     connectées au réseau,
 *   - un tableau à 2 colonnes : nom de l'utilisateur et bouton "Écrire",
 *   - la licence et la signature en bas de fenêtre.
 */
public class MainView extends BorderPane {

    private final TextField pseudoField = new TextField();
    private final Label statusLabel = new Label("Tu n'es pas encore connecté.");
    private final TableView<String> table = new TableView<>();
    private final Button trouverButton = new Button("Trouver");

    public MainView() {
        setStyle("-fx-background-color:" + Colors.BG + ";");
        setTop(buildHeader());
        setCenter(buildBody());
        setBottom(buildFooter());

        /* Le même bouton réagit à la touche Entrée. */
        pseudoField.setOnAction(e -> onTrouver());
        trouverButton.setOnAction(e -> onTrouver());
    }

    /* ------------------------------------------------------------------ */
    /*                          Construction de l'UI                       */
    /* ------------------------------------------------------------------ */

    private VBox buildHeader() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(20, 26, 14, 26));
        header.setStyle("-fx-background-color:" + Colors.TEAL_DARK + ";");

        Label title = new Label("ConnectChat");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Label welcome = new Label(
                "Bienvenue ! Retrouve tes camarades connectés sur le réseau "
                + "et discute avec eux en toute simplicité.");
        welcome.setWrapText(true);
        welcome.setTextFill(Color.web(Colors.CLOUD));

        header.getChildren().addAll(title, welcome);
        return header;
    }

    private VBox buildBody() {
        /* --- Ligne d'identification + bouton Trouver --- */
        Label pseudoLabel = new Label("Ton pseudo :");
        pseudoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        pseudoLabel.setTextFill(Color.web(Colors.TEXT));

        pseudoField.setPromptText("Ex : Steve");
        pseudoField.setPrefWidth(220);
        pseudoField.setStyle(cssField());

        trouverButton.setPrefWidth(130);
        trouverButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        trouverButton.setStyle(cssTrouver());

        HBox identRow = new HBox(10, pseudoLabel, pseudoField, trouverButton);
        identRow.setAlignment(Pos.CENTER_LEFT);

        /* --- Barre de statut (connexion, erreurs...) --- */
        statusLabel.setWrapText(true);
        statusLabel.setTextFill(Color.web(Colors.MUTED));
        statusLabel.setFont(Font.font("System", 12));

        /* --- Le tableau des personnes connectées --- */
        Label tableTitle = new Label("Personnes connectées au réseau");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        tableTitle.setTextFill(Color.web(Colors.TEAL_DARK));

        buildTable();

        VBox body = new VBox(10, identRow, statusLabel, tableTitle, table);
        body.setPadding(new Insets(16, 26, 16, 26));
        VBox.setVgrow(table, Priority.ALWAYS);
        return body;
    }

    /**
     * Construction du tableau à 2 colonnes :
     * colonne "Utilisateur" + colonne avec le bouton "Écrire".
     */
    private void buildTable() {
        table.setPlaceholder(new Label("Aucune personne connectée pour le moment."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.SINGLE);

        /* Colonne 1 : le nom, précédé d'un petit point vert "en ligne". */
        TableColumn<String, String> colNom = new TableColumn<>("Utilisateur");
        colNom.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue()));
        colNom.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Circle dot = new Circle(5, Color.web(Colors.GREEN));
                Label name = new Label(item);
                name.setFont(Font.font("System", FontWeight.BOLD, 14));
                name.setTextFill(Color.web(Colors.TEXT));
                setGraphic(new HBox(10, dot, name));
                setText(null);
            }
        });

        /* Colonne 2 : le bouton "Écrire" qui ouvre la fenêtre de chat. */
        TableColumn<String, Void> colBouton = new TableColumn<>("Action");
        colBouton.setCellFactory(col -> new TableCell<>() {
            private final Button ecrire = new Button("Écrire");

            {
                ecrire.setStyle(cssEcrire());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty) {
                    setGraphic(null);
                } else {
                    ecrire.setOnAction(e ->
                            openChat(getTableView().getItems().get(getIndex())));
                    setGraphic(ecrire);
                }
            }
        });

        table.getColumns().setAll(colNom, colBouton);
    }

    private VBox buildFooter() {
        Label authors = new Label(
                "Réalisé par  Mekontso Olivier Steve  &&  Wome Franck");
        authors.setFont(Font.font("System", FontWeight.BOLD, 13));
        authors.setTextFill(Color.web(Colors.CLOUD));

        Label license = new Label(
                "© 2026 ConnectChat - Licence MIT - Projet pédagogique "
                + "Java : Sockets + JavaFX");
        license.setFont(Font.font("System", 11));
        license.setTextFill(Color.web(Colors.MUTED));

        VBox footer = new VBox(2, authors, license);
        footer.setPadding(new Insets(10, 26, 10, 26));
        footer.setStyle("-fx-background-color:" + Colors.FOOTER + ";");
        footer.setAlignment(Pos.CENTER_LEFT);
        return footer;
    }

    /* ------------------------------------------------------------------ */
    /*                              Logique                                */
    /* ------------------------------------------------------------------ */

    /**
     * Écouteur réseau : tous les événements arrivent ici, puis on les
     * retransmet sur le thread JavaFX (Platform.runLater) pour pouvoir
     * modifier l'interface en toute sécurité.
     */
    private final ChatClient.ClientListener listener = new ChatClient.ClientListener() {
        @Override
        public void onMessage(Message m) {
            Platform.runLater(() -> handleMessage(m));
        }

        @Override
        public void onDisconnected(String reason) {
            Platform.runLater(() -> {
                Session.client = null;
                Session.myName = null;
                statusLabel.setText(reason);
                trouverButton.setDisable(false);
            });
        }
    };

    /** Traite un message reçu par le serveur. */
    private void handleMessage(Message m) {
        switch (m.type) {
            case Message.REGISTER_OK:
                Session.myName = m.to;
                statusLabel.setText("Connecté au réseau sous le pseudo « " + m.to + " ».");
                /* À chaque connexion (ou reconnexion), on repart sur une liste
                   fraîche. Le bouton est réactivé ici, et pas seulement à
                   l'arrivée de USER_LIST : il ne peut plus rester bloqué. */
                trouverButton.setDisable(false);
                if (Session.client != null && Session.client.isRunning()) {
                    Session.client.send(new Message(Message.REQUEST_LIST, m.to, null, null));
                }
                break;

            case Message.USER_LIST:
                fillTable(m.content);
                long nb = table.getItems().size();
                statusLabel.setText(nb == 0
                        ? "Personne n'est connecté pour le moment."
                        : nb + " personne(s) connectée(s) sur le réseau.");
                trouverButton.setDisable(false);
                break;

            case Message.CHAT:
                /* Un nouvel arrivant dans la conversation : on l'ajoute à
                   l'historique du contact (et on le sauvegarde sur disque). */
                Session.historyFor(m.from).add(m);
                Session.sauver(m.from);
                break;

            case Message.INFO:
                if (m.to != null && !m.to.isEmpty()) {
                    Session.historyFor(m.to).add(m);
                    Session.sauver(m.to);
                }
                break;

            case Message.ERROR:
                statusLabel.setText(m.content);
                trouverButton.setDisable(false);
                if (m.to != null && !m.to.isEmpty()) {
                    /* Erreur liée à un contact précis : on l'affiche dans
                       la fenêtre de chat correspondante, si elle existe. */
                    Session.historyFor(m.to)
                            .add(new Message(Message.INFO, "Serveur", m.to, m.content));
                    Session.sauver(m.to);
                }
                break;

            default:
                break;
        }
    }

    /** Clic sur "Trouver" : connexion + demande de la liste des personnes. */
    private void onTrouver() {
        String name = pseudoField.getText().trim();

        if (name.length() < 2) {
            statusLabel.setText("Entre d'abord un pseudo d'au moins 2 caractères.");
            return;
        }
        trouverButton.setDisable(true);
        statusLabel.setText("Connexion au serveur (" + App.HOST + ":" + App.PORT + ")...");

        /* La connexion réseau ne doit JAMAIS bloquer l'interface. */
        new Thread(() -> {
            try {
                /* 1. On crée (ou réutilise) la connexion au serveur. */
                if (Session.client == null || !Session.client.isRunning()) {
                    Session.client = new ChatClient(App.HOST, App.PORT, listener);
                }

                /* 2. Déjà enregistré avec ce pseudo ? juste une liste à jour.
                   Sinon on (ré)enregistre : le serveur gère les reconnexions. */
                if (Session.myName != null && Session.myName.equals(name)) {
                    Session.client.send(new Message(Message.REQUEST_LIST, name, null, null));
                } else {
                    Session.client.send(new Message(Message.REGISTER, name, null, name));
                }
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Session.client = null;
                    Session.myName = null;
                    statusLabel.setText("Serveur injoignable sur " + App.HOST + ":"
                            + App.PORT + ". Démarre ChatServer puis réessaie.");
                    trouverButton.setDisable(false);
                });
            }
        }).start();
    }

    /** Remplit le tableau avec les noms reçus du serveur. */
    private void fillTable(String content) {
        if (content == null || content.isEmpty()) {
            table.getItems().setAll(List.of());
        } else {
            table.getItems().setAll(List.of(content.split("\n")));
        }
    }

    /** Ouvre la deuxième fenêtre : la discussion avec un contact. */
    private void openChat(String contact) {
        Stage stage = new Stage();
        stage.setTitle("Discussion avec " + contact);
        stage.getIcons().addAll(Icons.list());
        stage.setScene(new javafx.scene.Scene(new ChatView(contact), 500, 620));
        stage.show();
    }

    /* ------------------------------------------------------------------ */
    /*                         Petites feuilles de style                   */
    /* ------------------------------------------------------------------ */

    private String cssField() {
        return "-fx-background-color:" + Colors.WHITE + ";"
                + "-fx-background-radius:20;"
                + "-fx-padding:8 14 8 14;"
                + "-fx-border-color:" + Colors.TEAL_MED + ";"
                + "-fx-border-radius:20;";
    }

    private String cssTrouver() {
        return "-fx-background-color:" + Colors.GREEN + ";"
                + "-fx-text-fill:#06443C;"
                + "-fx-background-radius:20;"
                + "-fx-padding:8 16 8 16;"
                + "-fx-cursor:hand;";
    }

    private String cssEcrire() {
        return "-fx-background-color:" + Colors.TEAL_MED + ";"
                + "-fx-text-fill:" + Colors.WHITE + ";"
                + "-fx-background-radius:18;"
                + "-fx-padding:6 18 6 18;"
                + "-fx-cursor:hand;";
    }
}