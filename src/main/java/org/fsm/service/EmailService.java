package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendContactEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Gửi email thông báo hủy đơn hàng cho khách hàng
     */
    public void sendOrderCancellationEmail(String customerEmail, String customerName, 
                                          String orderCode, String reason) {
        String subject = "Order Cancellation Notice - " + orderCode;
        String body = String.format(
            "Dear %s,\n\n" +
            "We regret to inform you that your order %s has been cancelled.\n\n" +
            "Reason: %s\n\n" +
            "If you have already made a payment, we will process a refund within 5-7 business days.\n\n" +
            "If you have any questions or concerns, please contact our customer service.\n\n" +
            "We apologize for any inconvenience caused.\n\n" +
            "Best regards,\n" +
            "Fashion Shop Team",
            customerName != null ? customerName : "Valued Customer",
            orderCode,
            reason != null && !reason.trim().isEmpty() ? reason : "Product out of stock or technical issue"
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customerEmail);
        message.setSubject(subject);
        message.setText(body);
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw - email failure shouldn't block order cancellation
            System.err.println("Failed to send cancellation email to " + customerEmail + ": " + e.getMessage());
        }
    }
}
