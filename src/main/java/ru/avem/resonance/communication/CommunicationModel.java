package ru.avem.resonance.communication;

import ru.avem.resonance.Constants;
import ru.avem.resonance.communication.connections.Connection;
import ru.avem.resonance.communication.connections.SerialConnection;
import ru.avem.resonance.communication.devices.DeviceController;
import ru.avem.resonance.communication.devices.avem_voltmeter.AvemVoltmeterController;
import ru.avem.resonance.communication.devices.deltaC2000.DeltaCP2000Controller;
import ru.avem.resonance.communication.devices.ikas.IKASController;
import ru.avem.resonance.communication.devices.parmaT400.ParmaT400Controller;
import ru.avem.resonance.communication.devices.phasemeter.PhaseMeterController;
import ru.avem.resonance.communication.devices.pm130.PM130Controller;
import ru.avem.resonance.communication.devices.pr200.OwenPRController;
import ru.avem.resonance.communication.devices.trm.TRMController;
import ru.avem.resonance.communication.modbus.ModbusController;
import ru.avem.resonance.communication.modbus.RTUController;
import ru.avem.resonance.utils.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static ru.avem.resonance.communication.devices.DeviceController.*;
import static ru.avem.resonance.communication.devices.deltaC2000.DeltaCP2000Controller.*;
import static ru.avem.resonance.communication.devices.ikas.IKASController.*;
import static ru.avem.resonance.communication.devices.phasemeter.PhaseMeterController.START_STOP_REGISTER;
import static ru.avem.resonance.communication.devices.pr200.OwenPRController.*;
import static ru.avem.resonance.utils.Utils.sleep;


public class CommunicationModel extends Observable implements Observer {
    public static final Object LOCK = new Object();

    private static CommunicationModel instance = new CommunicationModel();

    private Connection RS485Connection;

    public OwenPRController owenPRController;
    public PM130Controller pm130Controller;
    public AvemVoltmeterController avemVoltmeterController;
    public IKASController ikasController;
    public ParmaT400Controller parmaT400Controller;
    public PhaseMeterController phaseMeterController;
    public DeltaCP2000Controller deltaCP2000Controller;
    //    public FRA800Controller fra800ObjectController;
    public TRMController trmController;

    private int kms1;
    private int kms2;

    private boolean lastOne;
    private boolean isFinished;

    private volatile boolean isDeviceStateOn;

    public List<DeviceController> devicesControllers = new ArrayList<>();

    private CommunicationModel() {

        connectMainBus();
        ModbusController modbusController = new RTUController(RS485Connection);

        pm130Controller = new PM130Controller(1, this, modbusController, PM130_ID);
        devicesControllers.add(pm130Controller);

        parmaT400Controller = new ParmaT400Controller(2, this, modbusController, PARMA400_ID);
        devicesControllers.add(parmaT400Controller);

        avemVoltmeterController = new AvemVoltmeterController(3, this, modbusController, AVEM_ID);
        devicesControllers.add(avemVoltmeterController);

        phaseMeterController = new PhaseMeterController(4, this, modbusController, PHASEMETER_ID);
        devicesControllers.add(phaseMeterController);

        ikasController = new IKASController(5, this, modbusController, IKAS_ID);
        devicesControllers.add(ikasController);

        owenPRController = new OwenPRController(6, this, modbusController, PR200_ID);
        devicesControllers.add(owenPRController);

        trmController = new TRMController(7, this, modbusController, TRM_ID);
        devicesControllers.add(trmController);

        deltaCP2000Controller = new DeltaCP2000Controller(11, this, modbusController, DELTACP2000_ID);
        devicesControllers.add(deltaCP2000Controller);

        new Thread(() -> {
            while (!isFinished) {
                for (DeviceController deviceController : devicesControllers) {
                    if (deviceController.isNeedToRead()) {
                        if (deviceController instanceof PM130Controller) {
                            for (int i = 1; i <= 4; i++) {
                                deviceController.read(i);
                            }
                        } else if (deviceController instanceof ParmaT400Controller) {
                            for (int i = 1; i <= 4; i++) {
                                deviceController.read(i);
                            }
                        } else {
                            deviceController.read();
                        }
                        if (deviceController instanceof OwenPRController) {
                            resetDog();
                        }
                    }
                    if (isDeviceStateOn) {
                        deviceController.resetAllDeviceStateOnAttempts();
                    }
                }
                sleep(1);
            }
        }).start();
    }

    public static CommunicationModel getInstance() {
        return instance;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    private void notice(int deviceID, int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);
        notice(modelId, param, value);
    }

    public void setNeedToReadAllDevices(boolean isNeed) {
        owenPRController.setNeedToRead(isNeed);
        deltaCP2000Controller.setNeedToRead(isNeed);
        ikasController.setNeedToRead(isNeed);
        parmaT400Controller.setNeedToRead(isNeed);
        phaseMeterController.setNeedToRead(isNeed);
        pm130Controller.setNeedToRead(isNeed);
        trmController.setNeedToRead(isNeed);
    }

