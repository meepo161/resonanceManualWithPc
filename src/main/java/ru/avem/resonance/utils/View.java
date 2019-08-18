package ru.avem.resonance.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.util.Optional;

public class View {

    private View() {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface Actionable {
        void onAction();
    }

    public static void showConfirmDialog(String text, Actionable actionYes, Actionable actionNo) {
        showConfirmDialog("ВНИМАНИЕ", text, actionYes, actionNo);
    }

    public static void showConfirmDialog(String title, String text, Actionable actionYes, Actionable actionNo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(text);

        ButtonType buttonTypeYes = new ButtonType("Да", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Нет", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            actionYes.onAction();
        } else if (result.isPresent() && result.get() == buttonTypeNo) {
            actionNo.onAction();
        }
    }

    public enum DeviceState {
        UNDEFINED,
        RESPONDING,
        NOT_RESPONDING
    }

    public static void setDeviceState(Circle deviceStatus, DeviceState deviceState) {
        Platform.runLater(() -> {
            switch (deviceState) {
                case UNDEFINED:
                    deviceStatus.setFill(Color.DODGERBLUE);
                    break;
                case RESPONDING:
                    deviceStatus.setFill(Color.LIME);
                    break;
                case NOT_RESPONDING:
                    deviceStatus.setFill(Color.RED);
                    break;
            }
        });
    }
}
