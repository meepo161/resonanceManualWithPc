/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package ru.avem.resonance.communication.serial.driver;

import ru.avem.resonance.utils.Log;

import javax.usb.*;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cp21xxSerialDriver implements UsbSerialDriver {

    private static final String TAG = Cp21xxSerialDriver.class.getSimpleName();

    private final UsbDevice mDevice;
    private final UsbSerialPort mPort;

    public Cp21xxSerialDriver(UsbDevice device) {
        mDevice = device;
        mPort = new Cp21xxSerialPort(mDevice, 0);
    }

    @Override
    public UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public List<UsbSerialPort> getPorts() {
        return Collections.singletonList(mPort);
    }

    public class Cp21xxSerialPort extends CommonUsbSerialPort {

        private static final int DEFAULT_BAUD_RATE = 9600;

        private static final int USB_WRITE_TIMEOUT_MILLIS = 5000;

        /*
         * Configuration Request Types
         */
        private static final int REQTYPE_HOST_TO_DEVICE = 0x41;

        /*
         * Configuration Request Codes
         */
        private static final int SILABSER_IFC_ENABLE_REQUEST_CODE = 0x00;
        private static final int SILABSER_SET_BAUDDIV_REQUEST_CODE = 0x01;
        private static final int SILABSER_SET_LINE_CTL_REQUEST_CODE = 0x03;
        private static final int SILABSER_SET_MHS_REQUEST_CODE = 0x07;
        private static final int SILABSER_SET_BAUDRATE = 0x1E;
        private static final int SILABSER_FLUSH_REQUEST_CODE = 0x12;

        private static final int FLUSH_READ_CODE = 0x0a;
        private static final int FLUSH_WRITE_CODE = 0x05;

        /*
         * SILABSER_IFC_ENABLE_REQUEST_CODE
         */
        private static final int UART_ENABLE = 0x0001;
        private static final int UART_DISABLE = 0x0000;

        /*
         * SILABSER_SET_BAUDDIV_REQUEST_CODE
         */
        private static final int BAUD_RATE_GEN_FREQ = 0x384000;

        /*
         * SILABSER_SET_MHS_REQUEST_CODE
         */
        private static final int MCR_DTR = 0x0001;
        private static final int MCR_RTS = 0x0002;
        private static final int MCR_ALL = 0x0003;

        private static final int CONTROL_WRITE_DTR = 0x0100;
        private static final int CONTROL_WRITE_RTS = 0x0200;

        public Cp21xxSerialPort(UsbDevice device, int portNumber) {
            super(device, portNumber);
        }

        @Override
        public UsbSerialDriver getDriver() {
            return Cp21xxSerialDriver.this;
        }

        private int setConfigSingle(int request, int value) {
            UsbControlIrp irp = mDevice.createUsbControlIrp(
                    (byte) REQTYPE_HOST_TO_DEVICE,
                    (byte) request,
                    (short) value,
                    (short) 0
            );
//            irp.setData(new byte[1]);
            try {
                mDevice.syncSubmit(irp);
            } catch (UsbException e) {
                e.printStackTrace();
            }
//            return irp.getData()[0];
            return 0;
        }

        @Override
        public void open() throws IOException, UsbException {
            if (inputPipe != null || outputPipe != null) {
                throw new IOException("Already opened.");
            }

            boolean opened = false;
            List<UsbInterface> usbInterfaces = mDevice.getActiveUsbConfiguration().getUsbInterfaces();
            try {
                for (UsbInterface usbIface : usbInterfaces) {
                    usbIface.claim();
                }

                UsbInterface dataIface = usbInterfaces.get(usbInterfaces.size() - 1);
                for (UsbEndpoint ep : (List<UsbEndpoint>) dataIface.getUsbEndpoints()) {
                    if (ep.getType() == UsbConst.ENDPOINT_TYPE_BULK) {
                        if (ep.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                            UsbEndpoint readEndpoint = ep;
                            inputPipe = readEndpoint.getUsbPipe();
                        } else {
                            UsbEndpoint writeEndpoint = ep;
                            outputPipe = writeEndpoint.getUsbPipe();
                        }
                    }
                }

                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_ENABLE);
                setConfigSingle(SILABSER_SET_MHS_REQUEST_CODE, MCR_ALL | CONTROL_WRITE_DTR | CONTROL_WRITE_RTS);
                setConfigSingle(SILABSER_SET_BAUDDIV_REQUEST_CODE, BAUD_RATE_GEN_FREQ / DEFAULT_BAUD_RATE);
                //            setParameters(DEFAULT_BAUD_RATE, DEFAULT_DATA_BITS, DEFAULT_STOP_BITS, DEFAULT_PARITY);
                opened = true;
            } finally {
                if (!opened) {
                    try {
                        close();
                    } catch (IOException e) {
                        // Ignore IOExceptions during close()
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void close() throws IOException {
            if (inputPipe == null || outputPipe == null) {
                throw new IOException("Already closed");
            }
            try {
                setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_DISABLE);
                inputPipe.close();
                outputPipe.close();
                for (UsbInterface usbIface : (List<UsbInterface>) mDevice.getActiveUsbConfiguration().getUsbInterfaces()) {
                    usbIface.release();
                }
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            } finally {
                inputPipe = null;
                outputPipe = null;
            }
        }

        @Override
        public int read(byte[] dest, int timeoutMillis) {
            int numBytesRead = 0;
            synchronized (readBufferLock) {
                try {
                    inputPipe.open();
                    UsbIrp usbIrp = inputPipe.asyncSubmit(mReadBuffer);
                    usbIrp.waitUntilComplete(timeoutMillis);
                    numBytesRead = usbIrp.getActualLength();
                    System.arraycopy(mReadBuffer, 0, dest, 0, numBytesRead);
                } catch (UsbException e) {
//                    e.printStackTrace();
                    System.out.println("Pipe is already open");
                } finally {
                    try {
                        inputPipe.close();
                    } catch (UsbException e) {
//                        e.printStackTrace();
                        System.out.println("Pipe is still busy");
                    }
                }
            }
            return numBytesRead;
        }

        @Override
        public int write(byte[] src, int timeoutMillis) {
            int offset = 0;
            try {
                outputPipe.open();
                while (offset < src.length) {
                    final int writeLength;
                    final int amtWritten;

                    synchronized (writeBufferLock) {
                        final byte[] writeBuffer;

                        writeLength = Math.min(src.length - offset, mWriteBuffer.length);
                        if (offset == 0) {
                            writeBuffer = src;
                        } else {
                            // bulkTransfer does not support offsets, make a copy.
                            System.arraycopy(src, offset, mWriteBuffer, 0, writeLength);
                            writeBuffer = mWriteBuffer;
                        }

                        UsbIrp usbIrp = outputPipe.asyncSubmit(writeBuffer);
                        usbIrp.waitUntilComplete(timeoutMillis);
                        amtWritten = usbIrp.getActualLength();
                    }

                    Log.d(TAG, "Wrote amt=" + amtWritten + " attempted=" + writeLength);
                    offset += amtWritten;
                }
            } catch (UsbException e) {
                e.printStackTrace();
            } finally {
                try {
                    outputPipe.close();
                } catch (UsbException e) {
                    e.printStackTrace();
                }
            }

            return offset;
        }

        private void setBaudRate(int baudRate) throws IOException {
            byte[] data = new byte[]{
                    (byte) (baudRate & 0xff),
                    (byte) ((baudRate >> 8) & 0xff),
                    (byte) ((baudRate >> 16) & 0xff),
                    (byte) ((baudRate >> 24) & 0xff)
            };

            UsbControlIrp irp = mDevice.createUsbControlIrp(
                    (byte) REQTYPE_HOST_TO_DEVICE,
                    (byte) SILABSER_SET_BAUDRATE,
                    (short) 0,
                    (short) 0
            );
            irp.setData(data);
            try {
                mDevice.syncSubmit(irp);
            } catch (UsbException e) {
                e.printStackTrace();
            }

            if (irp.getData()[0] < 0) {
                throw new IOException("Error setting baud rate.");
            }
        }

        @Override
        public void setParameters(int baudRate, int dataBits, int stopBits, int parity)
                throws IOException {
            setBaudRate(baudRate);

            int configDataBits = 0;
            switch (dataBits) {
                case DATABITS_5:
                    configDataBits |= 0x0500;
                    break;
                case DATABITS_6:
                    configDataBits |= 0x0600;
                    break;
                case DATABITS_7:
                    configDataBits |= 0x0700;
                    break;
                case DATABITS_8:
                    configDataBits |= 0x0800;
                    break;
                default:
                    configDataBits |= 0x0800;
                    break;
            }

            switch (parity) {
                case PARITY_ODD:
                    configDataBits |= 0x0010;
                    break;
                case PARITY_EVEN:
                    configDataBits |= 0x0020;
                    break;
            }

            switch (stopBits) {
                case STOPBITS_1:
                    configDataBits |= 0;
                    break;
                case STOPBITS_2:
                    configDataBits |= 2;
                    break;
            }
            setConfigSingle(SILABSER_SET_LINE_CTL_REQUEST_CODE, configDataBits);
        }

        @Override
        public boolean getCD() {
            return false;
        }

        @Override
        public boolean getCTS() {
            return false;
        }

        @Override
        public boolean getDSR() {
            return false;
        }

        @Override
        public boolean getDTR() {
            return true;
        }

        @Override
        public void setDTR(boolean value) {
        }

        @Override
        public boolean getRI() {
            return false;
        }

        @Override
        public boolean getRTS() {
            return true;
        }

        @Override
        public void setRTS(boolean value) {
        }

        @Override
        public boolean purgeHwBuffers(boolean purgeReadBuffers,
                                      boolean purgeWriteBuffers) {
            int value = (purgeReadBuffers ? FLUSH_READ_CODE : 0)
                    | (purgeWriteBuffers ? FLUSH_WRITE_CODE : 0);

            if (value != 0) {
                setConfigSingle(SILABSER_FLUSH_REQUEST_CODE, value);
            }

            return true;
        }

    }

    public static Map<Integer, int[]> getSupportedDevices() {
        final Map<Integer, int[]> supportedDevices = new LinkedHashMap<>();
        supportedDevices.put(UsbId.VENDOR_SILABS,
                new int[]{
                        UsbId.SILABS_CP2102,
                        UsbId.SILABS_CP2105,
                        UsbId.SILABS_CP2108,
                        UsbId.SILABS_CP2110
                });
        return supportedDevices;
    }

}
