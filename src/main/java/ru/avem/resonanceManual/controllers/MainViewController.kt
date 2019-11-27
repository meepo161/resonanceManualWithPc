package ru.avem.resonanceManual.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import ru.avem.resonanceManual.Constants
import ru.avem.resonanceManual.Exitappable
import ru.avem.resonanceManual.Main
import ru.avem.resonanceManual.Main.Companion.PRIMARY_STAGE
import ru.avem.resonanceManual.Main.Companion.css
import ru.avem.resonanceManual.communication.CommunicationModel
import ru.avem.resonanceManual.db.DataBaseRepository
import ru.avem.resonanceManual.db.ProtocolRepository
import ru.avem.resonanceManual.db.TestItemRepository
import ru.avem.resonanceManual.db.model.Protocol
import ru.avem.resonanceManual.db.model.TestItem
import ru.avem.resonanceManual.model.MainModel
import ru.avem.resonanceManual.model.ResultModel
import ru.avem.resonanceManual.states.main.*
import ru.avem.resonanceManual.utils.Toast
import ru.avem.resonanceManual.utils.Utils
import java.io.File
import java.io.IOException
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class MainViewController : Statable {

    //region FXML
    @FXML
    lateinit var buttonProtocolNext: Button
    @FXML
    lateinit var menuBarProtocolSaveAs: MenuItem
    @FXML
    lateinit var editTestItem: MenuItem
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var comboBoxTestItem: ComboBox<TestItem>
    @FXML
    lateinit var textFieldSerialNumber: TextField
    @FXML
    lateinit var checkMenuItemTheme: CheckMenuItem
    //endregion

    //region vars
    private var mainModel: MainModel = MainModel.instance
    private var communicationModel: CommunicationModel? = null
    private var exitappable: Exitappable? = null
    private var protocolFileChooser: FileChooser? = null
    private var dbFileChooser: FileChooser? = null

    private var allTestItems = TestItemRepository.getAllTestItems()
    var currentTestItem: TestItem = mainModel.currentTestItem
    private val resultData = FXCollections.observableArrayList<ResultModel>()

    private val idleState = IdleState(this)
    private val waitState = WaitState(this)
    private val resultState = ResultState(this)
    private var currentState: State = idleState
    //endregion

    @FXML
    private fun initialize() {
        css = "white"
        if (css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        mainModel = MainModel.instance
        communicationModel = CommunicationModel.getInstance()
        initData()
        protocolFileChooser = FileChooser()
        protocolFileChooser!!.initialDirectory = File(System.getProperty("user.home"))
        protocolFileChooser!!.extensionFilters.add(FileChooser.ExtensionFilter("AVEM Protocol (*.axml)", "*.axml"))

        dbFileChooser = FileChooser()
        dbFileChooser!!.initialDirectory = File(System.getProperty("user.home"))
        dbFileChooser!!.extensionFilters.add(FileChooser.ExtensionFilter("AVEM Database (*.adb)", "*.adb"))
        checkMenuItemTheme.isSelected = false
        toIdleState()
    }


    private fun toInitIdleState() {
        menuBarProtocolSaveAs.isDisable = true
    }

    override fun toIdleState() {
        toInitIdleState()
        textFieldSerialNumber.clear()
        textFieldSerialNumber.isDisable = false
        comboBoxTestItem.isDisable = false
        buttonProtocolNext.text = "Создать"
        currentState = idleState
        initData()
    }

    override fun toWaitState() {
        comboBoxTestItem.isDisable = true
        textFieldSerialNumber.isDisable = true
        buttonProtocolNext.text = "Далее"
        menuBarProtocolSaveAs.isDisable = false
        currentState = waitState
    }

    override fun toResultState() {
        val currentProtocol = mainModel.currentProtocol
        currentProtocol.millis = System.currentTimeMillis()
        ProtocolRepository.insertProtocol(currentProtocol)
        Toast.makeText("Результаты проведенных испытаний сохранены").show(Toast.ToastType.INFORMATION)
        currentState = resultState
        initData()
    }

    private fun initData() {
        allTestItems = TestItemRepository.getAllTestItems()
        comboBoxTestItem.items.clear()
        comboBoxTestItem.selectionModel.clearSelection()
        comboBoxTestItem.items.setAll(allTestItems)
        comboBoxTestItem.selectionModel.clearSelection()
        comboBoxTestItem.selectionModel.selectFirst()
        resultData.clear()
        handleSelectTestItemExperiment()
    }

    @FXML
    fun handleSelectTestItemExperiment() {
        currentTestItem = comboBoxTestItem.selectionModel.selectedItem
        mainModel.currentTestItem = currentTestItem
    }

    @FXML
    private fun handleMenuBarProtocolOpen() {
        currentState.toIdleState()
        protocolFileChooser!!.title = "Выберите файл протокола"
        val file = protocolFileChooser!!.showOpenDialog(PRIMARY_STAGE)
        if (file != null) {
            openProtocolFromFile(file)
        }
    }

    private fun openProtocolFromFile(file: File) {
        try {
            val context = JAXBContext.newInstance(Protocol::class.java)
            val um = context.createUnmarshaller()
            val protocol = um.unmarshal(file) as Protocol
            comboBoxTestItem.selectionModel.select(protocol.getObject())
            mainModel.currentProtocol = protocol
            currentState.toWaitState()
            Toast.makeText(String.format("Протокол %s успешно загружен", file.name)).show(Toast.ToastType.INFORMATION)
        } catch (e: Exception) {
            Toast.makeText("Ошибка загрузки протокола").show(Toast.ToastType.ERROR)
        }
    }

    @FXML
    @Throws(IOException::class)
    private fun handleMenuBarProtocolOpenFromDB() {
        currentState.toIdleState()
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/protocolSelector.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Выберите протокол из списка"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        dialogStage.isResizable = false
        val scene = Scene(page)
        dialogStage.scene = scene
        val controller = loader.getController<ProtocolSelectorController>()
        controller.setDialogStage(dialogStage)

        dialogStage.showAndWait()
        if (!controller.isCanceled) {
            mainModel.applyIntermediateProtocol()
            comboBoxTestItem.selectionModel.select(mainModel.currentProtocol.getObject())
            currentState.toWaitState()
        }
    }

    @FXML
    private fun handleMenuBarProtocolSaveAs() {
        protocolFileChooser!!.title = "Сохраните файл протокола"
        var file: File? = protocolFileChooser!!.showSaveDialog(PRIMARY_STAGE)
        if (file != null) {
            if (!file.path.endsWith(".axml")) {
                file = File(file.path + ".axml")
            }
            saveProtocolToFile(file)
        }
    }

    private fun saveProtocolToFile(file: File) {
        try {
            val context = JAXBContext.newInstance(Protocol::class.java)
            val m = context.createMarshaller()
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
            m.marshal(mainModel.currentProtocol, file)
            Toast.makeText(String.format("Протокол %s успешно сохранён", file.name)).show(Toast.ToastType.INFORMATION)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(String.format("Ошибка при сохранении протокола %s", file.name)).show(Toast.ToastType.ERROR)
        }
    }

    @FXML
    fun handleExit() {
        currentState.toIdleState()
        Platform.exit()
    }

    @FXML
    @Throws(IOException::class)
    private fun handleProtocols() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/protocolEditor.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Протоколы"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        val scene = Scene(page)
        dialogStage.scene = scene

        dialogStage.showAndWait()
    }

    @FXML
    @Throws(IOException::class)
    private fun handleMenuProfiles() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/registerWindow.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Редактировать профиль"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        dialogStage.isResizable = false
        val scene = Scene(page)
        dialogStage.scene = scene
        dialogStage.isResizable = false
        dialogStage.showAndWait()
    }

    @FXML
    private fun handleImportDB() {
        dbFileChooser!!.title = "Выберите файл базы данных для импорта"
        val file = dbFileChooser!!.showOpenDialog(PRIMARY_STAGE)
        if (file != null) {
            importDBFromFile(file)
        }
    }

    @FXML
    private fun importDBFromFile(file: File) {
        try {
            Utils.copyFileFromFile(file, File(DataBaseRepository.DATABASE_NAME))
            Toast.makeText(String.format("База успешно импортирована из файла %s", file.absolutePath)).show(Toast.ToastType.INFORMATION)
        } catch (e: IOException) {
            Toast.makeText("Ошибка при импорте базы данных").show(Toast.ToastType.ERROR)
        }
    }

    @FXML
    private fun handleExportDB() {
        dbFileChooser!!.title = "Сохраните базу данных в файл"
        var file: File? = dbFileChooser!!.showSaveDialog(PRIMARY_STAGE)
        if (file != null) {
            if (!file.path.endsWith(".adb")) {
                file = File(file.path + ".adb")
            }
            exportDBToFile(file)
        }
    }

    private fun exportDBToFile(file: File) {
        try {
            Utils.copyFileFromFile(File(DataBaseRepository.DATABASE_NAME), file)
            Toast.makeText(String.format("База успешно экспортирована в файл %s", file.absolutePath)).show(Toast.ToastType.INFORMATION)
        } catch (e: IOException) {
            Toast.makeText("Ошибка при экспорте базы данных").show(Toast.ToastType.ERROR)
        }
    }

    @FXML
    @Throws(IOException::class)
    private fun handleDeviceState() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/deviceStateWindow.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Состояние устройств"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        val scene = Scene(page)
        dialogStage.isResizable = false
        dialogStage.scene = scene

        dialogStage.setOnCloseRequest {
            val communicationModel = CommunicationModel.getInstance()
            communicationModel.finalizeAllDevices()
            communicationModel.deleteObservers()
            communicationModel.setDeviceStateOn(false)
        }
        dialogStage.showAndWait()
    }

    @FXML
    @Throws(IOException::class)
    private fun handleCurrentProtection() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/currentProtectionWindow.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Состояние защит"
        dialogStage.initModality(Modality.APPLICATION_MODAL)
        dialogStage.isResizable = false
        dialogStage.scene = Scene(page)

        dialogStage.setOnCloseRequest {
            communicationModel!!.finalizeAllDevices()
            communicationModel!!.deleteObservers()
        }
        dialogStage.showAndWait()
    }

    @FXML
    private fun handleButtonProtocolNext() {
        if (comboBoxTestItem.selectionModel.selectedItem != null) {
            mainModel.createNewProtocol(textFieldSerialNumber.text, currentTestItem)
            startExperiment()
            toResultState()
        } else {
            Toast.makeText("Выберите объект испытания").show(Toast.ToastType.INFORMATION)
        }
    }

    fun handleMenuBarProtocolNew() {
        toIdleState()
    }

    private fun startExperiment(): Boolean {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/experiment1ViewManual.fxml")
        var controller: ExperimentController? = null
        try {
            val page = loader.load<Parent>()

            val dialogStage = Stage()
            dialogStage.title = "Опыт"
            dialogStage.initModality(Modality.WINDOW_MODAL)
            dialogStage.initOwner(PRIMARY_STAGE)
            val scene = Scene(page, Constants.Display.WIDTH.toDouble(), Constants.Display.HEIGHT.toDouble())
            dialogStage.scene = scene
            dialogStage.initStyle(StageStyle.TRANSPARENT)
            controller = loader.getController<ExperimentController>()
            controller!!.setDialogStage(dialogStage)

            dialogStage.showAndWait()


        } catch (e: IOException) {
            e.printStackTrace()
        }

        communicationModel!!.finalizeAllDevices()
        communicationModel!!.deleteObservers()

        return controller != null && controller.isCanceled
    }

    fun setMain(exitappable: Exitappable) {
        this.exitappable = exitappable
    }

    @FXML
    fun handleCheckMenuItemTheme() {
        css = if (!checkMenuItemTheme.isSelected) { //сначала галочка ставится, потом срабатывает handle
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
            "white"
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
            "black"
        }
    }

    @FXML
    fun handleEditTestItem() {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource("layouts/testItemEditor.fxml")
        val page = loader.load<Parent>()

        val dialogStage = Stage()
        dialogStage.title = "Редактор объекта испытания"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        dialogStage.isResizable = false
        val scene = Scene(page)
        dialogStage.scene = scene
        dialogStage.isResizable = false
        dialogStage.showAndWait()
        toIdleState()
    }

    @FXML
    fun handleAddTestItem() {
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

    fun handleDeleteTestItem() {
        if (comboBoxTestItem.selectionModel.selectedItem != null) {
            TestItemRepository.deleteTestItem(currentTestItem)
            initDataForTestItem()
        } else {
            Toast.makeText("Выберите ОИ для удаления").show(Toast.ToastType.INFORMATION)
        }
    }

    private fun initDataForTestItem() {
        val allTestItems = TestItemRepository.getAllTestItems()
        comboBoxTestItem.items.clear()
        if (allTestItems.isNotEmpty()) {
            comboBoxTestItem.items.setAll(allTestItems)
            comboBoxTestItem.selectionModel.selectFirst()
        }
    }

    @FXML
    fun handleAbout() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Версия ПО"
        alert.headerText = "Версия: 0.1.0"
        alert.contentText = "Дата: 02.08.2019"
        alert.showAndWait()
    }

}
