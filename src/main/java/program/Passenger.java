package program;

import protocol.Client;
import protocol.ResponsesTrafficHandler;
import railwayNetworkAPI.Railway;
import railwayNetworkAPI.Response;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the passenger program
 */
public class Passenger {
    private Client client;
    private ResponsesTrafficHandler responsesTrafficHandler;
    private List<Response<?>> responses;
    private final JFrame window;
    private final List<JComponent> railwaysComponents;
    private final JComboBox<String> selectRailway;
    private final JTextArea comment;
    private final JLabel notification;
    private final JLabel isConnected;
    private final JLabel resultInputLabel;
    private final JLabel networkStatus;

    /**
     * Default class constructor
     */
    public Passenger() {
        this.window = new JFrame("Program");
        this.railwaysComponents = new ArrayList<>();
        this.notification = new JLabel("");
        this.isConnected = new JLabel("Establishing connection...");
        this.selectRailway = new JComboBox<>();
        this.comment = new JTextArea();
        this.resultInputLabel = new JLabel("");
        this.networkStatus = new JLabel("");
    }

    private void listenForNetworkStatus() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("networkIsSuspended")) {
                            if ((boolean) response.getData()) {
                                this.networkStatus.setText("Network: Suspended");
                            } else {
                                this.networkStatus.setText("Network: Open");
                            }
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForNotifications() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("editSchedules")) {
                            this.notification.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(5000);
                    this.notification.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForRailwaysSchedules() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("getRailwaysSchedulesAsPassenger")) {
                            removeRailwaysComponentsFromWindow();
                            addRailwaysComponentsToWindow(response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void requestForDataUpdates() {
        new Thread(() -> {
            while (true) {
                try {
                    this.client.sendRequestWithData("getRailwaysSchedulesAsPassenger",
                            this.client.getSessionInfo().getUsername());
                    this.client.sendRequest("networkIsSuspended");
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    private void addRailwaysComponentsToWindow(Object data) {
        int width = 500;
        int height = 70;
        int xPos = 40;
        int yPos = 20;
        List<String> selectRailwayOptions = new ArrayList<>();

        if (data.getClass().equals(String.class)) {
            JLabel label = new JLabel((String) data);
            this.railwaysComponents.add(label);
            this.window.add(label);
            label.setSize(width, height);
            yPos += 20;
            label.setLocation(xPos, yPos);
        } else {
            List<?> railwaySchedules = (List<?>) data;

            for (Object railwaySchedule : railwaySchedules) {
                HashMap<?, ?> map = (HashMap<?, ?>) railwaySchedule;
                String railwayLocals = (String) map.get("Locals");
                String schedules = (String) map.get("Schedules");
                Railway railway = (Railway) map.get("Railway");
                boolean hasThisRailway = (boolean) map.get("HasThisRailway");
                if (hasThisRailway) selectRailwayOptions.add(railwayLocals);

                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(hasThisRailway);
                checkBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        handleCheckBoxChange(e, railway);
                    }
                });

                JLabel label1 = new JLabel(railwayLocals);
                JLabel label2 = new JLabel(schedules);
                this.railwaysComponents.add(checkBox);
                this.railwaysComponents.add(label1);
                this.railwaysComponents.add(label2);
                this.window.add(checkBox);
                this.window.add(label1);
                this.window.add(label2);
                yPos += 20;
                checkBox.setSize(20, 20);
                checkBox.setLocation(xPos - 22, yPos + 27);
                label1.setSize(width, height);
                label1.setForeground(Color.darkGray);
                label1.setLocation(xPos, yPos);
                yPos += 20;
                label2.setSize(width, height);
                label2.setLocation(xPos, yPos);
                label2.setForeground(Color.gray);
            }

            int selectedIndex = this.selectRailway.getSelectedIndex();
            if (this.selectRailway.isPopupVisible()) {
                this.selectRailway.setModel(new DefaultComboBoxModel(selectRailwayOptions.toArray()));
                this.selectRailway.setPopupVisible(true);
                if (selectedIndex != -1 && selectedIndex < selectRailwayOptions.size()) {
                    this.selectRailway.setSelectedIndex(selectedIndex);
                }
            } else {
                this.selectRailway.setModel(new DefaultComboBoxModel(selectRailwayOptions.toArray()));
                if (selectedIndex != -1 && selectedIndex < selectRailwayOptions.size()) {
                    this.selectRailway.setSelectedIndex(selectedIndex);
                }
            }
        }
    }

    private void handleCheckBoxChange(ItemEvent e, Railway railway) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Railway", railway);
        map.put("Passenger", this.client.getSessionInfo().getUsername());

        if (e.getStateChange() == ItemEvent.SELECTED) {
            this.client.sendRequestWithData("addRailwayToPassenger", map);
        } else {
            this.client.sendRequestWithData("removeRailwayFromPassenger", map);
        }
    }

    private void removeRailwaysComponentsFromWindow() {
        for (JComponent label : this.railwaysComponents) {
            this.window.remove(label);
        }
        this.window.repaint();
    }

    private void showWindow() {
        this.window.setPreferredSize(new Dimension(720, 405));
        this.window.getContentPane().setLayout(new FlowLayout());
        this.window.setLayout(null);
        JLabel railways = new JLabel("RAILWAYS");
        JLabel reports = new JLabel("REPORT");
        JButton sendReport = new JButton("SEND");

        this.window.add(this.notification);
        this.window.add(this.isConnected);
        this.window.add(railways);
        this.window.add(reports);
        this.window.add(this.selectRailway);
        this.window.add(this.comment);
        this.window.add(sendReport);
        this.window.add(this.resultInputLabel);
        this.window.add(this.networkStatus);

        railways.setSize(500, 50);
        railways.setLocation(20, 10);
        reports.setSize(500, 50);
        reports.setLocation(450, 10);
        this.networkStatus.setSize(500, 50);
        this.networkStatus.setLocation(220, 10);
        this.notification.setSize(500, 50);
        this.notification.setLocation(20, 320);
        this.isConnected.setSize(500, 50);
        this.isConnected.setLocation(200, 300);
        this.selectRailway.setSize(200, 20);
        this.selectRailway.setLocation(450, 60);
        this.comment.setSize(200, 150);
        this.comment.setLocation(450, 90);
        this.comment.setLineWrap(true);
        this.comment.setWrapStyleWord(true);
        sendReport.setSize(100, 30);
        sendReport.setLocation(500, 250);
        this.resultInputLabel.setSize(500, 50);
        this.resultInputLabel.setLocation(450, 320);

        sendReport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendReportButtonPressed();
            }
        });

        this.window.pack();
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void sendReportButtonPressed() {
        if (this.comment.getText().isBlank()) {
            showBadResultInputLabel();
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("Username", this.client.getSessionInfo().getUsername());
            map.put("Comment", this.comment.getText());
            String railway = (String) this.selectRailway.getSelectedItem();
            if(railway != null) {
                String[] split = railway.replaceAll("\\s","").split("-");
                map.put("Railway", new Railway(split[0], split[1]));
                this.client.sendRequestWithData("reportScheduleAlteration", map);
            }
        }
        this.comment.setText("");
    }

    private void listenForReportResponse() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("reportScheduleAlteration")) {
                            this.resultInputLabel.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(2000);
                    this.resultInputLabel.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showBadResultInputLabel() {
        new Thread(() -> {
            try {
                this.resultInputLabel.setText("Report is empty :(");
                Thread.sleep(2000);
                this.resultInputLabel.setText("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Function that executes the program
     *
     * @param client the client
     * @param responses the list of responses
     * @param responsesTrafficHandler the responses traffic handler
     */
    public void start(Client client, List<Response<?>> responses, ResponsesTrafficHandler responsesTrafficHandler) {
        showWindow();
        this.client = client;
        this.responses = responses;
        this.responsesTrafficHandler = responsesTrafficHandler;
        listenForIsConnected();
        listenForNotifications();
        listenForRailwaysSchedules();
        listenForReportResponse();
        listenForNetworkStatus();
        requestForDataUpdates();
    }
}
