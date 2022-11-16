package prr.communications;

import prr.tariffs.TariffPlan;
import prr.terminals.Terminal;

public class VoiceCommunication extends InteractiveCommunication {

    public VoiceCommunication(int id, Terminal sender, Terminal receiver) {
        super(id, sender, receiver);
    }

    @Override
    public String getType() {
        return "VOICE";
    }

    @Override
    protected double accept(TariffPlan plan) {
        return plan.taxVoiceCommunication(this);
    }
}
