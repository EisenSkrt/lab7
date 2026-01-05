
package client;

import client.events.GameUpdateEvent;
import client.net.TicTacToeClient;
import common.net.MessageType;
import common.net.NetMessage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.greenrobot.eventbus.Subscribe;

public class ClientController {

    private final BorderPane root = new BorderPane();

    // connect pane
    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField("3000");
    private final Button connectBtn = new Button("Connect");
    private final Label connectStatus = new Label("Not connected");

    // game pane
    private final GridPane grid = new GridPane();
    private final Button[][] cells = new Button[3][3];
    private final Label info = new Label("Connect to start.");
    private final Button resetLocalBtn = new Button("Clear board (local UI)");

    private TicTacToeClient client;
    private char yourSymbol = '\0';
    private char currentTurn = '\0';
    private boolean gameOver = false;

    public ClientController() {
        root.setPadding(new Insets(12));

        VBox top = new VBox(10, buildConnectPane(), new Separator(), info);
        top.setPadding(new Insets(0,0,10,0));
        root.setTop(top);

        root.setCenter(buildBoard());
        root.setBottom(buildBottom());

        setBoardEnabled(false);
        wire();
    }

    private HBox buildConnectPane() {
        hostField.setPrefWidth(160);
        portField.setPrefWidth(90);

        HBox box = new HBox(10,
                new Label("Host:"), hostField,
                new Label("Port:"), portField,
                connectBtn,
                connectStatus
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox buildBoard() {
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                final int rr = r, cc = c;
                Button b = new Button("");
                b.setMinSize(120, 120);
                b.setStyle("-fx-font-size: 40; -fx-font-weight: bold;");
                b.setOnAction(e -> sendMove(rr, cc));
                cells[r][c] = b;
                grid.add(b, c, r);
            }
        }
        VBox v = new VBox(10, grid);
        v.setAlignment(Pos.CENTER);
        return v;
    }

    private HBox buildBottom() {
        resetLocalBtn.setOnAction(e -> clearBoardUI());
        HBox box = new HBox(10, resetLocalBtn);
        box.setPadding(new Insets(10,0,0,0));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void wire() {
        connectBtn.setOnAction(e -> connect());
    }

    public Pane getRoot() {
        return root;
    }

    private void connect() {
        // lock UI immediately
        connectBtn.setDisable(true);
        hostField.setDisable(true);
        portField.setDisable(true);

        String host = hostField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (Exception ex) {
            connectStatus.setText("Bad port");
            // unlock because we didn't actually connect
            connectBtn.setDisable(false);
            hostField.setDisable(false);
            portField.setDisable(false);
            return;
        }

        try {
            client = new TicTacToeClient(host, port);
            client.openConnection();
            connectStatus.setText("Connected ✅");
            info.setText("Connected. Waiting for server...");
            // keep locked on success
        } catch (Exception ex) {
            connectStatus.setText("Connect failed ❌");
            info.setText("Error: " + ex.getMessage());
            // unlock on failure so user can retry
            connectBtn.setDisable(false);
            hostField.setDisable(false);
            portField.setDisable(false);
        }
    }


    private void sendMove(int r, int c) {
        if (client == null || gameOver) return;
        if (yourSymbol == '\0') return;
        if (yourSymbol != currentTurn) {
            info.setText("Not your turn.");
            return;
        }

        NetMessage m = NetMessage.of(MessageType.MOVE).withMove(r, c);
        try {
            client.sendToServer(m);
        } catch (Exception ex) {
            info.setText("Send failed: " + ex.getMessage());
        }
    }

    @Subscribe
    public void onUpdate(GameUpdateEvent ev) {
        Platform.runLater(() -> applyMessage(ev.message));
    }

    private void applyMessage(NetMessage m) {
        if (m == null) return;

        if (m.type == MessageType.WAITING) {
            info.setText(m.text != null ? m.text : "Waiting...");
            setBoardEnabled(false);
            return;
        }

        if (m.type == MessageType.START || m.type == MessageType.UPDATE || m.type == MessageType.END || m.type == MessageType.ERROR) {
            if (m.board != null) renderBoard(m.board);
            if (m.yourSymbol != '\0') yourSymbol = m.yourSymbol;
            if (m.currentTurn != '\0') currentTurn = m.currentTurn;
            gameOver = m.gameOver;

            String status = (m.text != null ? m.text : "");
            String turnTxt = (currentTurn == '\0') ? "" : (" | Turn: " + currentTurn);
            String youTxt = (yourSymbol == '\0') ? "" : (" | You: " + yourSymbol);
            info.setText(status + youTxt + turnTxt);

            boolean canPlay = !gameOver && yourSymbol != '\0' && yourSymbol == currentTurn;
            setBoardEnabled(canPlay);
        }
    }

    private void renderBoard(char[][] b) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                char ch = b[r][c];
                cells[r][c].setText(ch == '\0' ? "" : String.valueOf(ch));
                // disable filled cells always
                if (ch != '\0') cells[r][c].setDisable(true);
            }
        }
    }

    private void setBoardEnabled(boolean enabled) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                boolean filled = !cells[r][c].getText().isEmpty();
                cells[r][c].setDisable(!enabled || filled);
            }
        }
    }

    private void clearBoardUI() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cells[r][c].setText("");
                cells[r][c].setDisable(true);
            }
        }
    }

    public void shutdown() {
        try {
            if (client != null) client.closeConnection();
        } catch (Exception ignored) {}
    }
}
