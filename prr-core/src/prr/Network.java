package prr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import prr.clients.Client;
import prr.communications.Communication;
import prr.communications.InteractiveCommunication;
import prr.communications.TextCommunication;
import prr.communications.VideoCommunication;
import prr.communications.VoiceCommunication;
import prr.exceptions.AlreadyFriendsException;
import prr.exceptions.ClientExistsException;
import prr.exceptions.ClientNotFoundException;
import prr.exceptions.CommunicationNotFoundException;
import prr.exceptions.CommunicationTypeUnsupportedAtDestinationException;
import prr.exceptions.CommunicationTypeUnsupportedAtOriginException;
import prr.exceptions.DestinationIsBusyException;
import prr.exceptions.DestinationIsOffException;
import prr.exceptions.DestinationIsSilentException;
import prr.exceptions.InvalidCommunicationTypeException;
import prr.exceptions.InvalidTerminalTypeException;
import prr.exceptions.InvalidTerminalUIDException;
import prr.exceptions.NotificationsAlreadyDisabledException;
import prr.exceptions.NotificationsAlreadyEnabledException;
import prr.exceptions.OriginIsBusyException;
import prr.exceptions.OriginIsOffException;
import prr.exceptions.TerminalExistsException;
import prr.exceptions.TerminalNotFoundException;
import prr.exceptions.UnrecognizedEntryException;
import prr.tariffs.TariffPlan;
import prr.terminals.BasicTerminal;
import prr.terminals.FancyTerminal;
import prr.terminals.Terminal;

/**
 * Class Store implements a store.
 */
public class Network implements Serializable {

	/** Serial number for serialization. */
	@Serial private static final long serialVersionUID = 202208091753L;

	/*
	 * ---- Clients Management ----
	 */

	/** A Map for registering all the clients by id. */
	private final Map<String, Client> _clients = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 *
	 * @return all the registered clients
	 */
	public Collection<Client> getClients() {
		return Collections.unmodifiableCollection(_clients.values());
	}

	/**
	 * 
	 * @return all the registered clients with debts
	 */
	public Collection<Client> getClientsWithDebts() {
		return _clients.values().stream()
					.filter(c -> (c.debts() > 0))
					.sorted(Comparator.comparing(Client::debts).reversed())
					.toList();
	}

	/**
	 * 
	 * @return all the registered clients without debts
	 */
	public Collection<Client> getClientsWithoutDebts() {
		return _clients.values().stream().filter(c -> (c.debts() == 0)).toList();
	}

	/**
	 * 
	 * @param id the id of the client.
	 * @return the respective client.
	 * @throws ClientNotFoundException if the client was not found
	 */
	public Client getClient(String id) throws ClientNotFoundException {
		Client client = _clients.get(id);
		if (client == null) throw new ClientNotFoundException();
		return client;
	}

	/**
	 *
	 * @param id the id of the client.
	 * @param name the name of the client.
	 * @param taxID the tax id of the client.
	 * @return the registered client.
	 * @throws ClientExistsException if the id is being used by another client
	 */
	public Client registerClient(String id, String name, int taxID) throws ClientExistsException {
		return registerClient(new Client(this, id, name, taxID));
	}

	/**
	 *
	 * @param client the client to register.
	 * @throws ClientExistsException if there is a client with the same id.
	 */
	private Client registerClient(Client client) throws ClientExistsException {
		if (_clients.putIfAbsent(client.getUID(), client) != null)
			throw new ClientExistsException();
		setChanged(true);
		return client;
	}

	/**
	 * 
	 * @param id the id of the client
	 * @throws ClientNotFoundException if the client was not found
	 * @throws NotificationsAlreadyDisabledException if the notifications are already disabled
	 */
	public void disableClientNotifications(String id)
			throws ClientNotFoundException, NotificationsAlreadyDisabledException {
		getClient(id).disableNotifications();
		setChanged(true);
	}

	/**
	 * 
	 * @param id the id of the client
 	 * @throws ClientNotFoundException if the client was not found
	 * @throws NotificationsAlreadyEnabledException if the notifications are already enabled
	 */
	public void enableClientNotifications(String id)
			throws ClientNotFoundException, NotificationsAlreadyEnabledException {
		getClient(id).enableNotifications();
		setChanged(true);
	}

	/*
	 * ---- Terminals Management ----
	 */

	/** A Map for registering all the terminals by id. */
	private final Map<String, Terminal> _terminals = new TreeMap<>();

	/**
	 *
	 * @return all the registered terminals.
	 */
	public Collection<Terminal> getTerminals() {
		return Collections.unmodifiableCollection(_terminals.values());
	}

