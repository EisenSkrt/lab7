
package common.net;

import java.io.Serializable;
import java.util.Arrays;

public class NetMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public MessageType type;

    // move
    public int row = -1;
    public int col = -1;

    // game state
    public char[][] board;        // 3x3
    public char yourSymbol = '\0';      // 'X' or 'O'
    public char currentTurn = '\0';     // whose turn now
    public boolean gameOver = false;

    // text for UI
    public String text;

    public static NetMessage of(MessageType type) {
        NetMessage m = new NetMessage();
        m.type = type;
        return m;
    }

    public NetMessage withText(String t) {
        this.text = t;
        return this;
    }

    public NetMessage withMove(int r, int c) {
        this.row = r;
        this.col = c;
        return this;
    }

    public NetMessage withBoard(char[][] b) {
        this.board = b;
        return this;
    }

    @Override
    public String toString() {
        return "NetMessage{" +
                "type=" + type +
                ", row=" + row +
                ", col=" + col +
                ", yourSymbol=" + yourSymbol +
                ", currentTurn=" + currentTurn +
                ", gameOver=" + gameOver +
                ", text='" + text + "'" +
                ", board=" + (board==null? "null" : Arrays.deepToString(board)) +
                '}';
    }
}
