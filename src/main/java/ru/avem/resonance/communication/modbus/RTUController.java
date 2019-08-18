package ru.avem.resonance.communication.modbus;

import ru.avem.resonance.communication.connections.Connection;
import ru.avem.resonance.communication.modbus.utils.CRC16;

import java.nio.ByteBuffer;

import static ru.avem.resonance.communication.CommunicationModel.LOCK;
import static ru.avem.resonance.utils.Utils.sleep;

public class RTUController implements ModbusController {
    private Connection connection;

    public RTUController(Connection connection) {
        this.connection = connection;
    }

    public RequestStatus reportSlaveID(byte deviceAddress, byte identifier, byte versionSoftware,
                                       byte versionHardware, int serialNumber,
                                       ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(11)
                .put(deviceAddress)
                .put(Command.REPORT_SLAVE_ID.getValue())
                .put(identifier)
                .put(versionSoftware)
                .put(versionHardware)
                .putInt(serialNumber);
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.REPORT_SLAVE_ID.getValue(), outputBuffer, inputBuffer);
    }

    public RequestStatus readInputRegisters(byte deviceAddress, short registerAddress,
                                            short numberOfRegisters, ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(8)
                .put(deviceAddress)
                .put(Command.READ_INPUT_REGISTERS.getValue());
        if (numberOfRegisters != 0) {
            outputBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters);
        }
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.READ_INPUT_REGISTERS.getValue(), outputBuffer, inputBuffer);
    }

    public RequestStatus writeSingleHoldingRegister(byte deviceAddress, short registerAddress,
                                                    byte[] data, ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(10)
                .put(deviceAddress)
                .put(Command.WRITE_SINGLE_HOLDING_REGISTER.getValue())
                .putShort(registerAddress)
                .put(data);
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.WRITE_SINGLE_HOLDING_REGISTER.getValue(), outputBuffer, inputBuffer);
    }

    public RequestStatus readMultipleHoldingRegisters(byte deviceAddress, short registerAddress,
                                                      short numberOfRegisters,
                                                      ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(8)
                .put(deviceAddress)
                .put(Command.READ_MULTIPLE_HOLDING_REGISTERS.getValue());
        if (numberOfRegisters != 0) {
            outputBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters);
        }
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.READ_MULTIPLE_HOLDING_REGISTERS.getValue(), outputBuffer, inputBuffer);
    }

    @Override
    public RequestStatus writeMultipleHoldingRegisters(byte deviceAddress, short registerAddress, short numberOfRegisters, ByteBuffer dataBuffer, ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(256)
                .put(deviceAddress)
                .put(Command.WRITE_MULTIPLE_HOLDING_REGISTER.getValue());
        if (numberOfRegisters != 0) {
            outputBuffer.putShort(registerAddress)
                    .putShort(numberOfRegisters)
                    .put((byte) (numberOfRegisters * 2))
                    .put(dataBuffer);
        }
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.WRITE_MULTIPLE_HOLDING_REGISTER.getValue(), sliceBuffer(outputBuffer), inputBuffer);
    }

    private ByteBuffer sliceBuffer(ByteBuffer outputBuffer) {
        ByteBuffer slicedBuffer = ByteBuffer.allocate(outputBuffer.position());
        return slicedBuffer.put(outputBuffer.array(), 0, outputBuffer.position());
    }

    @Override
    public RequestStatus readStatus(byte deviceAddress, ByteBuffer inputBuffer) {
        ByteBuffer outputBuffer = ByteBuffer.allocate(4)
                .put(deviceAddress)
                .put(Command.READ_EXCEPTION_STATUS.getValue());
        CRC16.sign(outputBuffer);
        return sendCommand(deviceAddress, Command.READ_EXCEPTION_STATUS.getValue(), outputBuffer, inputBuffer);
    }

    private RequestStatus sendCommand(byte deviceAddress, short command, ByteBuffer outputBuffer, ByteBuffer inputBuffer) {
        synchronized (LOCK) {
            RequestStatus status;

            if (!connection.isInitiatedConnection()) {
                return RequestStatus.PORT_NOT_INITIALIZED;
            }

            LogAnalyzer.addWrite();
            int writeFrameSize = connection.write(outputBuffer.array());
            if (writeFrameSize == -1) {
                return RequestStatus.PORT_NOT_INITIALIZED;
            }

            int attempt = 1;
            byte[] inputArray;
            int readFrameSize;
            do {
                sleep(ModbusController.READ_DELAY);
                inputArray = new byte[256];
                readFrameSize = connection.read(inputArray);
                if (readFrameSize == -1) {
                    return RequestStatus.PORT_NOT_INITIALIZED;
                }
            } while (attempt-- > 0 && readFrameSize <= 4);

            if (readFrameSize > 4 &&
                    inputArray[0] == deviceAddress &&
                    (inputArray[1] == command || inputArray[1] == (command & 0x80))) {
                if (CRC16.check(inputArray, readFrameSize)) {
                    if ((inputArray[1] & 0x80) == 0) {
                        LogAnalyzer.addSuccess();
                        status = RequestStatus.FRAME_RECEIVED;
                        ((ByteBuffer) inputBuffer.clear()).put(inputArray, 0, readFrameSize).flip()
                                .position(3);
                    } else {
                        switch (inputArray[2]) {
                            case 0x01:
                                status = RequestStatus.BAD_FUNCTION;
                                break;
                            case 0x02:
                                status = RequestStatus.BAD_DATA_ADDS;
                                break;
                            case 0x03:
                                status = RequestStatus.BAD_DATA_VALUE;
                                break;
                            case 0x04:
                                status = RequestStatus.DEVICE_FAILURE;
                                break;
                            default:
                                status = RequestStatus.UNKNOWN;
                                break;
                        }
                    }
                } else {
                    status = RequestStatus.BAD_CRC;
                }
            } else {
                status = RequestStatus.UNKNOWN;
            }
            sleep(ModbusController.DELAY);
            return status;
        }
    }
}