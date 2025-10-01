package xpenshare.event;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Singleton;

@Singleton
public class KafkaProducer {

    private final EventClient eventClient;

    public KafkaProducer(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    public void publish(String topic, String message) {
        eventClient.send(topic, message);
    }

    @KafkaClient
    interface EventClient {
        @Topic("") // dynamic topic set at runtime
        void send(String topic, String message);
    }
}
