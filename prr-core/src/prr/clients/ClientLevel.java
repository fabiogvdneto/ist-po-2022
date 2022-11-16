package prr.clients;

import java.io.Serializable;

import prr.communications.Communication;

abstract class ClientLevel implements Serializable {

    /** The client. */
    protected final Client _client;

    public ClientLevel(Client client) {
        _client = client;
    }

    /**
     * Called by {@link Client#onPerformPayment(int)} when paying a communication.
     */
    public void onPerformPayment() { 
        /* empty (should be overriden) */
    }

    /**
     * Called by {@link Client#onTextCommunication(int)} and {@link Client#onEndInteractiveCommunication(int)}
     * when performing a communication (sending a text or ending an interactive communication)
     * 
     * @param comm the communication performed
     */
    public void onPerformCommunication(Communication comm) {
        /* empty (should be overriden) */
    }

    @Override
    public abstract String toString();
}
