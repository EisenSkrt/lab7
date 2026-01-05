
package server;

public class TicTacToeServerMain {
    public static void main(String[] args) throws Exception {
        int port = 3000;
        if (args.length >= 1) port = Integer.parseInt(args[0]);

        TicTacToeServer server = new TicTacToeServer(port);
        System.out.println("Server listening on port " + port);
        server.listen();
    }
}
