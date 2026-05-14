package controllers;

import Entites.User;
import Services.UserCRUD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminUsersController {

    @FXML
    private ListView<User> lvUsers;

    @FXML
    private TextField tfSearch;

    @FXML
    private ComboBox<String> cbRoleFilter;

    @FXML
    private ComboBox<String> cbStatusFilter;

    @FXML
    private ComboBox<String> cbSort;

    @FXML
    private Label lblFilteredUsers;

    @FXML
    private Label lblFilteredAdmins;

    @FXML
    private Label lblFilteredBlocked;

    private final UserCRUD UserCRUD = new UserCRUD();
    private final ObservableList<User> masterUsers = FXCollections.observableArrayList();
    private final ObservableList<User> filteredUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initializeCombos();
        initializeListView();
        initializeListeners();
        loadUsers();
    }

    @FXML
    private void openAddPopup() {
        openUserPopup(null);
    }

    @FXML
    private void refreshUsers() {
        loadUsers();
    }

    private void initializeCombos() {
        cbRoleFilter.getItems().setAll("Tous", "ADMIN", "ARTIST", "BUYER", "TRAINER", "ORGANIZER");
        cbStatusFilter.getItems().setAll("Tous", "ACTIVE", "BLOCKED");
        cbSort.getItems().setAll("Username A-Z", "Username Z-A");

        cbRoleFilter.setValue("Tous");
        cbStatusFilter.setValue("Tous");
        cbSort.setValue("Username A-Z");
    }

    private void initializeListView() {
        lvUsers.setItems(filteredUsers);
        lvUsers.setCellFactory(listView -> new UserCardCell(this));
    }

    private void initializeListeners() {
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbRoleFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        cbSort.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void loadUsers() {
        try {
            masterUsers.setAll(UserCRUD.afficher());
            applyFilters();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur chargement", e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFilters() {
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String role = cbRoleFilter.getValue();
        String status = cbStatusFilter.getValue();

        List<User> result = masterUsers.stream()
                .filter(User -> keyword.isEmpty()
                        || safe(User.getUsername()).toLowerCase().contains(keyword)
                        || safe(User.getEmail()).toLowerCase().contains(keyword))
                .filter(User -> role == null || "Tous".equals(role) || role.equalsIgnoreCase(User.getRole()))
                .filter(User -> status == null || "Tous".equals(status) || status.equalsIgnoreCase(User.getStatus()))
                .sorted(getComparator())
                .collect(Collectors.toList());

        filteredUsers.setAll(result);
        updateQuickStats();
    }

    private Comparator<User> getComparator() {
        if ("Username Z-A".equals(cbSort.getValue())) {
            return Comparator.comparing((User User) -> safe(User.getUsername()), String.CASE_INSENSITIVE_ORDER).reversed();
        }

        return Comparator.comparing((User User) -> safe(User.getUsername()), String.CASE_INSENSITIVE_ORDER);
    }

    private void updateQuickStats() {
        long admins = filteredUsers.stream().filter(User -> "ADMIN".equalsIgnoreCase(User.getRole())).count();
        long blocked = filteredUsers.stream().filter(User -> "BLOCKED".equalsIgnoreCase(User.getStatus())).count();

        lblFilteredUsers.setText(String.valueOf(filteredUsers.size()));
        lblFilteredAdmins.setText(String.valueOf(admins));
        lblFilteredBlocked.setText(String.valueOf(blocked));
    }

    private void openEditPopup(User User) {
        if (User == null) {
            showAlert(Alert.AlertType.WARNING, "Selection obligatoire", "Veuillez selectionner un utilisateur.");
            return;
        }

        openUserPopup(User);
    }

    private void deleteUser(User User) {
        if (User == null) {
            showAlert(Alert.AlertType.WARNING, "Selection obligatoire", "Veuillez selectionner un utilisateur.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UserCRUD.supprimer(User.getId());
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Succes", "Utilisateur supprime avec succes.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur suppression", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void openUserPopup(User userToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserFormPopup.fxml"));
            Parent root = loader.load();

            UserFormPopupController controller = loader.getController();
            controller.setUser(userToEdit);

            Stage popupStage = new Stage();
            popupStage.setTitle(userToEdit == null ? "Ajouter membre" : "Modifier membre");
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL);

            Window owner = lvUsers.getScene() == null ? null : lvUsers.getScene().getWindow();
            if (owner != null) {
                popupStage.initOwner(owner);
            }

            controller.setStage(popupStage);
            popupStage.showAndWait();

            if (controller.isSaved()) {
                loadUsers();
                lvUsers.getSelectionModel().clearSelection();
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur pop-up", e.getMessage());
            e.printStackTrace();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class UserCardCell extends ListCell<User> {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        private final AdminUsersController parentController;

        private UserCardCell(AdminUsersController parentController) {
            this.parentController = parentController;
        }

        @Override
        protected void updateItem(User User, boolean empty) {
            super.updateItem(User, empty);

            if (empty || User == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label avatar = new Label(getInitials(User));
            avatar.getStyleClass().add("User-card-avatar");

            Label username = new Label(safeStatic(User.getUsername()));
            username.getStyleClass().add("User-card-title");

            Label email = new Label(safeStatic(User.getEmail()));
            email.getStyleClass().add("User-card-email");

            String fullNameValue = (safeStatic(User.getFirst_name()) + " " + safeStatic(User.getLast_name())).trim();
            Label meta = new Label(fullNameValue + "  |  " + formatDate(User.getCreated_at()));
            meta.getStyleClass().add("User-card-text");

            VBox identity = new VBox(3, username, email, meta);
            identity.getStyleClass().add("User-card-left");
            identity.setMinWidth(220);
            identity.setMaxWidth(360);

            Label role = new Label(safeStatic(User.getRole()));
            role.getStyleClass().add("User-card-chip");

            Label status = new Label(safeStatic(User.getStatus()));
            status.getStyleClass().add("ACTIVE".equalsIgnoreCase(User.getStatus()) ? "User-card-status-active" : "User-card-status-blocked");

            HBox badges = new HBox(8, role, status);
            badges.getStyleClass().add("User-card-badges");

            Button editButton = new Button("Modifier");
            editButton.setGraphic(new FontIcon("fas-edit"));
            editButton.getStyleClass().add("card-edit-button");
            editButton.setOnAction(event -> {
                event.consume();
                parentController.openEditPopup(User);
            });

            Button deleteButton = new Button("Supprimer");
            deleteButton.setGraphic(new FontIcon("fas-trash"));
            deleteButton.getStyleClass().add("card-delete-button");
            deleteButton.setOnAction(event -> {
                event.consume();
                parentController.deleteUser(User);
            });

            HBox actions = new HBox(8, editButton, deleteButton);
            actions.getStyleClass().add("User-card-actions");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox content = new HBox(14, avatar, identity, badges, spacer, actions);
            content.getStyleClass().add("User-card");

            setText(null);
            setGraphic(content);
        }

        private static String getInitials(User User) {
            String firstName = safeStatic(User.getFirst_name());
            String lastName = safeStatic(User.getLast_name());
            String username = safeStatic(User.getUsername());

            String first = !firstName.isEmpty() ? firstName.substring(0, 1) : "";
            String last = !lastName.isEmpty() ? lastName.substring(0, 1) : "";

            if (!(first + last).isEmpty()) {
                return (first + last).toUpperCase();
            }

            if (!username.isEmpty()) {
                return username.substring(0, 1).toUpperCase();
            }

            return "U";
        }

        private static String safeStatic(String value) {
            return value == null ? "" : value;
        }

        private static String formatDate(Timestamp timestamp) {
            if (timestamp == null) {
                return "Date inconnue";
            }

            return timestamp.toLocalDateTime().format(DATE_FORMATTER);
        }
    }
}


