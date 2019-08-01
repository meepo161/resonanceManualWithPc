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

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type) {
        this.type = type;
    }

    public TestItem(String type, ArrayList<Double> timesResonance, ArrayList<Double> voltageResonance) {
        this.type = type;
        this.timesResonance = timesResonance;
        this.voltageResonance = voltageResonance;
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
                Objects.equals(voltageResonance, testItem.voltageResonance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timesResonance, voltageResonance);
    }
}