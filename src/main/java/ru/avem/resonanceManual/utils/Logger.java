package ru.avem.resonanceManual.utils;

public class Logger {
    private final String TAG;

    public Logger(String tag) {
        this.TAG = tag;
    }

    public static Logger withTag(String tag) {
        return new Logger(tag);
    }

    public <T> Logger log(T message) {
            Log.d(TAG, message + "");
        return this;
    }
}
