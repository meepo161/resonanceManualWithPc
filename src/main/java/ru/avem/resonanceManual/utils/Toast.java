package ru.avem.resonanceManual.utils;

import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;

public class Toast {
    private Notifications notifications;

    private Toast(Notifications notifications) {
        this.notifications = notifications;
    }

    public void show(ToastType type) {
        switch (type) {
            case INFORMATION:
                notifications.title("Информация");
                notifications.showInformation();
                break;
            case CONFIRM:
                notifications.title("Подтверждение");
                notifications.showConfirm();
                break;
            case ERROR:
                notifications.title("Ошибка");
                notifications.showError();
                break;
            case WARNING:
                notifications.title("Внимание");
                notifications.showWarning();
                break;
            case NONE:
            default:
                notifications.show();
                break;
        }
    }

    public enum ToastType {
        INFORMATION,
        CONFIRM,
        ERROR,
        WARNING,
        NONE
    }

    public static Toast makeText(String text) {
        return new Toast(Notifications.create().text(text).position(Pos.BOTTOM_CENTER));
    }
}
