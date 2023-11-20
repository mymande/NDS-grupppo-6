package Ex4;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class Server extends AbstractActorWithStash {
	private boolean active = true;

	public Server() {
	}

	@Override
	public Receive createReceive() {
		return active();
	}

	public Receive sleep() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
				.match(WakeUp.class, this::wakeUp).build();
	}

	public Receive active() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
				.match(Sleep.class, this::sleep).build();
	}

	void onMessage(SimpleMessage msg) {
		if (active) {
			sender().tell(msg, self());
			System.out.println("Server: received message: " + msg.id);
		} else {
			stash();
			System.out.println("Server: stashed message: " + msg.id);
		}
	}

	void sleep(Sleep msg) {
		active = false;
		getContext().become(sleep());
		System.out.println("Server: sleep");
	}

	void wakeUp(WakeUp msg) {
		active = true;

		getContext().become(active());
		unstashAll();
		System.out.println("Server: Wakeup");
	}

	static Props props() {
		return Props.create(Server.class);
	}
}
