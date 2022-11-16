package prr.terminals;

import prr.Network;
import prr.clients.Client;
import prr.exceptions.InvalidTerminalUIDException;

public class FancyTerminal extends BasicTerminal {

    public FancyTerminal(Network network, Client owner, String uid)
            throws InvalidTerminalUIDException {
        super(network, owner, uid);
    }

    @Override
    public boolean isCommunicationTypeSupported(String type) {
        return (type.equals("VIDEO") || super.isCommunicationTypeSupported(type));
    }

    @Override
    public String getType() {
        return "FANCY";
    }
}
