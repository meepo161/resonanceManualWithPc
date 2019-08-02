package ru.avem.resonance.controllers

import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.AnchorPane
import ru.avem.resonance.Main
import ru.avem.resonance.db.TestItemRepository
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.utils.Toast

class TestItemEditorController {
    //region FXML
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var comboBoxTestItem: ComboBox<TestItem>
    @FXML
    lateinit var textFieldType: TextField
    //endregion

    private fun getSelectedTestItem() = comboBoxTestItem.selectionModel.selectedItem

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        initData()
    }

    private fun initData() {
        val allTestItems = TestItemRepository.getAllTestItems()

        comboBoxTestItem.items.clear()
        if (allTestItems.isNotEmpty()) {
            comboBoxTestItem.items.setAll(allTestItems)
            comboBoxTestItem.selectionModel.selectFirst()
            fillParameters()
        } else {
            clearParameters()
        }
    }

    private fun fillParameters() {
        if (getSelectedTestItem() != null) {
            textFieldType.text = getSelectedTestItem().type
        }
    }

    private fun clearParameters() {
        textFieldType.text = ""
        comboBoxTestItem.selectionModel.clearSelection()
    }

    @FXML
    fun handleComboBoxTestItem() {
        val selectedTestItem = getSelectedTestItem()
        if (selectedTestItem != null) {
            fillParameters()
        }
    }

    @FXML
    fun handleSave() {
        val selectedTestItem = getSelectedTestItem()
        if (selectedTestItem != null) {
            selectedTestItem.type = textFieldType.text
            TestItemRepository.updateTestItem(selectedTestItem)
            initData()
        } else {
            Toast.makeText("Выберите ОИ для сохранения").show(Toast.ToastType.INFORMATION)
        }
    }

    @FXML
    fun handleDelete() {
        val selectedTestItem = getSelectedTestItem()
        if (selectedTestItem != null) {
            TestItemRepository.deleteTestItem(selectedTestItem)
            initData()
        } else {
            Toast.makeText("Выберите ОИ для удаления").show(Toast.ToastType.INFORMATION)
        }
    }

    @FXML
    private fun handleAddTestItem() {
        val dialog = TextInputDialog("Двигатель")
        dialog.title = "Редактор типов двигателя"
        dialog.headerText = "Добавить новый тип двигателя"
        dialog.contentText = "Введите тип: "
        val result = dialog.showAndWait()
        if (result.isPresent) {
            var name = result.get()
            if (name.trim().isNotBlank()) {
                name = checkName((comboBoxTestItem.items.map { it.type }).toMutableList(), name)
                val testItem = TestItem(name)
                TestItemRepository.insertTestItem(testItem)
                comboBoxTestItem.items.add(testItem)
                comboBoxTestItem.selectionModel.selectLast()
            } else {
                Toast.makeText("Введите корректное наименование типа").show(Toast.ToastType.INFORMATION)
                handleAddTestItem()
            }
        }
    }

    private fun checkName(map: MutableList<String>, name: String, _i: Int = 1): String {
        var i = _i
        return if (map.contains(name)) {
            map.removeAll { it == name }
            val j = i - 1
            checkName(map, "${name.removeSuffix("($j)")}($i)", ++i)
        } else {
            name
        }
    }
}
