package prr.clients;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import prr.Network;
import prr.communications.Communication;
import prr.communications.CommunicationStatus;
import prr.exceptions.CommunicationNotFoundException;
import prr.exceptions.NotificationsAlreadyDisabledException;
import prr.exceptions.NotificationsAlreadyEnabledException;
import prr.exceptions.TerminalNotFoundException;
import prr.notifications.Notification;
import prr.notifications.NotificationDeliveryStrategy;
import prr.tariffs.TariffPlan;
import prr.terminals.Terminal;

public class Client implements Serializable {

    private final Network _network;
    private final String _uid;
    private String _name;
    private int _taxID;
    private ClientLevel _level = new NormalLevel(this);

    public Client(Network network, String uid, String name, int taxID) {
        _network = network;
        _uid = uid;
        _name = name;
        _taxID = taxID;
        
        setTariffPlan("base");
    }

    public String getUID() {
        return _uid;
    }

    public String getName() {
        return _name;
    }

    public int getTaxID() {
        return _taxID;
    }

    public String getLevel() {
        return _level.toString();
    }

    void setLevel(ClientLevel level) {
        _level = level;
    }
    
    /*
     * ---- Terminals ----
     */

    /** A Map storing terminals by id. */
    private final Map<String, Terminal> _terminals = new TreeMap<>();

    /**
     * 
     * @param id the id of the terminal
     * @return the corresponding terminal
     * @throws TerminalNotFoundException if this client does not own the terminal
     */
    public Terminal getTerminal(String id) throws TerminalNotFoundException {
        Terminal term = _terminals.get(id);
        if (term == null) throw new TerminalNotFoundException();
        return term;
    }

    /**
     * 
     * @return the terminals owned by this client
     */
    public Collection<Terminal> getTerminals() {
        return Collections.unmodifiableCollection(_terminals.values());
    }

    /**
     * Called when registering a terminal by {@link Network#registerTerminal(String, String, String)}.
     * 
     * @param name the id of the terminal
     */
    public void onRegisterTerminal(String id) {
        try {
            Terminal term = _network.getTerminal(id);
            if (term.getOwner() != this) return;
            _terminals.putIfAbsent(id, term);
        } catch (TerminalNotFoundException e) { /* just ignore */ }
    }

    /*
     * ---- Communications ----
     */

    /** A Map containing all ongoing communications by id. */
    private final Map<Integer, Communication> _ongoingComms = new TreeMap<>();

    /** A Map containing all in debt communications by id. */
    private final Map<Integer, Communication> _inDebtComms = new TreeMap<>();

    /** A Map containing all paid communications by id. */
    private final Map<Integer, Communication> _paidComms = new TreeMap<>();

    /**
     * Called by {@link Communication#performPayment()} when this client pays a communication
     * 
     * @param commID the communication id
     */
    public void onPerformPayment(int commID) {
        Communication comm = _inDebtComms.get(commID);

        if (comm == null) return;
        if (!comm.isPaid()) return;

        _inDebtComms.remove(commID); 
        _paidComms.put(commID, comm);
        _level.onPerformPayment();
    }

    /**
     * Called by {@link Network#registerTerminal(String, String, String)} when a new text communication
     * is sent and this terminal is either the origin or the destination.
     * 
     * @param commID the communication id
     */
    public void onTextCommunication(int commID) {
        try {
            Communication comm = _network.getCommunication(commID);

            if (!comm.isFromClient(getUID())) return;
            if (comm.isPaid()) return;

            _inDebtComms.putIfAbsent(commID, comm);
            _level.onPerformCommunication(comm);
        } catch (CommunicationNotFoundException e) { /* just ignore */ }
    }

    /**
     * Called by {@link Network#startInteractiveCommunication(String, String, String)} when a new communication
     * is started and this terminal is either the origin or the destination.
     * 
     * @param commID the communication id
     */
    public void onStartInteractiveCommunication(int commID) {
        try {
            Communication comm = _network.getCommunication(commID);
            
            if (!comm.isFromClient(getUID()) && !comm.isToClient(getUID())) return;

            _ongoingComms.putIfAbsent(commID, comm);
        } catch (CommunicationNotFoundException e) { /* just ignore */ }
    }