    public void setNeedToReadForDebug(boolean isNeed) {
        owenPRController.setNeedToRead(isNeed);
        deltaCP2000Controller.setNeedToRead(isNeed);
    }

    public void resetAllDevices() {
        owenPRController.resetAllAttempts();
        deltaCP2000Controller.resetAllAttempts();
        ikasController.resetAllAttempts();
        parmaT400Controller.resetAllAttempts();
        phaseMeterController.resetAllAttempts();
        pm130Controller.resetAllAttempts();
        trmController.resetAllAttempts();
    }

    private void connectMainBus() {
        RS485Connection = new SerialConnection(
                Constants.Communication.RS485_DEVICE_NAME,
                Constants.Communication.BAUDRATE,
                Constants.Communication.DATABITS,
                Constants.Communication.STOPBITS,
                Constants.Communication.PARITY,
                Constants.Communication.WRITE_TIMEOUT,
                Constants.Communication.READ_TIMEOUT);
        Logger.withTag("DEBUG_TAG").log("connectMainBus");
        if (!RS485Connection.isInitiatedConnection()) {
            Logger.withTag("DEBUG_TAG").log("!isInitiatedMainBus");
            RS485Connection.closeConnection();
            RS485Connection.initConnection();
        }
    }

    public void deinitPR() {
        owenPRController.write(RES_REGISTER, 1, 0);
    }

    public void finalizeAllDevices() {
        for (DeviceController deviceController : devicesControllers) {
            deviceController.setNeedToRead(false);
        }
    }

    private void resetDog() {
        if (lastOne) {
            owenPRController.write(RESET_DOG, 1, 0);
            lastOne = false;
        } else {
            owenPRController.write(RESET_DOG, 1, 1);
            lastOne = true;
        }
    }

    private void resetTimer() {
        lastOne = true;

        owenPRController.write(RESET_DOG, 1, 0);
        owenPRController.write(RESET_DOG, 1, 1);
        owenPRController.write(RESET_TIMER, 1, 1);
        owenPRController.write(RESET_TIMER, 1, 0);
    }

    public void offAllKms() {
        kms1 = 0;
        writeToKms1Register(kms1);
        kms2 = 0;
        writeToKms2Register(kms2);
    }

    public void onAllKms() {
        kms1 = 1;
        writeToKms1Register(kms1);
        kms2 = 2;
        writeToKms2Register(kms2);
    }

    private void writeToKms1Register(int value) {
        owenPRController.write(KMS1_REGISTER, 1, value);
    }

    private void writeToKms2Register(int value) {
        owenPRController.write(KMS2_REGISTER, 1, value);
    }

    public void onRegisterInTheKms(int numberOfRegister, int kms) {
        int mask = (int) Math.pow(2, --numberOfRegister);
        try {
            int kmsField = CommunicationModel.class.getDeclaredField("kms" + kms).getInt(this);
            kmsField |= mask;
            CommunicationModel.class.getDeclaredMethod(String.format("%s%d%s", "writeToKms", kms, "Register"), int.class).invoke(this, kmsField);
            CommunicationModel.class.getDeclaredField("kms" + kms).set(this, kmsField);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        Logger.withTag("DEBUG_TAG").log("numberOfRegister=" + numberOfRegister + " kms=" + kms);
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2);
    }

    public void offRegisterInTheKms(int numberOfRegister, int kms) {
        int mask = ~(int) Math.pow(2, --numberOfRegister);
        try {
            int kmsField = CommunicationModel.class.getDeclaredField("kms" + kms).getInt(this);
            kmsField &= mask;
            CommunicationModel.class.getDeclaredMethod(String.format("%s%d%s", "writeToKms", kms, "Register"), int.class).invoke(this, kmsField);
            CommunicationModel.class.getDeclaredField("kms" + kms).set(this, kmsField);
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
        }
        Logger.withTag("DEBUG_TAG").log("numberOfRegister=" + numberOfRegister + " kms=" + kms);
        Logger.withTag("DEBUG_TAG").log("1=" + kms1 + " 2=" + kms2);
    }

    public void initOwenPrController() {
        owenPRController.resetAllAttempts();
        owenPRController.setNeedToRead(true);
        offAllKms();
        resetTimer();
        owenPRController.write(RES_REGISTER, 1, 1);
    }

    public void startPhaseMeter() {
        phaseMeterController.write(START_STOP_REGISTER, (short) 0x01);
    }

