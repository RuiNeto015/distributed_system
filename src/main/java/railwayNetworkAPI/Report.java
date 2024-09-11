package railwayNetworkAPI;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a railway report
 */
public class Report implements Serializable {
    private final Passenger passenger;
    private final Railway railway;
    private final String comment;
    private final String dateTime;

    /**
     * Class constructor
     *
     * @param passenger the passenger that created the report
     * @param railway the target railway of the report
     * @param comment the report comment
     */
    public Report(Passenger passenger, Railway railway, String comment){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.passenger = passenger;
        this. railway = railway;
        this.comment = comment;
        this.dateTime = LocalDateTime.now().format(formatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(passenger, report.passenger) && Objects.equals(railway, report.railway) && Objects.equals(comment, report.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passenger, railway, comment);
    }

    /**
     * Getter for the passenger
     *
     * @return the passenger
     */
    public Passenger getPassenger() {
        return this.passenger;
    }

    /**
     * Getter for the railway
     *
     * @return the railway
     */
    public Railway getRailway() {
        return this.railway;
    }

    /**
     * Getter for the date time of the report
     *
     * @return the date time of the report
     */
    public String getDateTime() {
        return dateTime.toString();
    }

    /**
     * Getter for the comment of the report
     *
     * @return the comment of the report
     */
    public String getComment() {
        return this.comment;
    }
}
