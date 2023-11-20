package Ex2;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
				.match(OtherMessage.class, this::onOtherMessage).build();
	}

	void onMessage(SimpleMessage msg) {
		if (msg.increase) {
			++counter;
			unstashAll();
			System.out.println("Counter increased to " + counter);
		} else {
			if (counter <= 0) {
				stash();
			} else {
				--counter;
				System.out.println("Counter decreased to " + counter);
			}
		}
	}

	void onOtherMessage(OtherMessage msg) {
		if (counter <= 0) {
			stash();
		} else {
			--counter;
			System.out.println("Counter decreased to " + counter);
		}
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}