package evaluation23;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

	private double currentAverage;

	private int numOfSample = 0;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {

		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " +
				msg.getSender());
		if (msg.getTemperature() < 0) {
			System.out.println("Error");
			throw new Exception("Negative temperature");
		}
		currentAverage = (currentAverage * numOfSample + msg.getTemperature()) / (numOfSample + 1);
		numOfSample++;

		System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " +
				currentAverage);
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
	}
}
