package Ex4;

import java.io.IOException;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class main {

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef server = sys.actorOf(Server.props(), "server");

		server.tell(new SimpleMessage(1), ActorRef.noSender());
		server.tell(new Sleep(), ActorRef.noSender());
		server.tell(new SimpleMessage(2), ActorRef.noSender());
		server.tell(new WakeUp(), ActorRef.noSender());
		server.tell(new SimpleMessage(3), ActorRef.noSender());

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();

	}

}