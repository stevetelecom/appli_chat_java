import javafx.application.Application;

/**
 * Classe de lancement.
 *
 * On ne lance JAMAIS directement une classe qui étend Application :
 * JavaFx peut alors lever "JavaFX runtime components are missing".
 * C'est cette classe qui démarre proprement l'application (App).
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}