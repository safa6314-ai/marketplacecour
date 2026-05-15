package org.example.api;

import org.example.api.email.EmailApiService;
import org.example.api.courseai.CourseGenerationApiService;
import org.example.api.courseai.GeneratedCourse;
import org.example.api.storage.FileStorageApiService;
import org.example.api.storage.UploadResult;
import org.example.api.video.MeetingInfo;
import org.example.api.video.MeetingRequest;
import org.example.api.video.VideoConferenceApiService;

import java.nio.file.Path;

public class ApiUsageExample {

    public static void main(String[] args) {
        EmailApiService emailApi = new EmailApiService();
        emailApi.sendCourseReminder(
                "etudiant@example.com",
                "Amina",
                "Java debutant",
                "20/05/2026 a 10:00"
        );

        VideoConferenceApiService videoApi = new VideoConferenceApiService();
        MeetingInfo meeting = videoApi.createMeeting(new MeetingRequest(
                "Session Java debutant",
                "2026-05-20T10:00:00",
                60,
                "Africa/Lagos"
        ));
        System.out.println("Lien Zoom etudiant : " + meeting.joinUrl());

        FileStorageApiService storageApi = new FileStorageApiService();
        UploadResult result = storageApi.uploadCourseFile(Path.of("support-cours.pdf"));
        System.out.println("Fichier disponible ici : " + result.secureUrl());

        CourseGenerationApiService courseAiApi = new CourseGenerationApiService();
        GeneratedCourse generatedCourse = courseAiApi.generateCourse(
                "Un cours pratique pour apprendre JavaFX et creer une marketplace de cours."
        );
        System.out.println("Cours genere : " + generatedCourse.cours().getTitre());
        generatedCourse.chapitres().forEach(chapitre ->
                System.out.println(chapitre.getOrdre() + ". " + chapitre.getTitre())
        );
    }
}
