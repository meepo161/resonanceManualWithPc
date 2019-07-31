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
    private ArrayList<Double> voltageResonance = new ArrayList<>();

    @DatabaseField
    private Double viu;
    @DatabaseField
    private Double viuDC;

    public TestItem() {
        // ORMLite needs a no-arg constructor
    }

    public TestItem(String type) {
        this.type = type;
    }

    public TestItem(String type, ArrayList<Double> times, ArrayList<Double> voltageResonance, Double viu, Double viuDC) {
        this.type = type;
        this.times = times;
        this.voltageResonance = voltageResonance;
        this.viu = viu;
        this.viuDC = viuDC;
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

    public ArrayList<Double> getVoltageResonance() {
        return voltageResonance;
    }

    public void setVoltageResonance(ArrayList<Double> voltageResonance) {
        this.voltageResonance = voltageResonance;
    }

    public Double getViu() {
        return viu;
    }

    public void setViu(Double viu) {
        this.viu = viu;
    }

    public Double getViuDC() {
        return viuDC;
    }

    public void setViuDC(Double viuDC) {
        this.viuDC = viuDC;
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
                Objects.equals(voltageResonance, testItem.voltageResonance) &&
                Objects.equals(viu, testItem.viu) &&
                Objects.equals(viuDC, testItem.viuDC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, times, voltageResonance, viu, viuDC);
    }
}