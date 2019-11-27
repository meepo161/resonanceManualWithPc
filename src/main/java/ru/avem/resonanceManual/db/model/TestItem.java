package ru.avem.resonanceManual.db.model;

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
    private ArrayList<Double> timesResonance = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageResonance = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> speedResonance = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> timesViu = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageViu = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> speedViu = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> timesViuDC = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageViuDC = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> speedViuDC = new ArrayList<>();

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type) {
        this.type = type;
    }

    public TestItem(String type,
                    ArrayList<Double> timesResonance,
                    ArrayList<Double> voltageResonance,
                    ArrayList<Double> speedResonance,
                    ArrayList<Double> timesViu,
                    ArrayList<Double> voltageViu,
                    ArrayList<Double> speedViu,
                    ArrayList<Double> timesViuDC,
                    ArrayList<Double> voltageViuDC,
                    ArrayList<Double> speedViuDC) {
        this.type = type;
        this.timesResonance = timesResonance;
        this.voltageResonance = voltageResonance;
        this.speedResonance = speedResonance;
        this.timesViu = timesViu;
        this.voltageViu = voltageViu;
        this.speedViu = speedViu;
        this.timesViuDC = timesViuDC;
        this.voltageViuDC = voltageViuDC;
        this.speedViuDC = speedViuDC;
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

    public ArrayList<Double> getTimesResonance() {
        return timesResonance;
    }

    public void setTimesResonance(ArrayList<Double> timesResonance) {
        this.timesResonance = timesResonance;
    }

    public ArrayList<Double> getVoltageResonance() {
        return voltageResonance;
    }

    public void setVoltageResonance(ArrayList<Double> voltageResonance) {
        this.voltageResonance = voltageResonance;
    }

    public ArrayList<Double> getTimesViu() {
        return timesViu;
    }

    public void setTimesViu(ArrayList<Double> timesViu) {
        this.timesViu = timesViu;
    }

    public ArrayList<Double> getVoltageViu() {
        return voltageViu;
    }

    public void setVoltageViu(ArrayList<Double> voltageViu) {
        this.voltageViu = voltageViu;
    }

    public ArrayList<Double> getTimesViuDC() {
        return timesViuDC;
    }

    public void setTimesViuDC(ArrayList<Double> timesViuDC) {
        this.timesViuDC = timesViuDC;
    }

    public ArrayList<Double> getVoltageViuDC() {
        return voltageViuDC;
    }

    public void setVoltageViuDC(ArrayList<Double> voltageViuDC) {
        this.voltageViuDC = voltageViuDC;
    }

    public ArrayList<Double> getSpeedResonance() {
        return speedResonance;
    }

    public void setSpeedResonance(ArrayList<Double> speedResonance) {
        this.speedResonance = speedResonance;
    }

    public ArrayList<Double> getSpeedViu() {
        return speedViu;
    }

    public void setSpeedViu(ArrayList<Double> speedViu) {
        this.speedViu = speedViu;
    }

    public ArrayList<Double> getSpeedViuDC() {
        return speedViuDC;
    }

    public void setSpeedViuDC(ArrayList<Double> speedViuDC) {
        this.speedViuDC = speedViuDC;
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
                Objects.equals(timesResonance, testItem.timesResonance) &&
                Objects.equals(voltageResonance, testItem.voltageResonance) &&
                Objects.equals(speedResonance, testItem.speedResonance) &&
                Objects.equals(timesViu, testItem.timesViu) &&
                Objects.equals(voltageViu, testItem.voltageViu) &&
                Objects.equals(speedViu, testItem.speedViu) &&
                Objects.equals(timesViuDC, testItem.timesViuDC) &&
                Objects.equals(voltageViuDC, testItem.voltageViuDC) &&
                Objects.equals(speedViuDC, testItem.speedViuDC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timesResonance, voltageResonance, speedResonance, timesViu, voltageViu, speedViu, timesViuDC, voltageViuDC, speedViuDC);
    }
}