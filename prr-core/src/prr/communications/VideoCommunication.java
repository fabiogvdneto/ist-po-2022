package prr.communications;

import prr.tariffs.TariffPlan;
import prr.terminals.Terminal;

public class VideoCommunication extends InteractiveCommunication {

    public VideoCommunication(int id, Terminal sender, Terminal receiver) {
        super(id, sender, receiver);
    }

    @Override
    public String getType() {
        return "VIDEO";
    }

    @Override
    protected double accept(TariffPlan plan) {
        return plan.taxVideoCommunication(this);
    }
}
