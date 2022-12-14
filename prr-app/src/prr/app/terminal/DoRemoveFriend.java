package prr.app.terminal;

import prr.Network;
import prr.exceptions.FriendNotFoundException;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Remove friend.
 */
class DoRemoveFriend extends TerminalCommand {

	DoRemoveFriend(Network context, Terminal terminal) {
		super(Label.REMOVE_FRIEND, context, terminal);
		addStringField("key", Prompt.terminalKey());
	}

	@Override
	protected final void execute() throws CommandException {
        String key = stringField("key");

		try {
			_receiver.removeFriend(key);
		} catch (FriendNotFoundException e) {
			/* just ignore */
		}
	}
}
