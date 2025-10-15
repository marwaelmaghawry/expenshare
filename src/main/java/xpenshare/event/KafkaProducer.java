package xpenshare.event;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;


@Singleton
public class KafkaProducer {

    @Inject
    private final EventClient eventClient;

    public KafkaProducer(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    public void publishExpenseAdded(String message) {
        if (eventClient != null) {
            eventClient.sendExpense(message);
        }
    }

    public void publishUserCreated(String message) {
        if (eventClient != null) {
            eventClient.sendUser(message);
        }
    }

    public void publishGroupCreated(String message) {
        if (eventClient != null) {
            eventClient.sendGroup(message);
        }
    }

    public void publishNotificationWelcome(String message) {
        if (eventClient != null) {
            eventClient.sendNotification(message);
        }
    }

    public void publishSettlementConfirmed(String message) {
        if (eventClient != null) {
            eventClient.sendSettlementConfirmed(message);
        }
    }

    public void publishBalanceReminder(String message) {
        if (eventClient != null) {
            eventClient.sendBalanceReminder(message);
        }
    }


    @KafkaClient
    interface EventClient {
        @Topic("expense.added")
        void sendExpense(String message);

        @Topic("user.created")
        void sendUser(String message);

        @Topic("group.created")
        void sendGroup(String message);

        @Topic("notification.welcome")
        void sendNotification(String message);

        @Topic("settlement.confirmed")
        void sendSettlementConfirmed(String message);

        @Topic("balance.reminder")
        void sendBalanceReminder(String message);

    }


}
