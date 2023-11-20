package Ex3;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import scala.concurrent.Await;

public class main {

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef client = sys.actorOf(Client.props(), "client");
		final ActorRef server = sys.actorOf(Server.props(), "server");

		// Send messages from multiple threads in parallel
		// final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		// with different messages
		// for (int i = 0; i < numMessages; i++) {
		// exec.submit(() -> counter.tell(new OtherMessage(), ActorRef.noSender()));
		// exec.submit(() -> counter.tell(new SimpleMessage(true),
		// ActorRef.noSender()));
		// }

		client.tell(new ConfigMessage(server), ActorRef.noSender());

		client.tell(new Put("email", "ciao"), ActorRef.noSender());
		client.tell(new Get("email"), ActorRef.noSender());

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();

	}

}