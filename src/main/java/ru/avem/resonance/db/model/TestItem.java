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
    private ArrayList<Double> timesResonance = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageResonance = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> timesViu = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageViu = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> timesViuDC = new ArrayList<>();

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageViuDC = new ArrayList<>();

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type) {
        this.type = type;
    }

    public TestItem(String type,
                    ArrayList<Double> timesResonance,
                    ArrayList<Double> voltageResonance,
                    ArrayList<Double> timesViu,
                    ArrayList<Double> voltageViu,
                    ArrayList<Double> timesViuDC,
                    ArrayList<Double> voltageViuDC) {
        this.type = type;
        this.timesResonance = timesResonance;
        this.voltageResonance = voltageResonance;
        this.timesViu = timesViu;
        this.voltageViu = voltageViu;
        this.timesViuDC = timesViuDC;
        this.voltageViuDC = voltageViuDC;
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
                Objects.equals(timesViu, testItem.timesViu) &&
                Objects.equals(voltageViu, testItem.voltageViu) &&
                Objects.equals(timesViuDC, testItem.timesViuDC) &&
                Objects.equals(voltageViuDC, testItem.voltageViuDC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timesResonance, voltageResonance, timesViu, voltageViu, timesViuDC, voltageViuDC);
    }
}