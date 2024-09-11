package program;

import others.ConsoleColors;
import others.CustomTextField;
import protocol.Client;
import protocol.ResponsesTrafficHandler;
import railwayNetworkAPI.Railway;
import railwayNetworkAPI.Response;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents the admin program
 */
public class Admin {
    private Client client;
    private ResponsesTrafficHandler responsesTrafficHandler;
    private List<Response<?>> responses;
    private final JFrame window;
    private final List<JComponent> railwaysComponents;
    private final JComboBox<String> selectRailway;
    private final CustomTextField loc1;
    private final CustomTextField loc2;
    private final CustomTextField schedules;
    private final JLabel addRailwayResult;
    private final CustomTextField scheduleToChange;
    private final CustomTextField newSchedule;
    private final JLabel editScheduleResult;
    private List<HashMap<?, ?>> schedulesChanges;
    private final JLabel changesCounter;
    private final JLabel isConnected;
    private final JLabel networkStatus;
    private final JLabel networkStatusChangeResult;
    private final JLabel report;

    /**
     * Default class constructor
     */
    public Admin() {
        this.window = new JFrame("Admin");
        this.railwaysComponents = new ArrayList<>();
        this.selectRailway = new JComboBox<>();
        this.loc1 = new CustomTextField(20);
        this.loc1.setPlaceholder("location A");
        this.loc2 = new CustomTextField(20);
        this.loc2.setPlaceholder("location B");
        this.schedules = new CustomTextField(20);
        this.schedules.setPlaceholder("schedules(ex:hh:mm;hh:mm;hh:mm)");
        this.addRailwayResult = new JLabel("");
        this.scheduleToChange = new CustomTextField(20);
        this.scheduleToChange.setPlaceholder("old schedule");
        this.newSchedule = new CustomTextField(20);
        this.newSchedule.setPlaceholder("new schedule");
        this.editScheduleResult = new JLabel("");
        this.schedulesChanges = new ArrayList<>();
        this.changesCounter = new JLabel("Changes: 0");
        this.isConnected = new JLabel("Establishing connection...");
        this.networkStatus = new JLabel("");
        this.networkStatusChangeResult = new JLabel("");
        this.report = new JLabel("");
    }

