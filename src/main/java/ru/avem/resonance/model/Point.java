package ru.avem.resonance.model;

import java.io.Serializable;

public class Point implements Serializable {
    private double measuringUA;
    private double measuringUB;
    private double measuringUC;
    private double measuringIA;
    private double measuringIB;
    private double measuringIC;
    private double measuringTorque;
    private double measuringRotation;
    private double measuringF;
    private double measuringS;
    private double measuringP;
    private double measuringEfficiency;
    private float trmTemperature;
    private double measuringTime;
    private double measuringCos;
    private double measuringSlip;

    public Point(double measuringUA, double measuringUB, double measuringUC, double measuringIA, double measuringIB,
                 double measuringIC, double measuringTorque, double measuringRotation, double measuringF, double measuringS,
                 double measuringP, double measuringEfficiency, float trmTemperature, double measuringTime, double measuringCos, double measuringSlip) {
        this.measuringUA = measuringUA;
        this.measuringUB = measuringUB;
        this.measuringUC = measuringUC;
        this.measuringIA = measuringIA;
        this.measuringIB = measuringIB;
        this.measuringIC = measuringIC;
        this.measuringTorque = measuringTorque;
        this.measuringRotation = measuringRotation;
        this.measuringF = measuringF;
        this.measuringS = measuringS;
        this.measuringP = measuringP;
        this.measuringEfficiency = measuringEfficiency;
        this.trmTemperature = trmTemperature;
        this.measuringTime = measuringTime;
        this.measuringCos = measuringCos;
        this.measuringSlip = measuringSlip;
    }

    public double getMeasuringUA() {
        return measuringUA;
    }

    public double getMeasuringUB() {
        return measuringUB;
    }

    public double getMeasuringUC() {
        return measuringUC;
    }

    public double getMeasuringIA() {
        return measuringIA;
    }

    public double getMeasuringIB() {
        return measuringIB;
    }

    public double getMeasuringIC() {
        return measuringIC;
    }

    public double getMeasuringTorque() {
        return measuringTorque;
    }

    public double getMeasuringRotation() {
        return measuringRotation;
    }

    public double getMeasuringF() {
        return measuringF;
    }

    public double getMeasuringS() {
        return measuringS;
    }

    public double getMeasuringP() {
        return measuringP;
    }

    public double getMeasuringEfficiency() {
        return measuringEfficiency;
    }

    public float getTrmTemperature() {
        return trmTemperature;
    }

    public double getMeasuringTime() {
        return measuringTime;
    }

    public double getMeasuringCos() {
        return measuringCos;
    }

    public double getMeasuringSlip() {
        return measuringSlip;
    }

    @Override
    public String toString() {
        return "Point{" +
                "measuringUA=" + measuringUA +
                ", measuringUB=" + measuringUB +
                ", measuringUC=" + measuringUC +
                ", measuringIA=" + measuringIA +
                ", measuringIB=" + measuringIB +
                ", measuringIC=" + measuringIC +
                ", measuringTorque=" + measuringTorque +
                ", measuringRotation=" + measuringRotation +
                ", measuringF=" + measuringF +
                ", measuringS=" + measuringS +
                ", measuringP=" + measuringP +
                ", measuringEfficiency=" + measuringEfficiency +
                ", trmTemperature=" + trmTemperature +
                '}';
    }
}
