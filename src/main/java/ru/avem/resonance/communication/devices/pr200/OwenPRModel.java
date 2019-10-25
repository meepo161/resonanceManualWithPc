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

    void setStatesProtections(short statesProtections) {
        notice(PRIM2, (statesProtections & 0b1) > 0);
        notice(PRIM4, (statesProtections & 0b10) > 0);
        notice(PRIM5, (statesProtections & 0b100) > 0);
        notice(PRIM3, (statesProtections & 0b1000) > 0);
        notice(PRIM1, (statesProtections & 0b10000) > 0);
    }

    void setStatesButtons(short statesButtons) {
        notice(PRI7, (statesButtons & 0b1) > 0);
        notice(PRI8, (statesButtons & 0b10) > 0);
        notice(PRIM6, (statesButtons & 0b100) > 0);
        notice(PRIM7, (statesButtons & 0b1000) > 0);
        notice(PRIM8, (statesButtons & 0b10000) > 0);
    }

    void setMode(short mode) {
        notice(PRI1, (mode & 0b1) > 0);
        notice(PRI2, (mode & 0b10) > 0);
        notice(PRI3, (mode & 0b100) > 0);
        notice(PRI4, (mode & 0b1000) > 0);
        notice(PRI5, (mode & 0b10000) > 0);
    }

    private void notice(int param, Object value) {
        setChanged();
        notifyObservers(new Object[]{deviceID, param, value});
    }
}