package com.example.sales.adapter.mail;

import com.example.sales.domain.ReportData;
import com.example.sales.port.ReportSender;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sends the generated report via e‑mail with the PDF attached.
 */
@Component
public class EmailReportSender implements ReportSender {

    private static final Logger log = LoggerFactory.getLogger(EmailReportSender.class);

    private final JavaMailSender mail;
    private final List<String> recipients;
    private final String from;

    public EmailReportSender(JavaMailSender mail,
                             @Value("${report.recipients}") List<String> recipients,
                             @Value("${spring.mail.username:}") String from) {
        this.mail = mail;
        this.recipients = recipients;
        this.from = from;
    }

    @Override
    public void send(ReportData data, byte[] document, String filename) {
        if (recipients == null || recipients.isEmpty()) {
            log.warn("Attempted to send report email but recipient list is empty");
            return; // silently ignore – caller may decide to handle differently
        }
        try {
            MimeMessage msg = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8"); // true = multipart
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Звіт про продажі за " + data.period());
            helper.setText("Вітаю! У вкладенні — звіт про продажі за " + data.period() + ".");
            helper.addAttachment(filename, new ByteArrayResource(document));
            mail.send(msg);
            log.info("Report email sent to {} for period {}", recipients, data.period());
        } catch (Exception e) {
            log.error("Failed to send report email for period {}", data.period(), e);
            throw new IllegalStateException("Failed to send report email for " + data.period(), e);
        }
    }
}
