package prr.app.terminal;

import prr.Network;
import prr.app.exceptions.UnknownTerminalKeyException;
import prr.exceptions.AlreadyFriendsException;
import prr.exceptions.TerminalNotFoundException;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Add a friend.
 */
class DoAddFriend extends TerminalCommand {

	DoAddFriend(Network context, Terminal terminal) {
		super(Label.ADD_FRIEND, context, terminal);
		addStringField("key", Prompt.terminalKey());
	}

	@Override
	protected final void execute() throws CommandException {
		String key = stringField("key");

		try {
			_receiver.addFriend(key);
		} catch (TerminalNotFoundException e) {
			throw new UnknownTerminalKeyException(key);
		} catch (AlreadyFriendsException e) { /* just ignore */ }
	}
}
