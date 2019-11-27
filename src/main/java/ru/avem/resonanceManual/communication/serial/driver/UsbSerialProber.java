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

package ru.avem.resonanceManual.communication.serial.driver;

import ru.avem.resonanceManual.utils.Log;

import javax.usb.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mike wakerly (opensource@hoho.com)
 */
public class UsbSerialProber {
    private static final String TAG = UsbSerialProber.class.getSimpleName();

    private final ProbeTable mProbeTable;

    public UsbSerialProber(ProbeTable probeTable) {
        mProbeTable = probeTable;
    }

    public static UsbSerialProber getDefaultProber() {
        return new UsbSerialProber(getDefaultProbeTable());
    }
    
    public static ProbeTable getDefaultProbeTable() {
        final ProbeTable probeTable = new ProbeTable();
        probeTable.addDriver(Cp21xxSerialDriver.class);
        return probeTable;
    }

    /**
     * Finds and builds all possible {@link UsbSerialDriver UsbSerialDrivers}
     * from the currently-attached {@link UsbDevice} hierarchy. This method does
     * not require permission from the Android USB system, since it does not
     * open any of the devices.
     *
     * @return a list, possibly empty, of all compatible drivers
     */
    public List<UsbSerialDriver> findAllDrivers() throws UsbException {
        final List<UsbSerialDriver> result = new ArrayList<UsbSerialDriver>();
        UsbServices usbServices = UsbHostManager.getUsbServices();
        UsbHub rootUsbHub = usbServices.getRootUsbHub();

        findDriversInTheRoot(result, rootUsbHub);
        return result;
    }

    private void findDriversInTheRoot(List<UsbSerialDriver> result, UsbHub rootUsbHub) {
        List<UsbDevice> attachedUsbDevices = rootUsbHub.getAttachedUsbDevices();
        for (final UsbDevice usbDevice : attachedUsbDevices) {
            if (usbDevice.isUsbHub()) {
                Log.d(TAG, ">--usbHub " + usbDevice);
                findDriversInTheRoot(result, (UsbHub) usbDevice);
            } else {
                Log.d(TAG, ">--usbDevice " + usbDevice);
                final UsbSerialDriver driver = probeDevice(usbDevice);
                Log.d(TAG, "driver " + driver + "<--\n");
                if (driver != null) {
                    result.add(driver);
                }
            }
        }
    }
    
    /**
     * Probes a single device for a compatible driver.
     * 
     * @param usbDevice the usb device to probe
     * @return a new {@link UsbSerialDriver} compatible with this device, or
     *         {@code null} if none available.
     */
    public UsbSerialDriver probeDevice(final UsbDevice usbDevice) {
        final int vendorId = usbDevice.getUsbDeviceDescriptor().idVendor() & 0xffff;
        Log.d(TAG, "vendorId " + vendorId);
        final int productId = usbDevice.getUsbDeviceDescriptor().idProduct() & 0xffff;
        Log.d(TAG, "productId " + productId + "\n");

        final Class<? extends UsbSerialDriver> driverClass =
                mProbeTable.findDriver(vendorId, productId);
        if (driverClass != null) {
            final UsbSerialDriver driver;
            try {
                final Constructor<? extends UsbSerialDriver> ctor =
                        driverClass.getConstructor(UsbDevice.class);
                driver = ctor.newInstance(usbDevice);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return driver;
        }
        return null;
    }

}
