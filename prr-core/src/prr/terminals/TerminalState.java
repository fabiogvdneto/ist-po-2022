package prr.terminals;

import java.io.Serializable;
import java.util.Collection;

import prr.clients.Client;
import prr.notifications.Notification;

abstract class TerminalState implements Serializable {

    protected final Terminal _term;

    public TerminalState(Terminal term) {
        _term = term;
    }

    public boolean isOff() {
        return false;
    }

    public boolean isBusy() {
        return false;
    }

    public boolean isSilent() {
        return false;
    }

    public boolean isIdle() {
        return false;
    }

    public void turnOn() throws IllegalStateException {
        _term.setState(new IdleState(_term));
    }

    public void turnOff() throws IllegalStateException {
        _term.setState(new OffState(_term));
    }

    public void silence() throws IllegalStateException {
        _term.setState(new SilentState(_term));
    }

    public boolean canStartCommunication() {
        return false;
    }

    public boolean canEndCurrentCommunication() {
        return false;
    }

    public void onTextCommunicationAttempt(Client from) {
        /* empty (should be overriden) */
    }

    public void onInteractiveCommunicationAttempt(Client from) {
        /* empty (should be overriden) */
    }

    public void onStartInteractiveCommunication() {
        /* empty (should be overriden) */
    }

    public void onEndInteractiveCommunication() {
        /* empty (should be overriden) */
    }

    public final void sendNotif(Notification notif, Collection<Client> clients) {
        clients.forEach(c -> c.sendNotification(notif));
        clients.clear();
    }

    public final Notification createNotif(String type) {
        return new Notification(type, _term);
    }

    @Override
    public abstract String toString();
}