    private void listenForReports() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("reportScheduleAlteration")) {
                            this.report.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(2000);
                    this.report.setText("");
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
                        if (response.getMethodSignature().equals("getRailwaysSchedulesAsAdmin")) {
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
                    this.client.sendRequest("getRailwaysSchedulesAsAdmin");
                    this.client.sendRequest("networkIsSuspended");
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    private void listenForNetworkStatusChangeResult() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("suspendNetworkAndNotify") ||
                                response.getMethodSignature().equals("unsuspendNetworkAndNotify")
                        ) {
                            this.networkStatusChangeResult.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(2000);
                    this.networkStatusChangeResult.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForRailwaysChangesResult() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("addWayToRailwayListWithSchedules") ||
                                response.getMethodSignature().equals("removeSchedulesFromRailway")
                        ) {
                            this.addRailwayResult.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(2000);
                    this.addRailwayResult.setText("");
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

    private void listenForEditSchedulesResult() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("editSchedules")) {
                            this.editScheduleResult.setText((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
                            break;
                        }
                    }
                    Thread.sleep(2000);
                    this.editScheduleResult.setText("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void listenForReportsResponse() {
        new Thread(() -> {
            while (true) {
                try {
                    for (Response<?> response : this.responses) {
                        if (response.getMethodSignature().equals("getReports")) {
                            print((String) response.getData());
                            this.responsesTrafficHandler.removeResponse(response);
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

    private void addRailwaysComponentsToWindow(Object data) {
        int width = 500;
        int height = 70;
        int xPos = 50;
        int yPos = 30;
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
                selectRailwayOptions.add(railwayLocals);

                JLabel label1 = new JLabel(railwayLocals);
                JLabel label2 = new JLabel(schedules);
                JButton removeButton = new JButton("x");
                removeButton.addActionListener(e -> removeRailwayButtonPressed(railway));
                this.railwaysComponents.add(label1);
                this.railwaysComponents.add(label2);
                this.railwaysComponents.add(removeButton);
                this.window.add(label1);
                this.window.add(label2);
                this.window.add(removeButton);
                yPos += 20;
                removeButton.setSize(20, 20);
                removeButton.setLocation(xPos - 32, yPos + 32);
                removeButton.setMargin(new Insets(0, 0, 0, 0));
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

    private void removeRailwayButtonPressed(Railway railway) {
        this.client.sendRequestWithData("removeWayFromRailwayList", railway);
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
        JLabel editSchedule = new JLabel("EDIT SCHEDULE");
        JButton addRailway = new JButton("Add");
        JButton removeRailway = new JButton("Remove");
        JButton addScheduleChange = new JButton("Add change");
        JButton clearChanges = new JButton("Clear changes");
        JButton submitChanges = new JButton("Submit changes");
        JButton openNetwork = new JButton("Open network");
        JButton closeNetwork = new JButton("Close network");
        JButton printReports = new JButton("Print reports to console");

        //add elements to window
        this.window.add(railways);
        this.window.add(this.loc1);
        this.window.add(this.loc2);
        this.window.add(this.schedules);
        this.window.add(addRailway);
        this.window.add(this.selectRailway);
        this.window.add(removeRailway);
        this.window.add(this.addRailwayResult);
        this.window.add(this.scheduleToChange);
        this.window.add(this.newSchedule);
        this.window.add(addScheduleChange);
        this.window.add(clearChanges);
        this.window.add(submitChanges);
        this.window.add(editSchedule);
        this.window.add(this.editScheduleResult);
        this.window.add(this.changesCounter);
        this.window.add(this.networkStatus);
        this.window.add(openNetwork);
        this.window.add(closeNetwork);
        this.window.add(this.networkStatusChangeResult);
        this.window.add(this.isConnected);
        this.window.add(this.report);
        this.window.add(printReports);

        // add listeners to buttons
        addRailway.addActionListener(e -> addRailwayButtonPressed());
        removeRailway.addActionListener(e -> removeRailwayButtonPressed());
        addScheduleChange.addActionListener(e -> addScheduleChangeButtonPressed());
        clearChanges.addActionListener(e -> schedulesChanges = new ArrayList<>());
        submitChanges.addActionListener(e -> submitChangesButtonPressed());
        openNetwork.addActionListener(e -> client.sendRequest("unsuspendNetworkAndNotify"));
        closeNetwork.addActionListener(e -> client.sendRequest("suspendNetworkAndNotify"));
        printReports.addActionListener(e -> client.sendRequest("getReports"));

        // set position and size of elements
        railways.setSize(500, 50);
        railways.setLocation(20, 10);
        editSchedule.setSize(500, 50);
        editSchedule.setLocation(450, 10);
        this.loc1.setSize(100, 20);
        this.loc1.setLocation(100, 20);
        this.loc2.setSize(100, 20);
        this.loc2.setLocation(200, 20);
        this.schedules.setSize(200, 20);
        this.schedules.setLocation(100, 40);
        addRailway.setSize(70, 20);
        addRailway.setLocation(300, 20);
        addRailway.setMargin(new Insets(0, 0, 0, 0));
        removeRailway.setSize(70, 20);
        removeRailway.setLocation(300, 40);
        removeRailway.setMargin(new Insets(0, 0, 0, 0));
        this.addRailwayResult.setSize(500, 50);
        this.addRailwayResult.setLocation(100, 40);
        this.selectRailway.setSize(200, 20);
        this.selectRailway.setLocation(450, 60);
        this.scheduleToChange.setSize(200, 20);
        this.scheduleToChange.setLocation(450, 85);
        this.newSchedule.setSize(200, 20);
        this.newSchedule.setLocation(450, 110);
        this.changesCounter.setSize(200, 20);
        this.changesCounter.setLocation(450, 135);
        addScheduleChange.setSize(80, 20);
        addScheduleChange.setLocation(450, 160);
        addScheduleChange.setMargin(new Insets(0, 0, 0, 0));
        clearChanges.setSize(100, 20);
        clearChanges.setLocation(550, 160);
        clearChanges.setMargin(new Insets(0, 0, 0, 0));
        submitChanges.setSize(100, 20);
        submitChanges.setLocation(500, 190);
        submitChanges.setMargin(new Insets(0, 0, 0, 0));
        this.editScheduleResult.setSize(500, 50);
        this.editScheduleResult.setLocation(450, 200);
        this.networkStatus.setSize(500, 50);
        this.networkStatus.setLocation(450, 240);
        openNetwork.setSize(100, 20);
        openNetwork.setLocation(450, 280);
        openNetwork.setMargin(new Insets(0, 0, 0, 0));
        closeNetwork.setSize(100, 20);
        closeNetwork.setLocation(550, 280);
        closeNetwork.setMargin(new Insets(0, 0, 0, 0));
        this.networkStatusChangeResult.setSize(500, 50);
        this.networkStatusChangeResult.setLocation(450, 290);
        this.isConnected.setSize(500, 50);
        this.isConnected.setLocation(270, 300);
        this.report.setSize(400, 50);
        this.report.setLocation(250, 300);
        printReports.setSize(200, 20);
        printReports.setLocation(450, 320);
        printReports.setMargin(new Insets(0, 0, 0, 0));

        this.window.pack();
        this.window.setLocationRelativeTo(null);
        this.window.setResizable(false);
        this.window.setVisible(true);
        this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addRailwayButtonPressed() {
        new Thread(() -> {
            try {
                String error = validateRailwayFields();

                if (!error.isEmpty()) {
                    this.addRailwayResult.setText(error);
                    Thread.sleep(2000);
                    this.addRailwayResult.setText("");
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Location A", this.loc1.getText());
                    map.put("Location B", this.loc2.getText());
                    map.put("Schedules", this.schedules.getText());

                    this.client.sendRequestWithData("addWayToRailwayListWithSchedules", map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void removeRailwayButtonPressed() {
        new Thread(() -> {
            try {
                String error = validateRailwayFields();

                if (!error.isEmpty()) {
                    this.addRailwayResult.setText(error);
                    Thread.sleep(2000);
                    this.addRailwayResult.setText("");
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Location A", this.loc1.getText());
                    map.put("Location B", this.loc2.getText());
                    map.put("Schedules", this.schedules.getText());

                    this.client.sendRequestWithData("removeSchedulesFromRailway", map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String validateRailwayFields() {
        Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9](;([0-1]?[0-9]|2[0-3]):[0-5][0-9])*$");

        if (this.loc1.getText().isBlank() || this.loc1.getText().equals("location A") ||
                this.loc2.getText().isBlank() || this.loc2.getText().equals("location B") ||
                this.schedules.getText().isBlank() ||
                this.schedules.getText().equals("schedules(ex:hh:mm;hh:mm;hh:mm)")) {

            return "Empty fields are not valid";
        } else if (!pattern.matcher(this.schedules.getText()).find()) {
            return "Schedule is not valid";
        } else if (!railwaySchedulesAreValid()) {
            return "Schedule cannot have the same time twice";
        }
        return "";
    }

    private boolean railwaySchedulesAreValid() {
        Set<String> set = new HashSet<>();
        String[] schedules = this.schedules.getText().split(";");

        for (String str : schedules) {
            if (!set.contains(str)) {
                set.add(str);
            } else {
                return false;
            }
        }
        return true;
    }

    private void addScheduleChangeButtonPressed() {
        Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

        new Thread(() -> {
            try {
                if (this.scheduleToChange.getText().isBlank() || this.scheduleToChange.getText().equals("old schedule")
                        || this.newSchedule.getText().isBlank() || this.newSchedule.getText().equals("new schedule")) {
                    this.editScheduleResult.setText("Empty fields are not valid");
                    Thread.sleep(2000);
                    this.editScheduleResult.setText("");
                } else if (this.scheduleToChange.getText().equals(this.newSchedule.getText())) {
                    this.editScheduleResult.setText("The schedules cannot be the same");
                    Thread.sleep(2000);
                    this.editScheduleResult.setText("");
                } else if (!pattern.matcher(this.scheduleToChange.getText()).find() ||
                        !pattern.matcher(this.newSchedule.getText()).find()) {
                    this.editScheduleResult.setText("One of the schedules is not valid");
                    Thread.sleep(2000);
                    this.editScheduleResult.setText("");
                } else {
                    String railway = (String) this.selectRailway.getSelectedItem();
                    String[] split = railway.replaceAll("\\s", "").split("-");
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Location A", split[0]);
                    map.put("Location B", split[1]);
                    map.put("Old Schedule", this.scheduleToChange.getText());
                    map.put("New Schedule", this.newSchedule.getText());

                    if (!changeIsValid(map)) {
                        this.editScheduleResult.setText("Schedule conflicts");
                        Thread.sleep(2000);
                        this.editScheduleResult.setText("");
                    } else {
                        this.schedulesChanges.add(map);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean changeIsValid(HashMap<String, String> map) {
        for (HashMap<?, ?> i : this.schedulesChanges) {
            if (i.get("Location A").equals(map.get("locationA")) && i.get("Location B").equals(map.get("locationB")) &&
                    i.get("Old Schedule").equals(map.get("Old Schedule")) &&
                    i.get("New Schedule").equals(map.get("New Schedule"))) {

                return false;
            }
        }
        return true;
    }

    private void submitChangesButtonPressed() {
        new Thread(() -> {
            try {
                if (this.schedulesChanges.isEmpty()) {
                    this.editScheduleResult.setText("Add changes first :)");
                    Thread.sleep(2000);
                    this.editScheduleResult.setText("");
                } else {
                    this.client.sendRequestWithData("editSchedules", this.schedulesChanges);
                    schedulesChanges = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateChangesCounter() {
        new Thread(() -> {
            try {
                while (true) {
                    this.changesCounter.setText("Changes: " + this.schedulesChanges.size());
                    Thread.sleep(1000);
                }
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
        listenForRailwaysSchedules();
        listenForNetworkStatus();
        listenForNetworkStatusChangeResult();
        listenForRailwaysChangesResult();
        requestForDataUpdates();
        updateChangesCounter();
        listenForIsConnected();
        listenForReports();
        listenForEditSchedulesResult();
        listenForReportsResponse();
    }

    private void print(String message) {
        System.out.println(ConsoleColors.BLUE + message + ConsoleColors.RESET);
    }
}
