package ru.avem.resonance.communication.connections;

public interface Connection {
    boolean initConnection();

    void closeConnection();

    boolean isInitiatedConnection();

    int write(byte[] outputArray);

    int read(byte[] inputArray);
}
