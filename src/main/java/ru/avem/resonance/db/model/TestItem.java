package ru.avem.resonance.db.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Objects;

@DatabaseTable(tableName = "testItems")
public class TestItem {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String type;

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> times = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> torques = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> dots = new ArrayList<>();

    @DatabaseField
    private Double torque;
    @DatabaseField
    private Double power;
    @DatabaseField
    private Double voltage;
    @DatabaseField
    private Double averageCurrent;
    @DatabaseField
    private Double noLoadCurrent;
    @DatabaseField
    private Double rotation;
    @DatabaseField
    private Double kpd;
    @DatabaseField
    private Double temperature;
    @DatabaseField
    private String direction;

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type) {
        this.type = type;
    }

    public TestItem(String type, ArrayList<Double> times, ArrayList<Double> torques, ArrayList<Double> dots,
                    Double torque, Double power, Double voltage, Double averageCurrent, Double noLoadCurrent,
                    Double rotation, Double kpd, Double temperature, String direction) {
        this.type = type;
        this.times = times;
        this.torques = torques;
        this.dots = dots;
        this.torque = torque;
        this.power = power;
        this.voltage = voltage;
        this.averageCurrent = averageCurrent;
        this.noLoadCurrent = noLoadCurrent;
        this.rotation = rotation;
        this.kpd = kpd;
        this.temperature = temperature;
        this.direction = direction;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Double> getTimes() {
        return times;
    }

    public void setTimes(ArrayList<Double> times) {
        this.times = times;
    }

    public ArrayList<Double> getTorques() {
        return torques;
    }

    public void setTorques(ArrayList<Double> torques) {
        this.torques = torques;
    }

    public ArrayList<Double> getDots() {
        return dots;
    }

    public void setDots(ArrayList<Double> dots) {
        this.dots = dots;
    }


    public Double getTorque() {
        return torque;
    }

    public void setTorque(Double torque) {
        this.torque = torque;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getAverageCurrent() {
        return averageCurrent;
    }

    public void setAverageCurrent(Double averageCurrent) {
        this.averageCurrent = averageCurrent;
    }

    public Double getNoLoadCurrent() {
        return noLoadCurrent;
    }

    public void setNoLoadCurrent(Double noLoadCurrent) {
        this.noLoadCurrent = noLoadCurrent;
    }

    public Double getRotation() {
        return rotation;
    }

    public void setRotation(Double rotation) {
        this.rotation = rotation;
    }

    public Double getKpd() {
        return kpd;
    }

    public void setKpd(Double kpd) {
        this.kpd = kpd;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestItem testItem = (TestItem) o;
        return id == testItem.id &&
                Objects.equals(type, testItem.type) &&
                Objects.equals(times, testItem.times) &&
                Objects.equals(torques, testItem.torques) &&
                Objects.equals(dots, testItem.dots) &&
                Objects.equals(torque, testItem.torque) &&
                Objects.equals(power, testItem.power) &&
                Objects.equals(voltage, testItem.voltage) &&
                Objects.equals(averageCurrent, testItem.averageCurrent) &&
                Objects.equals(noLoadCurrent, testItem.noLoadCurrent) &&
                Objects.equals(rotation, testItem.rotation) &&
                Objects.equals(kpd, testItem.kpd) &&
                Objects.equals(temperature, testItem.temperature) &&
                Objects.equals(direction, testItem.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, times, torques, dots, torque, power, voltage, averageCurrent, noLoadCurrent, rotation, kpd, temperature, direction);
    }
}