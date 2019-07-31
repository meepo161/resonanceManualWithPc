package ru.avem.resonance.communication.devices.parmaT400;

import ru.avem.resonance.communication.devices.DeviceController;
import ru.avem.resonance.communication.modbus.ModbusController;

import java.nio.ByteBuffer;
import java.util.Observer;

public class ParmaT400Controller implements DeviceController {
    private static final short F_REGISTER = 0;
    private static final short U_REGISTER = 4;
    private static final short I_REGISTER = 7;
    private static final short COS_REGISTER = 24;
    private static final short TIME_REGISTER = 27;

    private static final int NUM_OF_WORDS_IN_REGISTER = 1;
    private static final short NUM_OF_REGISTERS = 3 * NUM_OF_WORDS_IN_REGISTER;

    private ParmaT400Model model;
    private ModbusController modbusController;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;
    private byte address;

    public ParmaT400Controller(int address, Observer observer, ModbusController controller, int deviceID) {
        this.address = (byte) address;
        model = new ParmaT400Model(observer, deviceID);
        modbusController = controller;
    }

    @Override
    public void resetAllAttempts() {
        resetReadAttempts();
        resetReadAttemptsOfAttempts();
        resetWriteAttempts();
        resetWriteAttemptsOfAttempts();
    }


    public void resetWriteAttempts() {
        writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    }

    private void resetWriteAttemptsOfAttempts() {
        writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    }

    public void resetReadAttempts() {
        readAttempt = NUMBER_OF_READ_ATTEMPTS;
    }

    private void resetReadAttemptsOfAttempts() {
        readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    }

    public void read(Object... args) {
        if (thereAreReadAttempts()) {
            readAttempt--;
            switch ((Integer) args[0]) {
                case 1:
                    if (!getFPQ().equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                        read(args);
                    } else {
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    }
                    break;
                case 2:
                    if (!getU().equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                        read(args);
                    } else {
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    }
                    break;
                case 3:
                    if (!getI().equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                        read(args);
                    } else {
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    }
                    break;
                case 4:
                    if (!getTIMEandCOS().equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                        read(args);
                    } else {
                        resetReadAttempts();
                        resetReadAttemptsOfAttempts();
                    }
                    break;
            }
        } else {
           readAttemptOfAttempt--;
            if (readAttemptOfAttempt <= 0) {
                model.setReadResponding(false);
            } else {
                resetReadAttempts();
            }
        }
    }

    private ModbusController.RequestStatus getFPQ() {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ModbusController.RequestStatus statusFPQ = modbusController.readInputRegisters(
                address, F_REGISTER, NUM_OF_REGISTERS, inputBuffer);
        if (statusFPQ.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
            model.setReadResponding(true);
            try {
                model.setF(inputBuffer.getShort());
                model.setP(inputBuffer.getShort());
                model.setQ(inputBuffer.getShort());
            } catch (Exception ignored) {
            }
        }
        return statusFPQ;
    }

    private ModbusController.RequestStatus getU() {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ModbusController.RequestStatus statusU = modbusController.readInputRegisters(
                address, U_REGISTER, NUM_OF_REGISTERS, inputBuffer);
        if (statusU.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
            model.setReadResponding(true);
            try {
                model.setUab(inputBuffer.getShort());
                model.setUbc(inputBuffer.getShort());
                model.setUca(inputBuffer.getShort());
            } catch (Exception ignored) {
            }
        }
        return statusU;
    }

    private ModbusController.RequestStatus getI() {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ModbusController.RequestStatus statusI = modbusController.readInputRegisters(
                address, I_REGISTER, NUM_OF_REGISTERS, inputBuffer);
        if (statusI.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
            model.setReadResponding(true);
            try {
                model.setIa(inputBuffer.getShort());
                model.setIb(inputBuffer.getShort());
                model.setIc(inputBuffer.getShort());
            } catch (Exception ignored) {
            }
        }
        return statusI;
    }

    private ModbusController.RequestStatus getTIMEandCOS() {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ModbusController.RequestStatus statusTIMEandCOS = modbusController.readInputRegisters(
                address, TIME_REGISTER, NUM_OF_REGISTERS, inputBuffer);
        if (statusTIMEandCOS.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
            model.setReadResponding(true);
            try {
                model.setTimeLow(inputBuffer.getShort());
                model.setTimeHigh(inputBuffer.getShort());
                model.setCos(inputBuffer.getShort());
            } catch (Exception ignored) {
            }
        }
        return statusTIMEandCOS;
    }

    @Override
    public void write(Object... args) {
        short register = (short) args[0];
        int numOfRegisters = (int) args[1];
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        ByteBuffer dataBuffer = ByteBuffer.allocate(2 * numOfRegisters);
        for (int i = 2; i < numOfRegisters + 2; i++) {
            dataBuffer.putShort((short) ((int) args[i]));
        }
        dataBuffer.flip();

        if (thereAreWriteAttempts()) {
            writeAttempt--;
            ModbusController.RequestStatus status = modbusController.writeMultipleHoldingRegisters(
                    address, register, (short) numOfRegisters, dataBuffer, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.PORT_NOT_INITIALIZED)) {
                return;
            }
            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                model.setWriteResponding(true);
                resetWriteAttempts();
                resetWriteAttemptsOfAttempts();
            } else {
                write(args);
            }
        } else {
            writeAttemptOfAttempt--;
            if (writeAttemptOfAttempt <= 0) {
                model.setWriteResponding(false);
            } else {
                resetWriteAttempts();
            }
        }
    }


    @Override
    public boolean thereAreReadAttempts() {
        return readAttempt > 0;
    }


    @Override
    public boolean thereAreWriteAttempts() {
        return writeAttempt > 0;
    }

    @Override
    public boolean isNeedToRead() {
        return isNeedToRead;
    }

    @Override
    public void setNeedToRead(boolean isNeedToRead) {
        if (isNeedToRead) {
            model.resetResponding();
        }
        this.isNeedToRead = isNeedToRead;
    }

    @Override
    public void resetAllDeviceStateOnAttempts() {
        readAttempt = 1;
        readAttemptOfAttempt = 0;
        writeAttempt = 1;
        writeAttemptOfAttempt = 0;
    }
}