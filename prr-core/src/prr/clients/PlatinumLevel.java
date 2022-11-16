package prr.clients;

import prr.communications.Communication;

class PlatinumLevel extends ClientLevel {

	private int _textCommsCounter;

	public PlatinumLevel(Client client) {
		super(client);
	}

	@Override
	public void onPerformCommunication(Communication comm) {
        if (_client.balance() < 0) {
			_client.setLevel(new NormalLevel(_client));
			return;
		}

		if (comm.isInteractive()) {
			_textCommsCounter = 0;
			return;
		}

		if (++_textCommsCounter == 2) {
			_client.setLevel(new GoldLevel(_client));
		}
	}

	@Override
	public String toString() {
		return "PLATINUM";
	}
}
