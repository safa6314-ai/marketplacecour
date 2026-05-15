package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.entities.Event;
import org.example.services.GeocodingService;
import org.example.services.ServiceEvent;
import org.example.services.WeatherService;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendrierEvenements {
    private final ServiceEvent serviceEvent = new ServiceEvent();
    private final DateTimeFormatter eventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final Map<Integer, String> weatherCache = new HashMap<>();
    private YearMonth currentMonth = YearMonth.now();
    private List<Event> events = new ArrayList<>();

    @FXML private Label lblMonth;
    @FXML private Label lblStatus;
    @FXML private GridPane calendarGrid;

    @FXML
    public void initialize() {
        loadEvents();
        renderCalendar();
    }

    @FXML
    private void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
    }

    @FXML
    private void currentMonth() {
        currentMonth = YearMonth.now();
        renderCalendar();
    }

    @FXML
    private void refreshCalendar() {
        loadEvents();
        renderCalendar();
    }

    @FXML
    private void backToReservations() {
        try {
            EventNavigation.loadInCenter(calendarGrid, "/VoirEvenements.fxml");
        } catch (Exception e) {
            showError("Retour impossible : " + e.getMessage());
        }
    }

    private void loadEvents() {
        try {
            events = serviceEvent.afficher();
            weatherCache.clear();
            lblStatus.setText(events.size() + " evenement(s) dans la base Artevia");
        } catch (SQLException e) {
            events = new ArrayList<>();
            lblStatus.setText("Chargement calendrier impossible : " + e.getMessage());
        }
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        lblMonth.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + currentMonth.getYear());

        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of(i + 1);
            Label label = new Label(day.getDisplayName(TextStyle.SHORT, Locale.FRENCH));
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            label.getStyleClass().add("calendar-day-header");
            calendarGrid.add(label, i, 0);
        }

        Map<LocalDate, List<Event>> eventsByDate = events.stream()
                .filter(event -> event.getDateEvent() != null)
                .collect(Collectors.groupingBy(event -> event.getDateEvent().toLocalDate()));

        LocalDate firstDay = currentMonth.atDay(1);
        int firstColumn = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = currentMonth.lengthOfMonth();

        int row = 1;
        int column = firstColumn;
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            VBox cell = createDayCell(date, eventsByDate.getOrDefault(date, List.of()));
            calendarGrid.add(cell, column, row);

            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(LocalDate date, List<Event> dayEvents) {
        VBox cell = new VBox(5);
        cell.getStyleClass().add("calendar-cell");
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-cell-today");
        }

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.getStyleClass().add("calendar-day-number");
        cell.getChildren().add(dayLabel);

        for (Event event : dayEvents.stream().limit(3).toList()) {
            Label eventLabel = new Label(event.getDateEvent().format(eventTimeFormatter) + "  " + event.getTitre());
            eventLabel.getStyleClass().add("calendar-event-chip");
            eventLabel.setMaxWidth(Double.MAX_VALUE);
            eventLabel.setWrapText(true);
            cell.getChildren().add(eventLabel);

            String weather = getWeatherForEvent(event);
            if (!weather.isBlank()) {
                Label weatherLabel = new Label(weather);
                weatherLabel.getStyleClass().add("calendar-weather-label");
                weatherLabel.setMaxWidth(Double.MAX_VALUE);
                weatherLabel.setWrapText(true);
                cell.getChildren().add(weatherLabel);
            }
        }

        if (dayEvents.size() > 3) {
            Label more = new Label("+" + (dayEvents.size() - 3) + " autres");
            more.getStyleClass().add("calendar-more-label");
            cell.getChildren().add(more);
        }

        return cell;
    }

    private String getWeatherForEvent(Event event) {
        if (event == null || event.getLieu() == null || event.getLieu().isBlank() || event.getDateEvent() == null) {
            return "";
        }

        if (weatherCache.containsKey(event.getId())) {
            return weatherCache.get(event.getId());
        }

        String weatherText = "";
        try {
            double[] coordinates = GeocodingService.getCoordinates(event.getLieu());
            if (coordinates != null) {
                WeatherService.WeatherData weather = WeatherService.getWeatherForecast(
                        coordinates[0],
                        coordinates[1],
                        event.getDateEvent().toLocalDate()
                );
                if (weather != null) {
                    weatherText = weather.toDisplayText();
                }
            }
        } catch (Exception e) {
            weatherText = "";
        }

        weatherCache.put(event.getId(), weatherText);
        return weatherText;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Calendrier");
        alert.setHeaderText(null);
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }
}
