package prr.terminals;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import prr.Network;
import prr.clients.Client;
import prr.communications.Communication;
import prr.communications.CommunicationStatus;
import prr.exceptions.AlreadyFriendsException;
import prr.exceptions.CommunicationNotFoundException;
import prr.exceptions.FriendNotFoundException;
import prr.exceptions.InvalidTerminalUIDException;
import prr.exceptions.TerminalNotFoundException;

// FIXME add more import if needed (cannot import from pt.tecnico or prr.app)

/**
 * Abstract terminal.
 */
abstract public class Terminal implements Serializable {

	/** Serial number for serialization. */
    @Serial private static final long serialVersionUID = 202208091753L;

    private final Network _network;
    private final String _uid;
    private final Client _owner;

    public Terminal(Network network, Client owner, String id) throws InvalidTerminalUIDException {
        validateID(_uid = id);
        _network = network;
        _owner = owner;
    }

    private void validateID(String id) throws InvalidTerminalUIDException {
        if ((id.length() != 6) || !id.chars().allMatch(Character::isDigit))
            throw new InvalidTerminalUIDException();
    }

    /**
     *
     * @return the id of the terminal.
     */
    public String getUID() {
        return _uid;
    }

    /**
     *
     * @return the owner of the terminal.
     */
    public Client getOwner() {
        return _owner;
    }

    /*
     * ---- Friends ----
     */

    /** The friends of this terminal. */
    private final Map<String, Terminal> _friends = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * 
     * @return the friends of this terminal
     */
    public Collection<Terminal> getFriends() {
        return Collections.unmodifiableCollection(_friends.values());
    }

    /**
     * 
     * @param friend id of the friend
     * @return the corresponding friend
     * @throws FriendNotFoundException if the friend was not found
     */
    public Terminal getFriend(String friend) throws FriendNotFoundException {
        Terminal term = _friends.get(friend);
        if (term == null) throw new FriendNotFoundException();
        return term;
    }

    /**
     * 
     * @param friend the friend to add
     * @throws TerminalNotFoundException if the terminal was not found
     * @throws AlreadyFriendsException if the terminals are friends already
     */
    public void addFriend(String friend) throws TerminalNotFoundException, AlreadyFriendsException {
        if (getUID().equals(friend)) return; // a terminal cannot be friend of itself
        Terminal term = _network.getTerminal(friend); // throws TerminalNotFoundException
        if (_friends.putIfAbsent(friend, term) != null)
            throw new AlreadyFriendsException();
    }

    /**
     * 
     * @param friend the friend to remove
     * @throws FriendNotFoundException if the friend was not found
     */
    public void removeFriend(String friend) throws FriendNotFoundException {
        if (_friends.remove(friend) == null) throw new FriendNotFoundException();
    }

    /**
     * 
     * @param id the id to test
     * @return true if there is a friend with the given id
     */
    public boolean isFriend(String id) {
        return _friends.containsKey(id);
    }

    /**
     * 
     * @return true if the terminal has any friends
     */
    public boolean hasFriends() {
        return !_friends.isEmpty();
    }

    /*
     * ---- State ----
     */

    private TerminalState _state = new IdleState(this);

    void setState(TerminalState state) {
        _state = state;
    }

    public boolean isOff() {
        return _state.isOff();
    }

    public boolean isBusy() {
        return _state.isBusy();
    }

    public boolean isSilent() {
        return _state.isSilent();
    }

    public boolean isIdle() {
        return _state.isIdle();
    }

    public void turnOn() throws IllegalStateException {
        _state.turnOn();
    }

    public void turnOff() throws IllegalStateException {
        _state.turnOff();
    }

    public void silence() throws IllegalStateException {
        _state.silence();
    }

    /*
     * ---- Communications ----
     */

    /** All the communications sent/started by this terminal. */
    private final Map<Integer, Communication> _outbox = new TreeMap<>();

    /** All the communications received by this terminal. */
    private final Map<Integer, Communication> _inbox = new TreeMap<>();

    /** The ongoing communication (might be null). */
    private Communication _ongoing;

    /**
     * 
     * @param id the id of the communication
     * @return the corresponding communication
     * @throws CommunicationNotFoundException if the communication was not found
     */
    public Communication getFromInbox(int id) throws CommunicationNotFoundException {
        Communication comm = _inbox.get(id);
        if (comm == null) throw new CommunicationNotFoundException();
        return comm;
    }

    /**
     * 
     * @param id the id of the communication
     * @return the corresponding communication
     * @throws CommunicationNotFoundException if the communication was not found
     */
    public Communication getFromOutbox(int id) throws CommunicationNotFoundException {
        Communication comm = _outbox.get(id);
        if (comm == null) throw new CommunicationNotFoundException();
        return comm;
    }

    /**
     * 
     * @return all the communications sent/started by this terminal
     */
    public Collection<Communication> getOutbox() {
        return Collections.unmodifiableCollection(_outbox.values());
    }

