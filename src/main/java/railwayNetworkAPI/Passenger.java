package railwayNetworkAPI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the passenger
 */
public class Passenger implements Serializable {
    private String userName;
    private String password;
    private final List<Railway> railways;

    /**
     * Class constructor
     *
     * @param userName the passenger username
     * @param password the passenger password
     * @param railwaysList the passenger railways
     */
    public Passenger(String userName, String password, List<Railway> railwaysList) {
        this.userName = userName;
        this.password = password;
        this.railways = railwaysList;
    }

    /**
     * Class constructor
     *
     * @param userName the passenger username
     * @param password the passenger password
     */
    public Passenger(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.railways = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return Objects.equals(userName, passenger.userName) && Objects.equals(password, passenger.password) && Objects.equals(railways, passenger.railways);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password, railways);
    }

    /**
     * Getter for the username
     *
     * @return the username
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Getter for the password
     *
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Getter for the railways list
     *
     * @return railways list
     */
    public List<Railway> getRailways() {
        return this.railways;
    }

    /**
     * Function that adds a railway to the passenger
     *
     * @param railway the railway to be added
     */
    public void addRailway(Railway railway) {
        this.railways.add(railway);
    }

    /**
     * Function that removes a railway from a passenger
     *
     * @param railway the railway to be removed
     */
    public void removeRailway(Railway railway) {
        this.railways.remove(railway);
    }
}
