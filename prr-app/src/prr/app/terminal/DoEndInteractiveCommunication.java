package prr.app.terminal;

import prr.Network;
import prr.communications.Communication;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Command for ending communication.
 */
class DoEndInteractiveCommunication extends TerminalCommand {

	DoEndInteractiveCommunication(Network context, Terminal terminal) {
		super(Label.END_INTERACTIVE_COMMUNICATION, context, terminal, receiver -> receiver.canEndCurrentCommunication());
		addIntegerField("duration", Prompt.duration());
	}

	@Override
	protected final void execute() throws CommandException {
        int duration = integerField("duration");

		Communication comm = _receiver.getOngoingCommunication();

		if (comm == null) {
			_display.popup(Message.noOngoingCommunication());
			return;
		}

		long cost = Math.round(comm.finish(duration));
		_display.popup(Message.communicationCost(cost));
	}
}
