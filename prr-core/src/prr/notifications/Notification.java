package prr.notifications;

import java.io.Serializable;

import prr.terminals.Terminal;

public class Notification implements Serializable {

    private final String _type;
    private final Terminal _terminal;

    public Notification(String type, Terminal terminal) {
        _type = type;
        _terminal = terminal;
    }

    public String getType() {
        return _type;
    }

    public Terminal getTerminal() {
        return _terminal;
    }

    /**
     * Format: tipo-de-notificação|idTerminal
     *
     * @return the string representation of this notification.
     */
    @Override
    public String toString() {
        return getType() + "|" + getTerminal().getUID();
    }
}
