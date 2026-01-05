
package server;

import common.net.NetMessage;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TicTacToeServer extends AbstractServer {

    private final GameManager game = new GameManager();

    public TicTacToeServer(int port) {
        super(port);
    }

    @Override
    protected synchronized void clientConnected(ConnectionToClient client) {
        game.onClientConnected(client);

        try {
            if (!game.hasTwoPlayers()) {
                client.sendToClient(game.buildWaiting());
            } else {
                // We have 2 players -> start game and send START to both
                game.startIfReady();
                for (var c : getClients()) {
                    c.sendToClient(game.buildStart(c));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        game.onClientDisconnected(client);
        // Notify remaining player (if any)
        for (var c : getClients()) {
            try {
                c.sendToClient(game.buildWaiting());
            } catch (IOException ignored) {}
        }
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (!(msg instanceof NetMessage m)) return;

        switch (m.type) {
            case MOVE -> handleMove(m, client);
            default -> {
                // ignore
            }
        }
    }

    private synchronized void handleMove(NetMessage m, ConnectionToClient client) {
        if (!game.isPlayer(client)) {
            safeSend(client, game.buildErrorFor(client, "You are not registered as a player."));
            return;
        }
        boolean ok = game.playMove(client, m.row, m.col);
        if (!ok) {
            safeSend(client, game.buildErrorFor(client, "Illegal move / not your turn / cell taken."));
            return;
        }

        Character winner = game.winner();
        if (winner != null) {
            broadcastEnd("Winner: " + winner + " 🎉");
        } else if (game.isDraw()) {
            broadcastEnd("Draw 🤝");
        } else {
            broadcastUpdate("Move accepted. Turn: " + game.buildUpdateFor(client, "").currentTurn);
        }
    }

    private void broadcastUpdate(String text) {
        for (var c : getClients()) {
            safeSend(c, game.buildUpdateFor(c, text));
        }
    }

    private void broadcastEnd(String text) {
        for (var c : getClients()) {
            safeSend(c, game.buildEndFor(c, text));
        }
    }

    private void safeSend(ConnectionToClient c, NetMessage m) {
        try {
            c.sendToClient(m);
        } catch (IOException ignored) {}
    }

    private List<ConnectionToClient> getClients() {
        return Arrays.stream(getClientConnections())
                .filter(ConnectionToClient.class::isInstance)
                .map(ConnectionToClient.class::cast)
                .toList();
    }
}
