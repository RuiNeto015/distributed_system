package protocol.data;

import java.io.Serializable;

/**
 * Class that represents the transmitted object between sockets
 *
 * @param <T>
 */
public class Packet<T> implements Serializable {
    private final T data;

    /**
     * Default class constructor
     */
    public Packet() {
        this.data = null;
    }

    /**
     * Parametrized class constructor
     *
     * @param data the packet data
     */
    public Packet(T data) {
        this.data = data;
    }

    /**
     * Getter for the packet data
     *
     * @return the packet data
     */
    public T getData() {
        return data;
    }
}
