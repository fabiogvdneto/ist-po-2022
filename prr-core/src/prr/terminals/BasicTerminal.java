package prr.terminals;

import prr.Network;
import prr.clients.Client;
import prr.exceptions.InvalidTerminalUIDException;

public class BasicTerminal extends Terminal {

    public BasicTerminal(Network network, Client owner, String uid) throws InvalidTerminalUIDException {
        super(network, owner, uid);
    }

    @Override
    public boolean isCommunicationTypeSupported(String type) {
        return type.equals("VOICE");
    }

    @Override
    public String getType() {
        return "BASIC";
    }
}
