package ru.avem.resonance.model;

import java.io.Serializable;

public class Point implements Serializable {
    private double measuringUOut;
    private double measuringIC;
    private String measuringTime;

    public Point(double measuringUOut, double measuringIC, String measuringTime) {
        this.measuringUOut = measuringUOut;
        this.measuringIC = measuringIC;
        this.measuringTime = measuringTime;
    }

    public double getMeasuringUOut() {
        return measuringUOut;
    }

    public double getMeasuringIC() {
        return measuringIC;
    }

    public String getMeasuringTime() {
        return measuringTime;
    }

    @Override
    public String toString() {
        return "Point{" +
                "measuringUOut=" + measuringUOut +
                ", measuringIC=" + measuringIC +
                ", measuringTime=" + measuringTime +
                '}';
    }
}
