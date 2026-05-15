package org.example.services;

import org.example.entities.StudentGrade;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GradeService {
    private final Map<String, Map<String, StudentGrade>> gradesByEmail = new LinkedHashMap<>();

    public void saveGradeByStudentEmail(String email, String chapterKey, double grade, String comment) {
        String normalizedEmail = normalizeEmail(email);
        Map<String, StudentGrade> grades = gradesByEmail.computeIfAbsent(normalizedEmail, key -> new LinkedHashMap<>());
        StudentGrade existing = grades.get(chapterKey);
        if (existing == null) {
            grades.put(chapterKey, new StudentGrade(normalizedEmail, chapterKey, grade, comment));
        } else {
            existing.setGrade(grade);
            existing.setTeacherComment(comment);
        }
    }

    public void saveGradeByStudentEmail(String email, int chapterId, double grade) {
        saveGradeByStudentEmail(email, String.valueOf(chapterId), grade, "");
    }

    public List<StudentGrade> getGradesByStudentEmail(String email) {
        return gradesByEmail.getOrDefault(normalizeEmail(email), Collections.emptyMap())
                .values()
                .stream()
                .toList();
    }

    public double calculateAverageByEmail(String email) {
        Collection<StudentGrade> grades = gradesByEmail.getOrDefault(normalizeEmail(email), Collections.emptyMap()).values();
        return grades.stream().mapToDouble(StudentGrade::getGrade).average().orElse(0);
    }

    public void clear() {
        gradesByEmail.clear();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
