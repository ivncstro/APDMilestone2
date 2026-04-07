package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import com.transylvania.service.FeedbackService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class KioskFeedback implements BookingAware {

    @FXML private TextField phoneField;
    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    @FXML private Button star5;
    @FXML private Slider ratingSlider;
    @FXML private ToggleGroup visitGroup;
    @FXML private TextArea commentsArea;
    @FXML private Label charCountLabel;
    @FXML private Label Stars;

    private int currentRating = 0;
    private static final int MAX_CHARS = 500;
    private final FeedbackService feedbackService = new FeedbackService();

    @Override
    public void setBookingRequest(BookingRequest request) {
        // feedback remains a standalone screen
    }

    @FXML
    public void initialize() {
        commentsArea.textProperty().addListener((obs, old, val) -> {
            int len = val.length();
            if (len > MAX_CHARS) {
                commentsArea.setText(old);
            } else {
                charCountLabel.setText(len + "/" + MAX_CHARS + " characters");
            }
        });

        ratingSlider.setMin(0);
        ratingSlider.setMax(5);
        ratingSlider.setBlockIncrement(1);
        ratingSlider.valueProperty().addListener((obs, old, val) -> {
            int rating = (int) Math.round(val.doubleValue());
            setRating(rating);
        });
    }

    @FXML private void onStar1() { setRating(1); }
    @FXML private void onStar2() { setRating(2); }
    @FXML private void onStar3() { setRating(3); }
    @FXML private void onStar4() { setRating(4); }
    @FXML private void onStar5() { setRating(5); }

    private void setRating(int rating) {
        currentRating = rating;
        ratingSlider.setValue(rating);
        if (Stars != null) {
            Stars.setText(rating + (rating == 1 ? " Star" : " Stars"));
        }

        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setStyle("-fx-background-color: transparent; -fx-font-size: 24; "
                    + "-fx-text-fill: " + (i < rating ? "#f1c40f" : "#555555") + ";");
        }
    }

    @FXML
    private void onSubmit() {
        String phone = phoneField.getText().trim();
        String comments = commentsArea.getText().trim();
        Toggle selected = visitGroup.getSelectedToggle();
        boolean visitAgain = selected != null
                && "Yes".equals(((RadioButton) selected).getText());

        try {
            feedbackService.submitFeedback(phone, currentRating, visitAgain, comments);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Thank you!");
            alert.setContentText("Your feedback has been submitted.");
            alert.showAndWait();

            SceneNavigator.goToKioskMain();
        } catch (IllegalArgumentException e) {
            alert(e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Feedback failed");
            alert.setContentText("We could not save your feedback right now. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    private void onSkip() {
        SceneNavigator.goToKioskMain();
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
