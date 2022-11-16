package prr.terminals;

import java.util.HashSet;
import java.util.Set;

import prr.clients.Client;

class SilentState extends TerminalState {

    private final Set<Client> _missedInteractiveComms;

    public SilentState(Terminal term) {
        this(term, new HashSet<>());
    }

    public SilentState(Terminal term, Set<Client> missedInteractiveComms) {
        super(term);
        _missedInteractiveComms = missedInteractiveComms;
    }

    @Override
    public void turnOn() throws IllegalStateException {
        super.turnOn();

        sendNotif(createNotif("S2I"), _missedInteractiveComms);
    }

    @Override
    public void turnOff() throws IllegalStateException {
        _term.setState(new OffState(_term, _missedInteractiveComms));
    }

    @Override
    public void silence() throws IllegalStateException {
        /* do nothing (already silent) */
    }

    @Override
    public boolean canStartCommunication() {
        return true;
    }

    @Override
    public void onInteractiveCommunicationAttempt(Client from) {
        if (!from.canReceiveNotifications()) return;
        _missedInteractiveComms.add(from);
    }

    @Override
    public void onStartInteractiveCommunication() {
        _term.setState(new BusyState(_term, this, _missedInteractiveComms));
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    @Override
    public String toString() {
        return "SILENCE";
    }
}
