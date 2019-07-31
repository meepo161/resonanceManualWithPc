package ru.avem.resonance.communication.devices.phasemeter;

import java.util.Observable;
import java.util.Observer;

public class PhaseMeterModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int PERIOD_TIME0_PARAM = 1;
    public static final int IMPULSE_TIME0_PARAM = 2;
    public static final int PERIOD_TIME1_PARAM = 3;
    public static final int IMPULSE_TIME1_PARAM = 4;
    public static final int CPU_TIME_DELAY_PARAM = 5;
    public static final int WINDING_GROUP0_PARAM = 6;
    public static final int WINDING_GROUP1_PARAM = 7;
    public static final int START_STOP_PARAM = 8;
//    public static final int ID_SERIAL_NUMBER_PARAM = 9; useless, не работает
    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    PhaseMeterModel(Observer observer, int deviceID) {
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

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }

    public void setPeriodTime0(int periodTime0) {
        notice(PERIOD_TIME0_PARAM, periodTime0);
    }

    public void setImpulsTime0(int impulsTime0) {
        notice(IMPULSE_TIME0_PARAM, impulsTime0);
    }

    public void setPeriodTime1(int periodTime1) {
        notice(PERIOD_TIME1_PARAM, periodTime1);
    }

    public void setImpulsTime1(int impulsTime1) {
        notice(IMPULSE_TIME1_PARAM, impulsTime1);
    }

    public void setCPUTimeDelay(int CPUTimeDelay) {
        notice(CPU_TIME_DELAY_PARAM, CPUTimeDelay);
    }

    public void setWindingGroup0(short windingGroup0) {
        notice(WINDING_GROUP0_PARAM, windingGroup0);
    }

    public void setWindingGroup1(short windingGroup1) {
        notice(WINDING_GROUP1_PARAM, windingGroup1);
    }

    public void setStartStop(short startStop) {
        notice(START_STOP_PARAM, startStop);
    }

}