package prr.terminals;

class IdleState extends TerminalState {

    public IdleState(Terminal term) {
        super(term);
    }

    @Override
    public boolean isIdle() {
        return true;
    }

    @Override
    public void turnOn() throws IllegalStateException {
        /* do nothing (already on/idle) */
    }

    @Override
    public boolean canStartCommunication() {
        return true;
    }

    @Override
    public void onStartInteractiveCommunication() {
        _term.setState(new BusyState(_term, this));
    }

    @Override
    public String toString() {
        return "IDLE";
    }
}
