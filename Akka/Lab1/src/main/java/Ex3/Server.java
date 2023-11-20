package Ex3;

import java.util.HashMap;

import Ex3.Get;
import Ex3.Put;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class Server extends AbstractActorWithStash {

    private HashMap<String, String> book = new HashMap<String, String>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(Put.class, this::put)
                .match(Get.class, this::get).build();
    }

    void put(Put msg) {
        book.put(msg.email, msg.name);
    }

    void get(Get msg) {
        sender().tell(new Get(book.get(msg.email)), self());
    }

    static Props props() {
        return Props.create(Server.class);
    }
}