	/**
	 * 
	 * @return all the registered terminals not used yet (without communications)
	 */
	public Collection<Terminal> getUnusedTerminals() {
		return _terminals.values().stream().filter(Terminal::isUnused).toList();
	}

	/**
	 * 
	 * @return all the registered terminals with positive balance
	 */
	public Collection<Terminal> getTerminalsWithPositiveBalance() {
		return _terminals.values().stream().filter(term -> term.balance() > 0).toList();
	}

	/**
	 *
	 * @param id the id of the terminal.
	 * @return the corresponding terminal.
	 */
	public Terminal getTerminal(String id) throws TerminalNotFoundException {
		Terminal term = _terminals.get(id);
		if (term == null) throw new TerminalNotFoundException();
		return term;
	}

	/**
	 *
	 * @param clientID the id of the client that owns the terminal.
	 * @param termID the id of the terminal.
	 * @param type the type of the terminal.
	 * @return the registered terminal.
	 * @throws ClientNotFoundException if there is no client with the given id.
	 * @throws TerminalExistsException if there is a terminal with the given id already.
	 * @throws InvalidTerminalTypeException if the terminal type is not valid.
	 */
	public Terminal registerTerminal(String clientID, String termID, String type)
			throws ClientNotFoundException, TerminalExistsException,
				InvalidTerminalTypeException, InvalidTerminalUIDException {
		Client client = getClient(clientID);

		Terminal term = switch (type) {
			case "BASIC" -> new BasicTerminal(this, client, termID);
			case "FANCY" -> new FancyTerminal(this, client, termID);
			default -> throw new InvalidTerminalTypeException();
		};

		if (_terminals.putIfAbsent(termID, term) != null)
			throw new TerminalExistsException();

		client.onRegisterTerminal(termID);
		setChanged(true);
		return term;
	}

	/*
	 * ---- Communications Management ----
	 */

	/** A List for registering all the communications. */
	private List<Communication> _comms = new ArrayList<>();

	/**
	 * 
	 * @return all the registered communications
	 */
	public Collection<Communication> getCommunications() {
		return Collections.unmodifiableCollection(_comms);
	}

	/**
	 * 
	 * @param id the id of the communication
	 * @return the corresponding communication
	 * @throws CommunicationNotFoundException if the communication was not found
	 */
	public Communication getCommunication(int id) throws CommunicationNotFoundException {
		Communication comm = _comms.get(id - 1);
		if (comm == null) throw new CommunicationNotFoundException();
		return comm;
	}

	/**
	 * 
	 * @param id the id of the client
	 * @return all the communications started by the client
	 * @throws ClientNotFoundException if the client was not found
	 */
	public Collection<Communication> getCommunicationsFromClient(String id) throws ClientNotFoundException {
		return getClient(id).getOutbox();
	}

	/**
	 * 
	 * @param id the id of the client
	 * @return all the communications received by the client
	 * @throws ClientNotFoundException if the client was not found
	 */
	public Collection<Communication> getCommunicationsToClient(String id) throws ClientNotFoundException {
		return getClient(id).getInbox();
	}

	/**
	 * 
	 * @param originID the id of the origin terminal
	 * @param destinID the id of the destination terminal
	 * @param msg the message to be sent
	 * @return the text communication sent
	 * @throws TerminalNotFoundException if a terminal was not found
	 * @throws DestinationIsOffException if the destination is off
	 * @throws OriginIsOffException if the origin is off
	 * @throws OriginIsBusyException if the origin is busy
	 */
	public TextCommunication sendTextCommunication(String originID, String destinID, String msg)
			throws TerminalNotFoundException, DestinationIsOffException,
            	OriginIsOffException, OriginIsBusyException {
		Terminal origin = getTerminal(originID);
		Terminal destin = getTerminal(destinID);
		
		if (origin.isOff()) throw new OriginIsOffException();
		if (origin.isBusy()) throw new OriginIsBusyException();
		
		if (destin.isOff()) {
			destin.onTextCommunicationAttempt(origin.getOwner());
			throw new DestinationIsOffException();
		}
					
		int id = _comms.size() + 1;
		TextCommunication comm = new TextCommunication(id, origin, destin, msg);

		_comms.add(comm);
			
		destin.onTextCommunication(id);
		origin.onTextCommunication(id);
		destin.getOwner().onTextCommunication(id);
		origin.getOwner().onTextCommunication(id);

		setChanged(true);
		return comm;
	}

