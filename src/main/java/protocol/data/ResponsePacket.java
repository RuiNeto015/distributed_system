package protocol.data;

import java.io.Serializable;

/**
 * Class that represents a response packet
 * Response packets objects are created on the central node, and then send back to the respective/s local node/s
 * and then send packet to the respective/s client/s
 *
 * @param <T>
 */
public class ResponsePacket<T> extends Packet<T> implements Serializable {
    private final int clientSender;
    private final int localNodeSender;

    /**
     * Class constructor
     *
     * @param clientSender the client that sent the 'send packet'
     * @param localNodeSender the local node that sent the 'send packet'
     * @param data the packet data
     */
    public ResponsePacket(
            int clientSender,
            int localNodeSender,
            T data
            ) {
        super(data);
        this.clientSender = clientSender;
        this.localNodeSender = localNodeSender;
    }

    /**
     * Getter for the client that sent the 'send packet'
     *
     * @return the client that sent the 'send packet'
     */
    public int getClientSender() {
        return this.clientSender;
    }

    /**
     * Getter for the local node that sent the 'send packet'
     *
     * @return the local node that sent the 'send packet'
     */
    public int getLocalNodeSender() {
        return this.localNodeSender;
    }
}
