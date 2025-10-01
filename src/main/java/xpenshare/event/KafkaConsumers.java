package xpenshare.event;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Singleton;

@Singleton
@KafkaListener(groupId = "notification-group")
public class KafkaConsumers {

    @Topic("notification.welcome")
    public void handleWelcomeNotification(String message) {
        System.out.println("Received welcome notification: " + message);

        sendEmail(message);
        sendSMS(message);
    }

    @Topic("user.created")
    public void handleUserCreated(String message) {
        System.out.println("User created event received: " + message);

        logAnalytics(message);
        triggerWelcomeNotification(message);
    }

    @Topic("group.created")
    public void handleGroupCreated(String message) {
        System.out.println("Group created event received: " + message);

        notifyGroupMembers(message);
    }

    @Topic("expense.added")
    public void handleExpenseAdded(String message) {
        System.out.println("Expense added event received: " + message);

        updateExpenseSummary(message);
    }

    @Topic("settlement.confirmed")
    public void handleSettlementConfirmed(String message) {
        System.out.println("Settlement confirmed event received: " + message);

        sendSettlementConfirmation(message);
    }

    // ===== Placeholder methods =====
    private void sendEmail(String msg) {
        System.out.println("Sending email: " + msg);
    }

    private void sendSMS(String msg) {
        System.out.println("Sending SMS: " + msg);
    }

    private void logAnalytics(String msg) {
        System.out.println("Logging analytics: " + msg);
    }

    private void triggerWelcomeNotification(String msg) {
        System.out.println("Triggering welcome notification: " + msg);
    }

    private void notifyGroupMembers(String msg) {
        System.out.println("Notifying group members: " + msg);
    }

    private void updateExpenseSummary(String msg) {
        System.out.println("Updating expense summary: " + msg);
    }

    private void sendSettlementConfirmation(String msg) {
        System.out.println("Sending settlement confirmation: " + msg);
    }
}
