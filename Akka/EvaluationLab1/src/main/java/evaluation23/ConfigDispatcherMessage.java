package evaluation23;

import akka.actor.ActorRef;

public class ConfigDispatcherMessage {
    private ActorRef server;

    public ConfigDispatcherMessage(ActorRef server) {
        this.server = server;
    }

    public ActorRef getServerReference() {
        return this.server;
    }
}