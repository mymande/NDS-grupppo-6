package evaluation23;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class TemperatureSensorFaultyActor extends TemperatureSensorActor {
	// Faulty sensor to debug
	private ActorRef dispatcher;
	private final static int FAULT_TEMP = -50;

	@Override
	public AbstractActor.Receive createReceive() {
		return receiveBuilder().match(GenerateMsg.class, this::onGenerate)
				.match(ConfigDispatcherMessage.class, this::onConfig)
				.build();
	}

	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR " + self() + ": Sensing temperature!");
		dispatcher.tell(new TemperatureMsg(FAULT_TEMP, self()), self());
	}

	private void onConfig(ConfigDispatcherMessage msg) {
		dispatcher = msg.getServerReference();
	}

	static Props props() {
		return Props.create(TemperatureSensorFaultyActor.class);
	}

}
