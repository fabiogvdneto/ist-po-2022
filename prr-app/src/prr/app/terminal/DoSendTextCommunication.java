package prr.app.terminal;

import prr.Network;
import prr.app.exceptions.UnknownTerminalKeyException;
import prr.exceptions.DestinationIsOffException;
import prr.exceptions.OriginIsBusyException;
import prr.exceptions.OriginIsOffException;
import prr.exceptions.TerminalNotFoundException;
import prr.terminals.Terminal;
import pt.tecnico.uilib.menus.CommandException;
//FIXME add more imports if needed

/**
 * Command for sending a text communication.
 */
class DoSendTextCommunication extends TerminalCommand {

        DoSendTextCommunication(Network context, Terminal terminal) {
                super(Label.SEND_TEXT_COMMUNICATION, context, terminal, receiver -> receiver.canStartCommunication());
                addStringField("destinKey", Prompt.terminalKey());
                addStringField("msg", Prompt.textMessage());
        }

        @Override
        protected final void execute() throws CommandException {
                String destinKey = stringField("destinKey");
                String msg = stringField("msg");

                try {
                        _network.sendTextCommunication(_receiver.getUID(), destinKey, msg);
                } catch (TerminalNotFoundException e) {
                        throw new UnknownTerminalKeyException(destinKey);
                } catch (DestinationIsOffException e) {
                        _display.popup(Message.destinationIsOff(destinKey));
                } catch (OriginIsOffException | OriginIsBusyException e) {
                        e.printStackTrace(); /* should not happen */
                }
        }
} 
