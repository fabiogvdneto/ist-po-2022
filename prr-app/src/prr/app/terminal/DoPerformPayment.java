package prr.app.terminal;

import prr.Network;
import prr.exceptions.CommunicationAlreadyPaidException;
import prr.exceptions.CommunicationNotFoundException;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
// Add more imports if needed

/**
 * Perform payment.
 */
class DoPerformPayment extends TerminalCommand {

	DoPerformPayment(Network context, Terminal terminal) {
		super(Label.PERFORM_PAYMENT, context, terminal);
		addIntegerField("commKey", Prompt.commKey());
	}

	@Override
	protected final void execute() throws CommandException {
        Integer commKey = integerField("commKey");

		try {
			_receiver.getFromOutbox(commKey).performPayment();
		} catch (CommunicationNotFoundException | IllegalStateException | CommunicationAlreadyPaidException e) {
			_display.popup(Message.invalidCommunication());
		}
	}
}
