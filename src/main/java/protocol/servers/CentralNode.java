package protocol.servers;

import com.google.gson.Gson;
import others.ConsoleColors;
import railwayNetworkAPI.RailwayNetworkAPI;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Class that represents the central node
 * Entity responsible for executing the desired operations, on the held api instance, described on the received packets
 */
public class CentralNode {
    private ServerSocket serverSocket;
    private RailwayNetworkAPI railwayNetworkAPI;
    private List<CentralNodeConnectionHandler> serverClients;

    /**
     * Default class contructor
     */
    public CentralNode() {
        importData();
    }

    private void importData() {
        this.serverClients = new ArrayList<>();
        try {
            String str = Files.readString(Path.of("data.json"));
            this.railwayNetworkAPI = new Gson().fromJson(str, RailwayNetworkAPI.class);
        } catch (Exception e) {
            e.printStackTrace();
            this.railwayNetworkAPI = new RailwayNetworkAPI();
        }
    }

    /**
     * Function that starts the central node
     *
     * @param port the port where central node clients will connect to
     */
    public void startServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            Semaphore semaphore = new Semaphore(1);
            print("Central node is up and running on port:" + this.serverSocket.getLocalPort() + "!");

            while (!this.serverSocket.isClosed()) {
                Socket socket = this.serverSocket.accept();
                print("CN: new local node server connected");
                Thread thread = new Thread(new CentralNodeConnectionHandler(socket, this.serverClients,
                        this.railwayNetworkAPI, semaphore));
                thread.start();
            }
        } catch (IOException e) {
            printRed("Central node stopped");
        }
    }

    /**
     * Function to shut down the server
     */
    public void stopServer() {
        try {
            this.serverSocket.close();
            this.serverSocket = null;
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

    private void print(String message) {
        System.out.println(ConsoleColors.PURPLE_BOLD + message + ConsoleColors.RESET);
    }

    private void printRed(String message) {
        System.out.println(ConsoleColors.RED_BOLD + message + ConsoleColors.RESET);
    }
}
