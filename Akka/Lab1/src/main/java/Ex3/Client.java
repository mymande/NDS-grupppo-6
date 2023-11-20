package Ex3;

import java.util.concurrent.TimeoutException;
import static java.util.concurrent.TimeUnit.SECONDS;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;

public class Client extends AbstractActor {

	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
	private ActorRef server = null;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ConfigMessage.class, this::config).build();
	}

	private final Receive active() {
		return receiveBuilder().match(Get.class, this::get)
				.match(Put.class, this::put).build();
	}

	void config(ConfigMessage msg) {
		server = msg.ref;
		getContext().become(active());
	}

	void get(Get msg) {
		scala.concurrent.Future<Object> waitingForReply = Patterns.ask(server, msg, 5000);
		try {
			Get reply = (Get) waitingForReply.result(timeout, null);
			if (reply.email != null) {
				System.out.println("CLIENT: Received reply, email is " + reply.email);
			} else {
				System.out.println("CLIENT: Received reply, no email found!");
			}
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	void put(Put msg) {
		server.tell(msg, self());
	}

	static Props props() {
		return Props.create(Client.class);
	}
}