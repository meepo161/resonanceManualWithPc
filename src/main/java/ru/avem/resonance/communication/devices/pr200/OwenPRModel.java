package ru.avem.resonance.communication.devices.pr200;

import java.util.Observable;
import java.util.Observer;

public class OwenPRModel extends Observable {
    public static final int RESPONDING_PARAM = 0;
    public static final int PRI1 = 1;
    public static final int PRI2 = 2;
    public static final int PRI3 = 3;
    public static final int PRI4 = 4;
    public static final int PRI5 = 5;
    public static final int PRI6 = 6;
    public static final int PRI7 = 7;
    public static final int PRI8 = 8;

    public static final int PRIM1 = 9;
    public static final int PRIM2 = 10;
    public static final int PRIM3 = 11;
    public static final int PRIM4 = 12;
    public static final int PRIM5 = 13;
    public static final int PRIM6 = 14;
    public static final int PRIM7 = 15;
    public static final int PRIM8 = 16;

    public static final int PRI1_FIXED = 17;
    public static final int PRI2_FIXED = 18;
    public static final int PRI3_FIXED = 19;
    public static final int PRI4_FIXED = 20;
    public static final int PRI5_FIXED = 21;
    public static final int PRI6_FIXED = 22;
    public static final int PRI7_FIXED = 23;
    public static final int PRI8_FIXED = 24;

    public static final int PRIM1_FIXED = 25;
    public static final int PRIM2_FIXED = 26;
    public static final int PRIM3_FIXED = 27;
    public static final int PRIM4_FIXED = 28;
    public static final int PRIM5_FIXED = 29;
    public static final int PRIM6_FIXED = 30;
    public static final int PRIM7_FIXED = 31;
    public static final int PRIM8_FIXED = 32;

    private int deviceID;
    private boolean readResponding;
    private boolean writeResponding;

    OwenPRModel(Observer observer, int deviceID) {
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

    void setResponding() {
        notice(RESPONDING_PARAM, readResponding && writeResponding);
    }

    void setInstantInputStatus(short instantInputStatusInputStatus) {
        notice(PRI1, (instantInputStatusInputStatus & 0b1) > 0);
        notice(PRI2, (instantInputStatusInputStatus & 0b10) > 0);
        notice(PRI3, (instantInputStatusInputStatus & 0b100) > 0);
        notice(PRI4, (instantInputStatusInputStatus & 0b1000) > 0);
        notice(PRI5, (instantInputStatusInputStatus & 0b10000) > 0);
        notice(PRI6, (instantInputStatusInputStatus & 0b100000) > 0);
        notice(PRI7, (instantInputStatusInputStatus & 0b1000000) > 0);
    }

    void setFixedInputStatus(short fixedInputStatus) {
        notice(PRI1_FIXED, (fixedInputStatus & 0b1) > 0);
        notice(PRI2_FIXED, (fixedInputStatus & 0b10) > 0);
        notice(PRI3_FIXED, (fixedInputStatus & 0b100) > 0);
        notice(PRI4_FIXED, (fixedInputStatus & 0b1000) > 0);
        notice(PRI5_FIXED, (fixedInputStatus & 0b10000) > 0);
        notice(PRI6_FIXED, (fixedInputStatus & 0b100000) > 0);
        notice(PRI7_FIXED, (fixedInputStatus & 0b1000000) > 0);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}