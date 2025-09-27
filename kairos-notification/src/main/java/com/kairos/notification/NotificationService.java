package com.kairos.notification;

/**
 * A generic interface for sending notifications.
 * Implementations could be for email, SMS, or in-app push notifications.
 */
public interface NotificationService {

    /**
     * Sends a notification.
     * @param recipient The identifier for the recipient (e.g., email address, phone number).
     * @param subject The subject or title of the notification.
     * @param body The main content of the notification.
     */
    void send(String recipient, String subject, String body);
}