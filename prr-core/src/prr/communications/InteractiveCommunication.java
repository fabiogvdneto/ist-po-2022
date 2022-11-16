package prr.communications;

import prr.terminals.Terminal;

public abstract class InteractiveCommunication extends Communication {

    protected int _duration;

    public InteractiveCommunication(int id, Terminal sender, Terminal receiver) {
        super(id, sender, receiver);
    }

    @Override
    public boolean isInteractive() {
        return true;
    }

    @Override
    public double finish(int units) throws IllegalStateException {
        double cost = super.finish(units);
        getOrigin().onEndInteractiveCommunication();
        getDestination().onEndInteractiveCommunication();
        getOrigin().getOwner().onEndInteractiveCommunication(getUID());
        getDestination().getOwner().onEndInteractiveCommunication(getUID());
        return cost;
    }
}
