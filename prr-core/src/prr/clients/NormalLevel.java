package prr.clients;

import prr.communications.Communication;

class NormalLevel extends ClientLevel {

	public NormalLevel(Client client) {
		super(client);
	}

	@Override
	public void onPerformPayment() {
		if (_client.balance() > 500) {
			_client.setLevel(new GoldLevel(_client));
		}
	}

	@Override
	public void onPerformCommunication(Communication comm) {
		/* do nothing */
	}

	@Override
	public String toString() {
		return "NORMAL";
	}
}
