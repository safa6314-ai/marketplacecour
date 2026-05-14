package Utils;

import Entities.Abonnement;
import Entities.Souscription;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Invoice-style printable receipt (A4) with line items, totals, and signature. */
public final class ReceiptPdfExporter {

    private static final float MARGIN = 40;
    private static final float BRAND_R = 0.22f;
    private static final float BRAND_G = 0.25f;
    private static final float BRAND_B = 0.55f;

    private ReceiptPdfExporter() {}

    public static void writeReceipt(
            Abonnement plan,
            Souscription sub,
            String clientName,
            WritableImage signature,
            File destination
    ) throws Exception {
        BufferedImage sig = toBufferedImage(signature);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pw = page.getMediaBox().getWidth();
            float ph = page.getMediaBox().getHeight();
            float left = MARGIN;
            float right = pw - MARGIN;
            float contentW = right - left;

            String receiptNo = String.format(Locale.ROOT, "ART-%06d", sub.getIdSouscription());
            String issued = pdfSafe(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm", Locale.FRENCH)
                    .format(LocalDateTime.now()));
            String amount = String.format(Locale.US, "%.2f USD", plan.getPrix());
            String period = pdfSafe(sub.getDateDebut().toString()) + "  >  " + pdfSafe(sub.getDateFin().toString());

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float yTop = ph - MARGIN;

                // Outer frame
                cs.setStrokingColor(0.72f, 0.74f, 0.78f);
                cs.setLineWidth(1f);
                cs.addRect(left - 6, MARGIN - 6, contentW + 12, yTop - MARGIN + 12);
                cs.stroke();

                // Inner fine border
                cs.setLineWidth(0.35f);
                cs.setStrokingColor(0.88f, 0.89f, 0.92f);
                cs.addRect(left - 2, MARGIN - 2, contentW + 4, yTop - MARGIN + 4);
                cs.stroke();

                float headerH = 82;
                float headerBottom = yTop - headerH;
                cs.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
                cs.addRect(left, headerBottom, contentW, headerH);
                cs.fill();

                // Accent stripe
                cs.setNonStrokingColor(0.55f, 0.6f, 0.98f);
                cs.addRect(left, headerBottom, 6, headerH);
                cs.fill();

                cs.setNonStrokingColor(1f, 1f, 1f);
                text(cs, PDType1Font.HELVETICA_BOLD, 22, left + 22, headerBottom + 52, "RECU DE PAIEMENT");
                text(cs, PDType1Font.HELVETICA, 10, left + 22, headerBottom + 32, "Artevia - Services d'abonnement numerique");
                text(cs, PDType1Font.HELVETICA_OBLIQUE, 9, left + 22, headerBottom + 14, "Document officiel - Conservez ce justificatif pour vos archives");

                float rPad = right - 20;
                drawRightText(cs, PDType1Font.HELVETICA_BOLD, 14, rPad, headerBottom + 52, "REF. " + receiptNo);
                drawRightText(cs, PDType1Font.HELVETICA, 9, rPad, headerBottom + 34, "Date d'emission : " + issued);
                drawRightText(cs, PDType1Font.HELVETICA, 9, rPad, headerBottom + 18, "Mode de paiement : Carte bancaire (Stripe)");

                float y = headerBottom - 24;

                // PAYE badge
                float badgeW = 92;
                float badgeH = 28;
                cs.setNonStrokingColor(0.1f, 0.52f, 0.34f);
                cs.addRect(left, y - badgeH, badgeW, badgeH);
                cs.fill();
                cs.setNonStrokingColor(1f, 1f, 1f);
                text(cs, PDType1Font.HELVETICA_BOLD, 12, left + 16, y - badgeH + 9, "PAYE");
                y -= badgeH + 26;

                // Bill from / bill to
                float colGap = 14;
                float colW = (contentW - colGap) / 2;
                float boxH = 92;
                fillStrokeBox(cs, left, y - boxH, colW, boxH, 0.96f, 0.97f, 0.99f, 0.8f, 0.82f, 0.87f);
                fillStrokeBox(cs, left + colW + colGap, y - boxH, colW, boxH, 0.96f, 0.97f, 0.99f, 0.8f, 0.82f, 0.87f);

                cs.setNonStrokingColor(0.32f, 0.35f, 0.42f);
                text(cs, PDType1Font.HELVETICA_BOLD, 7, left + 12, y - 14, "EMETTEUR");
                text(cs, PDType1Font.HELVETICA_BOLD, 10, left + 12, y - 30, "Artevia SAS");
                text(cs, PDType1Font.HELVETICA, 8, left + 12, y - 44, "Plateforme marketplace");
                text(cs, PDType1Font.HELVETICA, 8, left + 12, y - 56, "contact@artevia.app");
                text(cs, PDType1Font.HELVETICA_OBLIQUE, 7, left + 12, y - 74, "TVA sur encaissements selon regime applicable");

                text(cs, PDType1Font.HELVETICA_BOLD, 7, left + colW + colGap + 12, y - 14, "CLIENT");
                text(cs, PDType1Font.HELVETICA_BOLD, 11, left + colW + colGap + 12, y - 32, truncate(pdfSafe(clientName), 36));
                text(cs, PDType1Font.HELVETICA, 8, left + colW + colGap + 12, y - 48, "Souscription #" + sub.getIdSouscription());
                text(cs, PDType1Font.HELVETICA, 8, left + colW + colGap + 12, y - 62, "Statut : " + pdfSafe(sub.getStatut()));

                y -= boxH + 26;

                cs.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
                text(cs, PDType1Font.HELVETICA_BOLD, 11, left, y, "DETAIL DES PRESTATIONS");
                y -= 6;
                cs.setStrokingColor(BRAND_R, BRAND_G, BRAND_B);
                cs.setLineWidth(1.5f);
                cs.moveTo(left, y);
                cs.lineTo(left + 200, y);
                cs.stroke();
                y -= 20;

                // Table
                float tableTop = y;
                float hdrH = 30;
                float row1H = 30;
                float row2H = 22;
                float totBlockH = 48;
                float tableH = hdrH + row1H + row2H + totBlockH + 8;
                float colDesc = left + 10;
                float colQty = left + contentW * 0.58f;
                float colUnit = left + contentW * 0.70f;
                float colAmt = left + contentW * 0.82f;

                cs.setStrokingColor(0.45f, 0.47f, 0.52f);
                cs.setLineWidth(0.7f);
                cs.addRect(left, tableTop - tableH, contentW, tableH);
                cs.stroke();

                cs.setNonStrokingColor(0.89f, 0.90f, 0.93f);
                cs.addRect(left + 0.5f, tableTop - hdrH + 0.5f, contentW - 1f, hdrH - 1f);
                cs.fill();
                cs.setStrokingColor(0.45f, 0.47f, 0.52f);
                cs.moveTo(left, tableTop - hdrH);
                cs.lineTo(right, tableTop - hdrH);
                cs.stroke();

                cs.setNonStrokingColor(0.18f, 0.2f, 0.26f);
                text(cs, PDType1Font.HELVETICA_BOLD, 8, colDesc, tableTop - 19, "DESCRIPTION");
                text(cs, PDType1Font.HELVETICA_BOLD, 8, colQty, tableTop - 19, "QTE");
                text(cs, PDType1Font.HELVETICA_BOLD, 8, colUnit, tableTop - 19, "P.U.");
                text(cs, PDType1Font.HELVETICA_BOLD, 8, colAmt, tableTop - 19, "MONTANT");

                float r1b = tableTop - hdrH - row1H;
                hLine(cs, left, right, r1b + row1H, 0.78f);
                String descLine = "Abonnement : " + truncate(pdfSafe(plan.getNom()), 52);
                cs.setNonStrokingColor(0.08f, 0.09f, 0.12f);
                text(cs, PDType1Font.HELVETICA, 9, colDesc, r1b + 11, descLine);
                text(cs, PDType1Font.HELVETICA, 9, colQty, r1b + 11, "1");
                text(cs, PDType1Font.HELVETICA, 9, colUnit, r1b + 11, amount);
                text(cs, PDType1Font.HELVETICA_BOLD, 9, colAmt, r1b + 11, amount);

                float r2b = r1b - row2H;
                hLine(cs, left, right, r2b + row2H, 0.78f);
                cs.setNonStrokingColor(0.4f, 0.42f, 0.48f);
                text(cs, PDType1Font.HELVETICA_OBLIQUE, 8, colDesc, r2b + 9,
                        "Periode : " + period + "  |  Duree : " + plan.getDureeMois() + " mois");

                float totTop = r2b - 6;
                float totW = 220;
                float totLeft = right - totW - 10;
                cs.setNonStrokingColor(0.95f, 0.96f, 1f);
                cs.addRect(totLeft, totTop - totBlockH, totW, totBlockH);
                cs.fill();
                cs.setStrokingColor(0.45f, 0.47f, 0.52f);
                cs.setLineWidth(0.5f);
                cs.addRect(totLeft, totTop - totBlockH, totW, totBlockH);
                cs.stroke();

                hLine(cs, totLeft + 8, totLeft + totW - 8, totTop - 18, 0.85f);
                cs.setNonStrokingColor(0.35f, 0.38f, 0.44f);
                text(cs, PDType1Font.HELVETICA, 9, totLeft + 12, totTop - 16, "Montant HT");
                drawRightText(cs, PDType1Font.HELVETICA, 9, totLeft + totW - 12, totTop - 16, amount);
                text(cs, PDType1Font.HELVETICA_OBLIQUE, 7, totLeft + 12, totTop - 26, "TVA : exonere / non applicable (0,00)");
                drawRightText(cs, PDType1Font.HELVETICA_OBLIQUE, 7, totLeft + totW - 12, totTop - 26, "0,00 USD");

                cs.setNonStrokingColor(BRAND_R, BRAND_G, BRAND_B);
                text(cs, PDType1Font.HELVETICA_BOLD, 12, totLeft + 12, totTop - 38, "TOTAL TTC");
                drawRightText(cs, PDType1Font.HELVETICA_BOLD, 12, totLeft + totW - 12, totTop - 38, amount);

                y = totTop - totBlockH - 22;
                drawDashedLine(cs, left + 20, y, right - 20, y);
                y -= 18;

                cs.setNonStrokingColor(0.38f, 0.4f, 0.46f);
                text(cs, PDType1Font.HELVETICA_BOLD, 9, left, y, "SIGNATURE DU CLIENT (lue et approuvee)");
                y -= 12;

                float sigW = Math.min(300, contentW - 24);
                PDImageXObject img = LosslessFactory.createFromImage(doc, sig);
                float sigH = img.getHeight() * (sigW / img.getWidth());
                float sigBoxH = sigH + 40;
                float sigBottom = y - sigBoxH;

                cs.setStrokingColor(0.45f, 0.47f, 0.52f);
                cs.setLineWidth(0.9f);
                cs.addRect(left, sigBottom, contentW, sigBoxH);
                cs.stroke();

                cs.setStrokingColor(0.82f, 0.84f, 0.88f);
                cs.setLineDashPattern(new float[] { 4, 4 }, 0);
                cs.addRect(left + 12, sigBottom + 12, contentW - 24, sigBoxH - 24);
                cs.stroke();
                cs.setLineDashPattern(new float[] {}, 0);

                cs.drawImage(img, left + 20, sigBottom + 18, sigW, sigH);

                cs.setNonStrokingColor(0.48f, 0.5f, 0.55f);
                text(cs, PDType1Font.HELVETICA_OBLIQUE, 7, left + 20, sigBottom + 8,
                        "Signature capturee electroniquement lors de la validation du paiement.");

                y = sigBottom - 22;
                cs.setNonStrokingColor(0.45f, 0.47f, 0.5f);
                text(cs, PDType1Font.HELVETICA_BOLD, 10, left, y, "Merci pour votre achat !");
                y -= 12;
                wrapParagraph(cs, left, y, contentW,
                        "Ce document atteste du paiement integral du montant indique pour l'abonnement designe. "
                                + "Les prestations sont soumises aux conditions generales Artevia. "
                                + "Support : support@artevia.app - Ce recu ne constitue pas une facture de TVA sauf mention contraire.",
                        8, 11.5f);
            }

