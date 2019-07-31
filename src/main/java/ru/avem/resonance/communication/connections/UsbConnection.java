package ru.avem.resonance.communication.connections;

import ru.avem.resonance.utils.Log;
import ru.avem.resonance.utils.Logger;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsbConnection {
    private static final String TAG = UsbConnection.class.getSimpleName();

    private int mTimeoutMillis;
    private String mDeviceName;

    private UsbPipe mInputPipe = null;
    private UsbPipe mOutputPipe = null;
    private UsbInterface mInterface = null;

    private final Object mReadBufferLock = new Object();

    public UsbConnection(String deviceName, int timeoutMillis) {
        mDeviceName = deviceName;
        mTimeoutMillis = timeoutMillis;
    }

    public void initPort() {
        UsbDevice usbDevice = initDevice();
        if (usbDevice != null) {
            if (!initConnection(usbDevice)) {
                mInputPipe = null;
                mOutputPipe = null;
                mInterface = null;
            }
        }
    }

    private UsbDevice initDevice() {
        return findDevice(mDeviceName);
    }

    private static UsbDevice findDevice(String deviceName) {
        try {
            List<UsbDevice> allDevices = findAllDevices();
            int i = 0;
            for (final UsbDevice usbDevice : allDevices) {
                try {
                    Logger.withTag(TAG).log(++i + ". usbDeviceProductString: " + usbDevice.getProductString());
                    if (Objects.equals(usbDevice.getProductString(), deviceName)) {
                        return usbDevice;
                    }
                } catch (UsbException | UnsupportedEncodingException ignored) {
                }
            }
        } catch (UsbException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<UsbDevice> findAllDevices() throws UsbException {
        final List<UsbDevice> result = new ArrayList<>();
        UsbServices usbServices = UsbHostManager.getUsbServices();
        UsbHub rootUsbHub = usbServices.getRootUsbHub();

        findDriversInTheRoot(result, rootUsbHub);
        return result;
    }

    private static void findDriversInTheRoot(List<UsbDevice> result, UsbHub rootUsbHub) {
        List<UsbDevice> attachedUsbDevices = rootUsbHub.getAttachedUsbDevices();
        for (final UsbDevice usbDevice : attachedUsbDevices) {
            if (usbDevice.isUsbHub()) {
                Log.d(TAG, ">--usbHub " + usbDevice);
                findDriversInTheRoot(result, (UsbHub) usbDevice);
            } else {
                Log.d(TAG, ">--usbDevice " + usbDevice);
                final UsbDevice finalUsbDevice = probeDevice(usbDevice);
                Log.d(TAG, "finalUsbDevice " + finalUsbDevice + "<--\n");
                if (finalUsbDevice != null) {
                    result.add(finalUsbDevice);
                }
            }
        }
    }

    private static UsbDevice probeDevice(UsbDevice usbDevice) {
        try {
            Logger.withTag(TAG).log("probe usbDevice: " + usbDevice);
            Logger.withTag(TAG).log("probe usbDeviceProductString: " + usbDevice.getProductString());
            return usbDevice;
        } catch (UsbException | UnsupportedEncodingException ignored) {
        }
        return null;
    }

    private boolean initConnection(UsbDevice usbDevice) {
        boolean inputPipeFound = false;
        boolean outputPipeFound = false;
        boolean opened = false;

        mInterface = (UsbInterface) usbDevice.getActiveUsbConfiguration().getUsbInterfaces().get(0);
        if (mInterface != null) {
            try {
                mInterface.claim();
                for (UsbEndpoint ep : (List<UsbEndpoint>) mInterface.getUsbEndpoints()) {
                    if (ep.getType() == UsbConst.ENDPOINT_TYPE_BULK) {
                        if (ep.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                            mInputPipe = ep.getUsbPipe();
                            inputPipeFound = true;
                        } else {
                            mOutputPipe = ep.getUsbPipe();
                            outputPipeFound = true;
                        }
                    }
                }
                opened = true;
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            } finally {
                if (!opened || !inputPipeFound || !outputPipeFound) {
                    closeConnection();
                }
            }
        }

        return opened && inputPipeFound && outputPipeFound;
    }

    private void closeConnection() {
        try {
            if (mInputPipe != null) {
                mInputPipe.close();
            }
            if (mOutputPipe != null) {
                mOutputPipe.close();
            }
            if (mInterface != null) {
                mInterface.release();
            }
        } catch (UsbClaimException e) {
            e.printStackTrace();
        } catch (UsbException e) {
            e.printStackTrace();
        } finally {
            mInputPipe = null;
            mOutputPipe = null;
            mInterface = null;
        }
    }

    public synchronized int write(byte[] writeBuffer, int timeoutMillis) {
        int numBytesWritten = -1;
        if (mOutputPipe != null) {
            try {
                mOutputPipe.open();
                UsbIrp usbIrp = mOutputPipe.asyncSubmit(writeBuffer);
                usbIrp.waitUntilComplete(timeoutMillis);
                numBytesWritten = usbIrp.getActualLength();
            } catch (UsbException e) {
                e.printStackTrace();
            } finally {
                try {
                    mOutputPipe.close();
                } catch (UsbException e) {
                    e.printStackTrace();
                }
            }
            return numBytesWritten;
        } else {
            return numBytesWritten;
        }
    }

    public synchronized int read(byte[] readBuffer, int timeoutMillis) {
        int numBytesRead = -1;
        synchronized (mReadBufferLock) {
            if (mInputPipe != null) {
                try {
                    mInputPipe.open();
                    UsbIrp usbIrp = mInputPipe.asyncSubmit(readBuffer);
                    usbIrp.waitUntilComplete(timeoutMillis);
                    numBytesRead = usbIrp.getActualLength();
                } catch (UsbException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        mInputPipe.close();
                    } catch (UsbException e) {
                        e.printStackTrace();
                    }
                }
                return numBytesRead;
            } else {
                return numBytesRead;
            }
        }
    }
}
