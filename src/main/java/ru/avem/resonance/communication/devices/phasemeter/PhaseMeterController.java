package ru.avem.resonance.communication.devices.phasemeter;

import ru.avem.resonance.communication.devices.DeviceController;
import ru.avem.resonance.communication.modbus.ModbusController;

import java.nio.ByteBuffer;
import java.util.Observer;

public class PhaseMeterController implements DeviceController {
    private static final short FIRST_REGISTER = 2;
    public static final short START_STOP_REGISTER = 0x0E;
    public static final int PHASE_METER_CANCELED = 0x00;
    public static final int PHASE_METER_IN_PROGRESS = 0x01;
    public static final int PHASE_METER_READY = 0x02;

    private static final int NUM_OF_WORDS_IN_REGISTER = 2;
    private static final short NUM_OF_REGISTERS = 8 * NUM_OF_WORDS_IN_REGISTER;


    private PhaseMeterModel model;
    private ModbusController modbusController;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;
    private byte address;

    public PhaseMeterController(int address, Observer observer, ModbusController controller, int deviceID) {
        this.address = (byte) address;
        model = new PhaseMeterModel(observer, deviceID);
        modbusController = controller;
    }

    @Override
    public void resetAllAttempts() {
        resetReadAttempts();
        resetReadAttemptsOfAttempts();
        resetWriteAttempts();
        resetWriteAttemptsOfAttempts();
    }

    public void resetReadAttempts() {
        readAttempt = NUMBER_OF_READ_ATTEMPTS;
    }

    private void resetReadAttemptsOfAttempts() {
        readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    }

    public void resetWriteAttempts() {
        writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    }

    private void resetWriteAttemptsOfAttempts() {
        writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    }

    @Override
    public void read(Object... args) {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        if (thereAreReadAttempts()) {
            readAttempt--;
            ModbusController.RequestStatus status = modbusController.readInputRegisters(
                    address, FIRST_REGISTER, NUM_OF_REGISTERS, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                model.setReadResponding(true);
                resetReadAttempts();
                resetReadAttemptsOfAttempts();
                model.setPeriodTime0(inputBuffer.getInt());
                model.setImpulsTime0(inputBuffer.getInt());
                model.setPeriodTime1(inputBuffer.getInt());
                model.setImpulsTime1(inputBuffer.getInt());
                model.setCPUTimeDelay(inputBuffer.getInt());
                model.setWindingGroup0(inputBuffer.getShort());
                model.setWindingGroup1(inputBuffer.getShort());
                model.setStartStop(inputBuffer.getShort());
            } else {
                read(args);
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

    @Override
    public void write(Object... args) {
        ByteBuffer inputBuffer = ByteBuffer.allocate(INPUT_BUFFER_SIZE);
        byte[] value = new byte[1];
        if (args[1] instanceof Short) {
            value = shortToByteArray((short) args[1]);
        }
        if (thereAreWriteAttempts()) {
            writeAttempt--;
            ModbusController.RequestStatus status = modbusController.writeSingleHoldingRegister(address,
                    (short) args[0], value, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.PORT_NOT_INITIALIZED)) {
                return;
            }
            if (!status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
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

    private byte[] shortToByteArray(short s) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(2);
        convertBuffer.clear();
        return convertBuffer.putShort(s).array();
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