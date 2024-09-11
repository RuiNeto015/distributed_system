package protocol.servers;

import protocol.data.SendPacket;
import java.util.List;

/**
 * Class that controls the access to the queue of packets that the local node has to send to the central node
 */
public class PacketTrafficHandler {
    private final List<SendPacket<?>> packetsToSend;

    /**
     * Class constructor
     *
     * @param packetsToSend the list of packets to send
     */
    public PacketTrafficHandler(List<SendPacket<?>> packetsToSend) {
        this.packetsToSend = packetsToSend;
    }

    /**
     * Function that waits while the list is empty
     *
     * @throws InterruptedException
     */
    synchronized public void waitWhileListEmpty() throws InterruptedException {
        while (this.packetsToSend.isEmpty()) {
            wait();
        }
    }

    /**
     * Function that notifies threads when a packet is added to the list
     */
    synchronized public void notifyElementAddedToTheList() {
        notify();
    }
}