	/**
	 * 
	 * @param originID the id of the origin terminal
	 * @param destinID the id of the destination terminal
	 * @param type the type of the interactive communication (may be VOICE or VIDEO)
	 * @return the interactive communication started
	 * @throws TerminalNotFoundException if a terminal was not found
	 * @throws InvalidCommunicationTypeException if the type specified is not valid
	 * @throws CommunicationTypeUnsupportedAtOriginException if the communication type is not supported at the origin
	 * @throws CommunicationTypeUnsupportedAtDestinationException if the communication type is not supported at the destination
	 * @throws DestinationIsOffException if the destination is off
	 * @throws DestinationIsBusyException if the destination is busy
	 * @throws DestinationIsSilentException if the destination is silent
	 * @throws OriginIsOffException if the origin is off
	 * @throws OriginIsBusyException if the origin is busy
	 */
	public InteractiveCommunication startInteractiveCommunication(String originID, String destinID, String type)
			throws TerminalNotFoundException, InvalidCommunicationTypeException,
				CommunicationTypeUnsupportedAtOriginException,
				CommunicationTypeUnsupportedAtDestinationException,
				DestinationIsOffException, DestinationIsBusyException, DestinationIsSilentException,
				OriginIsOffException, OriginIsBusyException {
		Terminal origin = getTerminal(originID);
		Terminal destin = getTerminal(destinID);
        
        if (!origin.isCommunicationTypeSupported(type))
            throw new CommunicationTypeUnsupportedAtOriginException();
            
        if (!destin.isCommunicationTypeSupported(type))
            throw new CommunicationTypeUnsupportedAtDestinationException();

		if (origin == destin) throw new DestinationIsBusyException();
		if (origin.isOff()) throw new OriginIsOffException();
		if (origin.isBusy()) throw new OriginIsBusyException();

		if (!destin.isIdle()) {
			destin.onInteractiveCommunicationAttempt(origin.getOwner());
			
			if (destin.isOff()) throw new DestinationIsOffException();
			if (destin.isBusy()) throw new DestinationIsBusyException();
			if (destin.isSilent()) throw new DestinationIsSilentException();
		}

        int id = _comms.size() + 1;
        InteractiveCommunication comm = switch (type) {
            case "VIDEO" -> new VideoCommunication(id, origin, destin);
            case "VOICE" -> new VoiceCommunication(id, origin, destin);
            default -> throw new InvalidCommunicationTypeException();
        };

        _comms.add(comm);

		destin.onStartInteractiveCommunication(id);
		origin.onStartInteractiveCommunication(id);
		destin.getOwner().onStartInteractiveCommunication(id);
		origin.getOwner().onStartInteractiveCommunication(id);

		setChanged(true);
		return comm;
	}

	/*
	 * ---- Credits Management ----
	 */

	/**
	 *
	 * @return the global balance (global payments - global debts).
	 */
	public double balance() {
		return getClients().stream().mapToDouble(Client::balance).sum();
	}

	/**
	 *
	 * @return the global payments.
	 */
	public double payments() {
		return getClients().stream().mapToDouble(Client::payments).sum();
	}

	/**
	 *
	 * @return the global debts.
	 */
	public double debts() {
		return getClients().stream().mapToDouble(Client::debts).sum();
	}

	/*
	 * ---- Plans Management ----
	 */

	/** The plans supported by the network. */
	private final Map<String, TariffPlan> _plans = new HashMap<>();

	{
		_plans.put("base", new BasePlan());
	}

	/**
	 * 
	 * @param id the id of the plan
	 * @return the corresponding plan
	 */
	public TariffPlan getTariffPlan(String id) {
		return _plans.get(id);
	}

	// addTariffPlan(String id, TariffPlan plan)
	//   if (_plans.putIfAbsent(id, plan) != null) -> throw new TariffPlanExistsException()

	// removeTariffPlan(String id)
	//   if (!_plans.containsKey(id)) -> throw new TariffPlanNotFoundException()
	//   foreach client:
	//     if (client.getTariffPlan().equals(id)) -> throw new TariffPlanInUseException()
	//   _plans.remove(id)

	private static class BasePlan implements TariffPlan {
		@Override
		public double taxTextCommunication(TextCommunication c) {
			return switch (c.getOrigin().getOwner().getLevel()) {
				case "PLATINUM" -> taxPlatTextComm(c);
				case "GOLD"     -> taxGoldTextComm(c);
				default         -> taxNormTextComm(c);
			};
		}

		private double taxNormTextComm(TextCommunication c) {
			int chars = c.getUnits();

			if (chars < 50) return 10;
			if (chars < 100) return 16;
			return (chars * 2);
		}

