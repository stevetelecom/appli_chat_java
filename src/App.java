import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée JavaFX de l'application ConnectChat.
 *
 * On prépare ici la fenêtre principale (MainView) et on lit les
 * paramètres optionnels de la ligne de commande :
 *   --host=192.168.1.10  --port=5050
 */
public class App extends Application {

    /* Paramètres réseau par défaut (modifiables à la ligne de commande). */
    public static String HOST = "localhost";
    public static int PORT = 5050;

    @Override
    public void start(Stage stage) {
        /* Lecture des arguments : --host=... et --port=... */
        var params = getParameters().getNamed();
        if (params.containsKey("host")) HOST = params.get("host");
        if (params.containsKey("port")) {
            try {
                PORT = Integer.parseInt(params.get("port"));
            } catch (NumberFormatException ignored) {
            }
        }

        stage.setTitle("ConnectChat - Chat local");
        stage.getIcons().addAll(Icons.list());
        stage.setScene(new Scene(new MainView(), 920, 660));
        stage.show();
    }

    public static void main(String[] args) {
        Launcher.main(args);
    }
}