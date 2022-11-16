package prr.clients;

import prr.communications.Communication;

class GoldLevel extends ClientLevel {

	private int _videoCommsCounter;

	public GoldLevel(Client client) {
		super(client);
	}
	
	@Override
	public void onPerformCommunication(Communication comm) {
        if (_client.balance() < 0) {
			_client.setLevel(new NormalLevel(_client));
			return;
		}

		if (!comm.getType().equals("VIDEO")) {
			_videoCommsCounter = 0;
			return;
		}

		if (++_videoCommsCounter == 5) {
			_client.setLevel(new PlatinumLevel(_client));
		}
	}

	@Override
	public String toString() {
		return "GOLD";
	}
}
