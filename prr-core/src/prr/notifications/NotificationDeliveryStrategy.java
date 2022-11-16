package prr.notifications;

import java.io.Serializable;

public interface NotificationDeliveryStrategy extends Serializable {
    void send(Notification notif);
}
