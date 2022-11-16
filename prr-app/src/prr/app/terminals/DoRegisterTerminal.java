package prr.app.terminals;

import prr.Network;
import prr.app.exceptions.DuplicateTerminalKeyException;
import prr.app.exceptions.InvalidTerminalKeyException;
import prr.app.exceptions.UnknownClientKeyException;
import prr.exceptions.ClientNotFoundException;
import prr.exceptions.InvalidTerminalTypeException;
import prr.exceptions.InvalidTerminalUIDException;
import prr.exceptions.TerminalExistsException;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Register terminal.
 */
class DoRegisterTerminal extends Command<Network> {

	DoRegisterTerminal(Network receiver) {
		super(Label.REGISTER_TERMINAL, receiver);
		addStringField("termKey", Prompt.terminalKey());
		addOptionField("termType", Prompt.terminalType(), "BASIC", "FANCY");
		addStringField("clientKey", Prompt.clientKey());
	}

	@Override
	protected final void execute() throws CommandException {
		String termKey = stringField("termKey");
		String termType = optionField("termType");
		String clientKey = stringField("clientKey");

		try {
			_receiver.registerTerminal(clientKey, termKey, termType);
		} catch (TerminalExistsException e) {
			throw new DuplicateTerminalKeyException(termKey);
		} catch (ClientNotFoundException e) {
			throw new UnknownClientKeyException(clientKey);
		} catch (InvalidTerminalUIDException e) {
			throw new InvalidTerminalKeyException(termKey);
		} catch (InvalidTerminalTypeException e) {
			e.printStackTrace(); /* should not happen */
		}
	}
}
