package prr.communications;

import prr.tariffs.TariffPlan;
import prr.terminals.Terminal;

public class TextCommunication extends Communication {

    private final String _message;

    public TextCommunication(int id, Terminal sender, Terminal receiver, String msg) {
        super(id, sender, receiver);
        _message = msg;
        finish(msg.length());
    }

    public String getMessage() {
        return _message;
    }

    @Override
    public String getType() {
        return "TEXT";
    }
    
    @Override
    protected double accept(TariffPlan plan) {
        return plan.taxTextCommunication(this);
    }
}