    /**
     * 
     * @return all the communications received by this terminal
     */
    public Collection<Communication> getInbox() {
        return Collections.unmodifiableCollection(_inbox.values());
    }

    /**
     * 
     * @return the ongoing communication (might be null)
     */
    public Communication getOngoingCommunication() {
        return _ongoing;
    }

    /**
     * 
     * @return true if the terminal is unused (has no communications yet)
     */
    public boolean isUnused() {
        return (_outbox.isEmpty() && _inbox.isEmpty());
    }

    /**
     * Checks if this terminal can end the current interactive communication.
     *
     * @return true if this terminal is busy (i.e., it has an active interactive communication) and
     *          it was the originator of this communication.
     **/
    public boolean canEndCurrentCommunication() {
        return _state.canEndCurrentCommunication();
    }

    /**
     * Checks if this terminal can start a new communication.
     *
     * @return true if this terminal is neither off neither busy, false otherwise.
     **/
    public boolean canStartCommunication() {
        return _state.canStartCommunication();
    }

    /**
     * Called by {@link Network#sendTextCommunication(String, String, String)} when
     * text communicating with someone.
     * 
     * @param commID the id of the communication
     */
    public void onTextCommunication(int commID) {
        try {
            addCommunication(_network.getCommunication(commID));
        } catch (CommunicationNotFoundException e) { /* just ignore */ }
    }

    /**
     * Called by {@link Network#startInteractiveCommunication(String, String, String)} when
     * starting an interactive communication with someone.
     * 
     * @param commID the id of the communication
     */
    public void onStartInteractiveCommunication(int commID) {
        try {
            Communication comm = _network.getCommunication(commID);

            if (!addCommunication(comm)) return;

            _ongoing = comm;
            _state.onStartInteractiveCommunication();
        } catch (CommunicationNotFoundException e) { /* just ignore */ }
    }

    /**
     * Called by {@link Network#sendTextCommunication(String, String, String)} when
     * someone attempts to send a text communication to this terminal but fails to do so.
     * 
     * @param from the client that made the attempt
     */
    public void onTextCommunicationAttempt(Client from) {
        _state.onTextCommunicationAttempt(from);
    }

    /**
     * Called by {@link Network#startInteractiveCommunication(String, String, String)} when
     * someone attempts to start an interactive communication with this terminal but fails to do so.
     * 
     * @param from the client that made the attempt
     */
    public void onInteractiveCommunicationAttempt(Client from) {
        _state.onInteractiveCommunicationAttempt(from);
    }

    private boolean addCommunication(Communication comm) {
        boolean outbox = addToOutbox(comm);
        boolean inbox = addToInbox(comm);
        return (inbox || outbox);
    }

    private boolean addToOutbox(Communication comm) {
        return comm.isOrigin(getUID())
                && (_outbox.putIfAbsent(comm.getUID(), comm) == null);
    }

    private boolean addToInbox(Communication comm) {
        return comm.isDestination(getUID())
                && (_inbox.putIfAbsent(comm.getUID(), comm) == null);
    }

    /**
     * Called by {@link Communication#finish(int)} when ending interactive communications.
     */
    public void onEndInteractiveCommunication() {
        if (_ongoing == null) return;
        if (_ongoing.getStatus() != CommunicationStatus.FINISHED) return;

        _ongoing = null;
        _state.onEndInteractiveCommunication();
    }

    /**
     * 
     * @param type the type of the communication to test
     * @return true if the communication type is supported by this terminal
     */
    public abstract boolean isCommunicationTypeSupported(String type);

    /*
     * ---- Credits ----
     */

    public double balance() {
        return _outbox.values().stream()
                    .mapToDouble(c -> (c.isPaid() ? c.getCost() : -c.getCost()))
                    .sum();
    }

    public double payments() {
        return _outbox.values().stream()
                    .mapToDouble(c -> (c.isPaid() ? c.getCost() : 0))
                    .sum();
    }

    public double debts() {
        return _outbox.values().stream()
                    .mapToDouble(c -> (c.isPaid() ? 0 : c.getCost()))
                    .sum();
    }

    /*
     * ---- Abstract ----
     */

    /**
     *
     * @return the type of this terminal object (BASIC or FANCY).
     */
    public abstract String getType();

    /*
     * ---- Object ----
     */

    /**
     * Format: terminalType|terminalId|clientId|terminalStatus|balance-paid|balance-debts|friend1,...,friendN
     *
     * @return the string representation of this terminal, in the format described above.
     */
    @Override
    public String toString() {
        String sep = "|";
        StringBuilder builder = new StringBuilder(getType())
                .append(sep).append(getUID())
                .append(sep).append(getOwner().getUID())
                .append(sep).append(_state)
                .append(sep).append(Math.round(payments()))
                .append(sep).append(Math.round(debts()));

        if (hasFriends())
            builder.append(sep).append(String.join(",", _friends.keySet()));

        return builder.toString();
    }
}
