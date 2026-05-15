package org.example.api.email;

public record EmailMessage(
        String toEmail,
        String toName,
        String subject,
        String htmlContent
) {
}
