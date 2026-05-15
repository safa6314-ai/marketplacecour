package Services;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebcamService {

    private static final Path CAPTURE_DIRECTORY = Path.of("security_captures");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public File captureImage(String email) {
        Webcam webcam = null;

        try {
            Files.createDirectories(CAPTURE_DIRECTORY);

            webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("[SECURITY] Aucune webcam detectee. Alerte sans photo.");
                return null;
            }

            Dimension size = WebcamResolution.VGA.getSize();
            webcam.setViewSize(size);
            webcam.open();

            BufferedImage image = webcam.getImage();
            if (image == null) {
                System.err.println("[SECURITY] La webcam n'a retourne aucune image.");
                return null;
            }

            File imageFile = CAPTURE_DIRECTORY
                    .resolve(buildFileName(email))
                    .toFile();

            ImageIO.write(image, "PNG", imageFile);
            return imageFile;
        } catch (IOException | RuntimeException e) {
            System.err.println("[SECURITY] Capture webcam impossible : " + e.getMessage());
            return null;
        } finally {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        }
    }

    private String buildFileName(String email) {
        String safeEmail = email == null ? "unknown" : email.replaceAll("[^A-Za-z0-9._-]", "_");
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        return "security_" + safeEmail + "_" + timestamp + ".png";
    }
}
