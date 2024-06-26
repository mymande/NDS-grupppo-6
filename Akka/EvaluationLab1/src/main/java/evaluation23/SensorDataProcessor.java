package evaluation23;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class SensorDataProcessor {

	public static void main(String[] args) {

		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		// Number of sensors for testing
		final int NO_SENSORS = 5;

		// Number of sensor readings to generate
		final int SENSING_ROUNDS = 3;

		final ActorSystem sys = ActorSystem.create("System");

		// Create sensor actors
		List<ActorRef> sensors = new LinkedList<ActorRef>();
		for (int i = 0; i < NO_SENSORS; i++) {
			sensors.add(sys.actorOf(TemperatureSensorActor.props(), "t" + i));
		}

		// Create dispatcher
		final ActorRef dispatcher = sys.actorOf(DispatcherActor.props(), "dispatcher");

		System.out.println("Load balancing \n \n ");
		// Create processors and sends them to the dispatcher
		for (int i = 0; i < DispatcherActor.NO_PROCESSORS; i++) {
			scala.concurrent.Future<Object> waitingForProcessor = ask(dispatcher,
					Props.create(SensorProcessorActor.class), 5000);
			ActorRef processor;
			try {
				processor = (ActorRef) waitingForProcessor.result(timeout, null);
				dispatcher.tell(new ConfigAddProcessorMsg(processor), ActorRef.noSender());
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Sends the dispatcher to all sensors
		for (int i = 0; i < NO_SENSORS; i++) {
			sensors.get(i).tell(new ConfigDispatcherMessage(dispatcher), ActorRef.noSender());
		}
		// Waiting until system is ready
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some temperature data
		for (int i = 0; i < SENSING_ROUNDS; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}

		// Waiting for temperature messages to arrive
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Re-configure dispatcher to use Round Robin
		dispatcher.tell(new DispatchLogicMsg(DispatchLogicMsg.ROUND_ROBIN), ActorRef.noSender());

		System.out.println("Round Robin \n \n ");
		// Waiting for dispatcher reconfiguration
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some more temperature data
		for (int i = 0; i < SENSING_ROUNDS + 1; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}

		// A new (faulty) sensor joins the system
		ActorRef faultySensor = sys.actorOf(TemperatureSensorFaultyActor.props(), "tFaulty");
		sensors.add(0, faultySensor);

		faultySensor.tell(new ConfigDispatcherMessage(dispatcher), ActorRef.noSender());

		// Wait until system is ready again
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some more temperature data
		for (int i = 0; i < SENSING_ROUNDS + 1; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}
	}
}
