package ru.avem.resonance.controllers

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
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
    lateinit var tFType: TextField
    @FXML
    lateinit var comboBoxTestItem: ComboBox<TestItem>
    @FXML
    lateinit var root: AnchorPane
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
            tFType.text = currentTestItem.type
        }
    }

    @FXML
    fun handleComboBoxTestItem() {
        val selectedItem = comboBoxTestItem.selectionModel.selectedItem
        if (selectedItem != null) {
            clearAllTF()
            val currentTestItem = TestItemRepository.getTestItem(selectedItem.toString())
            tFType.text = currentTestItem.type
        }
    }

    @FXML
    fun handleSave() {
        val selectedItem = comboBoxTestItem.selectionModel.selectedItem
        if (selectedItem != null) {
            val currentTestItem = TestItemRepository.getTestItem(selectedItem.toString())
            currentTestItem.type = tFType.text
            TestItemRepository.updateTestItem(currentTestItem)
            initData()
        } else {
            val currentTestItem = TestItem()
            currentTestItem.type = tFType.text
            TestItemRepository.insertTestItem(currentTestItem)
            initData()
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
        tFType.text = ""
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
}
