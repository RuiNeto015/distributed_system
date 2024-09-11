package railwayNetworkAPI;

import com.google.gson.Gson;
import protocol.data.SessionInfo;
import railwayNetworkAPI.interfaces.IRailwayNetworkAPI;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents the railway network api
 */
public class RailwayNetworkAPI implements IRailwayNetworkAPI {
    private final List<Passenger> passengerList;
    private final List<Railway> railwayList;
    private final List<Report> reportsList;
    private boolean isSuspended;

    /**
     * Default constructor
     */
    public RailwayNetworkAPI() {
        this.passengerList = new ArrayList<>();
        this.railwayList = new ArrayList<>();
        this.reportsList = new ArrayList<>();
        this.isSuspended = false;
    }

    //PASSENGERS
    @Override
    public Response<?> getPassengers() {
        try {
            if (this.passengerList.isEmpty()) {
                return new Response<>(
                        "No passengers available.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getPassengers");

            } else {
                return new Response<>(
                        this.passengerList,
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getPassengers");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error getting the passengers list.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "getPassengers");
        }
    }

    @Override
    public Response<?> registerPassenger(Object data) {
        try {
            Passenger passenger = (Passenger) data;

            if (passenger.getUserName().isBlank() || passenger.getPassword().isBlank()) {
                return new Response<>(
                        "Invalid username or password.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "registerPassenger");
            }

            if (findPassenger(passenger.getUserName()) != null) {
                return new Response<>(
                        "Username already taken.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "registerPassenger");
            }

            this.passengerList.add(passenger);
            writeLog("New passenger with username " + passenger.getUserName() + " registered");
            saveData();
            return new Response<>(
                    "Registration with success!",
                    true,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "registerPassenger");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during your registration.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "registerPassenger");
        }
    }

    @Override
    public Response<?> authenticatePassenger(Object data) {
        try {
            Passenger passenger = (Passenger) data;
            Passenger matchingPassenger = findPassenger(passenger.getUserName());

            if (matchingPassenger == null) {
                return new Response<>(
                        "Invalid username or password.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "authenticatePassenger");
            }

            if (matchingPassenger.getPassword().equals(passenger.getPassword())) {
                writeLog("Passenger with username " + passenger.getUserName() + " authenticated");
                return new Response<>(
                        new SessionInfo(passenger.getUserName()),
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "authenticatePassenger");
            } else {
                return new Response<>(
                        "Invalid username or password.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "authenticatePassenger");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during your login.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "authenticatePassenger");
        }
    }

    @Override
    public Response<?> addRailwayToPassenger(Object data) {
        try {
            HashMap<String, ?> passengerRailways = (HashMap<String, ?>) data;
            Railway matchingRailway = findRailway((Railway) passengerRailways.get("Railway"));
            Passenger matchingPassenger = findPassenger((String) passengerRailways.get("Passenger"));

            if (matchingRailway == null) {
                return new Response<>(
                        "The railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addRailwayToPassenger");
            }

            if (matchingPassenger == null) {
                return new Response<>(
                        "The passenger doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addRailwayToPassenger");
            }

            for (Railway r : matchingPassenger.getRailways()) {
                if (r.equals(matchingRailway)) {
                    return new Response<>(
                            "Passenger already has this railway.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "addRailwayToPassenger");
                }
            }

            matchingPassenger.addRailway(matchingRailway);
            writeLog("Railway" + matchingRailway.getLocalidade1() + "->" + matchingRailway.getLocalidade2() +
                    " added to passenger " + matchingPassenger.getUserName());
            saveData();
            return new Response<>(
                    "Railways successfully added to Passenger.",
                    true,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addRailwayToPassenger");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the railway to passenger addition.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addRailwayToPassenger");
        }
    }

    @Override
    public Response<?> removeRailwayFromPassenger(Object data) {
        try {
            HashMap<String, ?> passengerRailways = (HashMap<String, ?>) data;
            Railway matchingRailway = findRailway((Railway) passengerRailways.get("Railway"));
            Passenger matchingPassenger = findPassenger((String) passengerRailways.get("Passenger"));

            if (matchingPassenger == null) {
                return new Response<>(
                        "The passenger doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeRailwayFromPassenger");
            }

            if (matchingRailway == null) {
                return new Response<>(
                        "The railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeRailwayFromPassenger");

            }

            for (Railway r : matchingPassenger.getRailways()) {
                if (r.equals(matchingRailway)) {
                    matchingPassenger.removeRailway(r);
                    writeLog("Railway" + matchingRailway.getLocalidade1() + "->" + matchingRailway.getLocalidade2() +
                            " removed from passenger " + matchingPassenger.getUserName());
                    saveData();
                    return new Response<>(
                            "Railway successfully removed from Passenger.",
                            true,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "removeRailwayFromPassenger");
                }
            }

            return new Response<>(
                    "Passenger doesn't have this railway in list.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeRailwayFromPassenger");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the railway to passenger addition.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeRailwayFromPassenger");
        }
    }

    //RAILWAYS
    @Override
    public Response<?> getRailways() {
        try {
            if (this.railwayList.isEmpty()) {
                return new Response<>(
                        "No railways available.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailways");

            } else {
                return new Response<>(
                        this.railwayList,
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailways");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error getting the railways list.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "getRailways");
        }
    }

    @Override
    public Response<?> getRailwaysSchedulesAsAdmin() {
        try {
            if (this.railwayList.isEmpty()) {
                return new Response<>(
                        "No railways in the Network.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailwaysSchedulesAsAdmin");

            } else {
                ArrayList<Object> arrayToSend = new ArrayList<>();

                for (Railway r : this.railwayList) {

                    int helper = 0;
                    HashMap<String, Object> railwayInfo = new HashMap<>();
                    String schedules = "";

                    List<String> railwaySchedulesList = new ArrayList<>(r.getSchedules());

                    railwaySchedulesList.sort((a, b) -> {
                        try {
                            return new SimpleDateFormat("hh:mm").parse(a).compareTo(new SimpleDateFormat("hh:mm").parse(b));
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    for (String s : railwaySchedulesList) {
                        helper++;
                        if (helper == r.getSchedules().size()) {
                            schedules += s;
                        } else {
                            schedules += s + " | ";
                        }
                    }

                    railwayInfo.put("Locals", r.getLocalidade1() + " - " + r.getLocalidade2());
                    railwayInfo.put("Schedules", schedules);
                    railwayInfo.put("Railway", r);
                    arrayToSend.add(railwayInfo);
                }
                return new Response<>(
                        arrayToSend,
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailwaysSchedulesAsAdmin");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error getting the railways schedules.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "getRailwaysSchedulesAsAdmin");
        }
    }

    @Override
    public Response<?> getRailwaysSchedulesAsPassenger(Object data) {
        try {
            if (this.railwayList.isEmpty()) {
                return new Response<>(
                        "No railways in the Network.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailwaysSchedulesAsPassenger");

            } else {
                ArrayList<Object> arrayToSend = new ArrayList<>();
                Passenger matchingPassenger = null;

                //get the passenger with username passed by parameter
                for (Passenger p : this.passengerList) {
                    if (p.getUserName().equals(data)) {
                        matchingPassenger = p;
                        break;
                    }
                }

                if (matchingPassenger == null) {
                    return new Response<>(
                            "The username doesn't have correspondence.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "getRailwaysSchedulesAsPassenger");
                }

                for (Railway r : this.railwayList) {

                    int helper = 0;
                    HashMap<String, Object> railwayInfo = new HashMap<>();
                    String schedules = "";
                    boolean hasThisRailway = false;

                    for (Railway pr : matchingPassenger.getRailways()) {
                        if (pr.equals(r)) {
                            hasThisRailway = true;
                        }
                    }

                    List<String> railwaySchedulesList = new ArrayList<>(r.getSchedules());

                    railwaySchedulesList.sort((a, b) -> {
                        try {
                            return new SimpleDateFormat("hh:mm").parse(a).compareTo(new SimpleDateFormat("hh:mm").parse(b));
                        } catch (ParseException e) {
                            return 0;
                        }
                    });

                    for (String s : railwaySchedulesList) {
                        helper++;
                        if (helper == r.getSchedules().size()) {
                            schedules += s;
                        } else {
                            schedules += s + " | ";
                        }
                    }

                    railwayInfo.put("Railway", r);
                    railwayInfo.put("Locals", r.getLocalidade1() + " - " + r.getLocalidade2());
                    railwayInfo.put("Schedules", schedules);
                    railwayInfo.put("HasThisRailway", hasThisRailway);
                    arrayToSend.add(railwayInfo);
                }
                return new Response<>(
                        arrayToSend,
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getRailwaysSchedulesAsPassenger");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(
                    "There was an error getting the railways schedules.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "getRailwaysSchedulesAsPassenger");
        }
    }

    @Override
    public Response<?> addWayToRailwayList(Object data) {
        try {
            Railway railway = (Railway) data;

            if (findRailway(railway) != null) {
                return new Response<>(
                        "The railway already exists.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayList");

            } else {
                Railway oppositeRailway = new Railway(railway.getLocalidade2(), railway.getLocalidade1());
                this.railwayList.add(railway);
                this.railwayList.add(oppositeRailway);
                writeLog("New railway " + railway.getLocalidade1() + "->" + railway.getLocalidade2() + " added");
                saveData();
                return new Response<>(
                        "Railways -> " + railway.getLocalidade1() + " - " + railway.getLocalidade2() + " and "
                                + railway.getLocalidade2() + " - " + railway.getLocalidade1() + " added successfully.",
                        true,
                        ResponseType.BROADCAST,
                        ResponseType.BROADCAST,
                        "addWayToRailwayList");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the railway addition.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addWayToRailwayList");
        }
    }

    @Override
    public Response<?> addWayToRailwayListWithSchedules(Object data) {
        try {
            HashMap<String, ?> railwayWithSchedules = (HashMap<String, ?>) data;
            String locationA = (String) railwayWithSchedules.get("Location A");
            String locationB = (String) railwayWithSchedules.get("Location B");
            String schedules = (String) railwayWithSchedules.get("Schedules");

            Pattern pattern = Pattern.compile("^((0[0-9]|1[0-9]|2[0-3]):[0-5][0-9])+(;(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9])*$");

            //INPUT VALIDATIONS
            if (locationA.isBlank() || locationB.isBlank()) {
                return new Response<>(
                        "Locations cannot be blank.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayListWithSchedules");
            }

            if (schedules.isBlank()) {
                return new Response<>(
                        "You have to add some schedules.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayListWithSchedules");
            }

            if (!pattern.matcher(schedules).find()) {
                return new Response<>(
                        "The schedules input is not valid.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayListWithSchedules");
            }

            //PUT SCHEDULES IN AN ARRAYLIST
            List<String> schedulesArrayList = new ArrayList<>(Arrays.asList(schedules.split(";")));

            //IF HAS DUPLICATED VALUES
            List<String> hasDuplicatedSchedulesArray = new ArrayList<>();
            for (String schdl : schedulesArrayList) {
                if (hasDuplicatedSchedulesArray.contains(schdl)) {
                    return new Response<>(
                            "The list has repeated schedules.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "addWayToRailwayListWithSchedules");
                } else {
                    hasDuplicatedSchedulesArray.add(schdl);
                }
            }

            //IF RAILWAY DOES NOT EXIST
            if (findRailway(new Railway(locationA, locationB)) == null) {
                this.railwayList.add(new Railway(locationA, locationB));
                this.railwayList.add(new Railway(locationB, locationA));

                for (String schdl : schedulesArrayList) {
                    Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).addSchedule(schdl);
                    Objects.requireNonNull(findRailway(new Railway(locationB, locationA))).addSchedule(schdl);
                }
                saveData();
                writeLog("New schedules added to railway " + locationA + "->" + locationB);
                return new Response<>(
                        "Railway and schedules added successfully.",
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayListWithSchedules");
            }

            //IF RAILWAY EXISTS
            else {
                for (String schdl : schedulesArrayList) {
                    if (Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).getSchedules().contains(schdl)) {
                        return new Response<>(
                                "The Railway already has some schedule added.",
                                false,
                                ResponseType.UNICAST,
                                ResponseType.UNICAST,
                                "addWayToRailwayListWithSchedules");
                    }
                }

                for (String schdl : schedulesArrayList) {
                    Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).addSchedule(schdl);
                }
                saveData();
                writeLog("New schedules added to railway " + locationA + "->" + locationB);
                return new Response<>(
                        "Schedules added successfully.",
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addWayToRailwayListWithSchedules");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the request.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addWayToRailwayListWithSchedules");
        }
    }


    @Override
    public Response<?> removeSchedulesFromRailway(Object data) {
        try {
            HashMap<String, ?> railwayWithSchedules = (HashMap<String, ?>) data;
            String locationA = (String) railwayWithSchedules.get("Location A");
            String locationB = (String) railwayWithSchedules.get("Location B");
            String schedules = (String) railwayWithSchedules.get("Schedules");

            Pattern pattern = Pattern.compile("^((0[0-9]|1[0-9]|2[0-3]):[0-5][0-9])+(;(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9])*$");

            //INPUT VALIDATIONS
            if (locationA.isBlank() || locationB.isBlank()) {
                return new Response<>(
                        "Locations cannot be blank.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeSchedulesFromRailway");
            }

            if (schedules.isBlank()) {
                return new Response<>(
                        "You have to add some schedules.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeSchedulesFromRailway");
            }

            if (!pattern.matcher(schedules).find()) {
                return new Response<>(
                        "The schedules input is not valid.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeSchedulesFromRailway");
            }

            //IF RAILWAY DOES NOT EXIST
            if (findRailway(new Railway(locationA, locationB)) == null) {
                return new Response<>(
                        "The Railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeSchedulesFromRailway");
            }

            //PUT SCHEDULES IN AN ARRAYLIST
            List<String> schedulesArrayList = new ArrayList<>(Arrays.asList(schedules.split(";")));

            //IF HAS DUPLICATED VALUES
            List<String> hasDuplicatedSchedulesArray = new ArrayList<>();
            for (String schdl : schedulesArrayList) {
                if (hasDuplicatedSchedulesArray.contains(schdl)) {
                    return new Response<>(
                            "The list has repeated schedules.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "removeSchedulesFromRailway");
                } else {
                    hasDuplicatedSchedulesArray.add(schdl);
                }
            }

            //IF SOME SCHEDULE IS NOT ON THE RAILWAY SCHEDULES LIST
            for (String schdl : schedulesArrayList) {
                if (!Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).getSchedules().contains(schdl)) {
                    return new Response<>(
                            "The Railway doesn't have some schedule.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "removeSchedulesFromRailway");
                }
            }

            //IF THE SCHEDULE TO REMOVE IS THE LAST ONE ON THE RAILWAY
            if (Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).getSchedules().size() == 1) {
                this.railwayList.remove(new Railway(locationA, locationB));
                this.railwayList.remove(new Railway(locationB, locationA));
                saveData();
                writeLog("Railway " + locationA + "->" + locationB + " removed");
                return new Response<>(
                        "Last schedule removed. Railways removed.",
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeSchedulesFromRailway");
            }

            for (String schdl : schedulesArrayList) {
                Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).removeSchedule(schdl);
            }
            if (Objects.requireNonNull(findRailway(new Railway(locationA, locationB))).getSchedules().size() == 0) {
                this.railwayList.remove(new Railway(locationA, locationB));
                this.railwayList.remove(new Railway(locationB, locationA));
            }
            saveData();
            writeLog("Schedules removed from railway " + locationA + "->" + locationB);
            return new Response<>(
                    "Schedules successfully removed.",
                    true,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeSchedulesFromRailway");

        } catch (
                Exception e) {
            return new Response<>(
                    "There was an error during the request.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeSchedulesFromRailway");
        }

    }

    @Override
    public Response<?> editSchedules(Object data) {
        try {
            ArrayList<HashMap<String, String>> alterationsArray = (ArrayList<HashMap<String, String>>) data;
            ArrayList<HashMap<String, Object>> arrayToSend = new ArrayList<>();

            Pattern pattern = Pattern.compile("^0[0-9]|1[0-9]|2[0-3]:[0-5][0-9]$");

            //VALIDATIONS
            for (HashMap<String, String> alteration : alterationsArray) {

                //IF SOME ALTERATION HAS EMPTY VALUES
                if (alteration.get("Location A").isBlank() || alteration.get("Location B").isBlank()
                        || alteration.get("Old Schedule").isBlank() || alteration.get("New Schedule").isBlank()) {
                    return new Response<>(
                            "Empty values are not allowed.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "editSchedules");

                    //IF THE RAILWAY DOES NOT EXIST
                } else if (findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B"))) == null) {
                        return new Response<>(
                                "The railway does not exist.",
                                false,
                                ResponseType.UNICAST,
                                ResponseType.UNICAST,
                                "editSchedules");

                    //IF SOME ALTERATION HAS THE SAME OLD AND NEW SCHEDULE
                } else if (alteration.get("Old Schedule").equals(alteration.get("New Schedule"))) {
                    return new Response<>(
                            "Some alteration has the same old and new schedule.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "editSchedules");

                    //IF SOME ALTERATION SCHEDULE HAS A BAD hh:mm FORMAT
                } else if (!pattern.matcher(alteration.get("Old Schedule")).find() || !pattern.matcher(alteration.get("New Schedule")).find()) {
                    return new Response<>(
                            "Some alteration has invalid schedules inputs.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "editSchedules");
                }

                //IF A RAILWAY DOESN'T CONTAIN A "OLD SCHEDULE"
                if (!Objects.requireNonNull(findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B")))).getSchedules().contains(alteration.get("Old Schedule"))) {
                    return new Response<>(
                            "Some Railway doesn't have some Old Schedule to edit.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "editSchedules");
                }

                //IF A RAILWAY ALREADY CONTAINS A "NEW SCHEDULE"
                if (Objects.requireNonNull(findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B")))).getSchedules().contains(alteration.get("New Schedule"))) {
                    return new Response<>(
                            "Some Railway already has some New Schedule",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "editSchedules");
                }
            }

            for (HashMap<String, String> alteration : alterationsArray) {
                HashMap<String, Object> tmpHashMap = new HashMap<>();
                tmpHashMap.put("Location A", alteration.get("Location A"));
                tmpHashMap.put("Location B", alteration.get("Location B"));
                tmpHashMap.put("Target Passengers", getTargetPassengers(findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B")))));

                findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B"))).removeSchedule(alteration.get("Old Schedule"));
                findRailway(new Railway(alteration.get("Location A"), alteration.get("Location B"))).addSchedule(alteration.get("New Schedule"));

                tmpHashMap.put("Message", "Railway " + alteration.get("Location A") + " - " + alteration.get("Location B") + " -> " +
                        "Schedule " + alteration.get("Old Schedule") + " edited to " + alteration.get("New Schedule") + ".");

                arrayToSend.add(tmpHashMap);
            }
            saveData();
            return new Response<>(
                    arrayToSend,
                    true,
                    ResponseType.BROADCAST,
                    ResponseType.MULTICAST,
                    "editSchedules");

        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>(
                    "There was an error during the schedules edit.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "editSchedules");
        }
    }

    @Override
    public Response<?> removeWayFromRailwayList(Object data) {
        try {
            Railway matchingRailway = findRailway((Railway) data);

            if (matchingRailway == null) {
                return new Response<>(
                        "The railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeWayFromRailwayList");
            }

            for (Railway r : this.railwayList) {
                if (r.getLocalidade1().equals(matchingRailway.getLocalidade2()) && r.getLocalidade2().equals(matchingRailway.getLocalidade1())) {
                    this.railwayList.remove(matchingRailway);
                    this.railwayList.remove(r);
                    saveData();
                    return new Response<>(
                            "Railways removed successfully.",
                            true,
                            ResponseType.BROADCAST,
                            ResponseType.BROADCAST,
                            "removeWayFromRailwayList");
                }
            }

            return new Response<>(
                    "Something went wrong.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeWayFromRailwayList");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the railway removal.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeWayFromRailwayList");
        }
    }

    @Override
    public Response<?> addScheduleToRailway(Object data) {
        try {
            HashMap<String, ?> railwaySchedulesMap = (HashMap<String, ?>) data;
            Railway matchingRailway = findRailway((Railway) railwaySchedulesMap.get("Railway"));
            String schedule = (String) railwaySchedulesMap.get("Schedule");

            Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

            if (matchingRailway == null) {
                return new Response<>(
                        "The Railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addScheduleToRailway");
            }

            if (!pattern.matcher(schedule).find()) {
                return new Response<>(
                        "The schedule is invalid.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "addScheduleToRailway");
            }

            for (String s : matchingRailway.getSchedules()) {
                if (s.equals(schedule)) {
                    return new Response<>(
                            "Schedule already exists in this railway.",
                            false,
                            ResponseType.UNICAST,
                            ResponseType.UNICAST,
                            "addScheduleToRailway");
                }
            }

            matchingRailway.addSchedule(schedule);
            saveData();
            return new Response<>(
                    "Schedule added successfully.",
                    true,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addScheduleToRailway");

        } catch (
                Exception e) {
            return new Response<>(
                    "There was an error during the schedule to railway addition.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "addScheduleToRailway");
        }
    }

    @Override
    public Response<?> editScheduleFromRailway(Object data) {
        try {
            HashMap<String, ?> railwaySchedulesMap = (HashMap<String, ?>) data;
            Railway matchingRailway = findRailway((Railway) railwaySchedulesMap.get("Railway"));
            String scheduleToEdit = (String) railwaySchedulesMap.get("Schedule");
            String newSchedule = (String) railwaySchedulesMap.get("New Schedule");

            Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

            if (matchingRailway == null) {
                return new Response<>(
                        "The railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "editScheduleFromRailway");
            }

            if (!pattern.matcher(scheduleToEdit).find() || !pattern.matcher(newSchedule).find()) {
                return new Response<>(
                        "The schedules are invalid.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "editScheduleFromRailway");
            }

            if (scheduleToEdit.equals(newSchedule)) {
                return new Response<>(
                        "The schedules are the same.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "editScheduleFromRailway");
            }

            for (String s : matchingRailway.getSchedules()) {
                if (s.equals(scheduleToEdit)) {
                    matchingRailway.removeSchedule(scheduleToEdit);
                    matchingRailway.addSchedule(newSchedule);
                    List<String> targetPassengers = getTargetPassengers(matchingRailway);
                    saveData();
                    return new Response<>(
                            "Railway " + matchingRailway.getLocalidade1() + " - " + matchingRailway.getLocalidade2() + " -> " +
                                    "Schedule " + scheduleToEdit + " edited to " + newSchedule + ".",
                            true,
                            ResponseType.BROADCAST,
                            ResponseType.MULTICAST,
                            targetPassengers,
                            "editScheduleFromRailway");
                }
            }

            return new Response<>(
                    "Schedule doesn't exist in this railway.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "editScheduleFromRailway");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the schedule edit.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "editScheduleFromRailway");
        }
    }

    private List<String> getTargetPassengers(Railway railway) {
        List<String> passengers = new ArrayList<>();

        for (Passenger p : this.passengerList) {
            if (p.getRailways().contains(railway)) {
                passengers.add(p.getUserName());
            }
        }
        return passengers;
    }

    @Override
    public Response<?> removeScheduleFromRailway(Object data) {
        try {
            HashMap<String, ?> railwaySchedulesMap = (HashMap<String, ?>) data;
            Railway matchingRailway = findRailway((Railway) railwaySchedulesMap.get("Railway"));
            String schedule = (String) railwaySchedulesMap.get("Schedule");

            Pattern pattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

            if (matchingRailway == null) {
                return new Response<>(
                        "The Railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeScheduleFromRailway");

            } else if (!pattern.matcher(schedule).find()) {
                return new Response<>(
                        "The schedule is invalid.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeScheduleFromRailway");

            } else if (!matchingRailway.getSchedules().isEmpty()) {
                for (String s : matchingRailway.getSchedules()) {
                    if (s.equals(schedule)) {
                        matchingRailway.removeSchedule(schedule);
                        saveData();
                        return new Response<>(
                                "Schedule removed successfully.",
                                true,
                                ResponseType.UNICAST,
                                ResponseType.UNICAST,
                                "removeScheduleFromRailway");
                    }
                }

            } else {
                return new Response<>(
                        "Schedule doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "removeScheduleFromRailway");
            }

            return new Response<>(
                    "Something went wrong.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeScheduleFromRailway");

        } catch (Exception e) {
            return new Response<>(
                    "There was an error during the schedule to railway removal.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "removeScheduleFromRailway");
        }
    }

    //NETWORK
    @Override
    public Response<?> suspendNetworkAndNotify() {
        try {
            if (this.isSuspended) {
                return new Response<>(
                        "Railway Network is already suspended.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "suspendNetworkAndNotify");
            }
            this.isSuspended = true;
            saveData();
            writeLog("Network suspended");
            return new Response<>(
                    "Railway Network is suspended.",
                    true,
                    ResponseType.BROADCAST,
                    ResponseType.BROADCAST,
                    "suspendNetworkAndNotify");

        } catch (Exception e) {
            return new Response<>(
                    "Error during the network suspend and notify.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "suspendNetworkAndNotify");
        }
    }

    @Override
    public Response<?> unsuspendNetworkAndNotify() {
        try {
            if (!this.isSuspended) {
                return new Response<>(
                        "Railway Network is already unsuspended.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "suspendNetworkAndNotify");
            }
            this.isSuspended = false;
            saveData();
            writeLog("Network unsuspended");
            return new Response<>(
                    "Railway Network is unsuspended.",
                    true,
                    ResponseType.BROADCAST,
                    ResponseType.BROADCAST,
                    "unsuspendNetworkAndNotify");

        } catch (Exception e) {
            return new Response<>(
                    "Error during the network unsuspend and notify.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "unsuspendNetworkAndNotify");
        }
    }

    @Override
    public Response<?> networkIsSuspended() {
        return new Response<>(
                this.isSuspended,
                true,
                ResponseType.UNICAST,
                ResponseType.UNICAST,
                "networkIsSuspended");
    }

    //REPORTS
    @Override
    public Response<?> getReports() {
        try {
            if (this.reportsList.isEmpty()) {
                return new Response<>(
                        "No reports available.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getReports");

            } else {
                String result = "";

                for(Report report : this.reportsList) {
                    result += "[" + report.getDateTime() + "]:[" + report.getPassenger().getUserName() + "]:[" +
                            report.getRailway().getLocalidade1() + "-" + report.getRailway().getLocalidade2() + "]:" +
                            report.getComment() + "\n";
                }

                return new Response<>(
                        result,
                        true,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "getReports");
            }

        } catch (Exception e) {
            return new Response<>(
                    "There was an error getting the railways list.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "getReports");
        }
    }

    @Override
    public Response<?> reportScheduleAlteration(Object data) {
        try {
            HashMap<?, ?> map = (HashMap<?, ?>) data;
            Passenger matchingPassenger = findPassenger((String) map.get("Username"));
            Railway matchingRailway = findRailway((Railway) map.get("Railway"));
            String comment = (String) map.get("Comment");
            boolean passengerHasThisRailwayAdded = false;

            if (matchingPassenger == null) {
                return new Response<>(
                        "The passenger doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "reportScheduleAlteration");
            }

            if (matchingRailway == null) {
                return new Response<>(
                        "The railway doesn't exist.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "reportScheduleAlteration");
            }

            if (comment.equals("")) {
                return new Response<>(
                        "Comment cannot be empty.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "reportScheduleAlteration");
            }

            for (Railway r : matchingPassenger.getRailways()) {
                if (r.equals(matchingRailway)) {
                    passengerHasThisRailwayAdded = true;
                    break;
                }
            }
            if (!passengerHasThisRailwayAdded) {
                return new Response<>(
                        "You don't have this railway added.",
                        false,
                        ResponseType.UNICAST,
                        ResponseType.UNICAST,
                        "reportScheduleAlteration");
            }

            this.reportsList.add(new Report(matchingPassenger, matchingRailway, comment));
            List<String> targetPassengers = new ArrayList<>();
            Passenger admin = findPassenger("admin");
            targetPassengers.add(matchingPassenger.getUserName());
            targetPassengers.add(admin.getUserName());
            saveData();
            writeLog("New report added by " + matchingPassenger.getUserName());
            return new Response<>(
                    "New report added.",
                    true,
                    ResponseType.BROADCAST,
                    ResponseType.MULTICAST,
                    targetPassengers,
                    "reportScheduleAlteration");
        } catch (
                Exception e) {
            return new Response<>(
                    "That was an error during the report send.",
                    false,
                    ResponseType.UNICAST,
                    ResponseType.UNICAST,
                    "reportScheduleAlteration");
        }
    }

    //FINDS
    private Passenger findPassenger(String username) {
        for (Passenger ppp : this.passengerList) {
            if (ppp.getUserName().equals(username)) {
                return ppp;
            }
        }
        return null;
    }

    private Railway findRailway(Railway railway) {
        for (Railway r : this.railwayList) {
            if (r.getLocalidade1().equals(railway.getLocalidade1()) && r.getLocalidade2().equals(railway.getLocalidade2())) {
                return r;
            }
        }
        return null;
    }

    private void saveData() {
        try {
            String str = new Gson().toJson(this);
            FileWriter fileWriter = new FileWriter("data.json");
            fileWriter.write(str);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(String string) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dateTime = LocalDateTime.now().format(formatter);
            FileWriter fileWriter = new FileWriter("logs.txt", true);
            fileWriter.write("[" + dateTime + "]:" + string + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
