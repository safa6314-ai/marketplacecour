package Services;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Normalizer;

public class SpeechService {

    private static final String VOICE_NAME = "kevin16";

    public void speak(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (speakWithWindowsFrenchVoice(text)) {
            return;
        }

        System.setProperty(
                "freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory"
        );

        Voice voice = VoiceManager.getInstance().getVoice(VOICE_NAME);
        if (voice == null) {
            throw new IllegalStateException("Voix FreeTTS introuvable: " + VOICE_NAME);
        }

        String texteLisible = preparerTextePourFreeTTS(text);

        voice.allocate();
        try {
            voice.speak(texteLisible);
        } finally {
            voice.deallocate();
        }
    }

    private boolean speakWithWindowsFrenchVoice(String text) {
        String script = """
                Add-Type -AssemblyName System.Speech;
                $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer;
                $voice = $synth.GetInstalledVoices() |
                    Where-Object { $_.VoiceInfo.Culture.Name -like 'fr-*' } |
                    Select-Object -First 1;
                if (-not $voice) { exit 2 }
                $synth.SelectVoice($voice.VoiceInfo.Name);
                $synth.Rate = 0;
                $synth.Speak([Console]::In.ReadToEnd());
                """;

        try {
            Process process = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-Command",
                    script
            ).start();

            try (Writer writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(text);
            }

            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String preparerTextePourFreeTTS(String text) {
        String texteSansAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return texteSansAccents
                .replace("œ", "oe")
                .replace("Œ", "Oe")
                .replace("æ", "ae")
                .replace("Æ", "Ae")
                .replace("?", ".")
                .replace("!", ".")
                .replaceAll("[^a-zA-Z0-9.,;:()'\\-\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
