package prr.app.main;

import prr.NetworkManager;
import prr.app.exceptions.FileOpenFailedException;
import prr.exceptions.MissingFileAssociationException;
import pt.tecnico.uilib.forms.Form;
import pt.tecnico.uilib.menus.Command;
import pt.tecnico.uilib.menus.CommandException;

import java.io.IOException;
//FIXME add more imports if needed

/**
 * Command to save a file.
 */
class DoSaveFile extends Command<NetworkManager> {

	DoSaveFile(NetworkManager receiver) {
		super(Label.SAVE_FILE, receiver);
	}

	@Override
	protected final void execute() throws CommandException {
		try {
			try {
				_receiver.save();
			} catch (MissingFileAssociationException e) {
				_receiver.saveAs(Form.requestString(Prompt.newSaveAs()));
			}
		} catch (MissingFileAssociationException | IOException e) {
			throw new FileOpenFailedException(e);
		}
	}
}
