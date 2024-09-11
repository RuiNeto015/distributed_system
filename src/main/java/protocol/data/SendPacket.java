package protocol.data;

import java.io.Serializable;

/**
 * Class that represents a packet that is sent to the local node
 * Send packets are created on the client and then sent to the local node, and then sent to the central node
 *
 * @param <T>
 */
public class SendPacket<T> extends Packet<T> implements Serializable {
    private final String methodSignature;
    private int clientSender;
    private int localNodeSender;

    /**
     * Class constructor
     *
     * @param methodSignature the method signature of the api instance, held by the central node, that will be executed
     */
    public SendPacket(String methodSignature) {
        super();
        this.methodSignature = methodSignature;
    }

    /**
     * Class constructor
     *
     * @param methodSignature the method signature of the api instance, held by the central node, that will be executed
     * @param data the args the method takes
     */
    public SendPacket(String methodSignature, T data) {
        super(data);
        this.methodSignature = methodSignature;
    }

    /**
     * Getter for the method signature
     *
     * @return the method signature
     */
    public String getMethodSignature() {
        return this.methodSignature;
    }

    /**
     * Setter for the port of the client that sent this packet
     *
     * @param clientSender the port of the client that sent this packet
     */
    public void setClientSender(int clientSender) {
        this.clientSender = clientSender;
    }

    /**
     * Setter for the port of the local node that is sending this packet to the local node
     *
     * @param localNodeSender the port of the local node that is sending this packet to the local node
     */
    public void setLocalNodeSender(int localNodeSender) {
        this.localNodeSender = localNodeSender;
    }

    /**
     * Getter for the port of the client that sent this packet
     *
     * @return the port of the client that sent this packet
     */
    public int getClientSender() {
        return this.clientSender;
    }

    /**
     * Getter for the port of the local node that is sending this packet to the local node
     *
     * @return the port of the local node that is sending this packet to the local node
     */
    public int getLocalNodeSender() {
        return this.localNodeSender;
    }
}
