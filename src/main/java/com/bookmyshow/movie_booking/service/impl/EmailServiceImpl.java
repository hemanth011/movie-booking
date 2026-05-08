package com.bookmyshow.movie_booking.service.impl;

import com.bookmyshow.movie_booking.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Sends booking confirmation email with QR code embedded.
     * Runs asynchronously so it doesn't block the booking flow.
     */
    @Async
    @Override
    public void sendBookingConfirmation(String toEmail, String userName,
                                        String movieTitle, String theatreName,
                                        String screenName, String showDate,
                                        String showTime, String seats,
                                        String referenceId, Double totalAmount,
                                        String qrCodeBase64) {
        log.info("Sending booking confirmation email to: {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎬 Booking Confirmed — " + referenceId);

            // HTML email body
            String html = buildEmailHtml(userName, movieTitle, theatreName,
                    screenName, showDate, showTime, seats,
                    referenceId, totalAmount, qrCodeBase64);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Builds the HTML email body for booking confirmation.
     */
    private String buildEmailHtml(String userName, String movieTitle,
                                  String theatreName, String screenName,
                                  String showDate, String showTime,
                                  String seats, String referenceId,
                                  Double totalAmount, String qrCodeBase64) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 30px auto; background: #1A1A2E; border-radius: 12px; overflow: hidden; }
                .header { background: #F84464; padding: 24px; text-align: center; }
                .header h1 { color: white; margin: 0; font-size: 22px; }
                .body { padding: 30px; color: #ffffff; }
                .greeting { font-size: 16px; margin-bottom: 20px; }
                .ticket { background: #16213E; border-radius: 10px; padding: 20px; margin: 20px 0; }
                .row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #0F3460; }
                .row:last-child { border-bottom: none; }
                .label { color: #94A3B8; font-size: 13px; }
                .value { color: #ffffff; font-size: 13px; font-weight: bold; }
                .reference { color: #F84464; font-size: 18px; font-weight: bold; letter-spacing: 2px; text-align: center; margin: 10px 0; }
                .qr-section { text-align: center; margin: 20px 0; }
                .qr-section img { width: 160px; height: 160px; background: white; padding: 8px; border-radius: 8px; }
                .qr-label { color: #94A3B8; font-size: 12px; margin-top: 8px; }
                .footer { background: #0F3460; padding: 16px; text-align: center; color: #94A3B8; font-size: 12px; }
                .amount { color: #F84464; font-size: 20px; font-weight: bold; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🎬 BookMyShow — Booking Confirmed!</h1>
                </div>
                <div class="body">
                  <p class="greeting">Hi <strong>%s</strong>, your booking is confirmed! 🎉</p>

                  <div class="ticket">
                    <div class="reference">%s</div>

                    <div class="row">
                      <span class="label">Movie</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Theatre</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Screen</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Date</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Time</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Seats</span>
                      <span class="value">%s</span>
                    </div>
                    <div class="row">
                      <span class="label">Amount Paid</span>
                      <span class="amount">₹%.2f</span>
                    </div>
                  </div>

                  <div class="qr-section">
                    <p class="qr-label">Show this QR code at the theatre entrance</p>
                    <img src="data:image/png;base64,%s" alt="QR Code"/>
                    <p class="qr-label">Ref: %s</p>
                  </div>
                </div>
                <div class="footer">
                  This is an automated email. Please do not reply.<br/>
                  © 2026 BookMyShow Clone
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                userName, referenceId, movieTitle, theatreName,
                screenName, showDate, showTime, seats,
                totalAmount, qrCodeBase64, referenceId
        );
    }
}