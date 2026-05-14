package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.entities.Event;
import org.example.entities.Participation;
import org.example.services.ServiceEvent;
import org.example.services.ServiceParticipation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class CalendrierEvenements implements Initializable {

    @FXML private Label lblMonthYear;
    @FXML private GridPane calendarGrid;

    private YearMonth currentYearMonth;
    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final ServiceParticipation serviceParticipation = new ServiceParticipation();
    private List<Event> userEvents = new ArrayList<>();
    private Map<LocalDate, String> holidays = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentYearMonth = YearMonth.now();
        chargerEvenementsUtilisateur();
        chargerJoursFeries(currentYearMonth.getYear());
        drawCalendar();
    }

    private void chargerJoursFeries(int year) {
        Task<Map<LocalDate, String>> holidayTask = new Task<>() {
            @Override protected Map<LocalDate, String> call() throws Exception {
                Map<LocalDate, String> result = new HashMap<>();
                try {
                    // API Nager.Date pour la Tunisie (TN)
                    URL url = new URL("https://date.nager.at/api/v3/PublicHolidays/" + year + "/TN");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    if (conn.getResponseCode() == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder content = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) content.append(line);
                        in.close();
                        
                        JSONArray jsonArray = new JSONArray(content.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            LocalDate date = LocalDate.parse(obj.getString("date"));
                            result.put(date, obj.getString("localName"));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur chargement API Jours fériés: " + e.getMessage());
                }
                return result;
            }
        };

        holidayTask.setOnSucceeded(e -> {
            holidays.putAll(holidayTask.getValue());
            Platform.runLater(this::drawCalendar);
        });

        new Thread(holidayTask).start();
    }

    private void chargerEvenementsUtilisateur() {
        try {
            List<Participation> participations = serviceParticipation.getByPersonne(Home.ID_CLIENT);
            for (Participation p : participations) {
                Event ev = serviceEvent.getById(p.getId_event());
                if (ev != null) {
                    userEvents.add(ev);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        
        // Header Jours de la semaine
        String[] days = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #8a8fa3; -fx-padding: 5;");
            calendarGrid.add(lbl, i, 0);
            GridPane.setHalignment(lbl, javafx.geometry.HPos.CENTER);
        }

        lblMonthYear.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase() 
                             + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayBox = new VBox(5);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setStyle("-fx-border-color: #f1edff; -fx-padding: 5; -fx-min-height: 80;");
            
            Label lblDay = new Label(String.valueOf(day));
            lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e1f2f;");
            dayBox.getChildren().add(lblDay);

            LocalDate date = currentYearMonth.atDay(day);

            // ── Affichage Jour Férié (API) ──
            if (holidays.containsKey(date)) {
                Label holidayLabel = new Label("✨ " + holidays.get(date));
                holidayLabel.setStyle("-fx-text-fill: #f57c00; -fx-font-size: 9px; -fx-font-weight: bold;");
                holidayLabel.setWrapText(true);
                dayBox.getChildren().add(holidayLabel);
                dayBox.setStyle(dayBox.getStyle() + " -fx-background-color: #fff9f0;");
            }

            // ── Affichage Événements (DB) ──
            for (Event ev : userEvents) {
                if (ev.getDate_debut().toLocalDate().equals(date)) {
                    Label eventLabel = new Label(ev.getTitre());
                    eventLabel.setStyle("-fx-background-color: #5b2b91; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 5; -fx-padding: 2 5;");
                    eventLabel.setMaxWidth(100);
                    eventLabel.setEllipsisString("...");
                    
                    Tooltip tooltip = new Tooltip(ev.getTitre() + "\n" + ev.getLieu() + "\n" + ev.getType());
                    Tooltip.install(eventLabel, tooltip);
                    
                    dayBox.getChildren().add(eventLabel);
                }
            }

            calendarGrid.add(dayBox, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }
}
