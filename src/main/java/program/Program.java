package program;

import protocol.Client;
import protocol.ResponsesTrafficHandler;
import railwayNetworkAPI.Response;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Program class
 */
public class Program extends JFrame implements ActionListener {
    private JFrame window;
    private Client client;
    private List<Response<?>> responses;
    private ResponsesTrafficHandler responsesTrafficHandler;
    private Map<String, JButton> buttons;
    private Map<String, JTextField> textFields;
    private String userName;
    private JLabel isConnected;
    private Map<String, JLabel> labels;

    /**
     * Default class constructor
     */
    public Program() {
        try {
            this.window = new JFrame("Login");
            this.responses = new ArrayList<>();
            this.responsesTrafficHandler = new ResponsesTrafficHandler(this.responses);
            this.buttons = new HashMap<>();
            initButtons();
            this.textFields = new HashMap<>();
            initTextFields();
            this.labels = new HashMap<>();
            initLabels();
            this.isConnected = new JLabel("Establishing connection...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForIsConnected() {
        new Thread(
                () -> {
                    while (true) {
                        try {
                            boolean isConnected = this.client.getIsConnected();
                            if (!isConnected) {
                                this.isConnected.setText("Establishing connection...");
                            } else {
                                this.isConnected.setText("");
                            }
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).start();
    }

    private void listenForAuthenticationTry() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("authenticatePassenger")) {
                            this.responsesTrafficHandler.removeResponse(response);
                            if (response.isSuccessful() && this.userName.equals("admin")) {
                                Admin admin = new Admin();
                                admin.start(this.client, this.responses, this.responsesTrafficHandler);
                                this.window.dispose();
                            } else if(response.isSuccessful()) {
                                Passenger passenger = new Passenger();
                                passenger.start(this.client, this.responses, this.responsesTrafficHandler);
                                this.window.dispose();
                            } else {
                                this.labels.get("error").setText(response.getData().toString());
                            }
                            break;
                        }
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForRegistrationTry() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("registerPassenger")) {
                            this.responsesTrafficHandler.removeResponse(response);
                            this.labels.get("error").setText(response.getData().toString());
                            break;
                        }
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {}
            }
        }).start();
    }

    private void initButtons() {
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        loginBtn.setBackground(Color.DARK_GRAY);
        loginBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(Color.GRAY);
        registerBtn.setForeground(Color.WHITE);
        this.buttons.put("login", loginBtn);
        this.buttons.put("register", registerBtn);
    }

    private void initTextFields() {
        this.textFields.put("username", new JTextField());
        this.textFields.put("password", new JTextField());
    }

    private void initLabels() {
        this.labels.put("username", new JLabel("Username"));
        this.labels.put("password", new JLabel("Password"));
        this.labels.put("error", new JLabel(""));
    }

    private void showWindow() {
        this.window.setPreferredSize(new Dimension(720, 405));
        this.window.getContentPane().setLayout(new FlowLayout());
        this.window.setLayout(null);

        // adiciona labels à janela
        this.window.add(this.labels.get("username"));
        this.window.add(this.labels.get("password"));
        this.window.add(this.labels.get("error"));
        this.window.add(this.isConnected);

        // posição de labels
        this.labels.get("username").setSize(100, 30);
        this.labels.get("username").setLocation(250, 30);
        this.labels.get("password").setSize(100, 30);
        this.labels.get("password").setLocation(250, 95);
        this.labels.get("error").setSize(200, 30);
        this.labels.get("error").setLocation(250, 270);
        this.isConnected.setSize(500, 50);
        this.isConnected.setLocation(290, 320);

        // define listeners para botões
        this.buttons.get("login").addActionListener(this);
        this.buttons.get("register").addActionListener(this);

        // adiciona botões à janela
        this.window.add(this.buttons.get("login"));
        this.window.add(this.buttons.get("register"));

        // posição dos botões
        this.buttons.get("login").setSize(220, 30);
        this.buttons.get("login").setLocation(250, 180);
        this.buttons.get("register").setSize(220, 30);
        this.buttons.get("register").setLocation(250, 230);

        // adiciona text fields à janela
        this.window.add(this.textFields.get("username"));
        this.window.add(this.textFields.get("password"));

        // posição de text fields
        this.textFields.get("username").setSize(220, 30);
        this.textFields.get("username").setLocation(250, 55);
        this.textFields.get("password").setSize(220, 30);
        this.textFields.get("password").setLocation(250, 120);

        this.window.pack();
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();

        if (source.equals(this.buttons.get("login"))) {
            handleLoginButtonClick();
            this.userName = this.textFields.get("username").getText();
        } else if (source.equals(this.buttons.get("register"))) {
            handleRegisterButtonClick();
        }
    }

    private void handleLoginButtonClick() {
        this.client.sendRequestWithData("authenticatePassenger",
                new railwayNetworkAPI.Passenger(
                        this.textFields.get("username").getText(),
                        this.textFields.get("password").getText()
                )
        );
    }

    private void handleRegisterButtonClick() {
        this.client.sendRequestWithData("registerPassenger",
                new railwayNetworkAPI.Passenger(
                        this.textFields.get("username").getText(),
                        this.textFields.get("password").getText()
                )
        );
    }

    /**
     * Function to execute the program
     */
    public void start() {
        showWindow();
        this.client = new Client(this.responsesTrafficHandler);
        this.client.listenForResponse();
        listenForAuthenticationTry();
        listenForRegistrationTry();
        listenForIsConnected();
    }

    public static void main(String[] args) {
        Program program = new Program();
        program.start();
    }
}
