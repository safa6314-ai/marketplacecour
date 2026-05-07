package org.example.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import org.example.entities.Cours;
import org.example.entities.Chapitres;
import org.example.services.CoursService;
import org.example.services.ChapitreService;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CourseManagerController implements Initializable {

    // HEADER
    @FXML private Label statusLabel;

    // LEFT PANEL - Course list
    @FXML private TextField courseSearchField;
    @FXML private Label courseCountLabel;

    @FXML private TableView<Cours> courseTableView;
    @FXML private TableColumn<Cours, String> courseTitleColumn;
    @FXML private TableColumn<Cours, String> courseCategoryColumn;
    @FXML private TableColumn<Cours, String> courseLevelColumn;

    @FXML private Button addCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button deleteCourseButton;

    // CENTER TOP - Course details
    @FXML private Label courseIdLabel;
    @FXML private TextField courseTitleField;
    @FXML private TextField courseCategoryField;
    @FXML private ComboBox<String> courseLevelCombo;
    @FXML private TextArea courseDescriptionArea;

    // CENTER BOTTOM LEFT - Chapter list
    @FXML private Label chapterCountLabel;

    @FXML private TableView<Chapitres> chapterTableView;
    @FXML private TableColumn<Chapitres, Integer> chapterOrderColumn;
    @FXML private TableColumn<Chapitres, String> chapterTitleColumn;

    @FXML private Button addChapterButton;
    @FXML private Button editChapterButton;
    @FXML private Button deleteChapterButton;

    // CENTER BOTTOM RIGHT - Chapter details
    @FXML private Label chapterIdLabel;
    @FXML private TextField chapterOrderField;
    @FXML private TextField chapterTitleField;
    @FXML private TextArea chapterDescriptionArea;
    @FXML private TextArea chapterContentArea;

    @FXML private Button saveChapterButton;

    private final ObservableList<Cours> allCourses = FXCollections.observableArrayList();
    private final ObservableList<Cours> filteredCourses = FXCollections.observableArrayList();
    private final ObservableList<Chapitres> visibleChapters = FXCollections.observableArrayList();
    
    private final CoursService coursService = new CoursService();
    private final ChapitreService chapitreService = new ChapitreService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wireTables();
        wireListeners();
        ensureLevelOptions();

        try {
            loadCoursesFromDb();
            setStatus("Data loaded from database. Ready.");
        } catch (RuntimeException e) {
            allCourses.clear();
            filteredCourses.clear();
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
                    courseTableView.getSelectionModel().select(c);
                    break;
                }
            }
        } else if (!filteredCourses.isEmpty()) {
            courseTableView.getSelectionModel().selectFirst();
        }
    }

    private void loadChaptersForCourse(Cours cours) {
        Chapitres selectedChapter = getSelectedChapter();
        
        if (cours == null) {
            visibleChapters.clear();
            updateChapterCount();
            clearChapterForm();
            return;
        }
        
        List<Chapitres> chapitresList;
        try {
            chapitresList = chapitreService.getByCoursId(cours.getId());
        } catch (RuntimeException e) {
            visibleChapters.clear();
            updateChapterCount();
            clearChapterForm();
            setStatus(cleanError(e));
            return;
        }
        visibleChapters.setAll(chapitresList);
        updateChapterCount();
        
        if (selectedChapter != null) {
            for (Chapitres c : visibleChapters) {
                if (c.getId() == selectedChapter.getId()) {
                    chapterTableView.getSelectionModel().select(c);
                    return;
                }
            }
        }
        
        clearChapterForm();
    }

    @FXML
    private void handleAddCourse(ActionEvent event) {
        String title = text(courseTitleField);
        if (title.isEmpty()) {
            setStatus("Course title is required.");
            return;
        }

        Cours cours = new Cours(
                title,
                text(courseDescriptionArea),
                0.0,
                textOrDefault(courseCategoryField, "General"),
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
        selected.setCategorie(textOrDefault(courseCategoryField, "General"));
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
        Cours selected = getSelectedCourse();
        if (selected == null) {
            setStatus("Select a course to delete.");
            return;
        }

        try {
            coursService.supprimer(selected.getId());

            clearCourseForm();
            visibleChapters.clear();
            clearChapterForm();
            updateChapterCount();

            loadCoursesFromDb();

            setStatus("Course deleted.");
        } catch (RuntimeException e) {
            showError("Could not delete course", e);
        }
    }

    @FXML
    private void handleAddChapter(ActionEvent event) {
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

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void wireTables() {
        courseTitleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitre()));
        courseCategoryColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCategorie()));
        courseLevelColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNiveau()));
        
        chapterOrderColumn.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getOrdre()));
        chapterTitleColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitre()));

        courseTableView.setItems(filteredCourses);
        chapterTableView.setItems(visibleChapters);
    }

    private void wireListeners() {
        courseTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            showCourse(selected);
            loadChaptersForCourse(selected);
        });

        chapterTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            showChapter(selected);
        });

        courseSearchField.textProperty().addListener((obs, oldValue, newValue) -> applyCourseFilter());
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
            courseTableView.getSelectionModel().select(selected);
        } else if (!filteredCourses.isEmpty()) {
            courseTableView.getSelectionModel().selectFirst();
        }
    }

    private void showCourse(Cours course) {
        if (course == null) {
            clearCourseForm();
            return;
        }
        courseIdLabel.setText("#" + course.getId());
        courseTitleField.setText(course.getTitre());
        courseCategoryField.setText(course.getCategorie());
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
    }

    private void clearCourseForm() {
        courseIdLabel.setText("");
        courseTitleField.clear();
        courseCategoryField.clear();
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

    private void updateCourseCount() {
        courseCountLabel.setText(filteredCourses.size() + " courses");
    }

    private void updateChapterCount() {
        chapterCountLabel.setText(visibleChapters.size() + " chapters");
    }

    private Cours getSelectedCourse() {
        return courseTableView.getSelectionModel().getSelectedItem();
    }

    private Chapitres getSelectedChapter() {
        return chapterTableView.getSelectionModel().getSelectedItem();
    }

    private boolean selectCourseById(int id) {
        if (id <= 0) {
            return false;
        }
        for (Cours c : filteredCourses) {
            if (c.getId() == id) {
                courseTableView.getSelectionModel().select(c);
                return true;
            }
        }
        return false;
    }

    private boolean selectCourseByTitle(String title) {
        for (Cours c : filteredCourses) {
            if (title.equals(c.getTitre())) {
                courseTableView.getSelectionModel().select(c);
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
                chapterTableView.getSelectionModel().select(c);
                return true;
            }
        }
        return false;
    }

    private boolean selectChapterByTitle(String title) {
        for (Chapitres c : visibleChapters) {
            if (title.equals(c.getTitre())) {
                chapterTableView.getSelectionModel().select(c);
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

    private static String textOrDefault(TextInputControl input, String fallback) {
        String value = text(input);
        return value.isEmpty() ? fallback : value;
    }

    private static String comboValueOrDefault(ComboBox<String> comboBox, String fallback) {
        String value = comboBox.getValue();
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static int parseOrder(String raw, int fallback) {
        try {
            int parsed = Integer.parseInt(raw.trim());
            return Math.max(parsed, 1);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
