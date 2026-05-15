package org.example.services;

import org.example.entities.StudentSubmission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubmissionService {
    private final List<StudentSubmission> submissions = new ArrayList<>();

    public void addSubmission(StudentSubmission submission) {
        submissions.add(submission);
    }

    public List<StudentSubmission> getSubmissionsByStudentEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return submissions.stream()
                .filter(submission -> normalizeEmail(submission.getStudentEmail()).equals(normalizedEmail))
                .toList();
    }

    public boolean hasSubmissionByStudentEmail(String email) {
        return !getSubmissionsByStudentEmail(email).isEmpty();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
