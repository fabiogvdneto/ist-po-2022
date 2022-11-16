package prr.terminals;

import java.util.HashSet;
import java.util.Set;

import prr.clients.Client;
import prr.notifications.Notification;

class OffState extends TerminalState {

    private final Set<Client> _missedTextComms = new HashSet<>();
    private final Set<Client> _missedInteractiveComms;

    public OffState(Terminal term) {
        this(term, new HashSet<>());
    }

    public OffState(Terminal term, Set<Client> missedInteractiveComms) {
        super(term);
        _missedInteractiveComms = missedInteractiveComms;
    }

    @Override
    public boolean isOff() {
        return true;
    }

    @Override
    public void turnOn() throws IllegalStateException {
        super.turnOn();

        Notification notif = createNotif("O2I");
        sendNotif(notif, _missedTextComms);
        sendNotif(notif, _missedInteractiveComms);
    }

    @Override
    public void turnOff() throws IllegalStateException {
        /* do nothing (already off) */
    }

    @Override
    public void silence() throws IllegalStateException {
        _term.setState(new SilentState(_term, _missedInteractiveComms));

        sendNotif(createNotif("O2S"), _missedTextComms);
    }

    @Override
    public void onTextCommunicationAttempt(Client from) {
        if (!from.canReceiveNotifications()) return;
        _missedInteractiveComms.remove(from);
        _missedTextComms.add(from);
    }

    @Override
    public void onInteractiveCommunicationAttempt(Client from) {
        if (!from.canReceiveNotifications()) return;
        _missedTextComms.remove(from);
        _missedInteractiveComms.add(from);
    }

    @Override
    public String toString() {
        return "OFF";
    }
}
