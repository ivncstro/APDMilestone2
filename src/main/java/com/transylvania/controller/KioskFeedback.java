package com.transylvania.controller;

import com.transylvania.config.BookingRequest;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SceneNavigator.BookingAware;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class KioskFeedback implements BookingAware {

    @FXML private TextField   phoneField;
    @FXML private Button      star1, star2, star3, star4, star5;
    @FXML private Slider      ratingSlider;
    @FXML private ToggleGroup visitGroup;
    @FXML private TextArea    commentsArea;
    @FXML private Label       charCountLabel;

    private int currentRating = 0;
    private static final int MAX_CHARS = 500;

    @Override
    public void setBookingRequest(BookingRequest request) { /* feedback is standalone */ }

    @FXML
    public void initialize() {
        // counter for characters from text
        commentsArea.textProperty().addListener((obs, old, val) -> {
            int len = val.length();
            if (len > MAX_CHARS) {
                commentsArea.setText(old);
            } else {
                charCountLabel.setText(len + "/" + MAX_CHARS + " characters");
            }
        });

        // syncs slider and stars
        ratingSlider.setMin(0);
        ratingSlider.setMax(5);
        ratingSlider.setBlockIncrement(1);
        ratingSlider.valueProperty().addListener((obs, old, val) -> {
            int r = (int) Math.round(val.doubleValue());
            setRating(r);
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
        charCountLabel.getScene();

        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setStyle("-fx-background-color: transparent; -fx-font-size: 24; "
                    + "-fx-text-fill: " + (i < rating ? "#f1c40f" : "#555555") + ";");
        }
    }

    @FXML
    private void onSubmit() {
        String phone    = phoneField.getText().trim();
        String comments = commentsArea.getText().trim();
        Toggle selected = visitGroup.getSelectedToggle();
        boolean visitAgain = selected != null
                && "Yes".equals(((RadioButton) selected).getText());


        if (phone.isEmpty()) {
            alert("Please enter your phone number.");
            return;
        }
        if (currentRating == 0) {
            alert("Please rate your stay (1–5 stars).");
            return;
        }

        // logs and shows success
        System.out.printf("Feedback submitted: phone=%s, rating=%d, visitAgain=%b, comments=%s%n",
                phone, currentRating, visitAgain, comments);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText("Thank you!");
        a.setContentText("Your feedback has been submitted.");
        a.showAndWait();

        SceneNavigator.goToKioskMain();
    }

    @FXML
    private void onSkip() {
        SceneNavigator.goToKioskMain();
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
