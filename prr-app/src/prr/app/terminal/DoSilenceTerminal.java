package prr.app.terminal;

import prr.Network;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Silence the terminal.
 */
class DoSilenceTerminal extends TerminalCommand {

	DoSilenceTerminal(Network context, Terminal terminal) {
		super(Label.MUTE_TERMINAL, context, terminal);
	}

	@Override
	protected final void execute() throws CommandException {
		if (_receiver.isSilent()) {
			_display.popup(Message.alreadySilent());
			return;
		}

		try {
			_receiver.silence();
		} catch (IllegalStateException e) { /* just ignore */ }
	}
}
