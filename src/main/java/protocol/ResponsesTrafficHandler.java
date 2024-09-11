package protocol;

import railwayNetworkAPI.Response;

import java.util.List;

/**
 * Class that controls the access to the list of responses gaven by the local node
 */
public class ResponsesTrafficHandler {
    private final List<Response<?>> responses;

    /**
     * Class constructor
     *
     * @param responses the list of responses
     */
    public ResponsesTrafficHandler(List<Response<?>> responses) {
        this.responses = responses;
    }

    /**
     * Function to add a new response
     *
     * @param response the new response to add
     */
    public synchronized void addResponse(Response<?> response) {
        this.responses.add(response);
    }

    /**
     * Function to remove a response
     *
     * @param response the response to be removed
     */
    public synchronized void removeResponse(Response<?> response) {
        this.responses.remove(response);
    }
}
