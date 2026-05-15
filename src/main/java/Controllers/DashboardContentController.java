package controllers;

import Entites.User;
import Services.UserCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardContentController {

    @FXML
    private Label lblCurrentDate;

    @FXML
    private Label lblTotalUsers;

    @FXML
    private Label lblTotalAdmins;

    @FXML
    private Label lblTotalArtists;

    @FXML
    private Label lblTotalBuyers;

    @FXML
    private Label lblTotalBlocked;

    @FXML
    private BarChart<String, Number> barChartUsers;

    @FXML
    private ListView<String> lvRecentActivity;

    private final UserCRUD UserCRUD = new UserCRUD();

    @FXML
    public void initialize() {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        loadStatistics();
        loadRecentActivity();
    }

    private void loadStatistics() {
        try {
            int totalUsers = UserCRUD.countAllUsers();
            int totalAdmins = UserCRUD.countByRole("ADMIN");
            int totalArtists = UserCRUD.countByRole("ARTIST");
            int totalBuyers = UserCRUD.countByRole("BUYER");
            int totalBlocked = UserCRUD.countBlockedUsers();

            lblTotalUsers.setText(String.valueOf(totalUsers));
            lblTotalAdmins.setText(String.valueOf(totalAdmins));
            lblTotalArtists.setText(String.valueOf(totalArtists));
            lblTotalBuyers.setText(String.valueOf(totalBuyers));
            lblTotalBlocked.setText(String.valueOf(totalBlocked));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Users", totalUsers));
            series.getData().add(new XYChart.Data<>("Admins", totalAdmins));
            series.getData().add(new XYChart.Data<>("Artists", totalArtists));
            series.getData().add(new XYChart.Data<>("Buyers", totalBuyers));
            series.getData().add(new XYChart.Data<>("Blocked", totalBlocked));

            barChartUsers.getData().setAll(series);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur statistiques", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRecentActivity() {
        try {
            List<User> users = UserCRUD.afficher();
            lvRecentActivity.setItems(FXCollections.observableArrayList());

            users.stream()
                    .sorted((u1, u2) -> {
                        if (u1.getCreated_at() == null || u2.getCreated_at() == null) {
                            return 0;
                        }
                        return u2.getCreated_at().compareTo(u1.getCreated_at());
                    })
                    .limit(8)
                    .forEach(User -> lvRecentActivity.getItems().add(
                            User.getUsername() + " - " + User.getRole() + " - " + User.getStatus()
                    ));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur activite", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


