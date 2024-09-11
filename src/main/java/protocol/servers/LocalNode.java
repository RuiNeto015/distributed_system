package protocol.servers;

import others.ConsoleColors;
import protocol.data.ResponsePacket;
import protocol.data.SendPacket;
import protocol.data.SessionInfo;
import railwayNetworkAPI.Response;
import railwayNetworkAPI.ResponseType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Class that represents the local node.
 * Entity responsible for coordinating the communication between the central node and client
 */
public class LocalNode {
    private ServerSocket serverSocket;
    private Socket centralNodeSocket;
    private final List<LocalNode> localNodes;
    private List<LocalNodeConnectionHandler> serverClients;
    private ObjectInputStream centralNodeObjectInputStream;
    private ObjectOutputStream centralNodeObjectOutputStream;
    private List<SendPacket<?>> packetsToSend;
    private final Semaphore semaphore;
    private PacketTrafficHandler packetTrafficHandler;

    /**
     * Class constructor
     *
     * @param localNodes represents the list of all local nodes
     */
    public LocalNode(List<LocalNode> localNodes) {
        this.localNodes = localNodes;
        this.serverClients = new ArrayList<>();
        this.semaphore = new Semaphore(1);
    }

    /**
     * Function that starts the local node
     *
     * @param port            the port where local node clients will connect to
     * @param centralNodeHost the central node host so that the local node can establish a connection with the central
     *                        node
     * @param centralNodePort the central node port so that the local node can establish a connection with the central
     *                        node
     */
    public void startServer(int port, String centralNodeHost, int centralNodePort) {
        try {
            this.serverSocket = new ServerSocket(port);
            this.centralNodeSocket = new Socket(centralNodeHost, centralNodePort);
            this.centralNodeObjectOutputStream = new ObjectOutputStream(this.centralNodeSocket.getOutputStream());
            this.centralNodeObjectInputStream = new ObjectInputStream(this.centralNodeSocket.getInputStream());
            this.packetsToSend = new ArrayList<>();
            this.packetTrafficHandler = new PacketTrafficHandler(this.packetsToSend);
            print("Local node is up and running on port: " + port + "!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to shut down the server
     */
    public void stopServer() {
        try {
            this.serverSocket.close();
            this.centralNodeSocket.close();
            this.serverSocket = null;
            this.centralNodeSocket = null;
            this.packetTrafficHandler = null;
            this.packetsToSend = null;

            for (LocalNodeConnectionHandler client : this.serverClients) {
                client.closeEverything();
            }
            this.serverClients = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to get the number of clients connected to the local node
     *
     * @return the number of clients connected to the local node
     */
    public int clientsConnected() {
        return this.serverClients.size();
    }

    /**
     * Function that checks if this local node is the node with fewer clients
     *
     * @return true if this local node is the node with fewer clients otherwise false
     */
    public boolean mostEmpty() {
        List<LocalNode> openNodes = new ArrayList<>();

        for (LocalNode ln : this.localNodes) {
            if (ln.serverSocket != null) {
                openNodes.add(ln);
            }
        }

        if (openNodes.isEmpty()) {
            return false;
        }

        LocalNode mostEmpty = openNodes.get(0);
        int min = Integer.MAX_VALUE;

        for (LocalNode ln : openNodes) {
            if (ln.clientsConnected() < min) {
                min = ln.clientsConnected();
                mostEmpty = ln;
            }
        }
        return mostEmpty == this;
    }

    /**
     * Function that loops while the server socket isn't closed and accepts new connections
     */
    public void listenForConnection() {
        int port = this.serverSocket.getLocalPort();

        new Thread(
                () -> {
                    try {
                        while (!this.serverSocket.isClosed()) {
                            Socket socket = this.serverSocket.accept();

                            if (mostEmpty()) {
                                print("LN_" + this.serverSocket.getLocalPort() + ": new client connected");
                                Thread thread = new Thread(new LocalNodeConnectionHandler(
                                        socket, this.packetsToSend, this.serverClients, this.packetTrafficHandler,
                                        this.serverSocket.getLocalPort()));
                                thread.start();
                            } else {
                                socket.close();
                            }
                        }
                    } catch (Exception e) {
                        print("LN_" + port + ": disconnected");
                    }
                }
        ).start();
    }

    /**
     * Function that loops while this local node is connected to the central node, receiving packets from the central
     * node and then sending them to the respective/s client/s
     */
    public void listenForCentralNodeResponse() {
        new Thread(
                () -> {
                    while (this.centralNodeSocket.isConnected()) {
                        try {
                            ResponsePacket<?> responsePacket =
                                    (ResponsePacket<?>) this.centralNodeObjectInputStream.readObject();
                            Response<?> apiResponse = (Response<?>) responsePacket.getData();
                            handlePacketResponse(apiResponse.getLayer2ResponseType(), responsePacket);

                            if (responsePacket.getLocalNodeSender() == this.serverSocket.getLocalPort()) {
                                this.semaphore.release(1);
                            }
                            print("LN_" + this.serverSocket.getLocalPort() + ": packet received from central node");
                        } catch (IOException | ClassNotFoundException e) {
                            break;
                        }
                    }
                }
        ).start();
    }

    private void handlePacketResponse(ResponseType responseType, ResponsePacket<?> responsePacket) {
        try {
            Response<?> apiResponse = (Response<?>) responsePacket.getData();

            if (responseType.equals(ResponseType.BROADCAST)) {
                for (LocalNodeConnectionHandler client : serverClients) {
                    client.writeObject(responsePacket);
                }
            } else if (responseType.equals(ResponseType.UNICAST)) {
                for (LocalNodeConnectionHandler client : serverClients) {
                    if (client.getClientSocket().getPort() == responsePacket.getClientSender()) {
                        client.writeObject(responsePacket);

                        if (apiResponse.getData().getClass().equals(SessionInfo.class)) {
                            client.setSessionInfo((SessionInfo) apiResponse.getData());
                        }
                    }
                }
            } else if (apiResponse.getMethodSignature().equals("editSchedules")) {
                List<?> responses = (List<?>) apiResponse.getData();

                for (int i = 0; i < responses.size(); i++) {
                    HashMap<?, ?> map = (HashMap<?, ?>) responses.get(i);
                    List<?> targetClients = (List<?>) map.get("Target Passengers");
                    String message = (String) map.get("Message");
                    Response<?> currentApiResponse = new Response(message, true,
                            ResponseType.BROADCAST, ResponseType.MULTICAST, "editSchedules");
                    ResponsePacket<?> packet = new ResponsePacket<>(
                            responsePacket.getClientSender(),
                            responsePacket.getLocalNodeSender(),
                            currentApiResponse
                    );

                    for (LocalNodeConnectionHandler client : serverClients) {
                        for (int j = 0; j < targetClients.size(); j++) {
                            String username = (String) targetClients.get(j);

                            if (username.equals(client.getSessionInfo().getUsername())) {
                                client.writeObject(packet);
                            }
                        }
                    }
                }
            } else {
                List<String> targetClients = apiResponse.getTargetClients();

                for (LocalNodeConnectionHandler client : serverClients) {
                    for (String tc : targetClients) {
                        if (tc.equals(client.getSessionInfo().getUsername())) {
                            client.writeObject(responsePacket);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function that loops while this local node is connected to the central node, receiving packets from the clients
     * and then sending them to the central node
     */
    public void sendPacketsToCentralNode() {
        new Thread(
                () -> {
                    while (this.centralNodeSocket.isConnected()) {
                        try {
                            this.packetTrafficHandler.waitWhileListEmpty();
                            this.semaphore.acquire();
                            print("LN_" + this.serverSocket.getLocalPort() + ": packet sent to central node");
                            SendPacket<?> packetToSend = this.packetsToSend.remove(0);
                            packetToSend.setLocalNodeSender(this.serverSocket.getLocalPort());
                            this.centralNodeObjectOutputStream.writeObject(packetToSend);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

    private void print(String message) {
        System.out.println(ConsoleColors.GREEN_BOLD + message + ConsoleColors.RESET);
    }
}

