
package server;

import common.net.NetMessage;
import common.net.MessageType;
import ocsf.server.ConnectionToClient;

import java.util.*;

public class GameManager {

    private final char[][] board = new char[3][3];
    private final Map<ConnectionToClient, Character> symbolByClient = new HashMap<>();
    private final List<ConnectionToClient> players = new ArrayList<>(2);

    private char currentTurn = '\0';
    private boolean gameOver = false;

    public synchronized boolean hasTwoPlayers() {
        return players.size() == 2;
    }

    public synchronized boolean isPlayer(ConnectionToClient c) {
        return symbolByClient.containsKey(c);
    }

    public synchronized void onClientConnected(ConnectionToClient c) {
        if (players.size() >= 2) return; // ignore extra clients in this lab

        players.add(c);
        // assign later when we have 2
    }

    public synchronized void onClientDisconnected(ConnectionToClient c) {
        players.remove(c);
        symbolByClient.remove(c);
        reset();
    }

    public synchronized void startIfReady() {
        if (players.size() != 2) return;

        // Randomly assign X/O
        List<Character> symbols = new ArrayList<>(List.of('X', 'O'));
        Collections.shuffle(symbols, new Random());
        symbolByClient.put(players.get(0), symbols.get(0));
        symbolByClient.put(players.get(1), symbols.get(1));

        // Randomly choose who starts
        currentTurn = new Random().nextBoolean() ? 'X' : 'O';
        gameOver = false;
    }

    public synchronized NetMessage buildStart(ConnectionToClient c) {
        NetMessage m = NetMessage.of(MessageType.START);
        m.board = copyBoard();
        m.yourSymbol = symbolByClient.getOrDefault(c, '\0');
        m.currentTurn = currentTurn;
        m.gameOver = gameOver;
        m.text = "Game started! You are " + m.yourSymbol + ". Turn: " + currentTurn;
        return m;
    }

    public synchronized NetMessage buildWaiting() {
        return NetMessage.of(MessageType.WAITING).withText("Waiting for another player...");
    }

    public synchronized NetMessage buildUpdateFor(ConnectionToClient c, String text) {
        NetMessage m = NetMessage.of(MessageType.UPDATE);
        m.board = copyBoard();
        m.yourSymbol = symbolByClient.getOrDefault(c, '\0');
        m.currentTurn = currentTurn;
        m.gameOver = gameOver;
        m.text = text;
        return m;
    }

    public synchronized NetMessage buildEndFor(ConnectionToClient c, String text) {
        NetMessage m = NetMessage.of(MessageType.END);
        m.board = copyBoard();
        m.yourSymbol = symbolByClient.getOrDefault(c, '\0');
        m.currentTurn = currentTurn;
        m.gameOver = true;
        m.text = text;
        return m;
    }

    public synchronized NetMessage buildErrorFor(ConnectionToClient c, String text) {
        NetMessage m = NetMessage.of(MessageType.ERROR);
        m.board = copyBoard();
        m.yourSymbol = symbolByClient.getOrDefault(c, '\0');
        m.currentTurn = currentTurn;
        m.gameOver = gameOver;
        m.text = text;
        return m;
    }

    public synchronized boolean isGameOver() {
        return gameOver;
    }

    public synchronized Character getSymbol(ConnectionToClient c) {
        return symbolByClient.get(c);
    }

    public synchronized boolean playMove(ConnectionToClient c, int r, int col) {
        if (gameOver) return false;
        if (!hasTwoPlayers()) return false;
        Character sym = symbolByClient.get(c);
        if (sym == null) return false;
        if (sym != currentTurn) return false;
        if (r < 0 || r > 2 || col < 0 || col > 2) return false;
        if (board[r][col] != '\0') return false;

        board[r][col] = sym;

        // win / draw?
        Character winner = winner();
        if (winner != null) {
            gameOver = true;
        } else if (isDraw()) {
            gameOver = true;
        } else {
            currentTurn = (currentTurn == 'X') ? 'O' : 'X';
        }
        return true;
    }

    public synchronized Character winner() {
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != '\0' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return board[i][0];
            if (board[0][i] != '\0' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return board[0][i];
        }
        if (board[0][0] != '\0' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return board[0][0];
        if (board[0][2] != '\0' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return board[0][2];
        return null;
    }

    public synchronized boolean isDraw() {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == '\0') return false;
            }
        }
        return winner() == null;
    }

    public synchronized char[][] copyBoard() {
        char[][] out = new char[3][3];
        for (int r = 0; r < 3; r++) System.arraycopy(board[r], 0, out[r], 0, 3);
        return out;
    }

    public synchronized void reset() {
        for (int r = 0; r < 3; r++) Arrays.fill(board[r], '\0');
        symbolByClient.clear();
        currentTurn = '\0';
        gameOver = false;
    }
}
