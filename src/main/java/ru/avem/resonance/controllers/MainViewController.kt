package ru.avem.resonance.controllers

import com.j256.ormlite.logger.LoggerFactory
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import ru.avem.resonance.Constants
import ru.avem.resonance.Exitappable
import ru.avem.resonance.Main
import ru.avem.resonance.Main.Companion.PRIMARY_STAGE
import ru.avem.resonance.Main.Companion.css
import ru.avem.resonance.communication.CommunicationModel
import ru.avem.resonance.db.DataBaseRepository
import ru.avem.resonance.db.ProtocolRepository
import ru.avem.resonance.db.TestItemRepository
import ru.avem.resonance.db.model.Protocol
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.ResultModel
import ru.avem.resonance.states.main.*
import ru.avem.resonance.utils.Toast
import ru.avem.resonance.utils.Utils
import java.awt.Toolkit
import java.io.File
import java.io.IOException
import java.util.*
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import kotlin.collections.ArrayList

class MainViewController : Statable {


    //region FXML
    @FXML
    lateinit var buttonProtocolCancel: Button
    @FXML
    lateinit var buttonProtocolNext: Button
    @FXML
    lateinit var buttonTestItemGenerate: Button
    @FXML
    lateinit var buttonAdd: Button
    @FXML
    lateinit var buttonRemove: Button
    @FXML
    lateinit var menuBarProtocolSaveAs: MenuItem
    @FXML
    lateinit var editTestItem: MenuItem
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var tabPane: TabPane
    @FXML
    lateinit var tabProtocol: Tab
    @FXML
    lateinit var tabResults: Tab
    @FXML
    lateinit var checkBoxResonance: CheckBox
    @FXML
    lateinit var checkBoxExperiment1: CheckBox
    @FXML
    lateinit var checkBoxExperiment2: CheckBox
    @FXML
    lateinit var gridPaneTimeTorque: GridPane
    @FXML
    lateinit var vBoxTime: VBox
    @FXML
    lateinit var vBoxTorque: VBox
    @FXML
    lateinit var anchorPaneTimeTorque: AnchorPane
    @FXML
    lateinit var scrollPaneTimeTorque: ScrollPane
    @FXML
    lateinit var comboBoxTestItem: ComboBox<TestItem>
    @FXML
    lateinit var textFieldSerialNumber: TextField
    @FXML
    lateinit var loadDiagram: LineChart<Number, Number>
    @FXML
    lateinit var tableViewResults: TableView<ResultModel>
    @FXML
    lateinit var columnTableDimension: TableColumn<ResultModel, String>
    @FXML
    lateinit var columnTableValue: TableColumn<ResultModel, String>
    @FXML
    lateinit var checkMenuItemTheme: CheckMenuItem
    @FXML
    lateinit var textFieldViu: TextField
    @FXML
    lateinit var textFieldViuDC: TextField

    //endregion

    //region vars
    private var mainModel: MainModel = MainModel.instance
    private var communicationModel: CommunicationModel? = null
    private var exitappable: Exitappable? = null
    private var protocolFileChooser: FileChooser? = null
    private var DBFileChooser: FileChooser? = null

    private lateinit var lastPair: Pair<TextField, TextField>
    private val stackPairs: Stack<Pair<TextField, TextField>> = Stack()

    private var allTestItems = TestItemRepository.getAllTestItems()
    private var currentTestItem: TestItem? = null
    private val resultData = FXCollections.observableArrayList<ResultModel>()

    private val idleState = IdleState(this)
    private val waitState = WaitState(this)
    private val resultState = ResultState(this)
    private var currentState: State = idleState

    //endregion

    companion object {
        const val HEIGHT_VBOX: Int = 57
        var isXXSelected = false
        var isElevatedRotation = false
        private val logger = LoggerFactory.getLogger(MainViewController::class.java)
    }

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

