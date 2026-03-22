package com.transylvania.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class SceneNavigator {

    public static final String KIOSK_MAIN     = "fxml/kiosk/KioskMain.fxml";
    public static final String KIOSK_DATES    = "fxml/kiosk/KioskDates.fxml";
    public static final String KIOSK_GUESTS   = "fxml/kiosk/KioskGuestCount.fxml";
    public static final String KIOSK_ROOMS    = "fxml/kiosk/KioskRoomDetails.fxml";
    public static final String KIOSK_DETAILS  = "fxml/kiosk/KioskDetails.fxml";
    public static final String KIOSK_ADDONS   = "fxml/kiosk/KioskAddon.fxml";
    public static final String KIOSK_TOTAL    = "fxml/kiosk/KioskTotal.fxml";
    public static final String KIOSK_CONFIRM  = "fxml/kiosk/KioskConfirmation.fxml";
    public static final String FEEDBACK       = "fxml/kiosk/FeedbackKiosk.fxml";

    public static final String ADMIN_LOGIN     = "fxml/admin/AdminLogin.fxml";
    public static final String ADMIN_DASHBOARD = "fxml/admin/AdminDashboard.fxml";
    public static final String ADMIN_WAITLIST  = "fxml/admin/AdminWaitlist.fxml";
    public static final String ADMIN_LOYALTY   = "fxml/admin/AdminLoyalty.fxml";
    public static final String ADMIN_REPORTS   = "fxml/admin/AdminReports.fxml";
    public static final String ADMIN_LOG       = "fxml/admin/AdminLog.fxml";
    public static final String ADMIN_PAYMENT   = "fxml/admin/AdminPayment.fxml";
    public static final String ADMIN_DISCOUNTS = "fxml/admin/AdminDiscounts.fxml";
    public static final String ADMIN_FEEDBACK  = "fxml/admin/AdminFeedback.fxml";

    private static Stage         primaryStage;
    private static BookingRequest currentBooking;

    public static void setPrimaryStage(Stage stage) { primaryStage = stage; }

    public static <C> void navigateTo(String fxmlPath, Consumer<C> extra) {
        try {
            String resourcePath = fxmlPath.startsWith("/") ? fxmlPath : "/" + fxmlPath;
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(resourcePath));
            Parent root = loader.load();
            C controller = loader.getController();
            if (controller instanceof BookingAware) {
                ((BookingAware) controller).setBookingRequest(currentBooking);
            }
            if (extra != null) extra.accept(controller);
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.sizeToScene();
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load FXML: " + fxmlPath, e);
        }
    }

    public static void navigateTo(String fxmlPath) { navigateTo(fxmlPath, null); }

    public static void startNewBooking() { currentBooking = new BookingRequest(); }

    public static void goToKioskMain()    { startNewBooking(); navigateTo(KIOSK_MAIN); }
    public static void goToDates()        { navigateTo(KIOSK_DATES); }
    public static void goToGuestCount()   { navigateTo(KIOSK_GUESTS); }
    public static void goToRoomDetails()  { navigateTo(KIOSK_ROOMS); }
    public static void goToDetails()      { navigateTo(KIOSK_DETAILS); }
    public static void goToAddons()       { navigateTo(KIOSK_ADDONS); }
    public static void goToTotal()        { navigateTo(KIOSK_TOTAL); }
    public static void goToFeedback()     { navigateTo(FEEDBACK); }
    public static void goToConfirmation(String code) {
        navigateTo(KIOSK_CONFIRM, (ConfirmationAware c) -> c.setConfirmationCode(code));
    }

    public static void goToAdminLogin()     { navigateTo(ADMIN_LOGIN); }
    public static void goToAdminDashboard() { navigateTo(ADMIN_DASHBOARD); }
    public static void goToAdminWaitlist()  { navigateTo(ADMIN_WAITLIST); }
    public static void goToAdminLoyalty()   { navigateTo(ADMIN_LOYALTY); }
    public static void goToAdminReports()   { navigateTo(ADMIN_REPORTS); }
    public static void goToAdminLog()       { navigateTo(ADMIN_LOG); }
    public static void goToAdminPayment()   { navigateTo(ADMIN_PAYMENT); }
    public static void goToAdminDiscounts() { navigateTo(ADMIN_DISCOUNTS); }
    public static void goToAdminFeedback()  { navigateTo(ADMIN_FEEDBACK); }

    public interface BookingAware { void setBookingRequest(BookingRequest request); }
    public interface ConfirmationAware { void setConfirmationCode(String code); }
}