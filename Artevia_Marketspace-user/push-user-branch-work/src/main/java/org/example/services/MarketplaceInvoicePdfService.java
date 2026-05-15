package org.example.services;

import org.example.entities.MarketplaceAchat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarketplaceInvoicePdfService {
    public Path generateInvoice(MarketplaceAchat achat) throws IOException {
        Path directory = Path.of("target", "invoices");
        Files.createDirectories(directory);
        Path output = directory.resolve("facture-achat-" + achat.getId() + ".pdf");
        Files.writeString(output, pdfContent(achat), StandardCharsets.ISO_8859_1);
        return output.toAbsolutePath();
    }

    private String pdfContent(MarketplaceAchat achat) {
        String lines = "BT\n/F1 18 Tf\n50 770 Td\n(Facture Marketplace Artevia) Tj\n" +
                "/F1 11 Tf\n0 -40 Td\n(MarketplaceAchat ID: " + esc(String.valueOf(achat.getId())) + ") Tj\n" +
                "0 -20 Td\n(Oeuvre: " + esc(achat.getNomOeuvre()) + ") Tj\n" +
                "0 -20 Td\n(Client: " + esc(achat.getNomAcheteur()) + ") Tj\n" +
                "0 -20 Td\n(Date: " + esc(String.valueOf(achat.getDateAchat())) + ") Tj\n" +
                "0 -20 Td\n(Statut: " + esc(achat.getStatut()) + ") Tj\n" +
                "0 -20 Td\n(Montant: " + String.format("%.2f", achat.getPrix()) + " DT) Tj\nET";
        int streamLength = lines.getBytes(StandardCharsets.ISO_8859_1).length;
        String pdf = "%PDF-1.4\n" +
                "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n" +
                "2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj\n" +
                "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n" +
                "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n" +
                "5 0 obj << /Length " + streamLength + " >> stream\n" + lines + "\nendstream endobj\n" +
                "xref\n0 6\n0000000000 65535 f \n" +
                "trailer << /Root 1 0 R /Size 6 >>\nstartxref\n0\n%%EOF";
        return pdf;
    }

    private String esc(String value) {
        return (value == null ? "" : value).replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
