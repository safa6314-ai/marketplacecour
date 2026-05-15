package Utils;

import java.security.SecureRandom;

public class CaptchaService {

    private final SecureRandom random = new SecureRandom();
    private int expectedResult;

    public String generateCaptcha() {
        int firstNumber = random.nextInt(9) + 1;
        int secondNumber = random.nextInt(9) + 1;
        expectedResult = firstNumber + secondNumber;

        return firstNumber + " + " + secondNumber + " = ?";
    }

    public boolean validateCaptcha(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            return Integer.parseInt(input.trim()) == expectedResult;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

