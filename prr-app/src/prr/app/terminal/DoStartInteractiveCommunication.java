package prr.app.terminal;

import prr.Network;
import prr.app.exceptions.UnknownTerminalKeyException;
import prr.exceptions.CommunicationTypeUnsupportedAtDestinationException;
import prr.exceptions.CommunicationTypeUnsupportedAtOriginException;
import prr.exceptions.DestinationIsBusyException;
import prr.exceptions.DestinationIsOffException;
import prr.exceptions.DestinationIsSilentException;
import prr.exceptions.InvalidCommunicationTypeException;
import prr.exceptions.OriginIsBusyException;
import prr.exceptions.OriginIsOffException;
import prr.exceptions.TerminalNotFoundException;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Command for starting communication.
 */
class DoStartInteractiveCommunication extends TerminalCommand {

	DoStartInteractiveCommunication(Network context, Terminal terminal) {
		super(Label.START_INTERACTIVE_COMMUNICATION, context, terminal, receiver -> receiver.canStartCommunication());
		addStringField("destinKey", Prompt.terminalKey());
		addOptionField("commType", Prompt.commType(), "VOICE", "VIDEO");
	}

	@Override
	protected final void execute() throws CommandException {
		String destinKey = stringField("destinKey");
		String commType = optionField("commType");

		try {
			_network.startInteractiveCommunication(_receiver.getUID(), destinKey, commType);
		} catch (TerminalNotFoundException e) {
			throw new UnknownTerminalKeyException(destinKey);
		} catch (DestinationIsOffException e) {
			_display.popup(Message.destinationIsOff(destinKey));
		} catch (DestinationIsBusyException e) {
			_display.popup(Message.destinationIsBusy(destinKey));
		} catch (DestinationIsSilentException e) {
			_display.popup(Message.destinationIsSilent(destinKey));
		} catch (CommunicationTypeUnsupportedAtOriginException e) {
			_display.popup(Message.unsupportedAtOrigin(_receiver.getUID(), commType));
		} catch (CommunicationTypeUnsupportedAtDestinationException e) {
			_display.popup(Message.unsupportedAtDestination(destinKey, commType));
		} catch (InvalidCommunicationTypeException | OriginIsOffException | OriginIsBusyException e) {
			e.printStackTrace(); // should not happen
		}
	}
}
