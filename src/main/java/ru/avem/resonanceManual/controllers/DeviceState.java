package ru.avem.resonanceManual.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ru.avem.resonanceManual.communication.devices.avem_voltmeter.AvemVoltmeterModel;
import ru.avem.resonanceManual.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.resonanceManual.communication.devices.latr.LatrModel;
import ru.avem.resonanceManual.communication.devices.pm130.PM130Model;
import ru.avem.resonanceManual.communication.devices.pr200.OwenPRModel;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.resonanceManual.communication.devices.DeviceController.*;

public class DeviceState implements Observer {

    @FXML
    protected Circle deviceStateCirclePM130;
    @FXML
    protected Circle deviceStateCircleAvem;
    @FXML
    protected Circle deviceStateCirclePR200;
    @FXML
    protected Circle deviceStateCircleDelta;
    @FXML
    protected Circle deviceStateCircleLatr;
    @FXML
    protected Circle deviceStateCircleKiloAvem;


    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);
        switch (modelId) {
            case PM130_ID:
                if (param == PM130Model.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case PR200_ID:
                if (param == OwenPRModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case AVEM_ID:
                if (param == AvemVoltmeterModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleAvem.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case DELTACP2000_ID:
                if (param == DeltaCP2000Model.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleDelta.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case LATR_ID:
                if (param == LatrModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleLatr.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case KILOAVEM_ID:
                if (param == AvemVoltmeterModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleKiloAvem.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
        }
    }
}
