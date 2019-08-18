package ru.avem.resonance.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment2Model {

    private final StringProperty voltage;
    private final StringProperty currentA;
    private final StringProperty currentB;
    private final StringProperty currentC;
    private final StringProperty currentLeak;
    private final StringProperty frequency;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();
    private ArrayList<StringProperty> protocol = new ArrayList<>();


    public Experiment2Model() {
        voltage = new SimpleStringProperty("");
        currentA = new SimpleStringProperty("");
        currentB = new SimpleStringProperty("");
        currentC = new SimpleStringProperty("");
        currentLeak = new SimpleStringProperty("");
        frequency = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(
                voltage,
                currentA,
                currentB,
                currentC,
                currentLeak,
                frequency,
                result));
    }

    public Experiment2Model(
            String voltage, String currentA, String currentB, String currentC, String currentLeak, String frequency, String result) {
        this.voltage = new SimpleStringProperty(voltage);
        this.currentA = new SimpleStringProperty(currentA);
        this.currentB = new SimpleStringProperty(currentB);
        this.currentC = new SimpleStringProperty(currentC);
        this.currentLeak = new SimpleStringProperty(currentLeak);
        this.frequency = new SimpleStringProperty(frequency);
        this.result = new SimpleStringProperty(result);
        protocol.addAll(Arrays.asList(
                this.voltage,
                this.currentA,
                this.currentB,
                this.currentC,
                this.currentLeak,
                this.frequency,
                this.result
        ));
    }

    public String getVoltage() {
        return voltage.get();
    }

    public StringProperty voltageProperty() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage.set(voltage);
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

    public String getCurrentLeak() {
        return currentLeak.get();
    }

    public StringProperty currentLeakProperty() {
        return currentLeak;
    }

    public void setCurrentLeak(String currentLeak) {
        this.currentLeak.set(currentLeak);
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