		private double taxGoldTextComm(TextCommunication c) {
			int chars = c.getUnits();
			return (chars < 100) ? 10 : (2 * chars);
		}

		private double taxPlatTextComm(TextCommunication c) {
			int chars = c.getUnits();
			return (chars < 50) ? 0 : 4;
		}

		@Override
		public double taxVoiceCommunication(VoiceCommunication c) {
			return switch (c.getOrigin().getOwner().getLevel()) {
				case "PLATINUM" -> taxPlatVoiceComm(c);
				case "GOLD"     -> taxGoldVoiceComm(c);
				default         -> taxNormVoiceComm(c);
			};
		}

		private double taxNormVoiceComm(VoiceCommunication c) {
			return c.getUnits() * (c.isFriendly() ? 10 : 20);
		}

		private double taxGoldVoiceComm(VoiceCommunication c) {
			return c.getUnits() * (c.isFriendly() ? 5 : 10);
		}

		private double taxPlatVoiceComm(VoiceCommunication c) {
			return c.getUnits() * (c.isFriendly() ? 10 : 20);
		}

		@Override
		public double taxVideoCommunication(VideoCommunication c) {
			return switch (c.getOrigin().getOwner().getLevel()) {
				case "PLATINUM" -> taxPlatVideoComm(c);
				case "GOLD"     -> taxGoldVideoComm(c);
				default         -> taxNormVideoComm(c);
			};
		}

		private double taxNormVideoComm(VideoCommunication c) {
			return c.getUnits() * (c.isFriendly() ? 15 : 30);
		}
		
		private double taxGoldVideoComm(VideoCommunication c) {
			return c.getUnits() * (c.isFriendly() ? 5 : 10);
		}

		private double taxPlatVideoComm(VideoCommunication comm) {
			return taxGoldVideoComm(comm);
		}
	}

	/*
	 * ---- IO ----
	 */

	/** Indicates if the network has changed since last save. */
	private boolean _changed;

	/**
	 *
	 * @return true if the network has changed since last save, false otherwise
	 */
	public boolean isChanged() {
		return _changed;
	}

	/**
	 *
	 * @param changed marks this object as changed/not changed
	 */
	void setChanged(boolean changed) {
		_changed = changed;
	}

	/**
	 * Read text input file and create corresponding domain entities.
	 *
	 * @param filename name of the text input file
	 * @throws UnrecognizedEntryException if some entry is not correct
	 * @throws IOException if there is an IO erro while processing the text file
	 */
	void importFile(String filename) throws UnrecognizedEntryException, IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = reader.readLine()) != null)
				registerEntry(line);
		}
		setChanged(true);
	}

	private void registerEntry(String entry) throws UnrecognizedEntryException {
		String[] fields = entry.split("\\|");

		try {
			switch (fields[0]) {
				case "CLIENT" -> registerClient(fields);
				case "BASIC", "FANCY" -> registerTerminal(fields);
				case "FRIENDS" -> registerFriends(fields);
				default -> throw new UnrecognizedEntryException(entry);
			}
		} catch (ClientExistsException | ClientNotFoundException
				| TerminalExistsException | TerminalNotFoundException
				| InvalidTerminalTypeException | InvalidTerminalUIDException
				| AlreadyFriendsException e) {
			throw new UnrecognizedEntryException(entry, e); // should not happen
		}
	}

	/**
	 * Format: CLIENT|id|nome|taxId
	 *
	 * @param fields
	 */
	private void registerClient(String... fields) throws ClientExistsException {	
		registerClient(fields[1], fields[2], Integer.parseInt(fields[3]));
	}

	/**
	 * Format: terminal-type|idTerminal|idClient|state
	 *
	 * @param fields
	 */
	private void registerTerminal(String... fields)
			throws ClientNotFoundException, TerminalExistsException,
				InvalidTerminalTypeException, InvalidTerminalUIDException {
		Terminal term = registerTerminal(fields[2], fields[1], fields[0]);

		switch (fields[3]) {
			case "ON" -> term.turnOn(); /* redundant as the terminal is on by default */
			case "SILENCE" -> term.silence();
			case "OFF" -> term.turnOff();
		}
	}

	/**
	 * Format: FRIENDS|idTerminal|idTerminal1,...,idTerminalN
	 *
	 * @param fields
	 */
	private void registerFriends(String... fields)
			throws TerminalNotFoundException, AlreadyFriendsException {
		Terminal term = getTerminal(fields[1]);

		fields = fields[2].split(",");

		for (int i = 0; i < fields.length; i++)
			term.addFriend(fields[i]);
	}
}
