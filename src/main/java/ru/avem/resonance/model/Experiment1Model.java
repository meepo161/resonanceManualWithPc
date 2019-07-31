package ru.avem.resonance.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment1Model {

    private final StringProperty voltageA;
    private final StringProperty voltageB;
    private final StringProperty voltageC;
    private final StringProperty currentA;
    private final StringProperty currentB;
    private final StringProperty currentC;
    private final StringProperty torque;
    private final StringProperty rotation;
    private final StringProperty frequency;
    private final StringProperty power;
    private final StringProperty powerActive;
    private final StringProperty efficiency;
    private final StringProperty temperature;
    private final StringProperty timeExpr;
    private final StringProperty cos;
    private final StringProperty slip;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();
    private ArrayList<StringProperty> protocol = new ArrayList<>();


    public Experiment1Model() {
        voltageA = new SimpleStringProperty("");
        voltageB = new SimpleStringProperty("");
        voltageC = new SimpleStringProperty("");
        currentA = new SimpleStringProperty("");
        currentB = new SimpleStringProperty("");
        currentC = new SimpleStringProperty("");
        torque = new SimpleStringProperty("");
        rotation = new SimpleStringProperty("");
        frequency = new SimpleStringProperty("");
        power = new SimpleStringProperty("");
        powerActive = new SimpleStringProperty("");
        efficiency = new SimpleStringProperty("");
        temperature = new SimpleStringProperty("");
        timeExpr = new SimpleStringProperty("");
        cos = new SimpleStringProperty("");
        slip = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(
                voltageA,
                voltageB,
                voltageC,
                currentA,
                currentB,
                currentC,
                torque,
                rotation,
                frequency,
                power,
                powerActive,
                efficiency,
                temperature,
                timeExpr,
                cos,
                slip,
                result));
    }

    public Experiment1Model(
            String voltageA, String voltageB, String voltageC, String currentA, String currentB, String currentC,
            String torque, String rotation, String frequency, String power, String powerActive, String efficiency,
            String temperature, String timeExpr, String cos, String slip, String result) {
        this.voltageA = new SimpleStringProperty(voltageA);
        this.voltageB = new SimpleStringProperty(voltageB);
        this.voltageC = new SimpleStringProperty(voltageC);
        this.currentA = new SimpleStringProperty(currentA);
        this.currentB = new SimpleStringProperty(currentB);
        this.currentC = new SimpleStringProperty(currentC);
        this.torque = new SimpleStringProperty(torque);
        this.rotation = new SimpleStringProperty(rotation);
        this.frequency = new SimpleStringProperty(frequency);
        this.power = new SimpleStringProperty(power);
        this.powerActive = new SimpleStringProperty(powerActive);
        this.efficiency = new SimpleStringProperty(efficiency);
        this.temperature = new SimpleStringProperty(temperature);
        this.timeExpr = new SimpleStringProperty(timeExpr);
        this.cos = new SimpleStringProperty(cos);
        this.slip = new SimpleStringProperty(slip);
        this.result = new SimpleStringProperty(result);
        protocol.addAll(Arrays.asList(
                this.voltageA,
                this.voltageB,
                this.voltageC,
                this.currentA,
                this.currentB,
                this.currentC,
                this.torque,
                this.rotation,
                this.frequency,
                this.power,
                this.powerActive,
                this.efficiency,
                this.temperature,
                this.timeExpr,
                this.cos,
                this.slip,
                this.result
        ));
    }

    public String getVoltageA() {
        return voltageA.get();
    }

    public StringProperty voltageAProperty() {
        return voltageA;
    }

    public void setVoltageA(String voltageA) {
        this.voltageA.set(voltageA);
    }

    public String getVoltageB() {
        return voltageB.get();
    }

    public StringProperty voltageBProperty() {
        return voltageB;
    }

    public void setVoltageB(String voltageB) {
        this.voltageB.set(voltageB);
    }

    public String getVoltageC() {
        return voltageC.get();
    }

    public StringProperty voltageCProperty() {
        return voltageC;
    }

    public void setVoltageC(String voltageC) {
        this.voltageC.set(voltageC);
    }

    public String getCurrentA() {
        return currentA.get();
    }

    public StringProperty currentAProperty() {
        return currentA;
    }

    public void setCurrentA(String currentA) {
        this.currentA.set(currentA);
    }

    public String getCurrentB() {
        return currentB.get();
    }

    public StringProperty currentBProperty() {
        return currentB;
    }

    public void setCurrentB(String currentB) {
        this.currentB.set(currentB);
    }

    public String getCurrentC() {
        return currentC.get();
    }

    public StringProperty currentCProperty() {
        return currentC;
    }

    public void setCurrentC(String currentC) {
        this.currentC.set(currentC);
    }

    public String getTorque() {
        return torque.get();
    }

    public StringProperty torqueProperty() {
        return torque;
    }

    public void setTorque(String torque) {
        this.torque.set(torque);
    }

    public String getRotation() {
        return rotation.get();
    }

    public StringProperty rotationProperty() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation.set(rotation);
    }

    public String getFrequency() {
        return frequency.get();
    }

    public StringProperty frequencyProperty() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency.set(frequency);
    }

    public String getPower() {
        return power.get();
    }

    public StringProperty powerProperty() {
        return power;
    }

    public void setPower(String power) {
        this.power.set(power);
    }

    public String getPowerActive() {
        return powerActive.get();
    }

    public StringProperty powerActiveProperty() {
        return powerActive;
    }

    public void setPowerActive(String powerActive) {
        this.powerActive.set(powerActive);
    }

    public String getEfficiency() {
        return efficiency.get();
    }

    public StringProperty efficiencyProperty() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency.set(efficiency);
    }

    public String getTemperature() {
        return temperature.get();
    }

    public StringProperty temperatureProperty() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature.set(temperature);
    }

    public String getTimeExpr() {
        return timeExpr.get();
    }

    public StringProperty timeExprProperty() {
        return timeExpr;
    }

    public void setTimeExpr(String timeExpr) {
        this.timeExpr.set(timeExpr);
    }

    public String getCos() {
        return cos.get();
    }

    public StringProperty cosProperty() {
        return cos;
    }

    public void setCos(String cos) {
        this.cos.set(cos);
    }

    public String getSlip() {
        return slip.get();
    }

    public StringProperty slipProperty() {
        return slip;
    }

    public void setSlip(String slip) {
        this.slip.set(slip);
    }

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public ArrayList<StringProperty> getProtocol() {
        return protocol;
    }

    public void setProtocol(ArrayList<StringProperty> protocol) {
        this.protocol = protocol;
    }

    public List<StringProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<StringProperty> properties) {
        this.properties = properties;
    }

    public void clearProperties() {
        properties.forEach(stringProperty -> stringProperty.set(""));
    }
}
