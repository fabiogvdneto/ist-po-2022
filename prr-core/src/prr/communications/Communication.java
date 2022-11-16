package prr.communications;

import java.io.Serializable;

import prr.exceptions.CommunicationAlreadyPaidException;
import prr.tariffs.TariffPlan;
import prr.terminals.Terminal;

public abstract class Communication implements Serializable, Comparable<Communication> {

    private final int _uid;
    private final Terminal _origin;
    private final Terminal _destin;

    private CommunicationStatus _status = CommunicationStatus.ONGOING;
    private int _units;
    private double _cost;
    private boolean _paid;

    public Communication(int id, Terminal origin, Terminal destin) {
        _uid = id;
        _origin = origin;
        _destin = destin;
    }

    public int getUID() {
        return _uid;
    }

    public Terminal getOrigin() {
        return _origin;
    }

    public Terminal getDestination() {
        return _destin;
    }

    public CommunicationStatus getStatus() {
        return _status;
    }

    public int getUnits() {
        return _units;
    }

    public double getCost() {
        return _cost;
    }

    public boolean isPaid() {
        return _paid;
    }

    public void performPayment() throws IllegalStateException, CommunicationAlreadyPaidException {
        if (getStatus() != CommunicationStatus.FINISHED) throw new IllegalStateException();
        if (isPaid()) throw new CommunicationAlreadyPaidException();
        
        _paid = true;
        getOrigin().getOwner().onPerformPayment(getUID());
    }

    public boolean isFriendly() {
        return getOrigin().isFriend(getDestination().getUID());
    }

    public boolean isOrigin(String id) {
        return getOrigin().getUID().equals(id);
    }

    public boolean isDestination(String id) {
        return getDestination().getUID().equals(id);
    }

    public boolean isFromClient(String id) {
        return getOrigin().getOwner().getUID().equals(id);
    }
    
    public boolean isToClient(String id) {
        return getDestination().getOwner().getUID().equals(id);
    }

    /**
     * Ends the communication by setting it as FINISHED and calculating its units and cost.
     * 
     * @param units the units (chars count if text, duration if interactive)
     * @return the cost of this communication
     * @throws IllegalStateException if the communication is already finished
     */
    public double finish(int units) throws IllegalStateException {
        if (_status == CommunicationStatus.FINISHED)
            throw new IllegalStateException(); // CommunicationAlreadyFinishedException ?

        _status = CommunicationStatus.FINISHED;
        _units = units;
        _cost = accept(getOrigin().getOwner().getTariffPlan());
        return _cost;
    }

    /**
     * Uses the given plan to calculate the cost of this communication.
     * 
     * @param plan the tariff plan to use
     * @return the cost of this communication
     */
    protected abstract double accept(TariffPlan plan); // Visitor

    /**
     * 
     * @return the type of this communication (TEXT, VOICE or VIDEO)
     */
    public abstract String getType();

    /**
     * 
     * @return true if interactive, false if text
     */
    public boolean isInteractive() {
        return false;
    }

    /**
     * Format: type|idCommunication|idSender|idReceiver|units|price|status
     *
     * @return the string representation of this communication, in the format described above.
     */
    @Override
    public String toString() {
        String sep = "|";
        return getType()
            + sep + getUID()
            + sep + getOrigin().getUID()
            + sep + getDestination().getUID()
            + sep + getUnits()
            + sep + Math.round(getCost())
            + sep + getStatus();
    }

    @Override
    public int compareTo(Communication c) {
        return Integer.compare(getUID(), c.getUID());
    }
}
