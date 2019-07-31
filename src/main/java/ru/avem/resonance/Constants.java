package ru.avem.resonance;

import ru.avem.resonance.communication.serial.driver.UsbSerialPort;
import ru.avem.resonance.utils.BuildConfig;

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
                public static final double SEC_IN_MIN = 60.0;
                public static final double MILLS_IN_SEC = 1000.0;
        }
}