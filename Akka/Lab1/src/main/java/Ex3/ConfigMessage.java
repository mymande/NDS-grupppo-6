package Ex3;

import akka.actor.ActorRef;

public class ConfigMessage {

    public ActorRef ref;

    public ConfigMessage(ActorRef ref) {
        this.ref = ref;
    }

}
