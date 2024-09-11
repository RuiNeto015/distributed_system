package protocol.servers;

import others.ConsoleColors;
import protocol.data.ResponsePacket;
import protocol.data.SendPacket;
import protocol.data.SessionInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Class that represents the new thread when a new client establishes a new connection to a local node
 */
public class LocalNodeConnectionHandler implements Runnable {
    private int port;
    private Socket clientSocket;
    private List<LocalNodeConnectionHandler> serverClients;
    private ObjectInputStream clientObjectInputStream;
    private ObjectOutputStream clientObjectOutputStream;
    private List<SendPacket<?>> packetsToSend;
    private SessionInfo sessionInfo;
    private PacketTrafficHandler packetTrafficHandler;

    /**
     * Class constructor
     *
     * @param clientSocket the client socket
     * @param packetsToSend the queue of packets that the local node has to send to the central node
     * @param serverClients the list of threads (clients) connected the local node
     * @param packetTrafficHandler object that coordinates the access to the list of 'packetsToSend'
     * @param port the local node port
     */
    public LocalNodeConnectionHandler(
            Socket clientSocket,
            List<SendPacket<?>> packetsToSend,
            List<LocalNodeConnectionHandler> serverClients,
            PacketTrafficHandler packetTrafficHandler,
            int port
    ) {
        try {
            serverClients.add(this);
            this.clientSocket = clientSocket;
            this.packetsToSend = packetsToSend;
            this.serverClients = serverClients;
            this.clientObjectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.clientObjectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
            this.packetTrafficHandler = packetTrafficHandler;
            this.port = port;
        } catch (Exception e) {
            closeEverything();
            removeClient();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.clientSocket.isConnected()) {
            try {
                SendPacket<?> packet = (SendPacket<?>) this.clientObjectInputStream.readObject();

                if (packet.getData() != null && packet.getData().getClass().equals(SessionInfo.class)) {
                    this.sessionInfo = (SessionInfo) packet.getData();
                } else {
                    packet.setClientSender(this.clientSocket.getPort());
                    this.packetsToSend.add(packet);
                    print("LN_" + this.port + ": packet received from client");
                    this.packetTrafficHandler.notifyElementAddedToTheList();
                }
            } catch (IOException | ClassNotFoundException e) {
                closeEverything();
                removeClient();
                print("LN_" + this.port + ": client disconnected");
                break;
            }
        }
    }

    /**
     * Function that sends a packet from the central node socket to the client socket
     *
     * @param responsePacket the packet to send
     */
    public void writeObject(ResponsePacket<?> responsePacket) {
        try {
            this.clientObjectOutputStream.writeObject(responsePacket);
            this.clientObjectOutputStream.flush();
        } catch (Exception e) {
            closeEverything();
            removeClient();
            e.printStackTrace();
        }
    }

    /**
     * Getter for the client socket
     *
     * @return the client socket
     */
    public Socket getClientSocket() {
        return this.clientSocket;
    }

    /**
     * Setter for the sessionInfo (holds the username of the client connected to this socket)
     *
     * @param sessionInfo the session info
     */
    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    /**
     * Getter for the sessionInfo
     *
     * @return the sessionInfo
     */
    public SessionInfo getSessionInfo() {
        return this.sessionInfo;
    }

    /**
     * Removes this thread from the list of threads
     */
    public void removeClient() {
        this.serverClients.remove(this);
    }

    /**
     * Function that closes the client socket, object input and output streams
     */
    public void closeEverything() {
        try {
            if (this.clientObjectInputStream != null) {
                this.clientObjectInputStream.close();
            }
            if (this.clientObjectOutputStream != null) {
                this.clientObjectOutputStream.close();
            }
            if (this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print(String message) {
        System.out.println(ConsoleColors.GREEN_BOLD + message + ConsoleColors.RESET);
    }
}
