package ru.avem.resonanceManual.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiment1Model {

    private final StringProperty voltage;
    private final StringProperty voltageARN;
    private final StringProperty currentB;
    private final StringProperty currentOI;
    private final StringProperty result;
    private List<StringProperty> properties = new ArrayList<>();
    private ArrayList<StringProperty> protocol = new ArrayList<>();


    public Experiment1Model() {
        voltage = new SimpleStringProperty("");
        voltageARN = new SimpleStringProperty("");
        currentB = new SimpleStringProperty("");
        currentOI = new SimpleStringProperty("");
        result = new SimpleStringProperty("");
        properties.addAll(Arrays.asList(
                voltage,
                voltageARN,
                currentB,
                currentOI,
                result));
    }

    public Experiment1Model(
            String voltage, String voltageARN, String currentB, String currentOI, String result) {
        this.voltage = new SimpleStringProperty(voltage);
        this.voltageARN = new SimpleStringProperty(voltageARN);
        this.currentB = new SimpleStringProperty(currentB);
        this.currentOI = new SimpleStringProperty(currentOI);
        this.result = new SimpleStringProperty(result);
        protocol.addAll(Arrays.asList(
                this.voltage,
                this.voltageARN,
                this.currentB,
                this.currentOI,
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

    public String getVoltageARN() {
        return voltageARN.get();
    }

    public StringProperty voltageARNProperty() {
        return voltageARN;
    }

    public void setVoltageARN(String voltageARN) {
        this.voltageARN.set(voltageARN);
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

    public String getCurrentOI() {
        return currentOI.get();
    }

    public StringProperty currentOIProperty() {
        return currentOI;
    }

    public void setCurrentOI(String currentOI) {
        this.currentOI.set(currentOI);
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
