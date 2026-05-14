package Services;

import Entities.Question;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class QuestionService extends QuestionCRUD {

    private static final Map<String, Integer> ORDRE_DIFFICULTE = Map.of(
            "facile", 1,
            "moyen", 2,
            "difficile", 3
    );

    public List<Question> trierParDifficulte() throws SQLException {
        List<Question> questions = afficher();

        questions.sort(Comparator.comparingInt(question ->
                ORDRE_DIFFICULTE.getOrDefault(normaliser(question.getNiveau()), Integer.MAX_VALUE)));

        return questions;
    }

    private String normaliser(String niveau) {
        return niveau == null ? "" : niveau.trim().toLowerCase();
    }
}
