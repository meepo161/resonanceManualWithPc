package ru.avem.resonance.communication.modbus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

class LogAnalyzer {
    private static int sWrite;
    private static int sSuccess;

    static void addWrite() {
        sWrite++;
    }

    static void addSuccess() {
        sSuccess++;
        DateFormat df = new SimpleDateFormat("mm:ss");
//        System.out.printf(
//                "%s Записано: %d, Удач: %d, Разница: %d, Процент: %.4f\n",
//                df.format(System.currentTimeMillis()), sWrite, sSuccess, sWrite - sSuccess, (sWrite - sSuccess) / (float) sWrite);
    }
}
