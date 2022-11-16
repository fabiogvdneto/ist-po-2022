package prr.terminals;

import java.util.HashSet;
import java.util.Set;

import prr.clients.Client;

class BusyState extends TerminalState {

    private final Set<Client> _missedInteractiveComms;
    private final TerminalState _prev;

    public BusyState(Terminal term, TerminalState prev) {
        this(term, prev, new HashSet<>());
    }

    public BusyState(Terminal term, TerminalState prev, Set<Client> missedInteractiveComms) {
        super(term);
        _prev = prev;
        _missedInteractiveComms = missedInteractiveComms;
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public void turnOn() throws IllegalStateException {
        throw new IllegalStateException();
    }

    @Override
    public void turnOff() throws IllegalStateException {
        throw new IllegalStateException();
    }

    @Override
    public void silence() throws IllegalStateException {
        throw new IllegalStateException();
    }

    @Override
    public boolean canEndCurrentCommunication() {
        return (_term.getOngoingCommunication().getOrigin() == _term);
    }

    @Override
    public void onInteractiveCommunicationAttempt(Client from) {
        if (!from.canReceiveNotifications()) return;
        _missedInteractiveComms.add(from);
    }

    @Override
    public void onEndInteractiveCommunication() {
        _term.setState(_prev);

        if (!_prev.isIdle()) return;

        sendNotif(createNotif("B2I"), _missedInteractiveComms);
    }

    @Override
    public String toString() {
        return "BUSY";
    }
}
