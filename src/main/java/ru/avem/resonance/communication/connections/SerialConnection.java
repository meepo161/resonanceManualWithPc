package ru.avem.resonance.communication.connections;

import ru.avem.resonance.communication.serial.driver.UsbSerialDriver;
import ru.avem.resonance.communication.serial.driver.UsbSerialPort;
import ru.avem.resonance.communication.serial.driver.UsbSerialProber;

import javax.usb.UsbException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class SerialConnection implements Connection {
    private static final String TAG = "StatusActivity";

    private String productName; //строковое имя прибора
    private int baudRate; //скорость
    private int dataBits; //8
    private int stopBits; //n
    private int parity;   //1
    private int writeTimeout; //таймаут записи
    private int readTimeout;  //таймаут чтения

    private UsbSerialPort port; //экземпляр интерфейса

    public SerialConnection(String productName, int baudRate, int dataBits,  //конструктор класса
                            int stopBits, int parity, int writeTimeout, int readTimeout) {
        this.productName = productName;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.writeTimeout = writeTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public boolean initConnection() {
//        Log.d("DEBUG_TAG", "initConnection");
        UsbSerialDriver usbSerialDriver = getSerialDriver(); //экземпляр интерфейса UsbSerialDriver присваеваем сериал драйвер
        if (usbSerialDriver != null) {  //если ничего не присвоилось
            UsbSerialPort port = usbSerialDriver.getPorts().get(0); //берем первый порт и присваевам его в port
            try {
                port.open();
                this.port = port;
                setPortParameters(baudRate, dataBits, stopBits, parity);
//                Log.d("DEBUG_TAG", "mPort = port");
            } catch (UsbException e) {
                e.printStackTrace();
//                Log.d("DEBUG_TAG", "mPort = UsbException");
            } catch (IOException e) {
                e.printStackTrace();
//                Log.d("DEBUG_TAG", "mPort = IOException");
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setPortParameters(int baudRate, int dataBits, int stopBits, int parity) {
        try {
            port.setParameters(baudRate, dataBits, stopBits, parity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UsbSerialDriver getSerialDriver() {
        List<UsbSerialDriver> availableDrivers; //создаем лист список
        try {
            availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(); //получаем все порты
        } catch (UsbException e) {
            e.printStackTrace();
            return null;
        }
        if (availableDrivers == null || availableDrivers.isEmpty()) {
            return null;
        }
        for (UsbSerialDriver availableDriver : availableDrivers) {
            try {
//                Log.d("PRODUCT_NAME", availableDriver.getDevice().getProductString());
                if (availableDriver.getDevice().getProductString().equals(productName)) { //сравниваем наш драйвер со списком
//                    Log.d("PRODUCT_NAME", "TRUE");
                    return availableDriver;
                }
            } catch (UsbException | UnsupportedEncodingException ignored) {
            }
        }
        return null;
    }

    @Override
    public void closeConnection() {
        try {
            if (port != null) {
                port.close();
            }
            port = null;
        } catch (UsbException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int write(byte[] outputArray) {
        int numBytesWrite = 0;
        try {
            if (port != null) {
                numBytesWrite = port.write(outputArray, writeTimeout);
            } else {
//                Log.i(TAG, "mPort null");
            }
//            Log.i(TAG, "Write " + numBytesWrite + " bytes.");
//            Log.i(TAG, "Write " + toHexString(outputArray));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UsbException e) {
            e.printStackTrace();
        }
        return numBytesWrite;
    }

    @Override
    public synchronized int read(byte[] inputArray) {
        int numBytesRead = 0;
        try {
            if (port != null) {
                numBytesRead = port.read(inputArray, readTimeout);
            } else {
//                Log.i(TAG, "mPort null");
            }
//            Log.i(TAG, "Read " + numBytesRead + " bytes.");
//            Log.i(TAG, "Read: " + toHexString(inputArray, numBytesRead));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UsbException e) {
            e.printStackTrace();
        }
        return numBytesRead;
    }

    @Override
    public boolean isInitiatedConnection() {
        return port != null;
    }
}
