package prr.app.clients;

import prr.Network;
import prr.app.exceptions.UnknownClientKeyException;
import prr.clients.Client;
import prr.exceptions.ClientNotFoundException;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Show the payments and debts of a client.
 */
class DoShowClientPaymentsAndDebts extends Command<Network> {

	DoShowClientPaymentsAndDebts(Network receiver) {
		super(Label.SHOW_CLIENT_BALANCE, receiver);
		addStringField("key", Prompt.key());
	}

	@Override
	protected final void execute() throws CommandException {
		String key = stringField("key");
		
		try {
			Client client = _receiver.getClient(key);
			long payments = Math.round(client.payments());
			long debts = Math.round(client.debts());

			_display.popup(Message.clientPaymentsAndDebts(key, payments, debts));
		} catch (ClientNotFoundException e) {
			throw new UnknownClientKeyException(key);
		}
	}
}
