
package client;

import client.net.TicTacToeClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Lab7 TicTacToe Client");

        // Build UI
        ClientController controller = new ClientController();

        Scene scene = new Scene(controller.getRoot(), 520, 600);
        stage.setScene(scene);
        stage.show();

        // Register controller to EventBus
        EventBus.getDefault().register(controller);

        // Make sure we disconnect on close
        stage.setOnCloseRequest(e -> {
            try { controller.shutdown(); } catch (Exception ignored) {}
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
