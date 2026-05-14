package Utils;

import javafx.geometry.Pos;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationHelper {

    private static Window getWindow() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    public static void success(String title, String message) {
        Notifications n = Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(3));
        
        Window owner = getWindow();
        if (owner != null) n.owner(owner);
        
        n.showInformation();
    }

    public static void error(String title, String message) {
        Notifications n = Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(4));

        Window owner = getWindow();
        if (owner != null) n.owner(owner);

        n.showError();
    }

    public static void warning(String title, String message) {
        Notifications n = Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.TOP_RIGHT)
                .hideAfter(Duration.seconds(3));

        Window owner = getWindow();
        if (owner != null) n.owner(owner);

        n.showWarning();
    }
}
