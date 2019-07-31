package ru.avem.resonance.communication.devices;

public interface DeviceController {
    int INPUT_BUFFER_SIZE = 256;
    byte NUMBER_OF_READ_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS = 5;

    byte NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS = 5;

    int PM130_ID = 1;
    int PARMA400_ID = 2;
    int AVEM_ID = 3;
    int PHASEMETER_ID = 4;
    int IKAS_ID = 5;
    int PR200_ID = 6;
    int TRM_ID = 7;
    int MEGACS_ID = 8;
    int DELTACP2000_ID = 11;
//    int FR_A800_OBJECT_ID = 11;

    void read(Object... args);

    boolean thereAreReadAttempts();

    void write(Object... args);

    boolean thereAreWriteAttempts();

    boolean isNeedToRead();

    void setNeedToRead(boolean isNeedToRead);

    void resetAllAttempts();

    void resetAllDeviceStateOnAttempts();
}