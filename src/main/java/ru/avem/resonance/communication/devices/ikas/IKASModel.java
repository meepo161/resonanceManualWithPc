package ru.avem.resonance.communication.devices.ikas;

import java.util.Observable;
import java.util.Observer;

public class IKASModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int READY_PARAM = 1;
    public static final int MEASURABLE_PARAM = 2;
    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    IKASModel(Observer observer, int deviceID) {
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

    public void setReady(float ready) {
        notice(READY_PARAM, ready);
    }

    public void setMeasurable(float measurable) {
        notice(MEASURABLE_PARAM, measurable);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}