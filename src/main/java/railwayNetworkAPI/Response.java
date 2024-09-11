package railwayNetworkAPI;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an api response
 *
 * @param <T>
 */
public class Response<T> implements Serializable {
    private T data;
    private Boolean isSuccessful;
    private ResponseType layer1ResponseType;
    private ResponseType layer2ResponseType;
    private List<String> targetClients;
    private String methodSignature;

    /**
     * Class constructor
     *
     * @param data the data of the response
     * @param isSuccessful if the response is successful
     * @param layer1ResponseType if the response is of type broadcast, multicast or unicast to the local nodes
     * @param layer2ResponseType if the response is of type broadcast, multicast or unicast to the local nodes clients
     * @param methodSignature the method signature of the method that originated this response
     */
    public Response(
            T data, Boolean isSuccessful,
            ResponseType layer1ResponseType,
            ResponseType layer2ResponseType,
            String methodSignature
            ) {
        this.data = data;
        this.isSuccessful = isSuccessful;
        this.layer1ResponseType = layer1ResponseType;
        this.layer2ResponseType = layer2ResponseType;
        this.methodSignature = methodSignature;
    }

    /**
     * Class constructor
     *
     * @param data the data of the response
     * @param isSuccessful if the response is successful
     * @param layer1ResponseType if the response is of type broadcast, multicast or unicast to the local nodes
     * @param layer2ResponseType if the response is of type broadcast, multicast or unicast to the local nodes clients
     * @param targetClients the clients that have to receive this response (if multicast)
     * @param methodSignature the method signature of the method that originated this response
     */
    public Response(
            T data, Boolean isSuccessful,
            ResponseType layer1ResponseType,
            ResponseType layer2ResponseType,
            List<String> targetClients,
            String methodSignature
    ) {
        this.data = data;
        this.isSuccessful = isSuccessful;
        this.layer1ResponseType = layer1ResponseType;
        this.layer2ResponseType = layer2ResponseType;
        this.targetClients = targetClients;
        this.methodSignature = methodSignature;
    }

    /**
     * Getter for the data
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Check if response is successful
     *
     * @return true if successful otherwise false
     */
    public Boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Getter for the response type (broadcast, multicast or unicast to the local nodes)
     *
     * @return the response type
     */
    public ResponseType getLayer1ResponseType() {
        return layer1ResponseType;
    }

    /**
     * Getter for the response type (broadcast, multicast or unicast to the local nodes clients)
     *
     * @return the response type
     */
    public ResponseType getLayer2ResponseType() {
        return layer2ResponseType;
    }

    /**
     * Getter for the clients that have to receive this response (if multicast)
     *
     * @return the clients that have to receive this response
     */
    public List<String> getTargetClients() {
        return targetClients;
    }

    /**
     * Getter for the method signature of the method that originated this response
     *
     * @return the method signature of the method that originated this response
     */
    public String getMethodSignature(){
        return this.methodSignature;
    }
}
