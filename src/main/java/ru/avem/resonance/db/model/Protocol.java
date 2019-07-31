package ru.avem.resonance.db.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import ru.avem.resonance.model.Point;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

@XmlRootElement
@DatabaseTable(tableName = "protocols")
public class Protocol {
    @DatabaseField(generatedId = true)
    private long id;


    @DatabaseField
    private String e1VoltageA = "";
    @DatabaseField
    private String e1VoltageB = "";
    @DatabaseField
    private String e1VoltageC = "";
    @DatabaseField
    private String e1CurrentA = "";
    @DatabaseField
    private String e1CurrentB = "";
    @DatabaseField
    private String e1CurrentC = "";
    @DatabaseField
    private String e1Torque = "";
    @DatabaseField
    private String e1Rotation = "";
    @DatabaseField
    private String e1Frequency = "";
    @DatabaseField
    private String e1Power = "";
    @DatabaseField
    private String e1PowerActive = "";
    @DatabaseField
    private String e1Effiency = "";
    @DatabaseField
    private String e1Temperature = "";
    @DatabaseField
    private String e1Result = "";

    @DatabaseField
    private String serialNumber;
    @DatabaseField
    private String type;
    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> times;
    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> voltageResonance;
    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Double> dots;
    @DatabaseField
    private Double viu;
    @DatabaseField
    private Double viuDC;
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
    @DatabaseField
    private String position1;
    @DatabaseField
    private String position1Number;
    @DatabaseField
    private String position1FullName;
    @DatabaseField
    private String position2;
    @DatabaseField
    private String position2Number;
    @DatabaseField
    private String position2FullName;
    @DatabaseField
    private long millis = System.currentTimeMillis();
    @DatabaseField
    private String date;
    @DatabaseField
    private String dayTime;

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Point> points;

    public Protocol() {
        // ORMLite and XML binder need a no-arg constructor
    }

    public Protocol(String serialNumber, TestItem selectedTestItem, Account firstTester, Account secondTester, long millis) {
        setObject(selectedTestItem);
        this.serialNumber = serialNumber;
        this.position1 = firstTester.getPosition();
        this.position1Number = firstTester.getNumber();
        this.position1FullName = firstTester.getFullName();
        this.position2 = secondTester.getPosition();
        this.position2Number = secondTester.getNumber();
        this.position2FullName = secondTester.getFullName();
        this.millis = millis;
        this.date = new SimpleDateFormat("dd.MM.yy").format(millis);
        this.dayTime = new SimpleDateFormat("HH:mm:ss").format(millis);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public TestItem getObject() {
        return new TestItem(type, times, voltageResonance, viu, viuDC);
    }

    public void setObject(TestItem object) {
        type = object.getType();
        times = object.getTimes();
        voltageResonance = object.getVoltageResonance();
        viu = object.getViu();
        viuDC = object.getViuDC();
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
        this.date = new SimpleDateFormat("dd.MM.yy").format(millis);
    }

    public String getE1VoltageA() {
        return e1VoltageA;
    }

    public void setE1VoltageA(String e1VoltageA) {
        this.e1VoltageA = e1VoltageA;
    }

    public String getE1VoltageB() {
        return e1VoltageB;
    }

    public void setE1VoltageB(String e1VoltageB) {
        this.e1VoltageB = e1VoltageB;
    }

    public String getE1VoltageC() {
        return e1VoltageC;
    }

    public void setE1VoltageC(String e1VoltageC) {
        this.e1VoltageC = e1VoltageC;
    }

    public String getE1CurrentA() {
        return e1CurrentA;
    }

    public void setE1CurrentA(String e1CurrentA) {
        this.e1CurrentA = e1CurrentA;
    }

    public String getE1CurrentB() {
        return e1CurrentB;
    }

    public void setE1CurrentB(String e1CurrentB) {
        this.e1CurrentB = e1CurrentB;
    }

    public String getE1CurrentC() {
        return e1CurrentC;
    }

    public void setE1CurrentC(String e1CurrentC) {
        this.e1CurrentC = e1CurrentC;
    }

    public String getE1Torque() {
        return e1Torque;
    }

    public void setE1Torque(String e1Torque) {
        this.e1Torque = e1Torque;
    }

    public String getE1Rotation() {
        return e1Rotation;
    }

    public void setE1Rotation(String e1Rotation) {
        this.e1Rotation = e1Rotation;
    }

    public String getE1Frequency() {
        return e1Frequency;
    }

    public void setE1Frequency(String e1Frequency) {
        this.e1Frequency = e1Frequency;
    }

    public String getE1Power() {
        return e1Power;
    }

    public void setE1Power(String e1Power) {
        this.e1Power = e1Power;
    }

    public String getE1PowerActive() {
        return e1PowerActive;
    }

    public void setE1PowerActive(String e1PowerActive) {
        this.e1PowerActive = e1PowerActive;
    }

    public String getE1Effiency() {
        return e1Effiency;
    }

    public void setE1Effiency(String e1Effiency) {
        this.e1Effiency = e1Effiency;
    }

    public String getE1Temperature() {
        return e1Temperature;
    }

    public void setE1Temperature(String e1Temperature) {
        this.e1Temperature = e1Temperature;
    }

    public String getE1Result() {
        return e1Result;
    }

    public void setE1Result(String e1Result) {
        this.e1Result = e1Result;
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

    public ArrayList<Double> getDots() {
        return dots;
    }

    public void setDots(ArrayList<Double> dots) {
        this.dots = dots;
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

    public String getPosition1() {
        return position1;
    }

    public void setPosition1(String position1) {
        this.position1 = position1;
    }

    public String getPosition1Number() {
        return position1Number;
    }

    public void setPosition1Number(String position1Number) {
        this.position1Number = position1Number;
    }

    public String getPosition1FullName() {
        return position1FullName;
    }

    public void setPosition1FullName(String position1FullName) {
        this.position1FullName = position1FullName;
    }

    public String getPosition2() {
        return position2;
    }

    public void setPosition2(String position2) {
        this.position2 = position2;
    }

    public String getPosition2Number() {
        return position2Number;
    }

    public void setPosition2Number(String position2Number) {
        this.position2Number = position2Number;
    }

    public String getPosition2FullName() {
        return position2FullName;
    }

    public void setPosition2FullName(String position2FullName) {
        this.position2FullName = position2FullName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String time) {
        this.dayTime = dayTime;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        System.out.println("setPoints " + points);
        this.points = points;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("Время проведения испытания: HH:mm:ss");
        return String.format("%s. № %s (%s) %s", id, serialNumber, type, sdf.format(millis));
    }


}
