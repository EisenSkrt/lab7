
package client.events;

import common.net.NetMessage;

public class GameUpdateEvent {
    public final NetMessage message;
    public GameUpdateEvent(NetMessage message) {
        this.message = message;
    }
}
