package ru.avem.resonanceManual.communication.modbus;


import java.nio.ByteBuffer;

public interface ModbusController {
    int DELAY = 20;
    int READ_DELAY = 20;
    enum Command {
        READ_MULTIPLE_HOLDING_REGISTERS((byte) 0x03),
        READ_INPUT_REGISTERS((byte) 0x04),
        WRITE_SINGLE_HOLDING_REGISTER((byte) 0x06),
        WRITE_MULTIPLE_HOLDING_REGISTER((byte) 0x10),
        READ_EXCEPTION_STATUS((byte) 0x07),
        REPORT_SLAVE_ID((byte) 0x11);

        private byte value;

        Command(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    enum RequestStatus {
        FRAME_RECEIVED, FRAME_TIME_OUT, BAD_CRC, BAD_FUNCTION, BAD_DATA_ADDS, BAD_DATA_VALUE,
        DEVICE_FAILURE, UNKNOWN, PORT_NOT_INITIALIZED
    }

    RequestStatus reportSlaveID(byte deviceAddress, byte identifier, byte versionSoftware,
                                byte versionHardware, int serialNumber,
                                ByteBuffer inputBuffer);

    RequestStatus readInputRegisters(byte deviceAddress, short registerAddress,
                                     short numberOfRegisters, ByteBuffer inputBuffer);

    RequestStatus writeSingleHoldingRegister(byte deviceAddress, short registerAddress,
                                             byte[] data, ByteBuffer inputBuffer);

    RequestStatus readMultipleHoldingRegisters(byte deviceAddress, short registerAddress,
                                               short numberOfRegisters, ByteBuffer inputBuffer);

    RequestStatus writeMultipleHoldingRegisters(byte deviceAddress, short registerAddress,
                                                short numberOfRegisters, ByteBuffer outputBuffer,
                                                ByteBuffer inputBuffer);

    RequestStatus readStatus(byte modbusAddress, ByteBuffer inputBuffer);
}