            doc.save(destination);
        }
    }

    private static void fillStrokeBox(
            PDPageContentStream cs, float x, float y, float w, float h,
            float fr, float fg, float fb, float sr, float sg, float sb
    ) throws Exception {
        cs.setNonStrokingColor(fr, fg, fb);
        cs.addRect(x, y, w, h);
        cs.fill();
        cs.setStrokingColor(sr, sg, sb);
        cs.setLineWidth(0.45f);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private static void hLine(PDPageContentStream cs, float x1, float x2, float y, float gray) throws Exception {
        cs.setStrokingColor(gray, gray, gray);
        cs.setLineWidth(0.4f);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    private static void text(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String t)
            throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(pdfSafe(t));
        cs.endText();
    }

    private static void drawRightText(PDPageContentStream cs, PDType1Font font, float size, float rightX, float y, String t)
            throws Exception {
        String s = pdfSafe(t);
        float w = font.getStringWidth(s) / 1000f * size;
        text(cs, font, size, rightX - w, y, s);
    }

    private static void drawDashedLine(PDPageContentStream cs, float x1, float y, float x2, float y2) throws Exception {
        cs.setStrokingColor(0.7f, 0.72f, 0.76f);
        cs.setLineWidth(0.6f);
        cs.setLineDashPattern(new float[] { 2, 4 }, 0);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y2);
        cs.stroke();
        cs.setLineDashPattern(new float[] {}, 0);
    }

    private static void wrapParagraph(PDPageContentStream cs, float x, float y, float maxW, String text, int fontSize, float lineGap)
            throws Exception {
        String s = pdfSafe(text);
        List<String> words = new ArrayList<>();
        for (String w : s.split(" ")) {
            if (!w.isEmpty()) words.add(w);
        }
        float lineY = y;
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            if (line.isEmpty()) {
                float wWidth = PDType1Font.HELVETICA.getStringWidth(w) / 1000f * fontSize;
                if (wWidth > maxW) {
                    String cut = w;
                    while (cut.length() > 1
                            && PDType1Font.HELVETICA.getStringWidth(cut + "...") / 1000f * fontSize > maxW) {
                        cut = cut.substring(0, cut.length() - 1);
                    }
                    if (cut.length() < w.length()) {
                        cut = cut + "...";
                    }
                    cs.setNonStrokingColor(0.48f, 0.5f, 0.54f);
                    text(cs, PDType1Font.HELVETICA, fontSize, x, lineY, cut);
                    lineY -= lineGap;
                    continue;
                }
                line.append(w);
                continue;
            }
            String trial = line + " " + w;
            float tw = PDType1Font.HELVETICA.getStringWidth(trial) / 1000f * fontSize;
            if (tw > maxW) {
                cs.setNonStrokingColor(0.48f, 0.5f, 0.54f);
                text(cs, PDType1Font.HELVETICA, fontSize, x, lineY, line.toString());
                lineY -= lineGap;
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(trial);
            }
        }
        if (!line.isEmpty()) {
            cs.setNonStrokingColor(0.48f, 0.5f, 0.54f);
            text(cs, PDType1Font.HELVETICA, fontSize, x, lineY, line.toString());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    private static String pdfSafe(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.replace('\r', ' ').replace('\n', ' ');
    }

    private static BufferedImage toBufferedImage(Image fx) {
        int w = (int) fx.getWidth();
        int h = (int) fx.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = fx.getPixelReader();
        for (int x = 0; x < w; x++) {
            for (int yy = 0; yy < h; yy++) {
                bi.setRGB(x, yy, reader.getArgb(x, yy) | 0xff000000);
            }
        }
        return bi;
    }
}
