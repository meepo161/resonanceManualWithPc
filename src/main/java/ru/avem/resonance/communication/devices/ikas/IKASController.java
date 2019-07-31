package ru.avem.resonance.communication.devices.ikas;

import ru.avem.resonance.communication.devices.DeviceController;
import ru.avem.resonance.communication.modbus.ModbusController;

import java.nio.ByteBuffer;
import java.util.Observer;


public class IKASController implements DeviceController {
    private static final short STATUS_REGISTER = 0;
    public static final short MEASURABLE_TYPE_REGISTER = 0x65;
    public static final short START_MEASURABLE_REGISTER = 0x64;
    public static final short TYPE_OF_RANGE_R_REGISTER = 0x67;
    public static final short RANGE_R_REGISTER = 0x68;
    public static final int MEASURABLE_TYPE_AB = 0x46;
    public static final int MEASURABLE_TYPE_BC = 0x44;
    public static final int MEASURABLE_TYPE_AC = 0x45;
    public static final int RANGE_TYPE_LESS_8 = 0x01;
    public static final int RANGE_TYPE_MORE_8_LESS_200 = 0x02;
    public static final int RANGE_TYPE_MORE_200 = 0x03;

    private static final int NUM_OF_WORDS_IN_REGISTER = 1;
    private static final short NUM_OF_REGISTERS = 2 * NUM_OF_WORDS_IN_REGISTER;

    private IKASModel model;
    private ModbusController modbusController;
    public byte readAttempt = NUMBER_OF_READ_ATTEMPTS;
    public byte readAttemptOfAttempt = NUMBER_OF_READ_ATTEMPTS_OF_ATTEMPTS;
    public byte writeAttempt = NUMBER_OF_WRITE_ATTEMPTS;
    public byte writeAttemptOfAttempt = NUMBER_OF_WRITE_ATTEMPTS_OF_ATTEMPTS;
    private boolean isNeedToRead;
    private byte address;

    public IKASController(int address, Observer observer, ModbusController controller, int id) {
        this.address = (byte) address;
        model = new IKASModel(observer, id);
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
                    address, STATUS_REGISTER, NUM_OF_REGISTERS, inputBuffer);
            if (status.equals(ModbusController.RequestStatus.FRAME_RECEIVED)) {
                model.setReadResponding(true);
                resetReadAttempts();
                resetReadAttemptsOfAttempts();
                model.setReady(inputBuffer.getFloat());
                try {
                    model.setMeasurable(inputBuffer.getFloat());
                } catch (Exception ignored) {
                }
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
        if (args[1] instanceof Integer) {
            value = intToByteArray((int) args[1]);
        } else if (args[1] instanceof Float) {
            value = floatToByteArray((float) args[1]);
        }
        if (thereAreWriteAttempts()) {
            writeAttempt--;
            ModbusController.RequestStatus status = modbusController.writeSingleHoldingRegister(address,
                    (short) args[0], value, inputBuffer);
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

    private byte[] intToByteArray(int i) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putInt(i).array();
    }

    private byte[] floatToByteArray(float f) {
        ByteBuffer convertBuffer = ByteBuffer.allocate(4);
        convertBuffer.clear();
        return convertBuffer.putFloat(f).array();
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

    public static int getRangeType(float supposedValue) {
        int rangeType = 1;
        if (supposedValue < 8) {
            rangeType = RANGE_TYPE_LESS_8;
        } else if (supposedValue > 8 && supposedValue < 200) {
            rangeType = RANGE_TYPE_MORE_8_LESS_200;
        } else if (supposedValue > 200) {
            rangeType = RANGE_TYPE_MORE_200;
        }
        return rangeType;
    }


}