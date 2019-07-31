package ru.avem.resonance.communication.devices.trm;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.resonance.communication.devices.DeviceController.TRM_ID;

public class TRMModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int T_AMBIENT_PARAM = 1;
    public static final int T_ENGINE_PARAM = 2;

    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    TRMModel(Observer observer, int deviceID) {
        addObserver(observer);
        this.deviceID = deviceID;
    }

 void resetResponding() {
        readResponding = true;
        writeResponding = true;
    }

    void setReadResponding(boolean readResponding) {
        this.readResponding = readResponding;
        setResponding();
    }

    void setWriteResponding(boolean writeResponding) {
        this.writeResponding = writeResponding;
        setResponding();
    }

    private void setResponding() {
        notice(RESPONDING_PARAM, readResponding && writeResponding);
    }

    public void setTAmbient(float t) {
        notice(T_AMBIENT_PARAM, t);
    }

    public void setTEngine(float t) {
        notice(T_ENGINE_PARAM, t);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{TRM_ID, param, value});
    }
}