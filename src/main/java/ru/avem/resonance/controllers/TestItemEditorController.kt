package ru.avem.resonance.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.AnchorPane
import ru.avem.resonance.Main
import ru.avem.resonance.db.TestItemRepository
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.utils.Toast

class TestItemEditorController {

    //region FXML
    @FXML
    lateinit var tFtype: TextField
    @FXML
    lateinit var tFTorque: TextField
    @FXML
    lateinit var tFPower: TextField
    @FXML
    lateinit var tFVoltage: TextField
    @FXML
    lateinit var tFAverageCurrent: TextField
    @FXML
    lateinit var tFNoLoadCurrent: TextField
    @FXML
    lateinit var tFRotation: TextField
    @FXML
    lateinit var tFKPD: TextField
    @FXML
    lateinit var tFTemperature: TextField
    @FXML
    lateinit var comboBoxTestItem: ComboBox<TestItem>
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var chBRotationLeft: CheckBox
    @FXML
    lateinit var chBRotationRight: CheckBox
    //endregion

    private var mainModel: MainModel? = null
    private var testItems: ObservableList<TestItem>? = null

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        mainModel = MainModel.instance
        initData()
        clearAllTF()
        comboBoxTestItem.selectionModel.clearSelection()
    }

    private fun initData() {
        if (TestItemRepository.getAllTestItems().isNotEmpty()) {
            comboBoxTestItem.items.removeAll()
            val allTestItems = TestItemRepository.getAllTestItems()
            testItems = FXCollections.observableArrayList(allTestItems)
            comboBoxTestItem.selectionModel.clearSelection()
            comboBoxTestItem.items.clear()
            comboBoxTestItem.items.setAll(testItems)
            comboBoxTestItem.selectionModel.selectFirst()
            val currentTestItem = TestItemRepository.getTestItem(comboBoxTestItem.selectionModel.selectedItem.toString())
            tFtype.text = currentTestItem.type
            tFTorque.text = currentTestItem.torque.toString()
            tFPower.text = currentTestItem.power.toString()
            tFVoltage.text = currentTestItem.voltage.toString()
            tFAverageCurrent.text = currentTestItem.averageCurrent.toString()
            tFNoLoadCurrent.text = currentTestItem.noLoadCurrent.toString()
            tFRotation.text = currentTestItem.rotation.toString()
            tFKPD.text = currentTestItem.kpd.toString()
            tFTemperature.text = currentTestItem.temperature.toString()
            direction = currentTestItem.direction
            if (direction == "right") {
                handleRotationRight()
            } else if (direction == "left") {
                handleRotationLeft()
            }
        }
    }

    @FXML
    fun handleComboBoxTestItem() {
        val selectedItem = comboBoxTestItem.selectionModel.selectedItem
        if (selectedItem != null) {
            clearAllTF()
            val currentTestItem = TestItemRepository.getTestItem(selectedItem.toString())
            tFtype.text = currentTestItem.type
            if (currentTestItem.torque.toString().isNotEmpty()) {
                tFTorque.text = currentTestItem.torque.toString()
            }
            if (currentTestItem.power.toString().isNotEmpty()) {
                tFPower.text = currentTestItem.power.toString()
            }
            if (currentTestItem.voltage.toString().isNotEmpty()) {
                tFVoltage.text = currentTestItem.voltage.toString()
            }
            if (currentTestItem.averageCurrent.toString().isNotEmpty()) {
                tFAverageCurrent.text = currentTestItem.averageCurrent.toString()
            }
            if (currentTestItem.noLoadCurrent.toString().isNotEmpty()) {
                tFNoLoadCurrent.text = currentTestItem.noLoadCurrent.toString()
            }
            if (currentTestItem.rotation.toString().isNotEmpty()) {
                tFRotation.text = currentTestItem.rotation.toString()
            }
            if (currentTestItem.kpd.toString().isNotEmpty()) {
                tFKPD.text = currentTestItem.kpd.toString()
            }
            if (currentTestItem.temperature.toString().isNotEmpty()) {
                tFTemperature.text = currentTestItem.temperature.toString()
            }
            direction = currentTestItem.direction
            if (direction == "right") {
                handleRotationRight()
            } else if (direction == "left") {
                handleRotationLeft()
            }
        }
    }

    @FXML
    fun handleSave() {
        if (tFTorque.text.toDoubleOrNull() == null) {
            Toast.makeText("Неверный формат записи номинального момента. Используйте вместо запятых - точки").show(Toast.ToastType.ERROR)
        } else if (tFRotation.text.toDoubleOrNull() == null) {
            Toast.makeText("Неверный формат записи частоты вращения. Используйте вместо запятых - точки").show(Toast.ToastType.ERROR)
        } else {
            val selectedItem = comboBoxTestItem.selectionModel.selectedItem
            if (selectedItem != null) {
                val currentTestItem = TestItemRepository.getTestItem(selectedItem.toString())
                currentTestItem.type = tFtype.text
                currentTestItem.torque = tFTorque.text.toDouble()
                currentTestItem.power = tFPower.text.toDouble()
                currentTestItem.voltage = tFVoltage.text.toDouble()
                currentTestItem.averageCurrent = tFAverageCurrent.text.toDouble()
                currentTestItem.noLoadCurrent = tFNoLoadCurrent.text.toDouble()
                currentTestItem.rotation = tFRotation.text.toDouble()
                currentTestItem.kpd = tFKPD.text.toDouble()
                currentTestItem.temperature = tFTemperature.text.toDouble()
                currentTestItem.direction = direction
                TestItemRepository.updateTestItem(currentTestItem)
                initData()
            } else {
                val currentTestItem = TestItem()
                currentTestItem.type = tFtype.text
                currentTestItem.torque = tFTorque.text.toDouble()
                currentTestItem.power = tFPower.text.toDouble()
                currentTestItem.voltage = tFVoltage.text.toDouble()
                currentTestItem.averageCurrent = tFAverageCurrent.text.toDouble()
                currentTestItem.noLoadCurrent = tFNoLoadCurrent.text.toDouble()
                currentTestItem.rotation = tFRotation.text.toDouble()
                currentTestItem.kpd = tFKPD.text.toDouble()
                currentTestItem.temperature = tFTemperature.text.toDouble()
                currentTestItem.direction = direction
                TestItemRepository.insertTestItem(currentTestItem)
                initData()
            }
        }
    }

    @FXML
    fun handleDelete() {
        if (comboBoxTestItem.selectionModel.selectedItem != null) {
            val currentTestItem = TestItemRepository.getTestItem(comboBoxTestItem.selectionModel.selectedItem.toString())
            TestItemRepository.deleteTestItem(currentTestItem)
            comboBoxTestItem.items.setAll(TestItemRepository.getAllTestItems())
            clearAllTF()
            initData()
        } else {
            Toast.makeText("Нечего удалять").show(Toast.ToastType.INFORMATION)
        }
    }

    private fun clearAllTF() {
        tFtype.text = ""
        tFtype.text = ""
        tFTorque.text = ""
        tFPower.text = ""
        tFVoltage.text = ""
        tFAverageCurrent.text = ""
        tFNoLoadCurrent.text = ""
        tFRotation.text = ""
        tFKPD.text = ""
        tFTemperature.text = ""
    }

    fun handleRotationLeft() {
        chBRotationLeft.isSelected = true
        chBRotationRight.isSelected = false
        direction = "left"
    }

    fun handleRotationRight() {
        chBRotationLeft.isSelected = false
        chBRotationRight.isSelected = true
        direction = "right"
    }

    @FXML
    private fun handleAddTestItem() {
        val dialog = TextInputDialog("Двигатель")
        dialog.title = "Редактор типов двигателя"
        dialog.headerText = "Добавить новый тип двигателя"
        dialog.contentText = "Введите тип: "
        val result = dialog.showAndWait()
        if (result.isPresent) {
            val testItem = TestItem(result.get())
            TestItemRepository.insertTestItem(testItem)
            comboBoxTestItem.items.add(testItem)
        }
    }

    companion object {
        var direction = ""
    }
}
