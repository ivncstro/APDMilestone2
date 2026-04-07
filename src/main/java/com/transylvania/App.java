package com.transylvania;

import com.transylvania.config.DataSeeder;
import com.transylvania.config.SceneNavigator;
import com.transylvania.config.SecurityUtil;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        DataSeeder.seed();
        // to generate hashed password for test
        // System.out.println(SecurityUtil.hashPassword("admin"));
        SceneNavigator.setPrimaryStage(stage);
        stage.setTitle("Hotel Transylvania Kiosk");
        SceneNavigator.goToKioskMain();
    }
}
