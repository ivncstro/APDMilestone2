package com.transylvania;

import com.transylvania.config.SceneNavigator;
import com.transylvania.service.ReservationService;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.setPrimaryStage(stage);
        stage.setTitle("Hotel Transilvania Kiosk");
        SceneNavigator.goToKioskMain();
    }
}