package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import org.example.entities.Avis;
import org.example.entities.CourseSummary;
import org.example.entities.Cours;
import org.example.entities.Chapitres;
import org.example.entities.Meeting;
import org.example.entities.Rendu;
import org.example.entities.Student;
import org.example.entities.StudentSubmission;
import org.example.api.courseai.CourseGenerationApiService;
import org.example.api.courseai.GeneratedCourse;
import org.example.api.email.EmailApiService;
import org.example.api.storage.FileStorageApiService;
import org.example.api.storage.UploadResult;
import org.example.api.video.MeetingInfo;
import org.example.api.video.MeetingRequest;
import org.example.api.video.VideoConferenceApiService;
import org.example.services.AdvancedPedagogyService;
import org.example.services.AvisService;
import org.example.services.CoursService;
import org.example.services.ChapitreService;
import org.example.services.GradeService;
import org.example.services.ProgressService;
import org.example.services.RenduService;
import org.example.services.StudentService;
import org.example.services.SubmissionService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CourseManagerController implements Initializable {

    // HEADER
    @FXML private Label statusLabel;
    @FXML private ToggleButton enseignantModeToggle;

    // NAVIGATION
    @FXML private Button homeNavButton;
    @FXML private Button courseNavButton;
    @FXML private Button studentsNavButton;
    @FXML private Button reportsNavButton;
    @FXML private VBox courseHeroSection;
    @FXML private VBox coursesSection;
    @FXML private VBox servicesSection;
    @FXML private VBox studentManagementSection;
    @FXML private VBox reportsSection;
    @FXML private Label reportCoursesLabel;
    @FXML private Label reportStudentsLabel;
    @FXML private Label reportPresentStudentsLabel;
    @FXML private Label reportAverageLabel;

    // LEFT PANEL - Course list
    @FXML private TextField courseSearchField;
    @FXML private Label courseCountLabel;

    @FXML private ListView<Cours> courseListView;

    @FXML private Button addCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button deleteCourseButton;

    // CENTER TOP - Course details
    @FXML private Label courseIdLabel;
    @FXML private TextField courseTitleField;
    @FXML private ComboBox<String> courseCategoryCombo;
    @FXML private ComboBox<String> courseLevelCombo;
    @FXML private TextArea courseDescriptionArea;

    // CENTER BOTTOM LEFT - Chapter list
    @FXML private Label chapterCountLabel;
    @FXML private TextField chapterSearchField;

    @FXML private ListView<Chapitres> chapterListView;

    @FXML private Button addChapterButton;
    @FXML private Button editChapterButton;
    @FXML private Button deleteChapterButton;

    // CENTER BOTTOM RIGHT - Chapter details
    @FXML private Label chapterIdLabel;
    @FXML private TextField chapterOrderField;
    @FXML private TextField chapterTitleField;
    @FXML private TextArea chapterContentArea;
    @FXML private ComboBox<String> ratingCombo;
    @FXML private TextArea feedbackCommentArea;
    @FXML private TextField renduFileField;
    @FXML private TextField apiStudentNameField;
    @FXML private TextField apiStudentEmailField;
    @FXML private TextField apiMeetingStartField;
    @FXML private TextField apiMeetingTopicField;
    @FXML private TextField apiMeetingDurationField;
    @FXML private TextField apiCloudUrlField;
    @FXML private TextArea apiCourseIdeaArea;
    @FXML private Button generateCourseButton;
    @FXML private Button createZoomButton;
    @FXML private Button joinZoomButton;
    @FXML private Button chooseSummaryFileButton;
    @FXML private Button uploadSummaryButton;
    @FXML private TextField advancedStudentEmailField;
    @FXML private ComboBox<String> advancedProgressCombo;
    @FXML private Label advancedResultLabel;
    @FXML private Label enseignantRenduStatusLabel;
    @FXML private Label renduAttemptLabel;
    @FXML private SplitPane chapterWorkspacePane;
    @FXML private TilePane courseTilePane;
    @FXML private VBox courseEditorBox;
    @FXML private VBox advancedProgressBox;
    @FXML private HBox enseignantEvaluationBox;
    @FXML private TextField chapterPdfField;
    @FXML private Button chooseChapterPdfButton;
    @FXML private Button openChapterPdfButton;
    @FXML private Button chooseRenduFileButton;
    @FXML private Button submitRenduButton;
    @FXML private Button submitEnseignantGradeButton;

    @FXML private Button saveChapterButton;

    // STUDENT MANAGEMENT
    @FXML private Label totalStudentsLabel;
    @FXML private Label activeStudentsLabel;
    @FXML private Label absentStudentsLabel;
    @FXML private Label globalAverageLabel;
    @FXML private Label completedChaptersLabel;
    @FXML private TextField studentSearchField;
    @FXML private TextField studentEmailLookupField;
    @FXML private ComboBox<String> studentFilterCombo;
    @FXML private ListView<StudentRecord> studentListView;

    private final ObservableList<Cours> allCourses = FXCollections.observableArrayList();
    private final ObservableList<Cours> filteredCourses = FXCollections.observableArrayList();
    private final ObservableList<Chapitres> allChaptersForCourse = FXCollections.observableArrayList();
    private final ObservableList<Chapitres> visibleChapters = FXCollections.observableArrayList();
    private final ObservableList<StudentRecord> allStudents = FXCollections.observableArrayList();
    private final ObservableList<StudentRecord> filteredStudents = FXCollections.observableArrayList();
    private final List<StudentSubmission> localRenduSubmissions = new ArrayList<>();
    private final DateTimeFormatter studentDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final CoursService coursService = new CoursService();
    private final ChapitreService chapitreService = new ChapitreService();
    private final AvisService avisService = new AvisService();
    private final RenduService renduService = new RenduService();
    private final AdvancedPedagogyService advancedPedagogyService = new AdvancedPedagogyService();
    private final StudentService studentService = new StudentService();
    private final GradeService gradeService = new GradeService();
    private final ProgressService progressService = new ProgressService();
    private final SubmissionService submissionService = new SubmissionService();
    private File selectedRenduFile;
    private File selectedChapterPdfFile;
    private File selectedSummaryFile;
    private Cours selectedCourse;
    private VBox chapterWorkspaceOriginalParent;
    private int chapterWorkspaceOriginalIndex = -1;
    private Stage chapterWindow;
    private boolean enseignantMode = true;
    private String currentUserRole = "TEACHER";
    private String activePage = "cours";
    private int studentIdSequence = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wireLists();
        wireListeners();
        wireInputFilters();
        ensureCategoryOptions();
        ensureLevelOptions();
        configureEvaluationControls();
        initializeStudentManagement();
        setEnseignantMode(enseignantModeToggle == null || enseignantModeToggle.isSelected());
        showChapterWorkspace(false);
        showPage("cours");

        try {
            loadCoursesFromDb();
            refreshStudentCourseOptions();
            updateReportStats();
            setStatus("Donn\u00e9es charg\u00e9es depuis la base. Pr\u00eat.");
        } catch (RuntimeException e) {
            allCourses.clear();
            filteredCourses.clear();
            allChaptersForCourse.clear();
            visibleChapters.clear();
            updateCourseCount();
            updateChapterCount();
            setStatus(cleanError(e));
        }
    }

    private void loadCoursesFromDb() {
        Cours selected = getSelectedCourse();
        List<Cours> coursList = coursService.afficher();
        allCourses.setAll(coursList);
        applyCourseFilter();
        
        if (selected != null) {
            for (Cours c : filteredCourses) {
                if (c.getId() == selected.getId()) {
                    selectCourse(c);
                    break;
                }
            }
        } else {
            selectedCourse = null;
            clearCourseForm();
            showChapterWorkspace(false);
        }
    }

    private void loadChaptersForCourse(Cours cours) {
        Chapitres selectedChapter = getSelectedChapter();
        
        if (cours == null) {
            allChaptersForCourse.clear();
            visibleChapters.clear();
            updateChapterCount();
            clearChapterForm();
            clearStudentSpace();
            showChapterWorkspace(false);
            return;
        }

        showChapterWorkspace(true);
        
        List<Chapitres> chapitresList;
        try {
            chapitresList = chapitreService.getByCoursId(cours.getId());
        } catch (RuntimeException e) {
            allChaptersForCourse.clear();
            visibleChapters.clear();
            updateChapterCount();
            clearChapterForm();
            clearStudentSpace();
            setStatus(cleanError(e));
            return;
        }
        allChaptersForCourse.setAll(chapitresList);
        applyChapterFilter();
        
        if (selectedChapter != null) {
            for (Chapitres c : visibleChapters) {
                if (c.getId() == selectedChapter.getId()) {
                    chapterListView.getSelectionModel().select(c);
                    return;
                }
            }
        }
        
        clearChapterForm();
        clearStudentSpace();
    }

    @FXML
    private void handleAddCourse(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        String title = text(courseTitleField);
        if (title.isEmpty()) {
            showValidationError("Titre obligatoire", "Le titre du cours est obligatoire.");
            return;
        }

        Cours cours = new Cours(
                title,
                text(courseDescriptionArea),
                0.0,
                comboValueOrDefault(courseCategoryCombo, "Peinture"),
                comboValueOrDefault(courseLevelCombo, "Debutant")
        );

        try {
            coursService.ajouter(cours);
            loadCoursesFromDb();

            boolean selected = selectCourseById(cours.getId());
            if (!selected) {
                selected = selectCourseByTitle(title);
            }

            if (!selected && !text(courseSearchField).isEmpty()) {
                setStatus("Cours ajout\u00e9, mais masqu\u00e9 par le filtre de recherche actuel.");
            } else {
                setStatus("Cours ajout\u00e9 : " + title);
            }
        } catch (RuntimeException e) {
            showError("Impossible d'ajouter le cours", e);
        }
    }

    @FXML
    private void handleEditCourse(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours selected = getSelectedCourse();
        if (selected == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez un cours \u00e0 modifier.");
            return;
        }

        String title = text(courseTitleField);
        if (title.isEmpty()) {
            showValidationError("Titre obligatoire", "Le titre du cours est obligatoire.");
            return;
        }

        selected.setTitre(title);
        selected.setCategorie(comboValueOrDefault(courseCategoryCombo, "Peinture"));
        selected.setDescription(text(courseDescriptionArea));
        selected.setNiveau(comboValueOrDefault(courseLevelCombo, "Debutant"));

        try {
            coursService.modifier(selected);
            loadCoursesFromDb();
            selectCourseById(selected.getId());
            setStatus("Cours modifi\u00e9 : #" + selected.getId());
        } catch (RuntimeException e) {
            showError("Impossible de modifier le cours", e);
        }
    }

    @FXML
    private void handleDeleteCourse(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours selected = getSelectedCourse();
        if (selected == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez un cours \u00e0 supprimer.");
            return;
        }

        try {
            coursService.supprimer(selected.getId());

            clearCourseForm();
            allChaptersForCourse.clear();
            visibleChapters.clear();
            clearChapterForm();
            clearStudentSpace();
            updateChapterCount();

            loadCoursesFromDb();

            setStatus("Cours supprim\u00e9.");
        } catch (RuntimeException e) {
            showError("Impossible de supprimer le cours", e);
        }
    }

    @FXML
    private void handleAddChapter(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        if (course == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez d'abord un cours.");
            return;
        }

        String title = text(chapterTitleField);
        if (title.isEmpty()) {
            showValidationError("Titre obligatoire", "Le titre du chapitre est obligatoire.");
            return;
        }

        Chapitres chapter = new Chapitres(
                title,
                chapterContentFromEditor(),
                parseOrder(chapterOrderField.getText(), visibleChapters.size() + 1),
                course.getId()
        );
        chapter.setPdfPath(text(chapterPdfField));

        try {
            chapitreService.ajouter(chapter);
            loadChaptersForCourse(course);

            boolean selected = selectChapterById(chapter.getId());
            if (!selected) {
                selectChapterByTitle(title);
            }

            setStatus("Chapitre ajout\u00e9 : " + title);
        } catch (RuntimeException e) {
            showError("Impossible d'ajouter le chapitre", e);
        }
    }

    @FXML
    private void handleEditChapter(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        Chapitres chapter = getSelectedChapter();
        if (course == null || chapter == null) {
            showValidationError("Chapitre obligatoire", "S\u00e9lectionnez un chapitre \u00e0 modifier.");
            return;
        }

        String title = text(chapterTitleField);
        if (title.isEmpty()) {
            showValidationError("Titre obligatoire", "Le titre du chapitre est obligatoire.");
            return;
        }

        chapter.setTitre(title);
        chapter.setContenu(chapterContentFromEditor());
        chapter.setPdfPath(text(chapterPdfField));
        chapter.setOrdre(parseOrder(chapterOrderField.getText(), chapter.getOrdre()));

        try {
            chapitreService.modifier(chapter);
            loadChaptersForCourse(course);
            selectChapterById(chapter.getId());
            setStatus("Chapitre modifi\u00e9 : #" + chapter.getId());
        } catch (RuntimeException e) {
            showError("Impossible de modifier le chapitre", e);
        }
    }

    @FXML
    private void handleDeleteChapter(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        Chapitres chapter = getSelectedChapter();
        if (course == null || chapter == null) {
            showValidationError("Chapitre obligatoire", "S\u00e9lectionnez un chapitre \u00e0 supprimer.");
            return;
        }

        try {
            chapitreService.supprimer(chapter.getId());
            loadChaptersForCourse(course);
            clearChapterForm();

            setStatus("Chapitre supprim\u00e9.");
        } catch (RuntimeException e) {
            showError("Impossible de supprimer le chapitre", e);
        }
    }

    @FXML
    private void handleSaveChapter(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        if (getSelectedChapter() == null) {
            handleAddChapter(event);
        } else {
            handleEditChapter(event);
        }
    }

    @FXML
    private void handleCourseSearch() {
        applyCourseFilter();
    }

    @FXML
    private void handleChapterSearch() {
        applyChapterFilter();
    }

    @FXML
    private void handleToggleEnseignantMode(ActionEvent event) {
        setEnseignantMode(enseignantModeToggle.isSelected());
    }

    @FXML
    private void handleShowCoursePage(ActionEvent event) {
        showPage("cours");
    }

    @FXML
    private void handleShowStudentsPage(ActionEvent event) {
        showPage("etudiants");
    }

    @FXML
    private void handleShowReportsPage(ActionEvent event) {
        showPage("rapports");
    }

    private void showPage(String page) {
        activePage = page == null ? "cours" : page;

        boolean showCourses = "cours".equals(activePage);
        boolean showStudents = "etudiants".equals(activePage);
        boolean showReports = "rapports".equals(activePage);

        setNodeVisible(courseHeroSection, showCourses);
        setNodeVisible(coursesSection, showCourses);
        setNodeVisible(servicesSection, showCourses);
        setNodeVisible(studentManagementSection, showStudents);
        setNodeVisible(reportsSection, showReports);
        showChapterWorkspace(showCourses && selectedCourse != null);

        setNavActive(courseNavButton, showCourses);
        setNavActive(homeNavButton, false);
        setNavActive(studentsNavButton, showStudents);
        setNavActive(reportsNavButton, showReports);

        if (showReports) {
            updateReportStats();
        }
    }

    private void setNodeVisible(Node node, boolean visible) {
        if (node == null) {
            return;
        }
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void setNavActive(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove("nav-item-active");
        if (active) {
            button.getStyleClass().add("nav-item-active");
        }
    }

    @FXML
    private void handleSubmitFeedback(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Chapitres chapter = getSelectedChapter();
        String email = text(advancedStudentEmailField);
        if (chapter == null) {
            showValidationError("Chapitre obligatoire", "Selectionnez un chapitre avant d'enregistrer la note.");
            return;
        }
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "Email de l'etudiant obligatoire.");
            return;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email est invalide.");
            return;
        }
        StudentRecord student = findStudentRecordByEmail(email);
        if (student == null) {
            showValidationError("Etudiant introuvable", "Etudiant introuvable avec cet email.");
            return;
        }

        String rating = comboValueOrDefault(ratingCombo, "");
        Integer parsedNote = parseRequiredIntegerNote(rating);
        if (parsedNote == null) {
            return;
        }

        try {
            int note = parsedNote;
            gradeService.saveGradeByStudentEmail(email, String.valueOf(chapter.getId()), note, text(feedbackCommentArea));
            avisService.ajouter(new Avis(chapter.getId(), text(feedbackCommentArea), note));
            updateStudentChapterGrade(student, chapter, note);
            feedbackCommentArea.clear();
            ratingCombo.getSelectionModel().clearSelection();
            refreshChapterList();
            studentListView.refresh();
            populateStudentForm(student);
            setStatus("Note enregistree pour " + student.getEmail() + " : " + note + " / 5");
        } catch (RuntimeException e) {
            showError("Impossible d'enregistrer la note", e);
        }
    }

    @FXML
    private void handleDownloadChapter(ActionEvent event) {
        Chapitres chapter = getSelectedChapter();
        if (chapter == null) {
            showValidationError("Chapitre obligatoire", "S\u00e9lectionnez un chapitre avant le t\u00e9l\u00e9chargement.");
            return;
        }

        downloadChapterToFile(chapter, chapterWorkspacePane);
    }

    private void downloadChapterToFile(Chapitres chapter, Node ownerNode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("T\u00e9l\u00e9charger le chapitre");
        fileChooser.setInitialFileName(safeFileName(valueOrFallback(chapter.getTitre(), "chapitre")) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"));

        File destination = fileChooser.showSaveDialog(ownerNode.getScene().getWindow());
        if (destination == null) {
            return;
        }

        String content = valueOrFallback(chapter.getTitre(), "Chapitre sans titre")
                + System.lineSeparator()
                + System.lineSeparator()
                + valueOrFallback(chapter.getContenu(), "");

        try {
            Files.writeString(destination.toPath(), content, StandardCharsets.UTF_8);
            setStatus("Chapitre t\u00e9l\u00e9charg\u00e9 : " + destination.getName());
        } catch (IOException e) {
            showError("Impossible de t\u00e9l\u00e9charger le chapitre", new IllegalStateException(e.getMessage(), e));
        }
    }

    @FXML
    private void handleChooseRenduFile(ActionEvent event) {
        if (!requireStudentModeForRendu()) {
            return;
        }

        Chapitres chapter = getSelectedChapter();
        String email = text(advancedStudentEmailField);
        if (email.isEmpty() || !isValidEmail(email)) {
            showValidationError("Email obligatoire", "Saisis un email etudiant valide avant de choisir le rendu.");
            return;
        }
        if (chapter != null && hasLocalRenduForStudentChapter(email, chapter)) {
            showValidationError("Tentative utilisee", "Tu ne peux mettre qu'un seul rendu pour ce chapitre.");
            updateRenduStatus(chapter);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir le fichier rendu");
        selectedRenduFile = fileChooser.showOpenDialog(renduFileField.getScene().getWindow());
        if (selectedRenduFile != null) {
            renduFileField.setText(selectedRenduFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleChooseChapterPdf(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir le PDF du chapitre");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        selectedChapterPdfFile = fileChooser.showOpenDialog(chapterPdfField.getScene().getWindow());
        if (selectedChapterPdfFile != null) {
            chapterPdfField.setText(selectedChapterPdfFile.getAbsolutePath());
            chapterContentArea.setText(chapterContentFromPdfPath(selectedChapterPdfFile.getAbsolutePath()));
        }
    }

    @FXML
    private void handleOpenChapterPdf(ActionEvent event) {
        String path = text(chapterPdfField);
        if (path.isEmpty()) {
            showValidationError("PDF obligatoire", "Choisissez d'abord un PDF.");
            return;
        }

        try {
            File pdf = new File(path);
            if (!pdf.exists()) {
                showValidationError("PDF introuvable", "Fichier PDF introuvable : " + path);
                return;
            }
            Desktop.getDesktop().open(pdf);
        } catch (IOException e) {
            showError("Impossible d'ouvrir le PDF", new IllegalStateException(e.getMessage(), e));
        }
    }

    @FXML
    private void handleSubmitRendu(ActionEvent event) {
        if (!requireStudentModeForRendu()) {
            return;
        }

        Chapitres chapter = getSelectedChapter();
        if (chapter == null) {
            showValidationError("Chapitre obligatoire", "S\u00e9lectionnez un chapitre avant d'envoyer un fichier.");
            return;
        }
        String email = text(advancedStudentEmailField);
        StudentRecord student = findStudentRecordByEmail(email);
        if (email.isEmpty() || !isValidEmail(email) || student == null) {
            showValidationError("Email obligatoire", "Saisis un email etudiant valide et existant avant d'envoyer le rendu.");
            return;
        }
        if (hasLocalRenduForStudentChapter(email, chapter)) {
            showValidationError("Tentative utilisee", "Tu ne peux mettre qu'un seul rendu pour ce chapitre.");
            updateRenduStatus(chapter);
            return;
        }
        if (selectedRenduFile == null) {
            showValidationError("Fichier obligatoire", "Choisissez d'abord un fichier rendu.");
            return;
        }

        try {
            saveLocalRenduSubmission(student, chapter, selectedRenduFile);
            selectedRenduFile = null;
            renduFileField.clear();
            updateRenduStatus(chapter);
            refreshChapterList();
            setStatus("Rendu envoy\u00e9 pour le chapitre : " + chapter.getTitre());
        } catch (RuntimeException e) {
            showError("Impossible d'envoyer le rendu", e);
        }
    }

    @FXML
    private void handleSendApiReminder(ActionEvent event) {
        Cours course = getSelectedCourse();
        if (course == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez un cours avant d'envoyer un rappel.");
            return;
        }

        String email = text(apiStudentEmailField);
        String studentName = valueOrFallback(text(apiStudentNameField), "\u00c9tudiant");
        if (!isValidPersonName(studentName)) {
            showValidationError("Nom invalide", "Le nom doit contenir seulement des lettres et des espaces.");
            return;
        }
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "L'email de l'\u00e9tudiant est obligatoire pour Brevo.");
            return;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email doit \u00eatre une adresse Gmail valide, par exemple nom@gmail.com.");
            return;
        }

        String startText = valueOrFallback(text(apiMeetingStartField), "prochaine session");
        runApiTask("Envoi du rappel Brevo...", () -> {
            new EmailApiService().sendCourseReminder(email, studentName, course.getTitre(), startText);
            return "Rappel Brevo envoy\u00e9 \u00e0 " + email;
        });
    }

    @FXML
    private void handleCreateApiMeeting(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }
        Cours course = getSelectedCourse();
        if (course == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez un cours avant de cr\u00e9er une r\u00e9union Zoom.");
            return;
        }

        String startTime = text(apiMeetingStartField);
        if (startTime.isEmpty()) {
            showValidationError("Date obligatoire", "La date de r\u00e9union est obligatoire. Exemple : 2026-05-20T10:00:00");
            return;
        }
        if (!isValidIsoDateTime(startTime)) {
            showValidationError("Date invalide", "La date Zoom doit respecter le format : 2026-05-20T10:00:00");
            return;
        }
        setStatus("Cr\u00e9ation de la r\u00e9union Zoom...");
        int duration = parsePositiveInt(text(apiMeetingDurationField));
        if (duration <= 0) {
            showValidationError("Duree invalide", "La duree doit etre un nombre superieur a 0.");
            return;
        }
        String topic = valueOrFallback(text(apiMeetingTopicField), "Session " + valueOrFallback(course.getTitre(), "Cours"));
        Task<MeetingInfo> task = new Task<>() {
            @Override
            protected MeetingInfo call() {
                return new VideoConferenceApiService().createMeeting(new MeetingRequest(
                        topic,
                        startTime,
                        duration,
                        "Africa/Lagos"
                ));
            }
        };

        task.setOnSucceeded(success -> {
            MeetingInfo meeting = task.getValue();
            if (!isZoomJoinUrl(meeting.joinUrl())) {
                showError("Erreur Zoom", new IllegalStateException("Zoom n'a pas retourne un lien join_url valide."));
                return;
            }
            studentService.addMeeting(new Meeting(
                    studentService.nextMeetingId(),
                    course.getId(),
                    valueOrFallback(course.getTitre(), "Cours"),
                    topic,
                    startTime,
                    duration,
                    meeting.joinUrl(),
                    meeting.startUrl(),
                    meeting.id(),
                    meeting.password(),
                    true,
                    LocalDateTime.now()
            ));
            setStatus("R\u00e9union Zoom cr\u00e9\u00e9e : " + meeting.joinUrl());
            showMeetingPopup(meeting);
        });
        task.setOnFailed(failure -> showError("Erreur Zoom", new RuntimeException(task.getException())));

        Thread thread = new Thread(task, "zoom-meeting-api-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleUploadApiFile(ActionEvent event) {
        File fileToUpload = selectedChapterPdfFile != null ? selectedChapterPdfFile : selectedRenduFile;
        if (fileToUpload == null) {
            showValidationError("Fichier obligatoire", "Choisissez un PDF de chapitre ou un rendu avant l'envoi vers Cloudinary.");
            return;
        }
        if (!fileToUpload.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            showValidationError("Fichier interdit", "Cloudinary accepte ici seulement les fichiers PDF.");
            return;
        }

        Path filePath = fileToUpload.toPath();
        runApiTask("Envoi du fichier vers Cloudinary...", () -> {
            UploadResult upload = new FileStorageApiService().uploadCourseFile(filePath);
            return "Envoi Cloudinary termin\u00e9 : " + upload.secureUrl();
        });
    }

    @FXML
    private void handleChooseSummaryFile(ActionEvent event) {
        if (!requireStudentMode()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir le resume du cours");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Resume (PDF, Word, image)", "*.pdf", "*.docx", "*.doc", "*.png", "*.jpg", "*.jpeg", "*.webp"));
        selectedSummaryFile = fileChooser.showOpenDialog(apiCloudUrlField.getScene().getWindow());
        if (selectedSummaryFile != null) {
            apiCloudUrlField.setText(selectedSummaryFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleUploadStudentSummary(ActionEvent event) {
        if (!requireStudentMode()) {
            return;
        }
        StudentRecord student = getSelectedStudent();
        if (student == null) {
            showValidationError("Etudiant obligatoire", "Selectionnez un etudiant avant d'envoyer un resume.");
            return;
        }
        Cours course = getSelectedCourse();
        if (course == null) {
            showValidationError("Cours obligatoire", "Selectionnez un cours avant d'envoyer un resume.");
            return;
        }
        if (!sameCourse(student.getCourse(), course)) {
            showValidationError("Cours incorrect", "L'etudiant selectionne doit etre inscrit au cours choisi.");
            return;
        }
        if (selectedSummaryFile == null) {
            showValidationError("Fichier obligatoire", "Choisissez un fichier resume avant l'envoi.");
            return;
        }

        Path filePath = selectedSummaryFile.toPath();
        File summaryFile = selectedSummaryFile;
        runApiTask("Envoi du resume vers Cloudinary...", () -> {
            UploadResult upload = new FileStorageApiService().uploadStudentSummary(filePath);
            CourseSummary summary = new CourseSummary(
                    studentService.nextSummaryId(),
                    student.getId(),
                    student.getFullName(),
                    student.getEmail(),
                    course.getId(),
                    valueOrFallback(course.getTitre(), student.getCourse()),
                    upload.secureUrl(),
                    summaryFile.getName(),
                    LocalDateTime.now(),
                    "envoye"
            );
            studentService.addSummary(summary);
            submissionService.addSubmission(new StudentSubmission(
                    student.getEmail(),
                    student.getFullName(),
                    valueOrFallback(course.getTitre(), student.getCourse()),
                    summaryFile.getName(),
                    upload.secureUrl(),
                    summary.getSentAt(),
                    "envoye"
            ));
            selectedSummaryFile = null;
            return "Resume envoye : " + upload.secureUrl();
        });
    }

    @FXML
    private void handleJoinZoomMeeting(ActionEvent event) {
        StudentRecord student = getSelectedStudent();
        Cours course = getSelectedCourse();
        int courseId = course == null ? findCourseIdByName(student == null ? "" : student.getCourse()) : course.getId();
        Meeting meeting = studentService.meetingsForCourse(courseId).stream()
                .reduce((first, second) -> second)
                .orElse(null);
        if (meeting == null || meeting.getJoinUrl().isBlank()) {
            showValidationError("Aucune reunion", "Aucune reunion n'a encore ete creee par l'enseignant.");
            return;
        }
        openExternalUrl(meeting.getJoinUrl());
    }

    @FXML
    private void handleGenerateApiCourse(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        String idea = text(apiCourseIdeaArea);
        if (idea.isEmpty()) {
            showValidationError("Description obligatoire", "Decrivez le cours que vous voulez generer.");
            return;
        }

        setStatus("Generation du cours avec GroqAI...");
        generateCourseButton.setDisable(true);

        Task<GeneratedCourse> task = new Task<>() {
            @Override
            protected GeneratedCourse call() {
                GeneratedCourse generatedCourse = new CourseGenerationApiService().generateCourse(idea);
                coursService.ajouter(generatedCourse.cours());
                int courseId = generatedCourse.cours().getId();
                for (Chapitres chapitre : generatedCourse.chapitres()) {
                    chapitre.setCoursId(courseId);
                    chapitreService.ajouter(chapitre);
                }
                return generatedCourse;
            }
        };

        task.setOnSucceeded(success -> {
            GeneratedCourse generatedCourse = task.getValue();
            loadCoursesFromDb();
            selectCourseById(generatedCourse.cours().getId());
            apiCourseIdeaArea.clear();
            generateCourseButton.setDisable(!enseignantMode);
            setStatus("Cours genere et ajoute : " + generatedCourse.cours().getTitre());
        });
        task.setOnFailed(failure -> {
            generateCourseButton.setDisable(!enseignantMode);
            showError("Impossible de generer le cours", new RuntimeException(task.getException()));
        });

        Thread thread = new Thread(task, "course-generation-api-task");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleAdvancedEnrollment(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        String email = text(advancedStudentEmailField);
        if (course == null) {
            showValidationError("Cours obligatoire", "S\u00e9lectionnez un cours avant d'inscrire un \u00e9tudiant.");
            return;
        }
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "L'email de l'\u00e9tudiant est obligatoire pour l'inscription.");
            return;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email doit \u00eatre une adresse Gmail valide, par exemple nom@gmail.com.");
            return;
        }

        try {
            advancedPedagogyService.inscrireEtudiant(course.getId(), email);
            String message = "\u00c9tudiant inscrit au cours " + course.getTitre() + " : " + email;
            advancedResultLabel.setText(message);
            setStatus(message);
        } catch (RuntimeException e) {
            showError("Impossible d'inscrire l'\u00e9tudiant", e);
        }
    }

    @FXML
    private void handleAdvancedProgress(ActionEvent event) {
        if (!requireStudentModeForProgress()) {
            return;
        }

        Chapitres chapter = getSelectedChapter();
        String email = text(advancedStudentEmailField);
        String progress = comboValueOrDefault(advancedProgressCombo, "");
        if (chapter == null) {
            showValidationError("Chapitre obligatoire", "S\u00e9lectionnez un chapitre avant d'enregistrer la progression.");
            return;
        }
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "L'email de l'\u00e9tudiant est obligatoire pour le suivi.");
            return;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email doit \u00eatre une adresse Gmail valide, par exemple nom@gmail.com.");
            return;
        }
        if (progress.isEmpty()) {
            showValidationError("Progression obligatoire", "Choisissez un statut de progression.");
            return;
        }
        StudentRecord student = findStudentRecordByEmail(email);
        if (student == null) {
            showValidationError("Etudiant introuvable", "Etudiant introuvable avec cet email.");
            return;
        }

        try {
            advancedPedagogyService.enregistrerProgression(chapter.getId(), email, progress);
            progressService.updateProgressByStudentEmail(email, String.valueOf(chapter.getId()), progress);
            updateStudentChapterProgress(student, chapter, progress);
            String message = "Progression enregistr\u00e9e pour " + email + " : " + progress;
            advancedResultLabel.setText(message);
            setStatus(message);
            studentListView.refresh();
            populateStudentForm(student);
        } catch (RuntimeException e) {
            showError("Impossible d'enregistrer la progression", e);
        }
    }

    @FXML
    private void handleAdvancedRecommendation(ActionEvent event) {
        if (!requireStudentModeForProgress()) {
            return;
        }

        Chapitres chapter = getSelectedChapter();
        String email = text(advancedStudentEmailField);
        if (chapter == null) {
            showValidationError("Chapitre obligatoire", "Selectionnez un chapitre avant de le recommander.");
            return;
        }
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "L'email de l'etudiant est obligatoire pour recommander ce chapitre.");
            return;
        }
        if (!isValidGmail(email)) {
            showValidationError("Email invalide", "L'email doit etre une adresse Gmail valide, par exemple nom@gmail.com.");
            return;
        }

        try {
            int count = advancedPedagogyService.recommanderChapitre(chapter.getId(), email);
            String message = count == 1
                    ? "Ce chapitre est recommande par 1 etudiant."
                    : "Ce chapitre est recommande par " + count + " etudiants.";
            advancedResultLabel.setText(message);
            setStatus(message);
        } catch (RuntimeException e) {
            showError("Impossible de recommander le chapitre", e);
        }
    }

    private void runApiTask(String loadingMessage, Supplier<String> action) {
        setStatus(loadingMessage);
        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return action.get();
            }
        };

        task.setOnSucceeded(event -> {
            String message = task.getValue();
            setStatus(message);
            if (message.contains("Resume envoye : ")) {
                apiCloudUrlField.setText(message.replace("Resume envoye : ", ""));
            }
            if (message.contains("Envoi Cloudinary termin\u00e9 : ")) {
                apiCloudUrlField.setText(message.replace("Envoi Cloudinary termin\u00e9 : ", ""));
            }
        });
        task.setOnFailed(event -> showError("Erreur services connectes", new RuntimeException(task.getException())));

        Thread thread = new Thread(task, "api-service-task");
        thread.setDaemon(true);
        thread.start();
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void openChapterReader(Chapitres chapter) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lecteur de chapitre");
        dialog.setHeaderText(valueOrFallback(chapter.getTitre(), "Chapitre sans titre"));
        dialog.getDialogPane().getStyleClass().add("dialog-theme");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label orderLabel = new Label("Chapitre " + chapter.getOrdre());
        orderLabel.getStyleClass().add("reader-order-badge");

        Label titleLabel = new Label(valueOrFallback(chapter.getTitre(), "Chapitre sans titre"));
        titleLabel.getStyleClass().add("reader-title");
        titleLabel.setWrapText(true);

        TextArea contentArea = new TextArea(valueOrFallback(chapter.getContenu(), ""));
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(16);
        contentArea.getStyleClass().add("reader-content-area");

        Button downloadButton = new Button("T\u00e9l\u00e9charger le chapitre");
        downloadButton.getStyleClass().add("reader-download-btn");
        downloadButton.setOnAction(event -> downloadChapterToFile(chapter, dialog.getDialogPane()));

        HBox header = new HBox(10, orderLabel, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("reader-header");

        HBox actions = new HBox(downloadButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("reader-actions");

        VBox content = new VBox(14, header, contentArea, actions);
        content.getStyleClass().add("reader-dialog-content");
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void setEnseignantMode(boolean enabled) {
        enseignantMode = enabled;
        currentUserRole = enabled ? "TEACHER" : "STUDENT";
        if (enseignantModeToggle != null) {
            enseignantModeToggle.setSelected(enabled);
            enseignantModeToggle.setText(enabled ? "Enseignant activ\u00e9" : "Enseignant d\u00e9sactiv\u00e9");
            enseignantModeToggle.getStyleClass().removeAll("mode-toggle-active", "mode-toggle-user");
            enseignantModeToggle.getStyleClass().add(enabled ? "mode-toggle-active" : "mode-toggle-user");
        }

        addCourseButton.setDisable(!enabled);
        editCourseButton.setDisable(!enabled);
        deleteCourseButton.setDisable(!enabled);
        addChapterButton.setDisable(!enabled);
        editChapterButton.setDisable(!enabled);
        deleteChapterButton.setDisable(!enabled);
        saveChapterButton.setDisable(!enabled);
        if (generateCourseButton != null) {
            generateCourseButton.setDisable(!enabled);
        }
        if (joinZoomButton != null) {
            joinZoomButton.setVisible(true);
            joinZoomButton.setManaged(true);
            joinZoomButton.setDisable(false);
        }
        if (createZoomButton != null) {
            createZoomButton.setVisible(enabled);
            createZoomButton.setManaged(enabled);
            createZoomButton.setDisable(!enabled);
        }
        if (chooseSummaryFileButton != null) {
            chooseSummaryFileButton.setDisable(enabled);
        }
        if (uploadSummaryButton != null) {
            uploadSummaryButton.setDisable(enabled);
        }
        courseEditorBox.setVisible(enabled);
        courseEditorBox.setManaged(enabled);

        courseTitleField.setDisable(!enabled);
        courseCategoryCombo.setDisable(!enabled);
        courseLevelCombo.setDisable(!enabled);
        courseDescriptionArea.setDisable(!enabled);
        if (apiCourseIdeaArea != null) {
            apiCourseIdeaArea.setDisable(!enabled);
        }
        chapterOrderField.setDisable(!enabled);
        chapterTitleField.setDisable(!enabled);
        chapterContentArea.setDisable(!enabled);
        chapterPdfField.setDisable(!enabled);
        chooseChapterPdfButton.setDisable(!enabled);
        openChapterPdfButton.setDisable(false);

        renduFileField.setDisable(enabled);
        chooseRenduFileButton.setDisable(enabled);
        submitRenduButton.setDisable(enabled);
        advancedProgressBox.setDisable(enabled);
        enseignantEvaluationBox.setVisible(enabled);
        enseignantEvaluationBox.setManaged(enabled);
        ratingCombo.setDisable(!enabled);
        feedbackCommentArea.setDisable(!enabled);
        submitEnseignantGradeButton.setVisible(enabled);
        submitEnseignantGradeButton.setManaged(enabled);
        submitEnseignantGradeButton.setDisable(!enabled);

        setStatus(enabled
                ? "Mode enseignant activ\u00e9. La gestion des cours et chapitres est disponible."
                : "Mode utilisateur activ\u00e9. Vous pouvez lire les chapitres, envoyer des avis, d\u00e9poser des rendus et t\u00e9l\u00e9charger.");
        refreshModeDependentStatus();
        if (studentListView != null) {
            studentListView.refresh();
        }
        populateStudentForm(getSelectedStudent());
    }

    private void updateRenduStatus(Chapitres chapter) {
        String email = text(advancedStudentEmailField);
        boolean hasRendu = chapter != null && !email.isBlank() && hasLocalRenduForStudentChapter(email, chapter);

        if (enseignantRenduStatusLabel != null) {
            enseignantRenduStatusLabel.setText(hasRendu
                    ? "Rendu soumis pour ce chapitre. Vous pouvez enregistrer une note."
                    : "Aucun rendu envoye pour cet etudiant. Vous pouvez quand meme enregistrer une note.");
        }
        if (renduAttemptLabel != null) {
            renduAttemptLabel.setText(hasRendu
                    ? "Tu ne peux mettre qu'un seul rendu. Tentative deja utilisee."
                    : "Il reste une tentative. Tu ne peux mettre qu'un seul rendu.");
        }

        boolean studentCanSubmit = !enseignantMode && chapter != null && !email.isBlank() && isValidEmail(email) && !hasRendu;
        if (renduFileField != null) {
            renduFileField.setDisable(!studentCanSubmit);
        }
        if (chooseRenduFileButton != null) {
            chooseRenduFileButton.setDisable(!studentCanSubmit);
        }
        if (submitRenduButton != null) {
            submitRenduButton.setDisable(!studentCanSubmit);
        }
    }

    private void refreshModeDependentStatus() {
        updateRenduStatus(getSelectedChapter());
    }

    private boolean requireEnseignantMode() {
        if (enseignantMode) {
            return true;
        }

        showValidationError("Action interdite", "Le mode enseignant est d\u00e9sactiv\u00e9. Cette action est verrouill\u00e9e.");
        return false;
    }

    private boolean requireStudentModeForRendu() {
        if (!enseignantMode) {
            return true;
        }

        showValidationError("Action interdite", "Le rendu est reserve au mode etudiant. Desactivez le mode enseignant pour envoyer un rendu.");
        return false;
    }

    private boolean requireStudentMode() {
        if (!enseignantMode) {
            return true;
        }

        showValidationError("Action interdite", "Passez en mode etudiant pour envoyer un resume ou rejoindre une reunion.");
        return false;
    }

    private boolean requireStudentModeForProgress() {
        if (!enseignantMode) {
            return true;
        }

        showValidationError("Action interdite", "Le suivi de progres est reserve au mode etudiant. Desactivez le mode enseignant pour l'utiliser.");
        return false;
    }

    private void initializeStudentManagement() {
        if (studentListView == null) {
            return;
        }

        if (studentFilterCombo != null) {
            studentFilterCombo.getItems().setAll("Tous", "Present", "Non present", "Male", "Female", "Moyenne faible", "Moyenne excellente");
            studentFilterCombo.setValue("Tous");
        }

        allStudents.setAll(
                createStudent("Sara Ben Ali", "sara@gmail.com", "Female", "Peinture", "Present",
                        List.of(4.0, 3.5, 4.5, 4.25), List.of("Termine", "Termine", "Termine", "En cours")),
                createStudent("Ahmed Mansour", "ahmed@gmail.com", "Male", "Manga", "Non present",
                        List.of(2.0, 3.0, 1.5, 0.0), List.of("En cours", "Non commence", "Non commence", "Non commence")),
                createStudent("Lina Trabelsi", "lina@gmail.com", "Female", "Poterie decorative", "Present",
                        List.of(3.75, 4.0, 3.5, 4.25), List.of("Termine", "Termine", "En cours", "Termine")),
                createStudent("Youssef Khaled", "youssef@gmail.com", "Male", "Design graphique", "Present",
                        List.of(4.75, 4.25, 4.0, 5.0), List.of("Termine", "Termine", "Termine", "Termine"))
        );
        applyStudentFilter();
        if (!filteredStudents.isEmpty()) {
            studentListView.getSelectionModel().selectFirst();
        }
        syncStudentServices();
        updateStudentStats();
    }

    private void syncStudentServices() {
        studentService.setStudents(allStudents.stream()
                .map(student -> new Student(
                        student.getId(),
                        student.getFullName(),
                        student.getEmail(),
                        student.getGender(),
                        student.getCourse(),
                        student.getStatus(),
                        student.getEnrollmentDate()
                ))
                .toList());
        gradeService.clear();
        progressService.clear();
        for (StudentRecord student : allStudents) {
            saveTrackingByEmail(student, "");
        }
    }

    private StudentRecord createStudent(String fullName, String email, String gender, String course, String status,
                                        List<Double> notes, List<String> progressions) {
        StudentRecord student = new StudentRecord(nextStudentId(), fullName, email, gender, course, status, LocalDate.now());
        List<String> chapterTitles = sampleChapterTitles(course);
        for (int i = 0; i < chapterTitles.size(); i++) {
            double note = i < notes.size() ? notes.get(i) : 0.0;
            String progress = i < progressions.size() ? progressions.get(i) : "Non commence";
            student.getChapters().add(new ChapterEvaluation(chapterTitles.get(i), note, progress));
        }
        return student;
    }

    private int nextStudentId() {
        return studentIdSequence++;
    }

    private List<String> sampleChapterTitles(String course) {
        String lower = valueOrFallback(course, "").toLowerCase(Locale.ROOT);
        if (lower.contains("manga")) {
            return List.of("Introduction a l'univers du manga", "Conception des personnages", "Creation d'une histoire", "Techniques de dessin");
        }
        if (lower.contains("design")) {
            return List.of("Bases de la composition", "Couleurs et typographie", "Creation d'une affiche", "Presentation du projet");
        }
        if (lower.contains("poterie")) {
            return List.of("Preparation de l'argile", "Modelage decoratif", "Textures et motifs", "Finition et cuisson");
        }
        return List.of("Introduction a la peinture", "Les couleurs et les melanges", "Les techniques de base", "Creation d'une oeuvre finale");
    }

    private void refreshStudentCourseOptions() {
    }

    private List<String> studentCourseOptions() {
        List<String> courseNames = allCourses.stream()
                .map(Cours::getTitre)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();
        if (courseNames.isEmpty()) {
            return List.of("Peinture", "Manga", "Poterie decorative", "Design graphique");
        }
        return courseNames;
    }

    private void applyStudentFilter() {
        if (studentSearchField == null) {
            return;
        }
        String query = text(studentSearchField).toLowerCase(Locale.ROOT);
        String filter = studentFilterCombo == null ? "Tous" : valueOrFallback(studentFilterCombo.getValue(), "Tous");
        StudentRecord selected = studentListView == null ? null : studentListView.getSelectionModel().getSelectedItem();
        filteredStudents.setAll(allStudents.filtered(student ->
                matchesStudentFilter(student, filter)
                        && (query.isBlank()
                        || student.getFullName().toLowerCase(Locale.ROOT).contains(query)
                        || student.getEmail().toLowerCase(Locale.ROOT).contains(query)
                        || student.getCourse().toLowerCase(Locale.ROOT).contains(query)
                        || student.getGender().toLowerCase(Locale.ROOT).contains(query)
                        || student.getStatus().toLowerCase(Locale.ROOT).contains(query)
                        || String.format(Locale.ROOT, "%.2f", student.getAverage()).contains(query))
        ));
        updateStudentStats();
        if (selected != null && filteredStudents.contains(selected)) {
            studentListView.getSelectionModel().select(selected);
        } else if (studentListView != null && !filteredStudents.isEmpty()) {
            studentListView.getSelectionModel().selectFirst();
        } else {
            populateStudentForm(null);
        }
    }

    private boolean matchesStudentFilter(StudentRecord student, String filter) {
        return switch (filter) {
            case "Present" -> "Present".equalsIgnoreCase(student.getStatus());
            case "Non present" -> "Non present".equalsIgnoreCase(student.getStatus());
            case "Male" -> "Male".equalsIgnoreCase(student.getGender());
            case "Female" -> "Female".equalsIgnoreCase(student.getGender());
            case "Moyenne faible" -> student.getAverage() < 2;
            case "Moyenne excellente" -> student.getAverage() >= 4.5;
            default -> true;
        };
    }

    @FXML
    private void handleAddStudent(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }
        showStudentFormModal(null);
    }

    @FXML
    private void handleEditStudent(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }
        StudentRecord selected = getSelectedStudent();
        if (selected == null) {
            showValidationError("Etudiant obligatoire", "Selectionnez un etudiant a modifier.");
            return;
        }
        showStudentFormModal(selected);
    }

    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }
        StudentRecord selected = getSelectedStudent();
        if (selected == null) {
            showValidationError("Etudiant obligatoire", "Selectionnez un etudiant a supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l'etudiant");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText("Voulez-vous supprimer " + selected.getFullName() + " ?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        allStudents.remove(selected);
        syncStudentServices();
        applyStudentFilter();
        setStatus("Etudiant supprime.");
    }

    @FXML
    private void handleToggleStudentAttendance(ActionEvent event) {
        if (!requireEnseignantMode()) {
            return;
        }
        StudentRecord selected = getSelectedStudent();
        if (selected == null) {
            showValidationError("Etudiant obligatoire", "Selectionnez un etudiant avant de changer la presence.");
            return;
        }
        selected.setStatus("Present".equalsIgnoreCase(selected.getStatus()) ? "Non present" : "Present");
        studentListView.refresh();
        populateStudentForm(selected);
        updateStudentStats();
        setStatus("Presence mise a jour : " + selected.getStatus());
    }

    @FXML
    private void handleShowStudentDetails(ActionEvent event) {
        showStudentDetails(getSelectedStudent());
    }

    @FXML
    private void handleFindStudentByEmail(ActionEvent event) {
        String email = text(studentEmailLookupField);
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "Email de l'etudiant obligatoire.");
            return;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email est invalide.");
            return;
        }
        if (!selectStudentByEmail(email, true)) {
            showValidationError("Etudiant introuvable", "Etudiant introuvable avec cet email.");
        }
    }

    private boolean validateStudentInput(TextField nameField, TextField emailField,
                                         ComboBox<String> genderCombo, ComboBox<String> courseCombo,
                                         ComboBox<String> statusCombo) {
        if (text(nameField).isEmpty()) {
            showValidationError("Nom obligatoire", "Le nom complet de l'etudiant est obligatoire.");
            return false;
        }
        if (text(nameField).length() < 3 || !isValidPersonName(text(nameField))) {
            showValidationError("Nom invalide", "Le nom doit contenir au moins 3 caracteres, avec des lettres et des espaces.");
            return false;
        }
        if (!isValidGmail(text(emailField))) {
            showValidationError("Email invalide", "L'email doit se terminer par @gmail.com.");
            return false;
        }
        if (genderCombo.getValue() == null || courseCombo.getValue() == null || statusCombo.getValue() == null) {
            showValidationError("Champs obligatoires", "Veuillez choisir un genre, un cours et un statut de presence.");
            return false;
        }
        return true;
    }

    private StudentRecord getSelectedStudent() {
        return studentListView == null ? null : studentListView.getSelectionModel().getSelectedItem();
    }

    private StudentRecord requireStudentFromLookupEmail() {
        String email = text(studentEmailLookupField);
        if (email.isEmpty()) {
            showValidationError("Email obligatoire", "Email de l'etudiant obligatoire.");
            return null;
        }
        if (!isValidEmail(email)) {
            showValidationError("Email invalide", "L'email est invalide.");
            return null;
        }
        StudentRecord student = findStudentRecordByEmail(email);
        if (student == null) {
            showValidationError("Etudiant introuvable", "Etudiant introuvable avec cet email.");
            return null;
        }
        if (studentListView != null) {
            studentListView.getSelectionModel().select(student);
        }
        return student;
    }

    private boolean selectStudentByEmail(String email, boolean updateSearch) {
        StudentRecord student = findStudentRecordByEmail(email);
        if (student == null) {
            return false;
        }
        if (updateSearch && studentSearchField != null) {
            studentSearchField.setText(student.getEmail());
        }
        if (!filteredStudents.contains(student)) {
            filteredStudents.setAll(student);
        }
        studentListView.getSelectionModel().select(student);
        populateStudentForm(student);
        return true;
    }

    private StudentRecord findStudentRecordByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return allStudents.stream()
                .filter(student -> normalizeEmail(student.getEmail()).equals(normalizedEmail))
                .findFirst()
                .orElse(null);
    }

    private void saveTrackingByEmail(StudentRecord student, String comment) {
        String email = normalizeEmail(student.getEmail());
        for (ChapterEvaluation chapter : student.getChapters()) {
            gradeService.saveGradeByStudentEmail(email, chapter.getChapterKey(), chapter.getNote(), comment);
            progressService.updateProgressByStudentEmail(email, chapter.getChapterKey(), chapter.getProgress());
        }
    }

    private void updateStudentChapterGrade(StudentRecord student, Chapitres chapter, double note) {
        String title = valueOrFallback(chapter.getTitre(), "");
        student.getChapters().stream()
                .filter(evaluation -> evaluation.getChapterTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(
                        evaluation -> evaluation.setNote(note),
                        () -> student.getChapters().add(new ChapterEvaluation(title, note, "Non commence"))
                );
    }

    private void updateStudentChapterProgress(StudentRecord student, Chapitres chapter, String progress) {
        String title = valueOrFallback(chapter.getTitre(), "");
        student.getChapters().stream()
                .filter(evaluation -> evaluation.getChapterTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(
                        evaluation -> evaluation.setProgress(progress),
                        () -> student.getChapters().add(new ChapterEvaluation(title, 0, progress))
                );
    }

    private String submissionStatusText(StudentRecord student) {
        List<StudentSubmission> submissions = submissionService.getSubmissionsByStudentEmail(student.getEmail());
        if (submissions.isEmpty()) {
            return "Aucun rendu envoye pour cet etudiant.";
        }
        StudentSubmission latest = submissions.get(submissions.size() - 1);
        return "Rendu : " + latest.getStatus()
                + " | " + latest.getFileName()
                + " | " + latest.getSentAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private boolean hasLocalRenduForStudentChapter(String email, Chapitres chapter) {
        String chapterKey = chapterRenduKey(chapter);
        String normalizedEmail = normalizeEmail(email);
        return localRenduSubmissions.stream()
                .anyMatch(submission -> normalizeEmail(submission.getStudentEmail()).equals(normalizedEmail)
                        && submission.getCourseName().equals(chapterKey));
    }

    private void saveLocalRenduSubmission(StudentRecord student, Chapitres chapter, File file) {
        StudentSubmission submission = new StudentSubmission(
                student.getEmail(),
                student.getFullName(),
                chapterRenduKey(chapter),
                file.getName(),
                file.getAbsolutePath(),
                LocalDateTime.now(),
                "envoye"
        );
        localRenduSubmissions.add(submission);
        submissionService.addSubmission(submission);
    }

    private String chapterRenduKey(Chapitres chapter) {
        return "chapitre:" + chapter.getId();
    }

    private void showStudentFormModal(StudentRecord studentToEdit) {
        boolean editMode = studentToEdit != null;

        Stage modalStage = new Stage();
        Window ownerWindow = null;
        if (studentManagementSection != null && studentManagementSection.getScene() != null) {
            ownerWindow = studentManagementSection.getScene().getWindow();
            modalStage.initOwner(ownerWindow);
        }
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.setTitle(editMode ? "Modifier \u00e9tudiant" : "Ajouter \u00e9tudiant");

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modal-overlay");

        VBox modal = new VBox(16);
        modal.getStyleClass().add("student-modal");
        modal.setMaxWidth(650);
        modal.setMaxHeight(560);
        modal.setOnMouseClicked(event -> event.consume());

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("modal-close");
        closeButton.setOnAction(event -> modalStage.close());

        Label title = new Label(editMode ? "Modifier \u00e9tudiant" : "Ajouter \u00e9tudiant");
        title.getStyleClass().add("student-detail-name");
        HBox header = new HBox(12, title, new Region(), closeButton);
        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField(editMode ? studentToEdit.getFullName() : "");
        nameField.setPromptText("Nom complet");
        nameField.getStyleClass().add("field-input");
        nameField.setTextFormatter(new TextFormatter<>(lettersOnlyFilterWithDialog("Nom complet")));

        TextField emailField = new TextField(editMode ? studentToEdit.getEmail() : "");
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("field-input");
        emailField.setTextFormatter(new TextFormatter<>(gmailEmailTypingFilter()));

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().setAll("Male", "Female");
        genderCombo.setPromptText("Genre");
        genderCombo.setMaxWidth(Double.MAX_VALUE);
        genderCombo.getStyleClass().add("field-input");
        genderCombo.setValue(editMode ? studentToEdit.getGender() : "Female");

        ComboBox<String> courseCombo = new ComboBox<>();
        courseCombo.getItems().setAll(studentCourseOptions());
        courseCombo.setPromptText("Cours choisi");
        courseCombo.setMaxWidth(Double.MAX_VALUE);
        courseCombo.getStyleClass().add("field-input");
        courseCombo.setValue(editMode ? studentToEdit.getCourse() : courseCombo.getItems().isEmpty() ? null : courseCombo.getItems().get(0));

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().setAll("Present", "Non present");
        statusCombo.setPromptText("Statut");
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.getStyleClass().add("field-input");
        statusCombo.setValue(editMode ? studentToEdit.getStatus() : "Present");

        GridPane form = new GridPane();
        form.getStyleClass().add("student-form");
        form.setHgap(12);
        form.setVgap(12);
        form.getColumnConstraints().addAll(percentColumn(), percentColumn());
        form.add(nameField, 0, 0);
        form.add(emailField, 1, 0);
        form.add(genderCombo, 0, 1);
        form.add(statusCombo, 1, 1);
        form.add(courseCombo, 0, 2, 2, 1);

        Button saveButton = new Button(editMode ? "Enregistrer les modifications" : "Ajouter \u00e9tudiant");
        saveButton.getStyleClass().add("btn-primary");
        saveButton.setOnAction(event -> {
            if (!validateStudentInput(nameField, emailField, genderCombo, courseCombo, statusCombo)) {
                return;
            }
            if (editMode) {
                studentToEdit.setFullName(text(nameField));
                studentToEdit.setEmail(text(emailField));
                studentToEdit.setGender(comboValueOrDefault(genderCombo, studentToEdit.getGender()));
                studentToEdit.setCourse(comboValueOrDefault(courseCombo, studentToEdit.getCourse()));
                studentToEdit.setStatus(comboValueOrDefault(statusCombo, studentToEdit.getStatus()));
                ensureStudentChapters(studentToEdit);
                syncStudentServices();
                studentListView.refresh();
                populateStudentForm(studentToEdit);
                updateStudentStats();
                setStatus("Etudiant modifie : " + studentToEdit.getFullName());
            } else {
                StudentRecord newStudent = createStudent(
                        text(nameField),
                        text(emailField),
                        comboValueOrDefault(genderCombo, "Female"),
                        comboValueOrDefault(courseCombo, "Peinture"),
                        comboValueOrDefault(statusCombo, "Present"),
                        List.of(0.0, 0.0, 0.0, 0.0),
                        List.of("Non commence", "Non commence", "Non commence", "Non commence")
                );
                allStudents.add(newStudent);
                syncStudentServices();
                applyStudentFilter();
                studentListView.getSelectionModel().select(newStudent);
                setStatus("Etudiant ajoute : " + newStudent.getFullName());
            }
            modalStage.close();
        });

        HBox actions = new HBox(10, saveButton);
        actions.setAlignment(Pos.CENTER_RIGHT);
        modal.getChildren().addAll(header, form, actions);
        overlay.getChildren().add(modal);
        overlay.setOnMouseClicked(event -> modalStage.close());

        double sceneWidth = ownerWindow == null ? 760 : Math.max(760, ownerWindow.getWidth());
        double sceneHeight = ownerWindow == null ? 620 : Math.max(620, ownerWindow.getHeight());
        Scene scene = new Scene(overlay, sceneWidth, sceneHeight);
        URL css = getClass().getResource("/org/example/ui/course-manager.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        modalStage.setScene(scene);
        if (ownerWindow != null) {
            modalStage.setX(ownerWindow.getX());
            modalStage.setY(ownerWindow.getY());
        }
        modalStage.showAndWait();
    }

    private void showStudentDetails(StudentRecord student) {
        if (student == null) {
            showValidationError("Etudiant obligatoire", "Selectionnez un etudiant avant d'afficher ses details.");
            return;
        }

        populateStudentForm(student);
        ensureStudentChapters(student);

        Stage modalStage = new Stage();
        Window ownerWindow = null;
        if (studentManagementSection != null && studentManagementSection.getScene() != null) {
            ownerWindow = studentManagementSection.getScene().getWindow();
            modalStage.initOwner(ownerWindow);
        }
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.setTitle("Details etudiant");

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("modal-overlay");

        VBox modal = new VBox(16);
        modal.getStyleClass().add("student-modal");
        modal.setMaxWidth(680);
        modal.setMaxHeight(690);
        modal.setOnMouseClicked(event -> event.consume());

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("modal-close");
        closeButton.setOnAction(event -> modalStage.close());

        Label name = new Label(student.getFullName());
        name.getStyleClass().add("student-detail-name");
        Label email = new Label(student.getEmail());
        email.getStyleClass().add("student-detail-muted");
        VBox identity = new VBox(4, name, email);
        HBox header = new HBox(12, identity, new Region(), closeButton);
        HBox.setHgrow(identity, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane infoGrid = new GridPane();
        infoGrid.getStyleClass().add("student-info-grid");
        infoGrid.setHgap(14);
        infoGrid.setVgap(14);
        infoGrid.getColumnConstraints().addAll(percentColumn(), percentColumn());
        infoGrid.add(studentInfoCell("Genre", student.getGender()), 0, 0);
        infoGrid.add(studentInfoCell("Statut", student.getStatus()), 1, 0);
        infoGrid.add(studentInfoCell("Cours inscrit", student.getCourse()), 0, 1);
        infoGrid.add(studentInfoCell("Niveau", averageLevel(student.getAverage())), 1, 1);

        double progress = student.getProgressRatio();
        Label average = new Label(String.format(Locale.ROOT, "Moyenne : %.2f / 5", student.getAverage()));
        average.getStyleClass().add("student-average-text");
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("student-progress-bar");
        Label progressText = new Label(student.getCompletedChapters() + " chapitre(s) termine(s) sur "
                + student.getChapters().size() + " = " + Math.round(progress * 100) + "%");
        progressText.getStyleClass().add("student-detail-muted");
        Label submissionText = new Label(submissionStatusText(student));
        submissionText.getStyleClass().add("student-detail-muted");
        submissionText.setWrapText(true);
        VBox averageBox = new VBox(8, average, progressBar, progressText, submissionText);
        averageBox.getStyleClass().add("student-average-box");

        Label notesTitle = new Label("Notes des chapitres");
        notesTitle.getStyleClass().add("student-block-title");
        VBox notesBox = new VBox(10);
        notesBox.getStyleClass().add("chapter-notes");
        renderModalChapterNotes(student, notesBox);

        Button saveNotesButton = new Button("Enregistrer les notes");
        saveNotesButton.getStyleClass().add("btn-primary");
        saveNotesButton.setDisable(!enseignantMode);
        saveNotesButton.setOnAction(event -> saveStudentNotesFromBox(student, notesBox));

        Label recommendationTitle = new Label("Recommandation");
        recommendationTitle.getStyleClass().add("student-block-title");
        Label recommendation = new Label(studentRecommendation(student));
        recommendation.getStyleClass().add("feature-text");
        recommendation.setWrapText(true);
        VBox recommendationBox = new VBox(6, recommendationTitle, recommendation);
        recommendationBox.getStyleClass().add("recommendation-card");

        ScrollPane scrollPane = new ScrollPane(new VBox(16, header, infoGrid, averageBox, notesTitle, notesBox, saveNotesButton, recommendationBox));
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("modal-scroll");
        modal.getChildren().add(scrollPane);

        overlay.getChildren().add(modal);
        overlay.setOnMouseClicked(event -> modalStage.close());

        double sceneWidth = ownerWindow == null ? 820 : Math.max(820, ownerWindow.getWidth());
        double sceneHeight = ownerWindow == null ? 720 : Math.max(720, ownerWindow.getHeight());
        Scene scene = new Scene(overlay, sceneWidth, sceneHeight);
        URL css = getClass().getResource("/org/example/ui/course-manager.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        modalStage.setScene(scene);
        if (ownerWindow != null) {
            modalStage.setX(ownerWindow.getX());
            modalStage.setY(ownerWindow.getY());
        }
        modalStage.showAndWait();
    }

    private void populateStudentForm(StudentRecord student) {
        if (student == null) {
            return;
        }
        if (studentEmailLookupField != null && !text(studentEmailLookupField).equalsIgnoreCase(student.getEmail())) {
            studentEmailLookupField.setText(student.getEmail());
        }
    }

    private ColumnConstraints percentColumn() {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(50);
        return column;
    }

    private VBox studentInfoCell(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("student-info-label");
        Label value = new Label(valueOrFallback(valueText, "-"));
        value.getStyleClass().add("student-info-value");
        value.setWrapText(true);
        VBox cell = new VBox(6, label, value);
        cell.getStyleClass().add("student-info-cell");
        return cell;
    }

    private void renderModalChapterNotes(StudentRecord student, VBox notesBox) {
        notesBox.getChildren().clear();
        for (ChapterEvaluation evaluation : student.getChapters()) {
            Label title = new Label(evaluation.getChapterTitle());
            title.getStyleClass().add("student-note-title");
            title.setWrapText(true);
            HBox.setHgrow(title, Priority.ALWAYS);

            TextField noteField = new TextField(String.valueOf((int) Math.round(evaluation.getNote())));
            noteField.getStyleClass().addAll("field-input", "student-note-input");
            noteField.setPrefWidth(76);
            noteField.setDisable(!enseignantMode);
            noteField.setTextFormatter(new TextFormatter<>(integerNoteFilter()));

            Label progressLabel = new Label(valueOrFallback(evaluation.getProgress(), "Non commence"));
            progressLabel.getStyleClass().add("modern-card-chip-muted");

            HBox row = new HBox(10, title, noteField, new Label("/5"), progressLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("chapter-note");
            row.setUserData(evaluation);
            notesBox.getChildren().add(row);
        }
    }

    private void saveStudentNotesFromBox(StudentRecord student, VBox notesBox) {
        for (Node node : notesBox.getChildren()) {
            if (!(node instanceof HBox row) || row.getUserData() == null) {
                continue;
            }
            ChapterEvaluation evaluation = (ChapterEvaluation) row.getUserData();
            TextField noteField = (TextField) row.lookup(".student-note-input");
            if (noteField == null) {
                continue;
            }
            Integer note = parseRequiredIntegerNote(noteField.getText());
            if (note == null) {
                return;
            }
            evaluation.setNote(note);
            gradeService.saveGradeByStudentEmail(student.getEmail(), evaluation.getChapterKey(), note, text(feedbackCommentArea));
        }
        studentListView.refresh();
        populateStudentForm(student);
        updateStudentStats();
        setStatus("Notes enregistrees pour : " + student.getEmail());
    }

    private void ensureStudentChapters(StudentRecord student) {
        if (!student.getChapters().isEmpty()) {
            return;
        }
        for (String title : sampleChapterTitles(student.getCourse())) {
            student.getChapters().add(new ChapterEvaluation(title, 0, "Non commence"));
        }
    }

    private void updateStudentStats() {
        if (totalStudentsLabel == null) {
            return;
        }
        totalStudentsLabel.setText(String.valueOf(allStudents.size()));
        long present = allStudents.stream().filter(student -> "Present".equalsIgnoreCase(student.getStatus())).count();
        activeStudentsLabel.setText(String.valueOf(present));
        if (absentStudentsLabel != null) {
            absentStudentsLabel.setText(String.valueOf(allStudents.size() - present));
        }
        double average = allStudents.stream().mapToDouble(StudentRecord::getAverage).average().orElse(0);
        globalAverageLabel.setText(String.format(Locale.ROOT, "%.2f / 5", average));
        if (completedChaptersLabel != null) {
            int completed = allStudents.stream().mapToInt(StudentRecord::getCompletedChapters).sum();
            completedChaptersLabel.setText(String.valueOf(completed));
        }
        updateReportStats();
    }

    private void updateReportStats() {
        if (reportCoursesLabel == null) {
            return;
        }
        long present = allStudents.stream().filter(student -> "Present".equalsIgnoreCase(student.getStatus())).count();
        double average = allStudents.stream().mapToDouble(StudentRecord::getAverage).average().orElse(0);
        reportCoursesLabel.setText(String.valueOf(allCourses.size()));
        reportStudentsLabel.setText(String.valueOf(allStudents.size()));
        reportPresentStudentsLabel.setText(String.valueOf(present));
        reportAverageLabel.setText(String.format(Locale.ROOT, "%.2f / 5", average));
    }

    private String studentRecommendation(StudentRecord student) {
        boolean hasUnfinished = student.getChapters().stream().anyMatch(chapter -> !"Termine".equalsIgnoreCase(chapter.getProgress()));
        boolean hasWeakChapter = student.getChapters().stream().anyMatch(chapter -> chapter.getNote() < 2);
        if (hasUnfinished) {
            return "Completer les chapitres restants avant l'evaluation finale.";
        }
        if (student.getAverage() < 2 || hasWeakChapter) {
            return "Cet etudiant doit revoir les chapitres ou la note est inferieure a 2/5.";
        }
        return "Cet etudiant peut passer au niveau suivant.";
    }

    private String averageLevel(double average) {
        if (average >= 4.5) {
            return "Excellent";
        }
        if (average >= 4) {
            return "Tres bien";
        }
        if (average >= 3) {
            return "Bien";
        }
        if (average >= 2) {
            return "Moyen";
        }
        return "Faible";
    }

    private String averageLevelStyle(double average) {
        if (average >= 4.5) {
            return "level-excellent";
        }
        if (average >= 4) {
            return "level-good";
        }
        if (average >= 2) {
            return "level-medium";
        }
        return "level-weak";
    }

    private static double parseDoubleInRange(String raw, double min, double max) {
        try {
            double parsed = Double.parseDouble(raw.trim().replace(',', '.'));
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception ignored) {
            return min;
        }
    }

    private void wireLists() {
        courseListView.setItems(filteredCourses);
        chapterListView.setItems(visibleChapters);
        courseListView.setCellFactory(list -> new CourseCardCell());
        chapterListView.setCellFactory(list -> new ChapterCardCell());
        if (studentListView != null) {
            studentListView.setItems(filteredStudents);
            studentListView.setCellFactory(list -> new StudentCardCell());
        }
    }

    private void wireListeners() {
        courseListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            showCourse(selected);
            loadChaptersForCourse(selected);
        });

        chapterListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            showChapter(selected);
        });

        chapterListView.setOnMouseClicked(event -> {
            if (!enseignantMode && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                Chapitres selected = getSelectedChapter();
                if (selected != null) {
                    openChapterReader(selected);
                }
            }
        });

        courseSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyCourseFilter());
        chapterSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyChapterFilter());
        if (studentSearchField != null) {
            studentSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyStudentFilter());
        }
        if (studentEmailLookupField != null) {
            studentEmailLookupField.textProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && isValidEmail(newValue)) {
                    selectStudentByEmail(newValue, false);
                }
            });
        }
        if (studentFilterCombo != null) {
            studentFilterCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyStudentFilter());
        }
        if (studentListView != null) {
            studentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> populateStudentForm(selected));
        }
    }

    private void wireInputFilters() {
        courseSearchField.setTextFormatter(new TextFormatter<>(freeTextFilter()));
        chapterSearchField.setTextFormatter(new TextFormatter<>(freeTextFilter()));
        courseTitleField.setTextFormatter(new TextFormatter<>(freeTextFilterWithDialog("Titre du cours")));
        chapterTitleField.setTextFormatter(new TextFormatter<>(freeTextFilterWithDialog("Titre du chapitre")));
        chapterOrderField.setTextFormatter(new TextFormatter<>(digitsOnlyFilterWithDialog("Ordre du chapitre")));
        apiStudentNameField.setTextFormatter(new TextFormatter<>(lettersOnlyFilterWithDialog("Nom de l'\u00e9tudiant")));
        apiStudentEmailField.setTextFormatter(new TextFormatter<>(gmailEmailTypingFilter()));
        if (apiMeetingTopicField != null) {
            apiMeetingTopicField.setTextFormatter(new TextFormatter<>(freeTextFilter()));
        }
        if (apiMeetingDurationField != null) {
            apiMeetingDurationField.setTextFormatter(new TextFormatter<>(digitsOnlyFilter()));
        }
        advancedStudentEmailField.setTextFormatter(new TextFormatter<>(emailTypingFilter()));
        advancedStudentEmailField.textProperty().addListener((obs, oldValue, newValue) ->
                updateRenduStatus(getSelectedChapter()));
        if (studentSearchField != null) {
            studentSearchField.setTextFormatter(new TextFormatter<>(freeTextFilter()));
        }
        if (studentEmailLookupField != null) {
            studentEmailLookupField.setTextFormatter(new TextFormatter<>(emailTypingFilter()));
        }
    }

    private void ensureCategoryOptions() {
        if (courseCategoryCombo.getItems().isEmpty()) {
            courseCategoryCombo.getItems().setAll(
                    "Peinture",
                    "Dessin",
                    "Poterie",
                    "C\u00e9ramique",
                    "Sculpture",
                    "Aquarelle",
                    "Calligraphie",
                    "Photographie",
                    "Illustration digitale",
                    "Design graphique"
            );
        }
    }

    private void ensureLevelOptions() {
        if (courseLevelCombo.getItems().isEmpty()) {
        courseLevelCombo.getItems().setAll("D\u00e9butant", "Interm\u00e9diaire", "Avanc\u00e9");
        }
    }

    private void configureEvaluationControls() {
        if (ratingCombo != null) {
            ratingCombo.setEditable(false);
            ratingCombo.getItems().setAll("0", "1", "2", "3", "4", "5");
        }
    }

    private void applyCourseFilter() {
        String query = courseSearchField == null ? "" : text(courseSearchField).toLowerCase(Locale.ROOT);
        Cours selected = getSelectedCourse();

        filteredCourses.setAll(allCourses.filtered(c -> {
            boolean matchTitle = c.getTitre() != null && c.getTitre().toLowerCase(Locale.ROOT).contains(query);
            boolean matchCat = c.getCategorie() != null && c.getCategorie().toLowerCase(Locale.ROOT).contains(query);
            return query.isBlank() || matchTitle || matchCat;
        }));

        updateCourseCount();
        renderCourseCards();
        
        if (selected != null && filteredCourses.contains(selected)) {
            selectCourse(selected);
        } else {
            selectedCourse = null;
            clearCourseForm();
            allChaptersForCourse.clear();
            visibleChapters.clear();
            updateChapterCount();
            clearChapterForm();
            clearStudentSpace();
            showChapterWorkspace(false);
        }
    }

    private void applyChapterFilter() {
        String query = chapterSearchField == null ? "" : text(chapterSearchField).toLowerCase(Locale.ROOT);
        Chapitres selected = getSelectedChapter();

        visibleChapters.setAll(allChaptersForCourse.filtered(ch -> {
            boolean matchTitle = ch.getTitre() != null && ch.getTitre().toLowerCase(Locale.ROOT).contains(query);
            boolean matchContent = ch.getContenu() != null && ch.getContenu().toLowerCase(Locale.ROOT).contains(query);
            return query.isBlank() || matchTitle || matchContent;
        }));

        updateChapterCount();

        if (selected != null && visibleChapters.contains(selected)) {
            chapterListView.getSelectionModel().select(selected);
        } else if (!visibleChapters.isEmpty()) {
            chapterListView.getSelectionModel().selectFirst();
        } else {
            clearChapterForm();
            clearStudentSpace();
        }
    }

    private void renderCourseCards() {
        if (courseTilePane == null) {
            return;
        }

        courseTilePane.getChildren().clear();
        for (Cours course : filteredCourses) {
            courseTilePane.getChildren().add(createCourseTile(course));
        }
    }

    private Node createCourseTile(Cours course) {
        Label icon = new Label(courseIcon(course));
        icon.getStyleClass().add("course-tile-icon");

        Label title = new Label(valueOrFallback(course.getTitre(), "Cours sans titre"));
        title.getStyleClass().add("course-tile-title");
        title.setWrapText(true);

        Label category = new Label(valueOrFallback(course.getCategorie(), "Peinture"));
        category.getStyleClass().add("course-tile-chip");

        Label level = new Label(valueOrFallback(course.getNiveau(), "Debutant"));
        level.getStyleClass().add("course-tile-chip-muted");

        int chapterCount = countChapters(course);
        Label chapters = new Label(chapterCount + (chapterCount == 1 ? " chapitre" : " chapitres"));
        chapters.getStyleClass().add("course-tile-count");

        HBox chips = new HBox(8, category, level);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14, icon, title, chips, chapters);
        card.getStyleClass().add("course-tile");
        card.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }

            if (event.getClickCount() == 2) {
                selectCourse(course);
                openCourseChaptersWindow(course);
            } else {
                previewCourse(course);
            }
        });
        if (selectedCourse != null && selectedCourse.getId() == course.getId()) {
            card.getStyleClass().add("course-tile-selected");
        }
        return card;
    }

    private void selectCourse(Cours course) {
        selectedCourse = course;
        showCourse(course);
        loadChaptersForCourse(course);
        renderCourseCards();
    }

    private void previewCourse(Cours course) {
        selectedCourse = course;
        showCourse(course);
        showChapterWorkspace(false);
        renderCourseCards();
        setStatus("Double-cliquez sur la carte du cours pour ouvrir les chapitres : " + valueOrFallback(course.getTitre(), "cours"));
    }

    private void openCourseChaptersWindow(Cours course) {
        if (chapterWorkspacePane == null) {
            return;
        }

        if (chapterWindow != null && chapterWindow.isShowing()) {
            chapterWindow.toFront();
            return;
        }

        if (chapterWorkspaceOriginalParent == null && chapterWorkspacePane.getParent() instanceof VBox parent) {
            chapterWorkspaceOriginalParent = parent;
            chapterWorkspaceOriginalIndex = parent.getChildren().indexOf(chapterWorkspacePane);
        }

        if (chapterWorkspaceOriginalParent != null) {
            chapterWorkspaceOriginalParent.getChildren().remove(chapterWorkspacePane);
        }

        chapterWorkspacePane.setVisible(true);
        chapterWorkspacePane.setManaged(true);
        chapterWorkspacePane.setPrefHeight(680);

        BorderPane root = new BorderPane(chapterWorkspacePane);
        root.getStyleClass().add("root-pane");
        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/org/example/ui/course-manager.css").toExternalForm());

        chapterWindow = new Stage();
        chapterWindow.initModality(Modality.NONE);
        chapterWindow.setTitle("Chapitres - " + valueOrFallback(course.getTitre(), "Cours"));
        chapterWindow.setScene(scene);
        chapterWindow.setMinWidth(980);
        chapterWindow.setMinHeight(640);
        chapterWindow.setOnCloseRequest(event -> restoreChapterWorkspace());
        chapterWindow.show();
    }

    private void restoreChapterWorkspace() {
        if (chapterWorkspaceOriginalParent == null || chapterWorkspacePane == null) {
            return;
        }

        BorderPane currentParent = chapterWorkspacePane.getParent() instanceof BorderPane borderPane ? borderPane : null;
        if (currentParent != null) {
            currentParent.setCenter(null);
        }

        if (!chapterWorkspaceOriginalParent.getChildren().contains(chapterWorkspacePane)) {
            int index = chapterWorkspaceOriginalIndex >= 0
                    ? Math.min(chapterWorkspaceOriginalIndex, chapterWorkspaceOriginalParent.getChildren().size())
                    : chapterWorkspaceOriginalParent.getChildren().size();
            chapterWorkspaceOriginalParent.getChildren().add(index, chapterWorkspacePane);
        }

        showChapterWorkspace(false);
        chapterWindow = null;
    }

    private int countChapters(Cours course) {
        try {
            return chapitreService.getByCoursId(course.getId()).size();
        } catch (RuntimeException e) {
            return 0;
        }
    }

    private static String courseIcon(Cours course) {
        String category = valueOrFallback(course.getCategorie(), "").toLowerCase(Locale.ROOT);
        if (category.contains("peinture")) {
            return "\uD83C\uDFA8";
        }
        if (category.contains("dessin")) {
            return "\u270F";
        }
        if (category.contains("poterie")) {
            return "\uD83C\uDFFA";
        }
        if (category.contains("c\u00E9ramique") || category.contains("ceramique")) {
            return "\u25D2";
        }
        if (category.contains("sculpture")) {
            return "\uD83D\uDDFF";
        }
        if (category.contains("aquarelle")) {
            return "\uD83D\uDCA7";
        }
        if (category.contains("calligraphie")) {
            return "\u2712";
        }
        if (category.contains("photographie")) {
            return "\uD83D\uDCF7";
        }
        if (category.contains("illustration")) {
            return "\uD83D\uDD8A";
        }
        if (category.contains("design")) {
            return "\u25C6";
        }
        return "\u2605";
    }
    private void showCourse(Cours course) {
        if (course == null) {
            clearCourseForm();
            showChapterWorkspace(false);
            return;
        }
        selectedCourse = course;
        courseIdLabel.setText("#" + course.getId());
        courseTitleField.setText(course.getTitre());
        setComboValue(courseCategoryCombo, course.getCategorie());
        if (course.getNiveau() == null || course.getNiveau().isBlank()) {
            courseLevelCombo.getSelectionModel().clearSelection();
        } else {
            courseLevelCombo.setValue(course.getNiveau());
        }
        courseDescriptionArea.setText(course.getDescription());
    }

    private void showChapter(Chapitres chapter) {
        if (chapter == null) {
            clearChapterForm();
            return;
        }
        chapterIdLabel.setText("#" + chapter.getId());
        chapterOrderField.setText(String.valueOf(chapter.getOrdre()));
        chapterTitleField.setText(chapter.getTitre());
        chapterPdfField.setText(valueOrFallback(chapter.getPdfPath(), ""));
        String displayedContent = valueOrFallback(chapter.getContenu(), "");
        if (displayedContent.isBlank() && chapter.getPdfPath() != null && !chapter.getPdfPath().isBlank()) {
            displayedContent = chapterContentFromPdfPath(chapter.getPdfPath());
        }
        chapterContentArea.setText(displayedContent);
        feedbackCommentArea.clear();
        ratingCombo.getSelectionModel().clearSelection();
        selectedRenduFile = null;
        selectedChapterPdfFile = chapter.getPdfPath() == null || chapter.getPdfPath().isBlank()
                ? null
                : new File(chapter.getPdfPath());
        renduFileField.clear();
        updateRenduStatus(chapter);
        advancedResultLabel.setText("Suivi de progres pret pour le chapitre : " + chapter.getTitre());
    }

    private void clearCourseForm() {
        selectedCourse = null;
        courseIdLabel.setText("");
        courseTitleField.clear();
        courseCategoryCombo.getSelectionModel().clearSelection();
        courseDescriptionArea.clear();
        courseLevelCombo.getSelectionModel().clearSelection();
    }

    private void clearChapterForm() {
        chapterIdLabel.setText("");
        chapterOrderField.clear();
        chapterTitleField.clear();
        chapterContentArea.clear();
        chapterPdfField.clear();
        selectedChapterPdfFile = null;
        updateRenduStatus(null);
    }

    private void clearStudentSpace() {
        feedbackCommentArea.clear();
        ratingCombo.getSelectionModel().clearSelection();
        selectedRenduFile = null;
        renduFileField.clear();
        updateRenduStatus(null);
        apiCloudUrlField.clear();
        advancedStudentEmailField.clear();
        advancedProgressCombo.getSelectionModel().clearSelection();
        advancedResultLabel.setText("Selectionnez un chapitre, puis utilisez le suivi de progres.");
    }

    private void showChapterWorkspace(boolean visible) {
        if (chapterWorkspacePane == null) {
            return;
        }

        boolean shouldShow = visible && "cours".equals(activePage);
        chapterWorkspacePane.setVisible(shouldShow);
        chapterWorkspacePane.setManaged(shouldShow);
    }

    private void updateCourseCount() {
        courseCountLabel.setText(filteredCourses.size() + " cours");
        updateReportStats();
    }

    private void updateChapterCount() {
        chapterCountLabel.setText(visibleChapters.size() + " chapitres");
    }

    private void refreshChapterList() {
        chapterListView.refresh();
    }

    private Cours getSelectedCourse() {
        return selectedCourse;
    }

    private Chapitres getSelectedChapter() {
        return chapterListView.getSelectionModel().getSelectedItem();
    }

    private boolean selectCourseById(int id) {
        if (id <= 0) {
            return false;
        }
        for (Cours c : filteredCourses) {
            if (c.getId() == id) {
                courseListView.getSelectionModel().select(c);
                selectCourse(c);
                return true;
            }
        }
        return false;
    }

    private boolean selectCourseByTitle(String title) {
        for (Cours c : filteredCourses) {
            if (title.equals(c.getTitre())) {
                selectCourse(c);
                return true;
            }
        }
        return false;
    }

    private boolean selectChapterById(int id) {
        if (id <= 0) {
            return false;
        }
        for (Chapitres c : visibleChapters) {
            if (c.getId() == id) {
                chapterListView.getSelectionModel().select(c);
                return true;
            }
        }
        return false;
    }

    private boolean selectChapterByTitle(String title) {
        for (Chapitres c : visibleChapters) {
            if (title.equals(c.getTitre())) {
                chapterListView.getSelectionModel().select(c);
                return true;
            }
        }
        return false;
    }

    private void showError(String title, RuntimeException e) {
        String message = cleanError(e);
        setStatus(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showMeetingPopup(MeetingInfo meeting) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("R\u00e9union Zoom cr\u00e9\u00e9e");
        alert.setHeaderText("Code du meet : " + meeting.id());
        alert.setContentText("Lien \u00e9tudiant : " + meeting.joinUrl());
        alert.showAndWait();
    }

    private void showTeacherRemark(Chapitres chapter) {
        int note = avisService.getLatestNoteForChapitre(chapter.getId());
        String remark = valueOrFallback(
                avisService.getLatestCommentForChapitre(chapter.getId()),
                "Aucune remarque enregistr\u00e9e pour ce chapitre."
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Remarque de l'enseignant");
        alert.setHeaderText(note >= 0 ? "Note " + note + "/5 - " + chapter.getTitre() : chapter.getTitre());
        alert.setContentText(remark);
        alert.showAndWait();
    }

    private void showValidationError(String title, String message) {
        setStatus("Saisie invalide.");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String cleanError(Throwable e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = "Unexpected error.";
        }
        return message.length() > 220 ? message.substring(0, 220) + "..." : message;
    }

    private static String text(TextInputControl input) {
        return input.getText() == null ? "" : input.getText().trim();
    }

    private static String comboValueOrDefault(ComboBox<String> comboBox, String fallback) {
        String value = comboBox.getValue();
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static void setComboValue(ComboBox<String> comboBox, String value) {
        if (value == null || value.isBlank()) {
            comboBox.getSelectionModel().clearSelection();
            return;
        }

        String trimmed = value.trim();
        if (!comboBox.getItems().contains(trimmed)) {
            comboBox.getItems().add(trimmed);
        }
        comboBox.setValue(trimmed);
    }

    private static UnaryOperator<TextFormatter.Change> lettersOnlyFilter() {
        return change -> containsOnlyLettersAndSpaces(change.getControlNewText()) ? change : null;
    }

    private static UnaryOperator<TextFormatter.Change> freeTextFilter() {
        return change -> containsAllowedTextCharacters(change.getControlNewText()) ? change : null;
    }

    private UnaryOperator<TextFormatter.Change> lettersOnlyFilterWithDialog(String fieldName) {
        return change -> {
            if (containsOnlyLettersAndSpaces(change.getControlNewText())) {
                return change;
            }

            showValidationError("Saisie interdite", fieldName + " accepte seulement des lettres et des espaces.");
            return null;
        };
    }

    private UnaryOperator<TextFormatter.Change> freeTextFilterWithDialog(String fieldName) {
        return change -> {
            if (containsAllowedTextCharacters(change.getControlNewText())) {
                return change;
            }

            showValidationError("Saisie interdite", fieldName + " contient un caractere non autorise.");
            return null;
        };
    }

    private static UnaryOperator<TextFormatter.Change> digitsOnlyFilter() {
        return change -> change.getControlNewText().chars().allMatch(Character::isDigit) ? change : null;
    }

    private static UnaryOperator<TextFormatter.Change> integerNoteFilter() {
        return change -> {
            String value = change.getControlNewText();
            if (value.isEmpty()) {
                return change;
            }
            return value.matches("[0-5]") ? change : null;
        };
    }

    private UnaryOperator<TextFormatter.Change> digitsOnlyFilterWithDialog(String fieldName) {
        return change -> {
            if (change.getControlNewText().chars().allMatch(Character::isDigit)) {
                return change;
            }

            showValidationError("Saisie interdite", fieldName + " accepte seulement des chiffres.");
            return null;
        };
    }

    private static UnaryOperator<TextFormatter.Change> gmailEmailTypingFilter() {
        return change -> {
            String value = change.getControlNewText();
            boolean validCharacters = value.chars().allMatch(ch ->
                    Character.isLetterOrDigit(ch)
                            || ch == '.'
                            || ch == '_'
                            || ch == '-'
                            || ch == '@');
            long atCount = value.chars().filter(ch -> ch == '@').count();
            return validCharacters && atCount <= 1 ? change : null;
        };
    }

    private static boolean containsOnlyLettersAndSpaces(String value) {
        return value.chars().allMatch(ch -> Character.isLetter(ch) || Character.isWhitespace(ch));
    }

    private static boolean containsAllowedTextCharacters(String value) {
        return value.chars().allMatch(ch ->
                Character.isLetterOrDigit(ch)
                        || Character.isWhitespace(ch)
                        || "'\".,;:!?()[]-_/@+#&".indexOf(ch) >= 0);
    }

    private static boolean isValidPersonName(String value) {
        return value != null && !value.isBlank() && containsOnlyLettersAndSpaces(value.trim());
    }

    private static boolean isValidGmail(String value) {
        if (value == null) {
            return false;
        }

        String email = value.trim().toLowerCase(Locale.ROOT);
        return email.matches("[a-z0-9._-]+@gmail\\.com");
    }

    private static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().toLowerCase(Locale.ROOT)
                .matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    }

    private static String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static UnaryOperator<TextFormatter.Change> emailTypingFilter() {
        return change -> {
            String value = change.getControlNewText();
            boolean validCharacters = value.chars().allMatch(ch ->
                    Character.isLetterOrDigit(ch)
                            || ch == '.'
                            || ch == '_'
                            || ch == '-'
                            || ch == '+'
                            || ch == '%'
                            || ch == '@');
            long atCount = value.chars().filter(ch -> ch == '@').count();
            return validCharacters && atCount <= 1 ? change : null;
        };
    }

    private static boolean isValidIsoDateTime(String value) {
        try {
            LocalDateTime.parse(value.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static int parseOrder(String raw, int fallback) {
        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(parsed, 1);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int parsePositiveInt(String raw) {
        try {
            return Integer.parseInt(valueOrFallback(raw, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer parseRequiredIntegerNote(String raw) {
        if (raw == null || raw.isBlank()) {
            showValidationError("Note obligatoire", "La note doit etre un nombre entier compris entre 0 et 5.");
            return null;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < 0 || parsed > 5) {
                showValidationError("Note invalide", "La note doit etre un nombre entier compris entre 0 et 5.");
                return null;
            }
            return parsed;
        } catch (NumberFormatException e) {
            showValidationError("Note invalide", "La note doit etre un nombre entier compris entre 0 et 5.");
            return null;
        }
    }

    private boolean sameCourse(String studentCourse, Cours course) {
        return course != null && valueOrFallback(studentCourse, "").equalsIgnoreCase(valueOrFallback(course.getTitre(), ""));
    }

    private int findCourseIdByName(String courseName) {
        return allCourses.stream()
                .filter(course -> valueOrFallback(course.getTitre(), "").equalsIgnoreCase(valueOrFallback(courseName, "")))
                .mapToInt(Cours::getId)
                .findFirst()
                .orElse(-1);
    }

    private static boolean isZoomJoinUrl(String url) {
        return url != null && url.matches("https://([a-zA-Z0-9.-]+\\.)?zoom\\.us/j/.+");
    }

    private void openExternalUrl(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException | RuntimeException e) {
            showError("Ouverture impossible", new IllegalStateException("Impossible d'ouvrir le lien : " + url, e));
        }
    }

    private String chapterContentFromEditor() {
        String content = text(chapterContentArea);
        String pdfPath = text(chapterPdfField);
        return content.isEmpty() && !pdfPath.isEmpty() ? chapterContentFromPdfPath(pdfPath) : content;
    }

    private static String chapterContentFromPdfPath(String pdfPath) {
        return "PDF du chapitre : " + pdfPath;
    }

    public static final class StudentRecord {
        private final int id;
        private String fullName;
        private String email;
        private String gender;
        private String course;
        private String status;
        private final LocalDate enrollmentDate;
        private final List<ChapterEvaluation> chapters = new ArrayList<>();

        public StudentRecord(int id, String fullName, String email, String gender, String course, String status, LocalDate enrollmentDate) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.gender = gender;
            this.course = course;
            this.status = status;
            this.enrollmentDate = enrollmentDate;
        }

        public int getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCourse() {
            return course;
        }

        public void setCourse(String course) {
            this.course = course;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDate getEnrollmentDate() {
            return enrollmentDate;
        }

        public List<ChapterEvaluation> getChapters() {
            return chapters;
        }

        public double getAverage() {
            return chapters.stream().mapToDouble(ChapterEvaluation::getNote).average().orElse(0);
        }

        public int getCompletedChapters() {
            return (int) chapters.stream().filter(chapter -> "Termine".equalsIgnoreCase(chapter.getProgress())).count();
        }

        public double getProgressRatio() {
            return chapters.isEmpty() ? 0 : (double) getCompletedChapters() / chapters.size();
        }
    }

    public static final class ChapterEvaluation {
        private final String chapterTitle;
        private double note;
        private String progress;

        public ChapterEvaluation(String chapterTitle, double note, String progress) {
            this.chapterTitle = chapterTitle;
            this.note = note;
            this.progress = progress;
        }

        public String getChapterTitle() {
            return chapterTitle;
        }

        public String getChapterKey() {
            return chapterTitle;
        }

        public double getNote() {
            return note;
        }

        public void setNote(double note) {
            this.note = note;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String progress) {
            this.progress = progress;
        }
    }

    private final class StudentCardCell extends ListCell<StudentRecord> {
        @Override
        protected void updateItem(StudentRecord student, boolean empty) {
            super.updateItem(student, empty);
            if (empty || student == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label avatar = new Label(initials(student.getFullName()));
            avatar.getStyleClass().add("user-card-avatar");

            Label name = new Label(student.getFullName());
            name.getStyleClass().add("user-card-title");
            Label email = new Label(student.getEmail());
            email.getStyleClass().add("user-card-email");
            Label course = new Label(student.getCourse());
            course.getStyleClass().add("user-card-text");
            VBox identity = new VBox(4, name, email, course);
            HBox.setHgrow(identity, Priority.ALWAYS);

            Label gender = new Label(student.getGender());
            gender.getStyleClass().add("user-card-chip");
            Label attendance = new Label(student.getStatus());
            attendance.getStyleClass().add("Present".equalsIgnoreCase(student.getStatus())
                    ? "user-card-status-active"
                    : "user-card-status-blocked");
            Label average = new Label(String.format(Locale.ROOT, "%.2f / 5", student.getAverage()));
            average.getStyleClass().add("modern-card-chip-muted");
            Label progress = new Label(Math.round(student.getProgressRatio() * 100) + "%");
            progress.getStyleClass().add("modern-card-chip");
            Label submission = new Label(submissionService.hasSubmissionByStudentEmail(student.getEmail()) ? "Rendu : Envoye" : "Rendu : Non envoye");
            submission.getStyleClass().add(submissionService.hasSubmissionByStudentEmail(student.getEmail())
                    ? "user-card-status-active"
                    : "user-card-status-blocked");

            Button details = new Button("Voir details");
            details.getStyleClass().add("btn-secondary");
            details.setOnAction(event -> {
                studentListView.getSelectionModel().select(student);
                populateStudentForm(student);
                showStudentDetails(student);
            });

            Button edit = new Button("Modifier");
            edit.getStyleClass().add("btn-secondary");
            edit.setDisable(!enseignantMode);
            edit.setOnAction(event -> {
                studentListView.getSelectionModel().select(student);
                showStudentFormModal(student);
            });

            Button delete = new Button("Supprimer");
            delete.getStyleClass().add("btn-danger");
            delete.setDisable(!enseignantMode);
            delete.setOnAction(event -> {
                studentListView.getSelectionModel().select(student);
                handleDeleteStudent(new ActionEvent(delete, delete));
            });

            Button notes = new Button("Gerer notes");
            notes.getStyleClass().add("btn-primary");
            notes.setDisable(!enseignantMode);
            notes.setOnAction(event -> {
                studentListView.getSelectionModel().select(student);
                populateStudentForm(student);
                showStudentDetails(student);
            });

            Button present = new Button("Present / Non present");
            present.getStyleClass().add("btn-secondary");
            present.setDisable(!enseignantMode);
            present.setOnAction(event -> {
                studentListView.getSelectionModel().select(student);
                handleToggleStudentAttendance(new ActionEvent(present, present));
            });

            FlowPane badges = new FlowPane(8, 6, gender, attendance, average, progress, submission);
            badges.getStyleClass().add("user-card-badges-flow");
            FlowPane actions = new FlowPane(6, 6, details, edit, delete, notes, present);
            actions.getStyleClass().add("user-card-actions-flow");

            VBox middle = new VBox(8, identity, badges, actions);
            HBox.setHgrow(middle, Priority.ALWAYS);
            HBox card = new HBox(12, avatar, middle);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setMaxWidth(Double.MAX_VALUE);
            card.prefWidthProperty().bind(studentListView.widthProperty().subtract(42));
            card.getStyleClass().add("user-card");

            setText(null);
            setGraphic(card);
        }
    }

    private static final class CourseCardCell extends ListCell<Cours> {
        @Override
        protected void updateItem(Cours course, boolean empty) {
            super.updateItem(course, empty);

            if (empty || course == null) {
                setText(null);
                setGraphic(null);
                return;
            }

        Label titleLabel = new Label(valueOrFallback(course.getTitre(), "Cours sans titre"));
            titleLabel.getStyleClass().add("modern-card-title");
            titleLabel.setWrapText(true);

        Label categoryLabel = new Label(valueOrFallback(course.getCategorie(), "Peinture"));
            categoryLabel.getStyleClass().add("modern-card-chip");

            Label levelLabel = new Label(valueOrFallback(course.getNiveau(), "Debutant"));
            levelLabel.getStyleClass().add("modern-card-chip-muted");

            HBox metaBox = new HBox(6, categoryLabel, levelLabel);
            metaBox.setAlignment(Pos.CENTER_LEFT);

            VBox textBox = new VBox(8, titleLabel, metaBox);
            textBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Label idLabel = new Label("#" + course.getId());
            idLabel.getStyleClass().add("modern-card-id");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox card = new HBox(10, textBox, spacer, idLabel);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("modern-card");

            setText(null);
            setGraphic(card);
        }
    }

    private final class ChapterCardCell extends ListCell<Chapitres> {
        @Override
        protected void updateItem(Chapitres chapter, boolean empty) {
            super.updateItem(chapter, empty);

            if (empty || chapter == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label orderLabel = new Label(String.valueOf(chapter.getOrdre()));
            orderLabel.getStyleClass().add("chapter-order-badge");

            Label titleLabel = new Label(valueOrFallback(chapter.getTitre(), "Chapitre sans titre"));
            titleLabel.getStyleClass().add("modern-card-title");
            titleLabel.setWrapText(true);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            boolean hasRendu = renduService.hasRenduForChapitre(chapter.getId());
            Label renduStatusLabel = new Label(hasRendu ? "\u2713" : "\u2715");
            renduStatusLabel.setStyle(hasRendu
                    ? "-fx-text-fill: #1f9d55; -fx-font-size: 18px; -fx-font-weight: 900;"
                    : "-fx-text-fill: #d62828; -fx-font-size: 18px; -fx-font-weight: 900;");

            int note = avisService.getLatestNoteForChapitre(chapter.getId());
            Label noteLabel = new Label(note >= 0 ? "Note " + note + "/5" : "Sans note");
            noteLabel.getStyleClass().add("modern-card-chip-muted");
            noteLabel.setStyle("-fx-cursor: hand;");
            noteLabel.setOnMouseClicked(event -> {
                event.consume();
                showTeacherRemark(chapter);
            });

            VBox infoBox = new VBox(4, titleLabel, noteLabel);
            HBox.setHgrow(infoBox, Priority.ALWAYS);

            HBox card = new HBox(10, orderLabel, infoBox, renduStatusLabel);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("modern-card");

            setText(null);
            setGraphic(card);
        }
    }

    private static String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String initials(String fullName) {
        String[] parts = valueOrFallback(fullName, "ET").split("\\s+");
        String first = parts.length > 0 && !parts[0].isBlank() ? parts[0].substring(0, 1) : "E";
        String second = parts.length > 1 && !parts[1].isBlank() ? parts[1].substring(0, 1) : "T";
        return (first + second).toUpperCase(Locale.ROOT);
    }

    private static String safeFileName(String value) {
        String safe = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "chapitre" : safe;
    }
}

