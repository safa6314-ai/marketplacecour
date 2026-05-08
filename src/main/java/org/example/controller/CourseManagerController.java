package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import org.example.entities.Avis;
import org.example.entities.Cours;
import org.example.entities.Chapitres;
import org.example.entities.Rendu;
import org.example.services.AvisService;
import org.example.services.CoursService;
import org.example.services.ChapitreService;
import org.example.services.RenduService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class CourseManagerController implements Initializable {

    // HEADER
    @FXML private Label statusLabel;
    @FXML private ToggleButton adminModeToggle;

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
    @FXML private TextArea chapterDescriptionArea;
    @FXML private TextArea chapterContentArea;
    @FXML private TextArea userChapterContentArea;
    @FXML private ComboBox<String> ratingCombo;
    @FXML private TextArea feedbackCommentArea;
    @FXML private TextField renduFileField;

    @FXML private Button saveChapterButton;

    private final ObservableList<Cours> allCourses = FXCollections.observableArrayList();
    private final ObservableList<Cours> filteredCourses = FXCollections.observableArrayList();
    private final ObservableList<Chapitres> allChaptersForCourse = FXCollections.observableArrayList();
    private final ObservableList<Chapitres> visibleChapters = FXCollections.observableArrayList();
    
    private final CoursService coursService = new CoursService();
    private final ChapitreService chapitreService = new ChapitreService();
    private final AvisService avisService = new AvisService();
    private final RenduService renduService = new RenduService();
    private File selectedRenduFile;
    private boolean adminMode = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wireLists();
        wireListeners();
        wireInputFilters();
        ensureCategoryOptions();
        ensureLevelOptions();
        setAdminMode(adminModeToggle == null || adminModeToggle.isSelected());

        try {
            loadCoursesFromDb();
            setStatus("Data loaded from database. Ready.");
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
                    courseListView.getSelectionModel().select(c);
                    break;
                }
            }
        } else if (!filteredCourses.isEmpty()) {
            courseListView.getSelectionModel().selectFirst();
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
            return;
        }
        
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
        if (!requireAdminMode()) {
            return;
        }

        String title = text(courseTitleField);
        if (title.isEmpty()) {
            setStatus("Course title is required.");
            return;
        }

        Cours cours = new Cours(
                title,
                text(courseDescriptionArea),
                0.0,
                comboValueOrDefault(courseCategoryCombo, "General"),
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
                setStatus("Course added, but hidden by the current search filter.");
            } else {
                setStatus("Course added: " + title);
            }
        } catch (RuntimeException e) {
            showError("Could not add course", e);
        }
    }

    @FXML
    private void handleEditCourse(ActionEvent event) {
        if (!requireAdminMode()) {
            return;
        }

        Cours selected = getSelectedCourse();
        if (selected == null) {
            setStatus("Select a course to edit.");
            return;
        }

        String title = text(courseTitleField);
        if (title.isEmpty()) {
            setStatus("Course title is required.");
            return;
        }

        selected.setTitre(title);
        selected.setCategorie(comboValueOrDefault(courseCategoryCombo, "General"));
        selected.setDescription(text(courseDescriptionArea));
        selected.setNiveau(comboValueOrDefault(courseLevelCombo, "Debutant"));

        try {
            coursService.modifier(selected);
            loadCoursesFromDb();
            selectCourseById(selected.getId());
            setStatus("Course updated: #" + selected.getId());
        } catch (RuntimeException e) {
            showError("Could not update course", e);
        }
    }

    @FXML
    private void handleDeleteCourse(ActionEvent event) {
        if (!requireAdminMode()) {
            return;
        }

        Cours selected = getSelectedCourse();
        if (selected == null) {
            setStatus("Select a course to delete.");
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

            setStatus("Course deleted.");
        } catch (RuntimeException e) {
            showError("Could not delete course", e);
        }
    }

    @FXML
    private void handleAddChapter(ActionEvent event) {
        if (!requireAdminMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        if (course == null) {
            setStatus("Select a course first.");
            return;
        }

        String title = text(chapterTitleField);
        if (title.isEmpty()) {
            setStatus("Chapter title is required.");
            return;
        }

        Chapitres chapter = new Chapitres(
                title,
                text(chapterContentArea),
                parseOrder(chapterOrderField.getText(), visibleChapters.size() + 1),
                course.getId()
        );

        try {
            chapitreService.ajouter(chapter);
            loadChaptersForCourse(course);

            boolean selected = selectChapterById(chapter.getId());
            if (!selected) {
                selectChapterByTitle(title);
            }

            setStatus("Chapter added: " + title);
        } catch (RuntimeException e) {
            showError("Could not add chapter", e);
        }
    }

    @FXML
    private void handleEditChapter(ActionEvent event) {
        if (!requireAdminMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        Chapitres chapter = getSelectedChapter();
        if (course == null || chapter == null) {
            setStatus("Select a chapter to edit.");
            return;
        }

        String title = text(chapterTitleField);
        if (title.isEmpty()) {
            setStatus("Chapter title is required.");
            return;
        }

        chapter.setTitre(title);
        chapter.setContenu(text(chapterContentArea));
        chapter.setOrdre(parseOrder(chapterOrderField.getText(), chapter.getOrdre()));

        try {
            chapitreService.modifier(chapter);
            loadChaptersForCourse(course);
            selectChapterById(chapter.getId());
            setStatus("Chapter updated: #" + chapter.getId());
        } catch (RuntimeException e) {
            showError("Could not update chapter", e);
        }
    }

    @FXML
    private void handleDeleteChapter(ActionEvent event) {
        if (!requireAdminMode()) {
            return;
        }

        Cours course = getSelectedCourse();
        Chapitres chapter = getSelectedChapter();
        if (course == null || chapter == null) {
            setStatus("Select a chapter to delete.");
            return;
        }

        try {
            chapitreService.supprimer(chapter.getId());
            loadChaptersForCourse(course);
            clearChapterForm();

            setStatus("Chapter deleted.");
        } catch (RuntimeException e) {
            showError("Could not delete chapter", e);
        }
    }

    @FXML
    private void handleSaveChapter(ActionEvent event) {
        if (!requireAdminMode()) {
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
    private void handleToggleAdminMode(ActionEvent event) {
        setAdminMode(adminModeToggle.isSelected());
    }

    @FXML
    private void handleSubmitFeedback(ActionEvent event) {
        Chapitres chapter = getSelectedChapter();
        if (chapter == null) {
            setStatus("Select a chapter before sending feedback.");
            return;
        }

        String rating = comboValueOrDefault(ratingCombo, "");
        if (rating.isEmpty()) {
            setStatus("Choose a rating from 1 to 5.");
            return;
        }

        try {
            int note = Math.max(1, Math.min(5, Integer.parseInt(rating)));
            avisService.ajouter(new Avis(chapter.getId(), text(feedbackCommentArea), note));
            feedbackCommentArea.clear();
            ratingCombo.getSelectionModel().clearSelection();
            setStatus("Avis sent for chapter: " + chapter.getTitre());
        } catch (RuntimeException e) {
            showError("Could not send feedback", e);
        }
    }

    @FXML
    private void handleDownloadChapter(ActionEvent event) {
        Chapitres chapter = getSelectedChapter();
        if (chapter == null) {
            setStatus("Select a chapter before downloading.");
            return;
        }

        downloadChapterToFile(chapter, userChapterContentArea);
    }

    private void downloadChapterToFile(Chapitres chapter, Node ownerNode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Download chapter");
        fileChooser.setInitialFileName(safeFileName(valueOrFallback(chapter.getTitre(), "chapter")) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));

        File destination = fileChooser.showSaveDialog(ownerNode.getScene().getWindow());
        if (destination == null) {
            return;
        }

        String content = valueOrFallback(chapter.getTitre(), "Untitled chapter")
                + System.lineSeparator()
                + System.lineSeparator()
                + valueOrFallback(chapter.getContenu(), "");

        try {
            Files.writeString(destination.toPath(), content, StandardCharsets.UTF_8);
            setStatus("Chapter downloaded: " + destination.getName());
        } catch (IOException e) {
            showError("Could not download chapter", new IllegalStateException(e.getMessage(), e));
        }
    }

    @FXML
    private void handleChooseRenduFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose rendu file");
        selectedRenduFile = fileChooser.showOpenDialog(renduFileField.getScene().getWindow());
        if (selectedRenduFile != null) {
            renduFileField.setText(selectedRenduFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleSubmitRendu(ActionEvent event) {
        Chapitres chapter = getSelectedChapter();
        if (chapter == null) {
            setStatus("Select a chapter before sending a file.");
            return;
        }
        if (selectedRenduFile == null) {
            setStatus("Choose a rendu file first.");
            return;
        }

        try {
            renduService.ajouter(new Rendu(
                    chapter.getId(),
                    selectedRenduFile.getName(),
                    selectedRenduFile.getAbsolutePath()
            ));
            selectedRenduFile = null;
            renduFileField.clear();
            setStatus("Rendu sent for chapter: " + chapter.getTitre());
        } catch (RuntimeException e) {
            showError("Could not send rendu", e);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void openChapterReader(Chapitres chapter) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chapter Reader");
        dialog.setHeaderText(valueOrFallback(chapter.getTitre(), "Untitled chapter"));
        dialog.getDialogPane().getStyleClass().add("dialog-theme");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label orderLabel = new Label("Chapter " + chapter.getOrdre());
        orderLabel.getStyleClass().add("reader-order-badge");

        Label titleLabel = new Label(valueOrFallback(chapter.getTitre(), "Untitled chapter"));
        titleLabel.getStyleClass().add("reader-title");
        titleLabel.setWrapText(true);

        TextArea contentArea = new TextArea(valueOrFallback(chapter.getContenu(), ""));
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(16);
        contentArea.getStyleClass().add("reader-content-area");

        Button downloadButton = new Button("Download Chapter");
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

    private void setAdminMode(boolean enabled) {
        adminMode = enabled;
        if (adminModeToggle != null) {
            adminModeToggle.setSelected(enabled);
            adminModeToggle.setText(enabled ? "Admin ON" : "Admin OFF");
            adminModeToggle.getStyleClass().removeAll("mode-toggle-active", "mode-toggle-user");
            adminModeToggle.getStyleClass().add(enabled ? "mode-toggle-active" : "mode-toggle-user");
        }

        addCourseButton.setDisable(!enabled);
        editCourseButton.setDisable(!enabled);
        deleteCourseButton.setDisable(!enabled);
        addChapterButton.setDisable(!enabled);
        editChapterButton.setDisable(!enabled);
        deleteChapterButton.setDisable(!enabled);
        saveChapterButton.setDisable(!enabled);

        courseTitleField.setDisable(!enabled);
        courseCategoryCombo.setDisable(!enabled);
        courseLevelCombo.setDisable(!enabled);
        courseDescriptionArea.setDisable(!enabled);
        chapterOrderField.setDisable(!enabled);
        chapterTitleField.setDisable(!enabled);
        chapterDescriptionArea.setDisable(!enabled);
        chapterContentArea.setDisable(!enabled);

        setStatus(enabled
                ? "Admin mode enabled. Course and chapter management is available."
                : "User mode enabled. You can read chapters, send feedback, upload rendus, and download chapters.");
    }

    private boolean requireAdminMode() {
        if (adminMode) {
            return true;
        }

        setStatus("Admin mode is OFF. This action is locked.");
        return false;
    }

    private void wireLists() {
        courseListView.setItems(filteredCourses);
        chapterListView.setItems(visibleChapters);
        courseListView.setCellFactory(list -> new CourseCardCell());
        chapterListView.setCellFactory(list -> new ChapterCardCell());
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
            if (!adminMode && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                Chapitres selected = getSelectedChapter();
                if (selected != null) {
                    openChapterReader(selected);
                }
            }
        });

        courseSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyCourseFilter());
        chapterSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyChapterFilter());
    }

    private void wireInputFilters() {
        courseSearchField.setTextFormatter(new TextFormatter<>(lettersOnlyFilter()));
        chapterSearchField.setTextFormatter(new TextFormatter<>(lettersOnlyFilter()));
        courseTitleField.setTextFormatter(new TextFormatter<>(lettersOnlyFilter()));
        chapterTitleField.setTextFormatter(new TextFormatter<>(lettersOnlyFilter()));
        chapterOrderField.setTextFormatter(new TextFormatter<>(digitsOnlyFilter()));
    }

    private void ensureCategoryOptions() {
        if (courseCategoryCombo.getItems().isEmpty()) {
            courseCategoryCombo.getItems().setAll(
                    "Java",
                    "IA",
                    "Web",
                    "Electronique",
                    "Design",
                    "Marketing",
                    "Business",
                    "Langues",
                    "General"
            );
        }
    }

    private void ensureLevelOptions() {
        if (courseLevelCombo.getItems().isEmpty()) {
            courseLevelCombo.getItems().setAll("Debutant", "Intermediaire", "Avance");
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
        
        if (selected != null && filteredCourses.contains(selected)) {
            courseListView.getSelectionModel().select(selected);
        } else if (!filteredCourses.isEmpty()) {
            courseListView.getSelectionModel().selectFirst();
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

    private void showCourse(Cours course) {
        if (course == null) {
            clearCourseForm();
            return;
        }
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
        chapterDescriptionArea.setText("");
        chapterContentArea.setText(chapter.getContenu());
        userChapterContentArea.setText(chapter.getContenu());
        feedbackCommentArea.clear();
        ratingCombo.getSelectionModel().clearSelection();
        selectedRenduFile = null;
        renduFileField.clear();
    }

    private void clearCourseForm() {
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
        chapterDescriptionArea.clear();
        chapterContentArea.clear();
    }

    private void clearStudentSpace() {
        userChapterContentArea.clear();
        feedbackCommentArea.clear();
        ratingCombo.getSelectionModel().clearSelection();
        selectedRenduFile = null;
        renduFileField.clear();
    }

    private void updateCourseCount() {
        courseCountLabel.setText(filteredCourses.size() + " courses");
    }

    private void updateChapterCount() {
        chapterCountLabel.setText(visibleChapters.size() + " chapters");
    }

    private Cours getSelectedCourse() {
        return courseListView.getSelectionModel().getSelectedItem();
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
                return true;
            }
        }
        return false;
    }

    private boolean selectCourseByTitle(String title) {
        for (Cours c : filteredCourses) {
            if (title.equals(c.getTitre())) {
                courseListView.getSelectionModel().select(c);
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

    private static UnaryOperator<TextFormatter.Change> digitsOnlyFilter() {
        return change -> change.getControlNewText().chars().allMatch(Character::isDigit) ? change : null;
    }

    private static boolean containsOnlyLettersAndSpaces(String value) {
        return value.chars().allMatch(ch -> Character.isLetter(ch) || Character.isWhitespace(ch));
    }

    private static int parseOrder(String raw, int fallback) {
        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(parsed, 1);
        } catch (Exception ignored) {
            return fallback;
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

            Label titleLabel = new Label(valueOrFallback(course.getTitre(), "Untitled course"));
            titleLabel.getStyleClass().add("modern-card-title");
            titleLabel.setWrapText(true);

            Label categoryLabel = new Label(valueOrFallback(course.getCategorie(), "General"));
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

    private static final class ChapterCardCell extends ListCell<Chapitres> {
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

            Label titleLabel = new Label(valueOrFallback(chapter.getTitre(), "Untitled chapter"));
            titleLabel.getStyleClass().add("modern-card-title");
            titleLabel.setWrapText(true);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            HBox card = new HBox(10, orderLabel, titleLabel);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("modern-card");

            setText(null);
            setGraphic(card);
        }
    }

    private static String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String safeFileName(String value) {
        String safe = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return safe.isEmpty() ? "chapter" : safe;
    }
}
