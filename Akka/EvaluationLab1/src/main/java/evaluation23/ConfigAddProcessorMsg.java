package evaluation23;

import akka.actor.ActorRef;

public class ConfigAddProcessorMsg {

    private ActorRef ref;

    public ConfigAddProcessorMsg(ActorRef ref) {
        this.ref = ref;
    }

    public ActorRef getProcessorReference() {
        return this.ref;
    }

}
