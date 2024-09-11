package program;

import protocol.servers.CentralNode;
import protocol.servers.LocalNode;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Program that controls all the nodes
 */
public class NodeControlStation implements ActionListener {
    private CentralNode centralNode;
    private List<LocalNode> localNodes;
    private JFrame window;
    private Map<String, JButton> buttons;
    private Map<String, JLabel> numberOfClientsLabel;

    /**
     * Default class constructor
     */
    public NodeControlStation() {
        try {
            this.centralNode = new CentralNode();
            this.localNodes = new ArrayList<>();
            initLocalNodes();
            this.window = new JFrame("Node Control Station");
            this.buttons = new HashMap<>();
            initButtons();
            this.numberOfClientsLabel = new HashMap<>();
            initNumberOfClientsLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLocalNodes() {
        try {
            this.localNodes.add(new LocalNode(this.localNodes));
            this.localNodes.add(new LocalNode(this.localNodes));
            this.localNodes.add(new LocalNode(this.localNodes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initButtons() {
        this.buttons.put("centralNode", new JButton("TURN ON"));
        this.buttons.put("localNode1", new JButton("TURN ON"));
        this.buttons.get("localNode1").setEnabled(false);
        this.buttons.put("localNode2", new JButton("TURN ON"));
        this.buttons.get("localNode2").setEnabled(false);
        this.buttons.put("localNode3", new JButton("TURN ON"));
        this.buttons.get("localNode3").setEnabled(false);
    }

    private void initNumberOfClientsLabel() {
        this.numberOfClientsLabel.put("centralNode", new JLabel("Clients: 0"));
        this.numberOfClientsLabel.put("localNode1", new JLabel("Clients: 0"));
        this.numberOfClientsLabel.put("localNode2", new JLabel("Clients: 0"));
        this.numberOfClientsLabel.put("localNode3", new JLabel("Clients: 0"));
    }

    private void showWindow() {
        this.window.setPreferredSize(new Dimension(300, 250));
        this.window.getContentPane().setLayout(new FlowLayout());
        this.window.setLayout(null);

        // criação de labels
        JLabel labelCentralNode = new JLabel("Central Node");
        JLabel labelLocalNode1 = new JLabel("Local Node 1");
        JLabel labelLocalNode2 = new JLabel("Local Node 2");
        JLabel labelLocalNode3 = new JLabel("Local Node 3");

        // adiciona labels à janela
        this.window.add(labelCentralNode);
        this.window.add(labelLocalNode1);
        this.window.add(labelLocalNode2);
        this.window.add(labelLocalNode3);
        this.window.add(this.numberOfClientsLabel.get("centralNode"));
        this.window.add(this.numberOfClientsLabel.get("localNode1"));
        this.window.add(this.numberOfClientsLabel.get("localNode2"));
        this.window.add(this.numberOfClientsLabel.get("localNode3"));

        // posição de labels
        labelCentralNode.setSize(100, 50);
        labelCentralNode.setLocation(10, 10);
        labelLocalNode1.setSize(100, 50);
        labelLocalNode1.setLocation(10, 70);
        labelLocalNode2.setSize(100, 50);
        labelLocalNode2.setLocation(10, 110);
        labelLocalNode3.setSize(100, 50);
        labelLocalNode3.setLocation(10, 150);

        this.numberOfClientsLabel.get("centralNode").setSize(100, 50);
        this.numberOfClientsLabel.get("centralNode").setLocation(210, 10);
        this.numberOfClientsLabel.get("localNode1").setSize(100, 50);
        this.numberOfClientsLabel.get("localNode1").setLocation(210, 70);
        this.numberOfClientsLabel.get("localNode2").setSize(100, 50);
        this.numberOfClientsLabel.get("localNode2").setLocation(210, 110);
        this.numberOfClientsLabel.get("localNode3").setSize(100, 50);
        this.numberOfClientsLabel.get("localNode3").setLocation(210, 150);

        // define listeners para botões
        this.buttons.get("centralNode").addActionListener(this);
        this.buttons.get("localNode1").addActionListener(this);
        this.buttons.get("localNode2").addActionListener(this);
        this.buttons.get("localNode3").addActionListener(this);

        // adiciona botões à janela
        this.window.add(this.buttons.get("centralNode"));
        this.window.add(this.buttons.get("localNode1"));
        this.window.add(this.buttons.get("localNode2"));
        this.window.add(this.buttons.get("localNode3"));

        // posição dos botões
        this.buttons.get("centralNode").setSize(100, 30);
        this.buttons.get("centralNode").setLocation(100, 20);
        this.buttons.get("localNode1").setSize(100, 30);
        this.buttons.get("localNode1").setLocation(100, 80);
        this.buttons.get("localNode2").setSize(100, 30);
        this.buttons.get("localNode2").setLocation(100, 120);
        this.buttons.get("localNode3").setSize(100, 30);
        this.buttons.get("localNode3").setLocation(100, 160);

        this.window.pack();
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source.equals(this.buttons.get("centralNode"))) {
            handleCentralNodeButtonClick();
        } else if (source.equals(this.buttons.get("localNode1"))) {
            handleLocalNodeButtonClick("localNode1", this.localNodes.get(0), 100);
        } else if (source.equals(this.buttons.get("localNode2"))) {
            handleLocalNodeButtonClick("localNode2", this.localNodes.get(1), 101);
        } else if (source.equals(this.buttons.get("localNode3"))) {
            handleLocalNodeButtonClick("localNode3", this.localNodes.get(2), 102);
        }
    }

    private void handleCentralNodeButtonClick() {
        if (this.buttons.get("centralNode").getText().equals("TURN ON")) {
            this.buttons.get("centralNode").setText("TURN OFF");
            for (String key : this.buttons.keySet()) {
                this.buttons.get(key).setEnabled(true);
            }
            new Thread(() -> this.centralNode.startServer(99)).start();
        } else {
            this.centralNode.stopServer();
            for (LocalNode localNode : this.localNodes) {
                localNode.stopServer();
            }
            for (String key : this.buttons.keySet()) {
                this.buttons.get(key).setText("TURN ON");
                if (!key.equals("centralNode")) this.buttons.get(key).setEnabled(false);
            }
        }
    }

    private void handleLocalNodeButtonClick(String localNodeName, LocalNode localNode, int port) {
        if (this.buttons.get(localNodeName).getText().equals("TURN ON")) {
            this.buttons.get(localNodeName).setText("TURN OFF");
            localNode.startServer(port, "localhost", 99);
            localNode.listenForConnection();
            localNode.listenForCentralNodeResponse();
            localNode.sendPacketsToCentralNode();
        } else {
            this.buttons.get(localNodeName).setText("TURN ON");
            localNode.stopServer();
        }
    }

    private void updateNumberOfClientsLabels() {
        new Thread(
                () -> {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            int clients = this.centralNode.clientsConnected();
                            int clients1 = this.localNodes.get(0).clientsConnected();
                            int clients2 = this.localNodes.get(1).clientsConnected();
                            int clients3 = this.localNodes.get(2).clientsConnected();
                            this.numberOfClientsLabel.get("centralNode").setText("Clients: " + clients);
                            this.numberOfClientsLabel.get("localNode1").setText("Clients: " + clients1);
                            this.numberOfClientsLabel.get("localNode2").setText("Clients: " + clients2);
                            this.numberOfClientsLabel.get("localNode3").setText("Clients: " + clients3);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        ).start();
    }

    /**
     * Function that executes the program
     */
    public void start() {
        showWindow();
        updateNumberOfClientsLabels();
    }

    public static void main(String[] args) {
        NodeControlStation nodeControlStation = new NodeControlStation();
        nodeControlStation.start();
    }
}
