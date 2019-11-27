package ru.avem.resonanceManual;

import ru.avem.resonanceManual.communication.serial.driver.UsbSerialPort;
import ru.avem.resonanceManual.utils.BuildConfig;

public final class Constants {
    public static final class Display {
        public static final int WIDTH = BuildConfig.DEBUG ? 1366 : 1366;
        public static final int HEIGHT = BuildConfig.DEBUG ? 768 : 768;
    }

    public static final class Communication {
        public static final String RS485_DEVICE_NAME = "CP2103 USB to RS-485";
        public static final int BAUDRATE = 38400;
        public static final int DATABITS = UsbSerialPort.DATABITS_8;
        public static final int STOPBITS = UsbSerialPort.STOPBITS_1;
        public static final int PARITY = UsbSerialPort.PARITY_NONE;
        public static final int WRITE_TIMEOUT = 150;
        public static final int READ_TIMEOUT = 150;
    }

    public static final class Experiments {
        public static final String EXPERIMENT1_NAME = "1. Испытание электродвигателя в основном режиме на холостом ходу.";
        public static final String EXPERIMENT2_NAME = "2. Испытание электродвигателя с  противодействующим моментом.";
    }

    public static final class Time {
        public static final double MILLS_IN_SEC = 1000.0;
    }

    public static final class Ends {
        public static final String LATR_STARTED = "01";
        public static final String LATR_WAITING = "02";
        public static final String LATR_CONFIG = "70";
        public static final String LATR_STOP_RESET = "03";
        public static final String LATR_UP_END = "81";
        public static final String LATR_DOWN_END = "82";
        public static final String LATR_BOTH_END = "83";
        public static final String LATR_TIME_ENDED = "84";
        public static final String LATR_ZASTRYAL = "85";

        public static final Short OMIK_DOWN_END = 59;
        public static final Short OMIK_UP_END = 55;
        public static final Short OMIK_BOTH_END = 63;
        public static final Short OMIK_NOONE_END = 51;
    }

    public static final class Vfd {
        public static final Short VFD_REVERSE = 1304;
        public static final Short VFD_FORWARD = 1280;
    }

    public enum Avem {
        VOLTAGE_AMP, VOLTAGE_AVERAGE, VOLTAGE_RMS, FREQUENCY
    }
}