    public void startMeasuringAB() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_AB);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startMeasuringBC() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_BC);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startMeasuringAC() {
        ikasController.write(MEASURABLE_TYPE_REGISTER, MEASURABLE_TYPE_AC);
        sleep(1 * 1000);
        ikasController.write(START_MEASURABLE_REGISTER, 0x01);
    }

    public void startObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b10);
    }

    public void startReversObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b10_00_10);
    }

    public void stopObject() {
        deltaCP2000Controller.write(CONTROL_REGISTER, 1, 0b1);
    }

    public void setObjectParams(int fOut, int voltageP1, int fP1) {
        deltaCP2000Controller.write(MAX_VOLTAGE_REGISTER, 1, 400 * 10);
        deltaCP2000Controller.write(MAX_FREQUENCY_REGISTER, 1, 210 * 100);
        deltaCP2000Controller.write(NOM_FREQUENCY_REGISTER, 1, 210 * 100);
        deltaCP2000Controller.write(CURRENT_FREQUENCY_OUTPUT_REGISTER, 1, fOut);
        deltaCP2000Controller.write(POINT_1_VOLTAGE_REGISTER, 1, voltageP1);
        deltaCP2000Controller.write(POINT_1_FREQUENCY_REGISTER, 1, fP1);
        deltaCP2000Controller.write(POINT_2_VOLTAGE_REGISTER, 1, 40);
        deltaCP2000Controller.write(POINT_2_FREQUENCY_REGISTER, 1, 50);
    }

    public void setObjectFCur(int fCur) {
        deltaCP2000Controller.write(CURRENT_FREQUENCY_OUTPUT_REGISTER, 1, fCur);
    }

    public void setObjectUMax(int voltageMax) {
        deltaCP2000Controller.write(POINT_1_VOLTAGE_REGISTER, 1, voltageMax);
    }


    public void initExperiment1Devices() {
    }

    public void initExperiment2Devices() {
        resetTimer();
        ikasController.setNeedToRead(true);
        ikasController.resetAllAttempts();
        trmController.setNeedToRead(true);
        trmController.resetAllAttempts();
    }

    public void initExperiment3Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        parmaT400Controller.setNeedToRead(true);
        parmaT400Controller.resetAllAttempts();
        phaseMeterController.setNeedToRead(true);
        phaseMeterController.resetAllAttempts();
    }

    public void initExperiment4Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment5Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
    }

    public void initExperiment6Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
    }

    public void initExperiment7Devices() {
        resetTimer();
        pm130Controller.setNeedToRead(true);
        pm130Controller.resetAllAttempts();
        deltaCP2000Controller.setNeedToRead(true);
        deltaCP2000Controller.resetAllAttempts();
        avemVoltmeterController.setNeedToRead(true);
        avemVoltmeterController.resetAllAttempts();
    }

    public void onKM2() {
        onRegisterInTheKms(1, 1);
    }

    public void onKM3() {
        onRegisterInTheKms(2, 1);
    }

    public void onKM4() {
        onRegisterInTheKms(3, 1);
    }

    public void onKM5() {
        onRegisterInTheKms(4, 1);
    }

    public void onKM6() {
        onRegisterInTheKms(5, 1);
    }

    public void onKM7() {
        onRegisterInTheKms(6, 1);
    }

    public void onKM11() {
        onRegisterInTheKms(7, 1);
    }

    public void onKM12() {
        onRegisterInTheKms(8, 1);
    }

    public void onKM13() {
        onRegisterInTheKms(1, 2);
    }

    public void onK10() {
        onRegisterInTheKms(2, 2);
    }

    public void onK9() {
        onRegisterInTheKms(3, 2);
    }

    public void onPR4M1() {
        onRegisterInTheKms(4, 2);
    }

    public void onPR5M1() {
        onRegisterInTheKms(5, 2);
    }

    public void onK8() {
        onRegisterInTheKms(6, 2);
    }

    public void onPR7M1() {
        onRegisterInTheKms(7, 2);
    }

    public void onPR8M1() {
        onRegisterInTheKms(8, 2);
    }


    public void offPR1() {
        offRegisterInTheKms(1, 1);
    }

    public void offPR2() {
        offRegisterInTheKms(2, 1);
    }

    public void offPR3() {
        offRegisterInTheKms(3, 1);
    }

    public void offKM5() {
        offRegisterInTheKms(4, 1);
    }

    public void offKM6() {
        offRegisterInTheKms(5, 1);
    }

    public void offKM7() {
        offRegisterInTheKms(6, 1);
    }

    public void offPR7() {
        offRegisterInTheKms(7, 1);
    }

    public void offPR8() {
        offRegisterInTheKms(8, 1);
    }

    public void offKM1M1() {
        offRegisterInTheKms(1, 2);
    }

    public void offPR2M1() {
        offRegisterInTheKms(2, 2);
    }

    public void offPR3M1() {
        offRegisterInTheKms(3, 2);
    }

    public void offPR4M1() {
        offRegisterInTheKms(4, 2);
    }

    public void offPR5M1() {
        offRegisterInTheKms(5, 2);
    }

    public void offPR6M1() {
        offRegisterInTheKms(6, 2);
    }

    public void offPR7M1() {
        offRegisterInTheKms(7, 2);
    }

    public void offPR8M1() {
        offRegisterInTheKms(8, 2);
    }

    public void setDeviceStateOn(boolean deviceStateOn) {
        isDeviceStateOn = deviceStateOn;
    }
}
