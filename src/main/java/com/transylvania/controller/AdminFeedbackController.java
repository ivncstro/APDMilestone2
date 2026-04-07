package com.transylvania.controller;

import com.transylvania.config.SceneNavigator;
import com.transylvania.model.Feedback;
import com.transylvania.service.FeedbackService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class AdminFeedbackController {

    private final FeedbackService feedbackService = new FeedbackService();
    private final ObservableList<FeedbackRow> feedbackRows = FXCollections.observableArrayList();

    @FXML private TextField guestSearchField;
    @FXML private ComboBox<String> ratingCombo;
    @FXML private ComboBox<String> sentimentCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableView<FeedbackRow> feedbackTable;
    @FXML private TableColumn<FeedbackRow, String> guestColumn;
    @FXML private TableColumn<FeedbackRow, String> ratingColumn;
    @FXML private TableColumn<FeedbackRow, String> phoneColumn;
    @FXML private TableColumn<FeedbackRow, LocalDate> dateColumn;
    @FXML private TableColumn<FeedbackRow, String> sentimentColumn;
    @FXML private TableColumn<FeedbackRow, String> visitAgainColumn;
    @FXML private TableColumn<FeedbackRow, String> commentsColumn;
    @FXML private Label averageRatingLabel;
    @FXML private Label totalEntriesLabel;
    @FXML private Label issueSummaryLabel;

    @FXML
    private void initialize() {
        ratingCombo.setItems(FXCollections.observableArrayList("All", "5", "4", "3", "2", "1"));
        ratingCombo.getSelectionModel().selectFirst();
        sentimentCombo.setItems(FXCollections.observableArrayList("All", "N/A"));
        sentimentCombo.getSelectionModel().selectFirst();

        guestColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().guestName()));
        ratingColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().rating()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().phone()));
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().submittedDate()));
        sentimentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().sentimentTag()));
        visitAgainColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().visitAgain()));
        commentsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().comment()));

        feedbackTable.setItems(feedbackRows);
        loadFeedback();
    }

    @FXML
    private void handleApplyFilters() {
        loadFeedback();
    }

    @FXML
    private void handleClearFilters() {
        guestSearchField.clear();
        ratingCombo.getSelectionModel().selectFirst();
        sentimentCombo.getSelectionModel().selectFirst();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        loadFeedback();
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Feedback Summary");
        fileChooser.setInitialFileName("feedback_summary.csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(feedbackTable.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            feedbackService.exportFeedbackCsv(file.getAbsolutePath(), currentFeedback());
            showInfo("Export successful", "Feedback summary exported successfully.");
        } catch (Exception e) {
            showError("Export failed", e.getMessage());
        }
    }

    @FXML private void goToDashboard() { SceneNavigator.goToAdminDashboard(); }
    @FXML private void goToWaitlist() { SceneNavigator.goToAdminWaitlist(); }
    @FXML private void goToLoyalty() { SceneNavigator.goToAdminLoyalty(); }
    @FXML private void goToReports() { SceneNavigator.goToAdminReports(); }
    @FXML private void goToLog() { SceneNavigator.goToAdminLog(); }
    @FXML private void goToPayment() { SceneNavigator.goToAdminPayment(); }
    @FXML private void goToDiscounts() { SceneNavigator.goToAdminDiscounts(); }
    @FXML private void logout() { SceneNavigator.goToAdminLogin(); }

    private void loadFeedback() {
        List<Feedback> matchingFeedback = currentFeedback();

        feedbackRows.setAll(matchingFeedback.stream()
                .map(this::toRow)
                .toList());

        double averageRating = matchingFeedback.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        averageRatingLabel.setText(String.format("Average Rating: %.2f", averageRating));
        totalEntriesLabel.setText("Entries: " + matchingFeedback.size());
        issueSummaryLabel.setText("Issue Tags: Not tracked");
    }

    private List<Feedback> currentFeedback() {
        return feedbackService.searchFeedback(
                guestSearchField.getText(),
                parseRating(ratingCombo.getValue()),
                normalizeSentiment(sentimentCombo.getValue()),
                fromDatePicker.getValue(),
                toDatePicker.getValue()
        );
    }

    private FeedbackRow toRow(Feedback feedback) {
        return new FeedbackRow(
                feedback.getGuest().getFirstName() + " " + feedback.getGuest().getLastName(),
                feedback.getRating() + " Stars",
                feedback.getGuest().getPhone(),
                feedback.getSubmittedDate(),
                feedback.getSentimentTag(),
                feedback.isVisitAgain() ? "Yes" : "No",
                feedback.getComment() == null || feedback.getComment().isBlank() ? "No comment" : feedback.getComment()
        );
    }

    private Integer parseRating(String value) {
        if (value == null || value.equalsIgnoreCase("All")) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private String normalizeSentiment(String value) {
        return value == null || value.equalsIgnoreCase("All") ? null : value;
    }

    private void showInfo(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record FeedbackRow(
            String guestName,
            String rating,
            String phone,
            LocalDate submittedDate,
            String sentimentTag,
            String visitAgain,
            String comment
    ) {}
}
