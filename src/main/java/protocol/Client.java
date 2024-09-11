package protocol;

import protocol.data.ResponsePacket;
import protocol.data.SendPacket;
import protocol.data.SessionInfo;
import railwayNetworkAPI.Response;
import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Class that represents a local node client
 */
public class Client {
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private List<Integer> ports;
    private int currentPortIndex;
    private ResponsesTrafficHandler responsesTrafficHandler;
    private boolean isConnected;
    private SessionInfo sessionInfo;

    /**
     * Class constructor
     *
     * @param responsesTrafficHandler object that coordinates the access to a list of response packets from the local
     *                                node
     */
    public Client(ResponsesTrafficHandler responsesTrafficHandler) {
        try {
            this.isConnected = false;
            this.ports = List.of(100, 101, 102);
            this.responsesTrafficHandler = responsesTrafficHandler;
            this.currentPortIndex = 0;
            this.socket = new Socket("localhost", ports.get(0));
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
            this.isConnected = true;
        } catch (IOException e) {
            closeEverything();
            connectToNextAvailable();
            System.out.println("Trying to establish connection...");
        }
    }

    private void connectToNextAvailable() {
        this.isConnected = false;
        while (this.currentPortIndex < this.ports.size()) {
            this.currentPortIndex++;

            if (this.currentPortIndex == this.ports.size()) {
                this.currentPortIndex = 0;
            }

            try {
                Thread.sleep(5000);
                this.socket = new Socket("localhost", this.ports.get(this.currentPortIndex));
                this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
                this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
                this.isConnected = true;
                if (this.sessionInfo != null) sendSessionInfo();
                break;
            } catch (IOException | InterruptedException e) {
                if (this.currentPortIndex == this.ports.size() - 1) {
                    this.currentPortIndex = -1;
                }
                closeEverything();
                System.out.println("Trying to establish connection...");
            }
        }
    }

    private void sendSessionInfo() {
        try {
            this.objectOutputStream.writeObject(new SendPacket<>("", this.sessionInfo));
        } catch (Exception e) {
            closeEverything();
            System.out.println("Trying to establish connection...");
        }
    }

    /**
     * Function that checks if this client is connected to a local node
     *
     * @return true if the client is connected otherwise false
     */
    public boolean getIsConnected() {
        return this.isConnected;
    }

    /**
     * Function to send a packet to the local node
     *
     * @param methodSignature the method signature of the api instance held by the central node
     */
    public void sendRequest(String methodSignature) {
        try {
            this.objectOutputStream.writeObject(new SendPacket<>(methodSignature));
            this.objectOutputStream.flush();
        } catch (IOException e) {
            closeEverything();
            System.out.println("Trying to establish connection...");
        }
    }

    /**
     * Function to send a packet to the local node
     *
     * @param methodSignature the method signature of the api instance held by the central node
     * @param data the args of the method
     */
    public void sendRequestWithData(String methodSignature, Object data) {
        try {
            this.objectOutputStream.writeObject(new SendPacket<>(methodSignature, data));
            this.objectOutputStream.flush();
        } catch (IOException e) {
            closeEverything();
            System.out.println("Trying to establish connection...");
        }
    }

    /**
     * Getter for the session info
     *
     * @return the session info
     */
    public SessionInfo getSessionInfo() {
        return this.sessionInfo;
    }

    /**
     * Function that loops while the socket is open, receiving response packets from a local node and adding them to
     * the list of response packets
     */
    public void listenForResponse() {
        new Thread(
                () -> {
                    while (!this.socket.isClosed()) {
                        try {
                            ResponsePacket<?> responsePacket = (ResponsePacket<?>) objectInputStream.readObject();
                            Response<?> response = (Response<?>) responsePacket.getData();
                            this.responsesTrafficHandler.addResponse(response);

                            if (response.getData().getClass().equals(SessionInfo.class)) {
                                this.sessionInfo = (SessionInfo) response.getData();
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            closeEverything();
                            connectToNextAvailable();
                            System.out.println("Trying to establish connection...");
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

    private void closeEverything() {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            if (this.objectInputStream != null) {
                this.objectInputStream.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
        } catch (IOException e) {
            System.out.println("Trying to establish connection...");
        }
    }
}
