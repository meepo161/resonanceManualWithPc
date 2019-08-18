package ru.avem.resonance.communication.devices;

public interface DeviceController {
    int INPUT_BUFFER_SIZE = 256;
    byte NUMBER_OF_READ_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS = 5;

    byte NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS = 5;
    byte NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS = 5;

    int PARMA400_ID = 1;
    int AVEM_ID = 2;
    int PR200_ID = 3;
    int LATR_ID = 250;
    int DELTACP2000_ID = 5;
    int KILOAVEM_ID = 6;

    void read(Object... args);

    boolean thereAreReadAttempts();

    void write(Object... args);

    boolean thereAreWriteAttempts();

    boolean isNeedToRead();

    void setNeedToRead(boolean isNeedToRead);

    void resetAllAttempts();

    void resetAllDeviceStateOnAttempts();
}