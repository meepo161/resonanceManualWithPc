package ru.avem.resonance.controllers;

import javafx.fxml.FXML;
import javafx.scene.shape.Circle;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.resonance.communication.devices.DeviceController.PR200_ID;

public class CurrentProtection implements Observer {
    @FXML
    private Circle deviceStateKM1;
    @FXML
    private Circle deviceStateKM2;
    @FXML
    private Circle deviceStateKM3;
    @FXML
    private Circle deviceStateKM4;
    @FXML
    private Circle deviceStateKM5;
    @FXML
    private Circle deviceStateKM6;
    @FXML
    private Circle deviceStateKM7;
    @FXML
    private Circle deviceStateKM8;
    @FXML
    private Circle deviceStateKM9;
    @FXML
    private Circle deviceStateKM10;
    @FXML
    private Circle deviceStateKM11;
    @FXML
    private Circle deviceStateKM12;
    @FXML
    private Circle deviceStateKM13;
    @FXML
    private Circle deviceStateKM14;
    @FXML
    private Circle deviceStateKM15;

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
            case PR200_ID:
                switch (param) {
//                    case OwenPRModel.DI1_DOORS_FIXED:
//                        Platform.runLater(() -> deviceStateKM1.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case OwenPRModel.DI2_OI_FIXED:
//                        Platform.runLater(() -> deviceStateKM2.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case OwenPRModel.DI3_PROTECTION_FIXED:
//                        Platform.runLater(() -> deviceStateKM3.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case OwenPRModel.DI5_K1_2_FIXED:
//                        Platform.runLater(() -> deviceStateKM5.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case OwenPRModel.DI6_KM1_2_FIXED:
//                        Platform.runLater(() -> deviceStateKM6.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case OwenPRModel.DI7_KM2_2_FIXED:
//                        Platform.runLater(() -> deviceStateKM7.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
                }
        }
    }
}