    /**
     * Called by {@link Communication#finish(int)} when finishing an interactive communication
     * and this terminal is either the origin or the destination.
     * 
     * @param commID the communication id
     */
    public void onEndInteractiveCommunication(int commID) {
        Communication comm = _ongoingComms.get(commID);

        if (comm == null) return;
        if (comm.getStatus() != CommunicationStatus.FINISHED) return;

        _ongoingComms.remove(commID);

        if (comm.isFromClient(getUID())) {
            _inDebtComms.put(commID, comm);
            _level.onPerformCommunication(comm);
        }
    }

    /**
     * 
     * @return all the communications started by this client
     */
    public Collection<Communication> getOutbox() {
        return _terminals.values().stream()
                .flatMap(t -> t.getOutbox().stream())
                .sorted().toList();
    }

    /**
     * 
     * @return all the communications received by this client
     */
    public Collection<Communication> getInbox() {
        return _terminals.values().stream()
                .flatMap(t -> t.getInbox().stream())
                .sorted().toList();
    }

    /*
     * ---- Credits ----
     */

    public double balance() {
        return (payments() - debts());
    }

    public double payments() {
        return _paidComms.values().stream().mapToDouble(Communication::getCost).sum();
    }

    public double debts() {
        return _inDebtComms.values().stream().mapToDouble(Communication::getCost).sum();
    }

    /*
     * ---- Notifications ----
     */

    /** All the notifications sent to this client (not read yet). */
    private final List<Notification> _notifs = new LinkedList<>();

    /** The delivery strategy. */
    private NotificationDeliveryStrategy _notificationDeliveryStrategy = new InAppNotificationDelivery();

    /** If this client is willing to receive notifications or not. */
    private boolean _notificationsEnabled = true;

    /**
     * 
     * @return true if the client has the notifications enabled
     */
    public boolean canReceiveNotifications() {
        return _notificationsEnabled;
    }
    
    /**
     * Enables the notifications.
     * 
     * @throws NotificationsAlreadyEnabledException if the notifications are already enabled
     */
    public void enableNotifications() throws NotificationsAlreadyEnabledException {
        if (canReceiveNotifications()) throw new NotificationsAlreadyEnabledException();
        _notificationsEnabled = true;
    }

    /**
     * Disables the notifications.
     * 
     * @throws NotificationsAlreadyDisabledException if the notifications are already disabled
     */
    public void disableNotifications() throws NotificationsAlreadyDisabledException {
        if (!canReceiveNotifications()) throw new NotificationsAlreadyDisabledException();
        _notificationsEnabled = false;
    }

    /**
     * Sends the notification to the player using its current delivery strategy.
     * 
     * @param notif the notification to send
     */
    public void sendNotification(Notification notif) {
        _notificationDeliveryStrategy.send(notif);
    }

    /**
     * Reads the notifications (this also clears them from the app).
     * 
     * @return all the notifications sent to the player in app that were not read yet
     */
    public List<Notification> readInAppNotifications() {
        List<Notification> result = List.copyOf(_notifs);
        _notifs.clear();
        return result;
    }

    /**
     * 
     * @param strategy the new delivery strategy
     */
    public void setNotificationDeliveryStrategy(NotificationDeliveryStrategy strategy) {
        _notificationDeliveryStrategy = strategy;
    }

    private class InAppNotificationDelivery implements NotificationDeliveryStrategy {
        @Override
        public void send(Notification notif) {
            _notifs.add(notif);
        }
    }

    /*
     * ---- Tariff Plans ----
     */

    private TariffPlan _tariffPlan; // setTariffPlan("base")

    /**
     * 
     * @return the tariff plan associated with this client
     */
    public TariffPlan getTariffPlan() {
        return _network.getTariffPlan(_tariffPlan + "-" + getLevel().toLowerCase());
    }

    /**
     * 
     * @param plan the name of the plan to use
     */
    public void setTariffPlan(String plan) {
        _tariffPlan = _network.getTariffPlan(plan); // if null -> throw new TariffPlanNotFoundException()
    }

    /*
     * ---- Object ----
     */

    /**
     * Format: CLIENT|key|name|taxId|type|notifications|terminals|payments|debts
     *
     * @return the string representation of this client, in the format described above
     */
    @Override
    public String toString() {
        String sep = "|";
        return "CLIENT"
                + sep + getUID()
                + sep + getName()
                + sep + getTaxID()
                + sep + getLevel()
                + sep + (canReceiveNotifications() ? "YES" : "NO")
                + sep + getTerminals().size()
                + sep + Math.round(payments())
                + sep + Math.round(debts());
	}
}
