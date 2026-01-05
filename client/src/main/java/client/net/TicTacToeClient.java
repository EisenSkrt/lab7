
package client.net;

import client.events.GameUpdateEvent;
import common.net.NetMessage;
import ocsf.client.AbstractClient;
import org.greenrobot.eventbus.EventBus;

public class TicTacToeClient extends AbstractClient {

    public TicTacToeClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof NetMessage m) {
            EventBus.getDefault().post(new GameUpdateEvent(m));
        }
    }

    @Override
    protected void connectionClosed() {
        EventBus.getDefault().post(new GameUpdateEvent(NetMessage.of(common.net.MessageType.ERROR).withText("Disconnected from server.")));
    }

    @Override
    protected void connectionException(Exception exception) {
        EventBus.getDefault().post(new GameUpdateEvent(NetMessage.of(common.net.MessageType.ERROR).withText("Connection error: " + exception.getMessage())));
    }
}