        DBFileChooser = FileChooser()
        DBFileChooser!!.initialDirectory = File(System.getProperty("user.home"))
        DBFileChooser!!.extensionFilters.add(FileChooser.ExtensionFilter("AVEM Database (*.adb)", "*.adb"))
        checkMenuItemTheme.isSelected = false
        toIdleState()
    }

    private fun toInitIdleState() {
        menuBarProtocolSaveAs.isDisable = true
        tabResults.isDisable = true
        buttonProtocolCancel.isDisable = true
        buttonProtocolNext.isDisable = true
    }

    override fun toIdleState() {
        toInitIdleState()
        textFieldSerialNumber.clear()
        textFieldSerialNumber.isDisable = false
        comboBoxTestItem.isDisable = false
        buttonProtocolCancel.text = "Очистить"
        buttonProtocolNext.text = "Создать"
        tabPane.selectionModel.select(tabProtocol)
        mainModel.currentProtocol = Protocol()
        buttonProtocolCancel.isDisable = false
        buttonProtocolNext.isDisable = true
        tabResults.isDisable = true
        currentState = idleState
        initData()
        checkBoxResonance.isSelected = true
    }

    override fun toWaitState() {
        comboBoxTestItem.isDisable = true
        buttonProtocolCancel.text = "Новый"
        textFieldSerialNumber.isDisable = true
        buttonProtocolNext.text = "Далее"
        menuBarProtocolSaveAs.isDisable = false
        currentState = waitState
        buttonProtocolCancel.isDisable = false
        buttonProtocolNext.isDisable = false
    }

    override fun toResultState() {
        tabResults.isDisable = false
        tabPane.selectionModel.select(tabResults)
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
        tableViewResults.items = resultData
        columnTableDimension.setCellValueFactory { cellData -> cellData.value.dimensionProperty() }
        columnTableValue.setCellValueFactory { cellData -> cellData.value.valueProperty() }
        initializeComboBoxResult()
        handleTestItemGenerate()
    }

    @FXML
    fun handleResonance() {
        if (checkBoxResonance.isSelected) {
            handleTestItemGenerate()
            buttonTestItemGenerate.isDisable = false
            scrollPaneTimeTorque.isDisable = false
            buttonAdd.isDisable = false
            buttonRemove.isDisable = false
        } else {
            buttonTestItemGenerate.isDisable = true
            scrollPaneTimeTorque.isDisable = true
            buttonAdd.isDisable = true
            buttonRemove.isDisable = true
        }
    }

    @FXML
    fun handleTestItemGenerate() {
        applyTimesAndTorques()
        handleComboBoxTestItem()
        buttonProtocolNext.isDisable = false
    }

    private fun applyTimesAndTorques() {
        val times: ArrayList<Double> = ArrayList()
        val torques: ArrayList<Double> = ArrayList()
        stackPairs.forEach {
            if (!it.first.text.isNullOrEmpty() &&
                    !it.second.text.isNullOrEmpty() &&
                    it.first.text.toDoubleOrNull() != null &&
                    it.second.text.toDoubleOrNull() != null) {
                times.add(it.first.text.toDouble())
                torques.add(it.second.text.toDouble())
            } else {
                Toast.makeText("Проверьте правильность введенных напряжений и времени проверки").show(Toast.ToastType.WARNING)
            }
        }
        if (currentTestItem != null) {
            currentTestItem!!.times = times
            currentTestItem!!.voltageResonance = torques
            TestItemRepository.updateTestItem(currentTestItem)
        }
    }

    @FXML
    fun handleTestItemCancel() {

    }

    @FXML
    fun handleAddPair() {
        addPair()
        buttonProtocolNext.isDisable = true
    }

    private fun addPair() {
        lastPair = newTextFieldsForChart()
        stackPairs.push(lastPair)
        vBoxTime.children.add(lastPair.first)
        vBoxTorque.children.add(lastPair.second)
        anchorPaneTimeTorque.prefHeight += HEIGHT_VBOX
    }

    @FXML
    fun handleRemovePair() {
        removePair()
        buttonProtocolNext.isDisable = true
    }

    private fun removePair() {
        lastPair = stackPairs.pop()
        vBoxTime.children.remove(lastPair.first)
        vBoxTorque.children.remove(lastPair.second)
        anchorPaneTimeTorque.prefHeight -= HEIGHT_VBOX
    }

    private fun newTextFieldsForChart(): Pair<TextField, TextField> {
        val time = TextField()
        time.isEditable = true
        time.prefWidth = 72.0
        time.maxWidth = 72.0
        time.setOnAction {
            handleTestItemGenerate()
        }

        val torque = TextField()
        torque.isEditable = true
        torque.prefWidth = 72.0
        torque.maxWidth = 72.0
        torque.setOnAction {
            handleTestItemGenerate()
        }
        return time to torque
    }

    @FXML
    fun handleComboBoxTestItem() {
        if (TestItemRepository.getAllTestItems().isNotEmpty()) {
            removeData()
            loadDiagram.data.clear()
            currentTestItem = TestItemRepository.getTestItem(comboBoxTestItem.selectionModel.selectedItem.toString())
            if (currentTestItem != null) {
                fillPairsOfLoadDiagram()
                val seriesForLoadDiagram = createLoadDiagram()
                loadDiagram.data.clear()
                loadDiagram.data.addAll(seriesForLoadDiagram)
            } else {
//                Logger.getAnonymousLogger().warning("currentTestItem = null$currentTestItem")
            }
        }
        textFieldViu.text = comboBoxTestItem.selectionModel.selectedItem.viu.toString()
        textFieldViuDC.text = comboBoxTestItem.selectionModel.selectedItem.viuDC.toString()
    }

    private fun fillPairsOfLoadDiagram() {
        for (i in 0 until currentTestItem!!.times.size) {
            handleAddPair()
            lastPair.first.text = currentTestItem!!.times[i].toString()
            lastPair.second.text = currentTestItem!!.voltageResonance[i].toString()
        }
    }

    private fun createLoadDiagram(): XYChart.Series<Number, Number> {
        val seriesTimesAndTorques = XYChart.Series<Number, Number>()

        var desperateDot = 0.0

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, currentTestItem!!.voltageResonance[0]))

        for (i in 0 until currentTestItem!!.times.size) {
            seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentTestItem!!.times[i], currentTestItem!!.voltageResonance[i]))
            if (i != currentTestItem!!.times.size - 1) {
                seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentTestItem!!.times[i], currentTestItem!!.voltageResonance[i + 1]))
            }
            desperateDot += currentTestItem!!.times[i]
        }

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, 0))

        return seriesTimesAndTorques
    }

    private fun removeData() {
        for (i in 0 until stackPairs.size) {
            removePair()
        }
    }

    private fun initializeComboBoxResult() {
        if (mainModel.currentProtocol != null) {
            val currentProtocol = mainModel.currentProtocol
            resultData.clear()
            resultData.add(ResultModel("Напряжение A, Н•м", currentProtocol.e1VoltageA))
            resultData.add(ResultModel("Напряжение B, Н•м", currentProtocol.e1VoltageB))
            resultData.add(ResultModel("Напряжение C, Н•м", currentProtocol.e1VoltageC))
            resultData.add(ResultModel("Ток A, Н•м", currentProtocol.e1CurrentA))
            resultData.add(ResultModel("Ток B, Н•м", currentProtocol.e1CurrentB))
            resultData.add(ResultModel("Ток C, Н•м", currentProtocol.e1CurrentC))
            resultData.add(ResultModel("Момент, Н•м", currentProtocol.e1Torque))
            resultData.add(ResultModel("Частота вращения, об/мин", currentProtocol.e1Rotation))
            resultData.add(ResultModel("Частота сети, Гц", currentProtocol.e1Frequency))
            resultData.add(ResultModel("Полная мощность потребляемая от ИП , кВт", currentProtocol.e1Power))
            resultData.add(ResultModel("Активная мощность, кВт", currentProtocol.e1PowerActive))
            resultData.add(ResultModel("Коэффициент полезного действия, %", currentProtocol.e1Effiency))
            resultData.add(ResultModel("Температура обмотки статора, °C", currentProtocol.e1Temperature))
        }
    }

    @FXML
    private fun handleCreateNewProtocol() {
        currentState.toIdleState()
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
            mainModel!!.currentProtocol = protocol
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
        DBFileChooser!!.title = "Выберите файл базы данных для импорта"
        val file = DBFileChooser!!.showOpenDialog(PRIMARY_STAGE)
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
        DBFileChooser!!.title = "Сохраните базу данных в файл"
        var file: File? = DBFileChooser!!.showSaveDialog(PRIMARY_STAGE)
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
        val controller: DeviceStateWindowController = loader.getController<DeviceStateWindowController>()

        val dialogStage = Stage()
        dialogStage.title = "Состояние устройств"
        dialogStage.initModality(Modality.WINDOW_MODAL)
        dialogStage.initOwner(PRIMARY_STAGE)
        val scene = Scene(page)
        dialogStage.isResizable = false
        dialogStage.scene = scene

        dialogStage.setOnCloseRequest { event ->
            controller.flag = false

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

        dialogStage.setOnCloseRequest { event ->
            communicationModel!!.finalizeAllDevices()
            communicationModel!!.deleteObservers()
        }
        dialogStage.showAndWait()
    }

    @FXML
    private fun handleSelectTestItem() {
        if (mainModel.isNeedRefresh) {
            mainModel.isNeedRefresh = false
        }
    }

    @FXML
    private fun handleButtonProtocolCancel() {
        toIdleState()
    }

    @FXML
    private fun handleButtonProtocolNext() {
        handleTestItemGenerate()
        if (!comboBoxTestItem.selectionModel.isEmpty) {
            mainModel.createNewProtocol(textFieldSerialNumber.text, TestItemRepository.getTestItem(comboBoxTestItem.selectionModel.selectedItem.type))
            isXXSelected = checkBoxExperiment2.isSelected
            isElevatedRotation = checkBoxExperiment1.isSelected
            startExperiment()
            toResultState()
        } else {
            Toast.makeText("Выберите объект испытания").show(Toast.ToastType.INFORMATION)
        }
        isXXSelected = checkBoxExperiment2.isSelected
        isElevatedRotation = checkBoxExperiment1.isSelected

    }

    fun handleMenuBarProtocolNew() {
        toIdleState()
    }

    fun showSize() {
        val widthScreen = Toolkit.getDefaultToolkit().screenSize.width - 400
        val width = widthScreen / 6 - 13
//        tabProtocol.style = "-fx-padding: 40 $width 40 $width"
//        tabExperiments.style = "-fx-padding: 40 $width 40 $width"
//        tabResults.style = "-fx-padding: 40 $width 40 $width"
    }


    private fun startExperiment(): Boolean {
        return startExperiment("layouts/experiment1View.fxml")
    }

    private fun startExperiment(layout: String): Boolean {
        val loader = FXMLLoader()
        loader.location = Main::class.java.getResource(layout)
        var controller: ExperimentController? = null
        try {
            val page = loader.load<Parent>()

            val dialogStage = Stage()
            dialogStage.title = "Опыт"
            dialogStage.initModality(Modality.WINDOW_MODAL)
            dialogStage.initOwner(PRIMARY_STAGE)
            val scene = Scene(page, Constants.Display.WIDTH.toDouble(), Constants.Display.HEIGHT.toDouble())
            dialogStage.scene = scene
            controller = loader.getController<ExperimentController>()
            controller!!.setDialogStage(dialogStage)

//            dialogStage.initStyle(StageStyle.TRANSPARENT)
            dialogStage.showAndWait()


        } catch (e: IOException) {
            e.printStackTrace()
        }

        communicationModel!!.finalizeAllDevices()
        communicationModel!!.deleteObservers()

        return controller != null && controller.isCanceled
    }

    fun handleEventLog() {

    }

    fun setMain(exitappable: Exitappable) {
        this.exitappable = exitappable
    }

    @FXML
    fun handleSaveCurrentProtocol() {
        ProtocolRepository.insertProtocol(mainModel!!.currentProtocol)
        Toast.makeText("Результаты проведенных испытаний сохранены").show(Toast.ToastType.INFORMATION)
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
    fun handleAbout() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Версия ПО"
        alert.headerText = "Версия: 1.0.2"
        alert.contentText = "Дата: 07.05.2019"
        alert.showAndWait()
    }

}


