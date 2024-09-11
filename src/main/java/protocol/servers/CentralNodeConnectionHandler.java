package protocol.servers;

import others.ConsoleColors;
import protocol.data.ResponsePacket;
import protocol.data.SendPacket;
import railwayNetworkAPI.RailwayNetworkAPI;
import railwayNetworkAPI.Response;
import railwayNetworkAPI.ResponseType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Class that represents the new thread when a new client establishes a new connection to a central node
 */
public class CentralNodeConnectionHandler implements Runnable {
    private Socket socket;
    private RailwayNetworkAPI railwayNetworkAPI;
    private List<CentralNodeConnectionHandler> serverClients = new ArrayList<>();
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private Semaphore semaphore;

    /**
     * Class constructor
     *
     * @param socket the client socket
     * @param serverClients the list of threads (clients) connected the local node
     * @param railwayNetworkAPI the api instance
     * @param semaphore semaphore that controls the api calls
     */
    public CentralNodeConnectionHandler(Socket socket, List<CentralNodeConnectionHandler> serverClients,
                                        RailwayNetworkAPI railwayNetworkAPI, Semaphore semaphore) {
        try {
            this.serverClients = serverClients;
            serverClients.add(this);
            this.socket = socket;
            this.railwayNetworkAPI = railwayNetworkAPI;
            this.semaphore = semaphore;
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            closeEverything();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (this.socket.isConnected()) {
            try {
                SendPacket<?> packet = (SendPacket<?>) this.objectInputStream.readObject();
                this.semaphore.acquire();
                print("CN: packet received from client in port " + this.socket.getPort());

                Response<?> apiResponse = handleApiCall(packet.getMethodSignature(), packet.getData());
                ResponsePacket<?> responsePacket = new ResponsePacket<>(
                        packet.getClientSender(),
                        packet.getLocalNodeSender(),
                        apiResponse);

                handlePacketResponse(apiResponse.getLayer1ResponseType(), responsePacket);
                print("CN: response packet sent");
                this.semaphore.release();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                closeEverything();
                print("CN: client disconnected");
                break;
            }
        }
    }

    private Response<?> handleApiCall(String methodSignature, Object data) {
        try {
            if (data == null) {
                Method method = this.railwayNetworkAPI.getClass().getDeclaredMethod(methodSignature);
                method.setAccessible(true);
                return (Response<?>) method.invoke(this.railwayNetworkAPI);
            } else {
                Method method = this.railwayNetworkAPI.getClass().getDeclaredMethod(methodSignature, Object.class);
                method.setAccessible(true);
                return (Response<?>) method.invoke(this.railwayNetworkAPI, data);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handlePacketResponse(ResponseType responseType, ResponsePacket<?> responsePacket) {
        try {
            if (responseType.equals(ResponseType.BROADCAST)) {
                for (CentralNodeConnectionHandler client : serverClients) {
                    client.objectOutputStream.writeObject(responsePacket);
                    client.objectOutputStream.flush();
                }
            } else {
                this.objectOutputStream.writeObject(responsePacket);
                this.objectOutputStream.flush();
            }
        } catch (Exception e) {
            closeEverything();
            e.printStackTrace();
        }
    }

    private void removeServerClient() {
        serverClients.remove(this);
    }

    private void closeEverything() {
        removeServerClient();

        try {
            if (this.objectInputStream != null) {
                this.objectInputStream.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print(String message) {
        System.out.println(ConsoleColors.PURPLE_BOLD + message + ConsoleColors.RESET);
    }
}
