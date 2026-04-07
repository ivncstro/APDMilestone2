package com.transylvania;

import com.transylvania.config.DataSeeder;
import com.transylvania.config.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        DataSeeder.seed();
        SceneNavigator.setPrimaryStage(stage);
        stage.setTitle("Hotel Transylvania Kiosk");
        SceneNavigator.goToKioskMain();
    }
}
