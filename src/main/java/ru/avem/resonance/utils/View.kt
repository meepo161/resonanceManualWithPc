package ru.avem.resonance.utils

import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.stage.Modality
import javafx.stage.StageStyle
import java.util.concurrent.atomic.AtomicBoolean



class View private constructor() {

    init {
        throw AssertionError()
    }

    companion object {

        fun showConfirmDialog(text: String, actionYes: () -> Unit, actionNo: () -> Unit) {
            showConfirmDialog("", text, actionYes, actionNo)
        }

        fun showConfirmDialog(title: String, text: String, actionYes: () -> Unit, actionNo: () -> Unit) {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.initStyle(StageStyle.UTILITY)
            alert.initModality(Modality.APPLICATION_MODAL)
            alert.title = title
            alert.headerText = text

            val buttonTypeYes = ButtonType("Да", ButtonBar.ButtonData.YES)
            val buttonTypeNo = ButtonType("Нет", ButtonBar.ButtonData.NO)

            alert.buttonTypes.setAll(buttonTypeYes, buttonTypeNo)

            val result = alert.showAndWait()
            if (result.isPresent && result.get() == buttonTypeYes) {
                actionYes.invoke()
            } else if (result.isPresent && result.get() == buttonTypeNo) {
                actionNo.invoke()
            }
        }

        private fun runLaterAndContinue(actionable: () -> Unit) {
            val isFinished = AtomicBoolean(true)
            Platform.runLater {
                actionable.invoke()
                isFinished.set(false)
            }
            while (isFinished.get()) {
                Utils.sleep(1)
            }
        }
    }



}
