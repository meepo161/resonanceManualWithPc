package ru.avem.resonanceManual.utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Log {
    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss-SSS");

    public static void d(String tag, String s) {
        System.out.printf("%s D/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void i(String tag, String s) {
        System.out.printf("%s I/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void w(String tag, String s, Exception e) {
        System.out.printf("%s W/%s: %s\n", df.format(System.currentTimeMillis()), tag, s);
    }

    public static void w(String tag, Exception e) {
        System.out.printf("%s W/%s: %s\n", df.format(System.currentTimeMillis()), tag, e.getMessage());
    }

    public static void e(String tag, String s, IOException e) {
        System.out.printf("%s E/%s: %s %s\n", df.format(System.currentTimeMillis()), s, tag, e.getMessage());
    }
}
