
package common.net;

public enum MessageType {
    CONNECT,          // client -> server (optional; server also handles OCSF connect hook)
    WAITING,          // server -> client (waiting for 2nd player)
    START,            // server -> client (assigned symbol + who starts)
    MOVE,             // client -> server (row/col)
    UPDATE,           // server -> client (board + turn)
    END,              // server -> client (win/draw message)
    ERROR             // server -> client (illegal move / not your turn)
}
