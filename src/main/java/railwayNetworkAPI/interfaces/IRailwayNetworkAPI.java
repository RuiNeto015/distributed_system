package railwayNetworkAPI.interfaces;

import railwayNetworkAPI.Response;

public interface IRailwayNetworkAPI {

    //PASSENGERS

    /**
     * Getter for the passengers
     *
     * @return the passengers
     */
    Response<?> getPassengers();

    /**
     * Method that registers a new passenger
     *
     * @param data the new passenger to be registered
     * @return a response containing the result of the operation
     */
    Response<?> registerPassenger(Object data);

    /**
     * Method that authenticates a passenger
     *
     * @param data the passenger to be authed
     * @return a response containing the result of the operation
     */
    Response<?> authenticatePassenger(Object data);

    /**
     * Method that adds a new railway to a passenger
     *
     * @param data the railway to be added to the passenger
     * @return a response containing the result of the operation
     */
    Response<?> addRailwayToPassenger(Object data);

    /**
     * Method that removes a railway from a passenger
     *
     * @param data the railway to be removed from the passenger
     * @return a response containing the result of the operation
     */
    Response<?> removeRailwayFromPassenger(Object data);

    //RAILWAYS

    /**
     * Getter for the railways on the network
     *
     * @return a response containing the result of the operation
     */
    Response<?> getRailways();

    /**
     * Getter for the railways schedules as admin
     *
     * @return a response containing the result of the operation
     */
    Response<?> getRailwaysSchedulesAsAdmin();

    /**
     * Getter for the railways schedules as passenger
     *
     * @param data the passenger username
     * @return a response containing the result of the operation
     */
    Response<?> getRailwaysSchedulesAsPassenger(Object data);

    /**
     * Method that adds a new railway to the railways list
     *
     * @param data the new railway to be added
     * @return a response containing the result of the operation
     */
    Response<?> addWayToRailwayList(Object data);

    /**
     * Method that removes schedules from a railway
     *
     * @param data Hashmap containing
     * @return a response containing the location A(String), location B(String) and the shedules(String)
     */
    public Response<?> removeSchedulesFromRailway(Object data);

    /**
     * Method that edits schedules from a railway
     *
     * @param data list of hashmaps
     * @return a response containing the location A(String), location B(String) and the shedules(String)
     */
    public Response<?> editSchedules(Object data);

    /**
     * Method that adds a schedules to a railway (if railway does not exist it is created)
     *
     * @param data Hashmap containing the location A(String), location B(String) and the shedules(String)
     * @return a response containing the result of the operation
     */
    public Response<?> addWayToRailwayListWithSchedules(Object data);

    /**
     * Method that removes a railway to from railways list
     *
     * @param data the railway to be removed
     * @return a response containing the result of the operation
     */
    Response<?> removeWayFromRailwayList(Object data);

    /**
     * Method to add a schedule to a railway
     *
     * @param data Hashmap containing the railway(Railway) and the schedule(String)
     * @return a response containing the result of the operation
     */
    Response<?> addScheduleToRailway(Object data);

    /**
     * Method to edit a schedule from a railway
     *
     * @param data Hashmap containing the railway(Railway), the schedule to be edited(String)
     *             and the new schedule(string)
     * @return a response containing the result of the operation
     */
    Response<?> editScheduleFromRailway(Object data);

    /**
     * Method to remove a schedule from a railway
     *
     * @param data Hashmap containing the railway(Railway) and the schedule(String)
     * @return a response containing the result of the operation
     */
    Response<?> removeScheduleFromRailway(Object data);

    //NETWORK

    /**
     * Method that suspends the network
     *
     * @return a response containing the result of the operation
     */
    Response<?> suspendNetworkAndNotify();

    /**
     * Method that unsuspend the network
     *
     * @return a response containing the result of the operation
     */
    Response<?> unsuspendNetworkAndNotify();

    /**
     * Method that checks if the network is suspended
     *
     * @return a response containing the result of the operation
     */
    Response<?> networkIsSuspended();

    //REPORTS

    /**
     * Getter for the reports
     *
     * @return a response containing the result of the operation
     */
    Response<?> getReports();

    /**
     * Method to report a schedule change
     *
     * @param data Hashmap containing the passenger username(String), the target railway(Railway) and
     *             the comment(String)
     * @return a response containing the result of the operation
     */
    Response<?> reportScheduleAlteration(Object data);
}
