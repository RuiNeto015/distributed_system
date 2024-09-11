package railwayNetworkAPI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the railway
 */
public class Railway implements Serializable {
    private final String localidade1;
    private final String localidade2;
    private final List<String> schedules;

    /**
     * Class constructor
     *
     * @param localidade1 the railway local A
     * @param localidade2 the railway local B
     * @param schedules the railway schedules
     */
    public Railway(String localidade1, String localidade2, List<String> schedules) {
        this.localidade1 = localidade1;
        this.localidade2 = localidade2;
        this.schedules = schedules;
    }

    /**
     * Class constructor
     *
     * @param localidade1 the railway local A
     * @param localidade2 the railway local B
     */
    public Railway(String localidade1, String localidade2) {
        this.localidade1 = localidade1;
        this.localidade2 = localidade2;
        this.schedules = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Railway railway = (Railway) o;
        return localidade1.equals(railway.localidade1) && localidade2.equals(railway.localidade2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localidade1, localidade2, schedules);
    }

    /**
     * Getter for the railway local A
     *
     * @return the railway local A
     */
    public String getLocalidade1() {
        return this.localidade1;
    }

    /**
     * Getter for the railway local B
     *
     * @return the railway local B
     */
    public String getLocalidade2() {
        return this.localidade2;
    }

    /**
     * Getter for the railway schedules
     *
     * @return the railway schedules
     */
    public List<String> getSchedules() {
        return this.schedules;
    }

    /**
     * Function to add a schedule to the railway
     *
     * @param schedule the new schedule to be added
     */
    public void addSchedule(String schedule) {
        this.schedules.add(schedule);
    }

    /**
     * Function to remove a schedule from a railway
     *
     * @param schedule the schedule to be removed
     */
    public void removeSchedule(String schedule) {
        this.schedules.remove(schedule);
    }
}
