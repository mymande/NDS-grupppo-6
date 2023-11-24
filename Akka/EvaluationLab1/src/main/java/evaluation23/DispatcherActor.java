package evaluation23;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class DispatcherActor extends AbstractActorWithStash {

	// #strategy
	private static SupervisorStrategy strategy = new OneForOneStrategy(
			1, // Max no of retries
			Duration.ofMinutes(1), // Within what time period
			DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
					.build());

	public final static int NO_PROCESSORS = 3;

	private ArrayList<ActorRef> processors = new ArrayList<ActorRef>();
	private Map<Integer, ActorRef> sensorToProcessor = new HashMap<Integer, ActorRef>();

	private int RRIndex = 0;

	public DispatcherActor() {
	}

	@Override
	public AbstractActor.Receive createReceive() {
		return loadBalancer();
	}

	private Receive loadBalancer() {
		return receiveBuilder().match(ConfigAddProcessorMsg.class, this::onAddProcessor)
				.match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.match(DispatchLogicMsg.class, this::changeDispatchLogic)
				.match(
						Props.class,
						props -> {
							getSender().tell(getContext().actorOf(props), getSelf());
						})
				.build();
	}

	private Receive roundRobin() {
		return receiveBuilder().match(ConfigAddProcessorMsg.class, this::onAddProcessor)
				.match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.match(DispatchLogicMsg.class, this::changeDispatchLogic)
				.match(
						Props.class,
						props -> {
							getSender().tell(getContext().actorOf(props), getSelf());
						})
				.build();
	}

	private void changeDispatchLogic(DispatchLogicMsg msg) {
		if (msg.getLogic() == DispatchLogicMsg.ROUND_ROBIN) {
			getContext().become(roundRobin());
		} else if (msg.getLogic() == DispatchLogicMsg.LOAD_BALANCER) {
			getContext().become(loadBalancer());
		}
	}

	private void onAddProcessor(ConfigAddProcessorMsg msg) {
		processors.add(msg.getProcessorReference());
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg) {

		if (sensorToProcessor.get(msg.getSender().hashCode()) == null) {
			sensorToProcessor.put(msg.getSender().hashCode(), processors.get(RRIndex));
			RRIndex++;
			if (RRIndex >= processors.size()) {
				RRIndex = 0;
			}
		}

		sensorToProcessor.get(msg.getSender().hashCode()).tell(msg, self());

	}

	private void dispatchDataRoundRobin(TemperatureMsg msg) {
		System.out.println("Dispatchig to Processor index " + RRIndex + " " + processors.get(RRIndex).toString());
		processors.get(RRIndex).tell(msg, self());
		RRIndex++;
		if (RRIndex >= processors.size()) {
			RRIndex = 0;
		}
	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

}
