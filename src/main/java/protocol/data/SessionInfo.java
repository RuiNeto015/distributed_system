package protocol.data;

import java.io.Serializable;

/**
 * Class that holds a user session info
 */
public class SessionInfo implements Serializable {
    private final String userName;

    /**
     * Class constructor
     *
     * @param userName the user name
     */
    public SessionInfo(String userName) {
        this.userName = userName;
    }

    /**
     * Getter for the username
     *
     * @return the username
     */
    public String getUsername() {
        return this.userName;
    }
}
