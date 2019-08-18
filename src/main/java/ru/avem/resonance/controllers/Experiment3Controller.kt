package ru.avem.resonance.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import ru.avem.resonance.Constants.Ends.*
import ru.avem.resonance.Constants.Vfd.VFD_FORWARD
import ru.avem.resonance.Constants.Vfd.VFD_REVERSE
import ru.avem.resonance.Main
import ru.avem.resonance.communication.CommunicationModel
import ru.avem.resonance.communication.devices.DeviceController.*
import ru.avem.resonance.communication.devices.avem_voltmeter.AvemVoltmeterModel
import ru.avem.resonance.communication.devices.deltaC2000.DeltaCP2000Model
import ru.avem.resonance.communication.devices.latr.LatrModel
import ru.avem.resonance.communication.devices.parmaT400.ParmaT400Model
import ru.avem.resonance.communication.devices.pr200.OwenPRModel
import ru.avem.resonance.communication.modbus.utils.Utils
import ru.avem.resonance.model.Experiment3Model
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment3Controller : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment3: TableView<Experiment3Model>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIA: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnIC: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnILeak: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnFrequency: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var tableColumnResultExperiment3: TableColumn<Experiment3Model, String>
    @FXML
    lateinit var textAreaExperiment3Log: TextArea
    @FXML
    lateinit var lineChartExperiment3: LineChart<Number, Number>
    @FXML
    lateinit var buttonStartStop: Button
    @FXML
    lateinit var buttonNext: Button
    @FXML
    lateinit var buttonCancelAll: Button
    @FXML
    lateinit var root: AnchorPane

    private val mainModel = MainModel.instance
    private val currentProtocol = mainModel.currentProtocol
    private val communicationModel = CommunicationModel.getInstance()
    private var experiment3Model: Experiment3Model? = null
    private val experiment3Data = FXCollections.observableArrayList<Experiment3Model>()

    private var dialogStage: Stage? = null
    private var isCanceled: Boolean = false

    @Volatile
    private var isNeedToRefresh: Boolean = false
    @Volatile
    private var isExperimentRunning: Boolean = false
    @Volatile
    private var isExperimentEnded = true
    @Volatile
    private var isDeviceOn = true
    @Volatile
    private var isNeedCheckLatrStatus: Boolean = false

    @Volatile
    private var isOwenPRResponding: Boolean = false
    @Volatile
    private var isDeltaResponding: Boolean = false
    @Volatile
    private var isDeltaReady50: Boolean = false
    @Volatile
    private var isDeltaReady0: Boolean = false
    @Volatile
    private var isParmaResponding: Boolean = false
    @Volatile
    private var isLatrResponding: Boolean = false
    @Volatile
    private var isAvemResponding: Boolean = false
    @Volatile
    private var isKiloAvemResponding: Boolean = false
    @Volatile
    private var latrStatusString: String = ""

    private val sdf = SimpleDateFormat("HH:mm:ss-SSS")
    private var logBuffer: String? = null
    @Volatile
    private var cause: String? = null
    @Volatile
    private var measuringU: Float = 0.0f
    @Volatile
    private var measuringIAvem: Float = 0.0f
    @Volatile
    private var measuringUKiloAvem: Float = 0.0f
    @Volatile
    private var measuringIA: Double = 0.0
    @Volatile
    private var measuringIB: Double = 0.0
    @Volatile
    private var measuringIC: Double = 0.0
    @Volatile
    private var isSchemeReady: Boolean = false
    @Volatile
    private var measuringF: Double = 0.0

    @Volatile
    private var statusEndsVFD: Short = 0
    @Volatile
    private var statusVFD: Short = 0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()

    private var coef: Double = 107.0
    private val firstVoltageLatr = (2000.0 / coef).toFloat()


    private val isThereAreAccidents: Boolean
        get() {
            if (isCanceled) {
                isExperimentRunning = false
                isExperimentEnded = true
            }
            return !isCanceled
        }

    private val isDevicesResponding: Boolean
        get() = isOwenPRResponding && isAvemResponding && isDeltaResponding && isLatrResponding
                && isParmaResponding

    private val points = ArrayList<Point>()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }

        experiment3Model = mainModel.experiment3Model
        experiment3Data.add(experiment3Model)
        tableViewExperiment3.items = experiment3Data
        tableViewExperiment3.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageViuDC
        timeList = currentProtocol.timesViuDC

        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIA.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIC.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnILeak.setCellValueFactory { cellData -> cellData.value.currentLeakProperty() }
        tableColumnFrequency.setCellValueFactory { cellData -> cellData.value.frequencyProperty() }
        tableColumnResultExperiment3.setCellValueFactory { cellData -> cellData.value.resultProperty() }

        val createLoadDiagram = createLoadDiagram()
        lineChartExperiment3.data.addAll(createLoadDiagram)
    }

    private fun createLoadDiagram(): XYChart.Series<Number, Number> {
        val seriesTimesAndTorques = XYChart.Series<Number, Number>()

        var desperateDot = 0.0

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, currentProtocol.voltageViuDC[0]))

        for (i in 0 until currentProtocol.timesViuDC.size) {
            seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViuDC[i], currentProtocol.voltageViuDC[i]))
            if (i != currentProtocol.timesViuDC.size - 1) {
                seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViuDC[i], currentProtocol.voltageViuDC[i + 1]))
            }
            desperateDot += currentProtocol.timesViuDC[i]
        }

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, 0))

        return seriesTimesAndTorques
    }

    private fun fillProtocolExperimentFields() {
        val currentProtocol = mainModel.currentProtocol
        // TODO
    }

    @FXML
    private fun handleNextExperiment() {
        fillProtocolExperimentFields()
        dialogStage!!.close()
    }

    @FXML
    private fun handleRunStopExperiment() {
        if (isExperimentEnded) {
            startExperiment()
        } else {
            stopExperiment()
        }
    }

    @FXML
    private fun handleExperimentCancel() {
        isExperimentRunning = false
        dialogStage!!.close()
    }

    private fun stopExperiment() {
        isNeedToRefresh = false
        buttonStartStop.isDisable = true
        setCause("Отменено оператором")
        isExperimentRunning = false
    }


    private fun startExperiment() {
        points.clear()
        isNeedToRefresh = true
        isNeedCheckLatrStatus = false
        isExperimentRunning = true
        isExperimentEnded = false
        buttonStartStop.text = "Остановить"
        buttonNext.isDisable = true
        buttonCancelAll.isDisable = true
        experiment3Model!!.clearProperties()
        isDeviceOn = false
        isSchemeReady = false
        cause = ""

        Thread {
            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания")
                communicationModel.initExperimentDevices()
            }

            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
                resetOmik()
            }

            if (isExperimentRunning && isDevicesResponding) {
                appendOneMessageToLog("Поднимаем напряжение на объекте испытания")
                communicationModel.startUpLATR(firstVoltageLatr, true)
                waitingLatrCoarse()
            }

            if (isExperimentRunning && isDevicesResponding) {
            }


//            while ((measuringU <= 100.0f * 0.97 || measuringU >= 100.0f * 1.03) && isExperimentRunning) {
//                appendOneMessageToLog("Точная регулировка...")
//
//                if (measuringU <= 100.0f * 0.97) {
//                    appendMessageToLog("Accurate UP")
//                    communicationModel.startUpLATR(380f, false)
//                    Thread.sleep(1000)
//                    communicationModel.stopLATR()
//                } else if (measuringU >= 100.0f * 1.03) {
//                    appendMessageToLog("Accurate DOWN")
//                    communicationModel.startUpLATR(1f, false)
//                    Thread.sleep(1000)
//                    communicationModel.stopLATR()
//                }
//            }

//            isNeedCheckLatrStatus = true
            isDeviceOn = true
//            communicationModel.stopLATR()

//            while (isExperimentStart && !isDevicesResponding) {
//                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "))
//                sleep(100)
//            }
//
//            if (isExperimentStart && isOwenPRResponding) {
//                appendOneMessageToLog("Инициализация кнопочного поста...")
//            }
//
//            while (isExperimentStart /*&& !is5K1_2_On*/) {
//                appendOneMessageToLog("Включите кнопочный пост")
//            }
//
//            if (isExperimentStart && !isThereAreAccidents) {
//                appendOneMessageToLog(getAccidentsString("Аварии"))
//                isExperimentStart = false
//            }
//
//            isSchemeReady = true
//
//            if (isExperimentStart && isDevicesResponding) {
//                appendOneMessageToLog("Инициализация испытания")
//            }
//
//            isNeedToRefresh = false
//
//            isExperimentStart = false
//            isExperimentEnd = true
//            sleep(500)
//            communicationModel.stopObject()
//            while (isExperimentStart && !isDeltaReady0) {
//                sleep(100)
//                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится")
//            }
            communicationModel.stopObject()
            isNeedToRefresh = false
            communicationModel.offAllKms() //разбираем все возможные схемы
            sleep(50)
            communicationModel.finalizeAllDevices() //прекращаем опрашивать устройства

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment3Model!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment3Model!!.result = "Неуспешно"
            } else {
                experiment3Model!!.result = "Успешно"
                appendMessageToLog("Испытание завершено успешно")
            }
            appendMessageToLog("\n------------------------------------------------\n")

            Platform.runLater()
            {
                buttonStartStop.text = "Запустить"
                isExperimentEnded = true
                isExperimentRunning = false
                buttonStartStop.isDisable = false
                buttonNext.isDisable = false
                buttonCancelAll.isDisable = false
            }

        }.start()
    }

    private fun waitingLatrCoarse() {
        appendOneMessageToLog("Грубая регулировка")
        while (isExperimentRunning && isDevicesResponding) {
            if (measuringU > 100.0f * 0.90 && measuringU < 100.0f * 1.03) {
                break
            }
            Thread.sleep(10)
        }
        appendOneMessageToLog("Грубая регулировка окончена")
        communicationModel.stopLATR()
    }

    private fun resetOmik() {
        if (statusEndsVFD != OMIK_DOWN_END && isExperimentRunning && isDevicesResponding) {
            appendOneMessageToLog("Возвращаем магнитопровод в исходное состояние")
            if (statusVFD != VFD_REVERSE && isExperimentRunning && isDevicesResponding) {
                communicationModel.changeRotation()
            }
            communicationModel.startObject()
            var waitingTime = 30
            while (isExperimentRunning && isDevicesResponding && (waitingTime-- > 0)) {
                sleep(100)
            }
            while (statusEndsVFD != OMIK_DOWN_END && isExperimentRunning && isDevicesResponding) {
                sleep(10)
                if (statusEndsVFD == OMIK_UP_END && isExperimentRunning && isDevicesResponding) {
                    setCause("Омик в верхнем положенении, двигаясь вниз")
                }
            }
            communicationModel.stopObject()
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment3Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message))
        }
    }

    private fun appendOneMessageToLog(message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(message)
        }
    }

    private fun getAccidentsString(mainText: String): String {
        return String.format("%s: %s",
                mainText,
//                if (is1DoorOn) "" else "открыта дверь, ",
//                if (is2OIOn) "" else "открыты концевики ОИ, ",
//                if (is6KM1_2_On) "" else "не замкнулся КМ1, ",
//                if (is7KM2_2_On) "" else "не замкнулся КМ2, ",
                if (isCanceled) "" else "нажата кнопка отмены, ")
    }

    private fun getNotRespondingDevicesString(mainText: String): String {
        return String.format("%s %s%s%s%s%s",
                mainText,
                if (isOwenPRResponding) "" else "Овен ПР ",
                if (isParmaResponding) "" else "Парма ",
                if (isDeltaResponding) "" else "Дельта ",
                if (isLatrResponding) "" else "Латр ",
                if (isAvemResponding) "" else "АВЭМ ")
    }

    private fun setCause(cause: String) {
        this.cause = cause
        if (cause.isNotEmpty()) {
            isExperimentRunning = false
        }
    }

    override fun update(o: Observable, values: Any) {
        val modelId = (values as Array<Any>)[0] as Int
        val param = values[1] as Int
        val value = values[2]

        when (modelId) {
            PR200_ID -> when (param) {
                OwenPRModel.RESPONDING_PARAM -> {
                    isOwenPRResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePR200.fill = if (value) Color.LIME else Color.RED }
                }
            }

            PARMA400_ID -> when (param) {
                ParmaT400Model.RESPONDING_PARAM -> {
                    isParmaResponding = value as Boolean
                    Platform.runLater { deviceStateCircleParma.fill = if (value) Color.LIME else Color.RED }
                }
                ParmaT400Model.IA_PARAM -> {
                    measuringIA = value as Double
                    val IA = String.format("%.2f", measuringIA)
                    experiment3Model!!.currentA = IA
                }
                ParmaT400Model.IB_PARAM -> {
                    measuringIB = value as Double
                    val IB = String.format("%.2f", measuringIB)
                    experiment3Model!!.currentB = IB
                }
                ParmaT400Model.IC_PARAM -> {
                    measuringIC = value as Double
                    val IC = String.format("%.2f", measuringIC)
                    experiment3Model!!.currentC = IC
                }
                ParmaT400Model.F_PARAM -> {
                    measuringF = value as Double
                    val FParma = String.format("%.2f", measuringF)
                    experiment3Model!!.currentC = FParma
                }
            }

            DELTACP2000_ID -> when (param) {
                DeltaCP2000Model.RESPONDING_PARAM -> {
                    isDeltaResponding = value as Boolean
                    Platform.runLater { deviceStateCircleDelta.fill = if (value) Color.LIME else Color.RED }
                }
                DeltaCP2000Model.ENDS_STATUS_PARAM -> {
                    statusEndsVFD = value as Short
                    checkEndsVFDStatus()
                }
                DeltaCP2000Model.STATUS_VFD -> {
                    statusVFD = value as Short
                    checkVFDStatus()
//                    when {
//                        statusEndsVFD == OMIK_DOWN_END -> communicationModel.stopObject()
//                        statusVFD == OMIK_UP_END -> communicationModel.stopObject()
//                        statusVFD == OMIK_BOTH_END -> communicationModel.stopObject()
//                    }
                }
                DeltaCP2000Model.CURRENT_FREQUENCY_PARAM -> setCurrentFrequencyObject(value as Short)
            }

            AVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_PARAM -> {
                    measuringIAvem = value as Float
                    val IAvem = String.format("%.2f", measuringIAvem)
                    experiment3Model!!.currentLeak = IAvem
                }
            }

//            KILOAVEM_ID -> when (param) {
//                AvemVoltmeterModel.RESPONDING_PARAM -> {
//                    isKiloAvemResponding = value as Boolean
//                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
//                }
//                AvemVoltmeterModel.U_PARAM -> {
//                    measuringUKiloAvem = value as Float
//                    val UKiloAvem = String.format("%.2f", measuringUKiloAvem)
//                    experiment3Model!!.voltage = UKiloAvem
//                }
//            }

            LATR_ID -> when (param) {
                LatrModel.RESPONDING_PARAM -> {
                    isLatrResponding = value as Boolean
                    Platform.runLater { deviceStateCircleLatr.fill = if (value) Color.LIME else Color.RED }
                }
                LatrModel.STATUS_PARAM -> {
                    latrStatusString = Utils.toHexString(value as Byte)
                    checkLatrError()
                    checkLatrStatus()
                }
                LatrModel.U_PARAM -> {
                    measuringU = (value as Float)
                    val uLatr = String.format("%.2f", measuringU * coef)
//                    experiment3Model!!.voltage = uLatr
                }
            }
        }
    }

    private fun checkLatrStatus() {
        when (latrStatusString) {
            LATR_STARTED -> {
                appendOneMessageToLog("Выход ЛАТРа на заданное напряжение")
            }
            LATR_WAITING -> {
                appendOneMessageToLog("Выдерживаем заданное напряжение на ЛАТРе")
            }
            LATR_CONFIG -> {
                appendOneMessageToLog("Режим кофигурации ЛАТР")
            }
            LATR_STOP_RESET -> {
                appendOneMessageToLog("Стоп/Ресет ЛАТР")
            }
        }
    }

    private fun checkLatrError() {
        when (latrStatusString) {
            LATR_UP_END -> {
                appendOneMessageToLog("Сработал верхний концевик ЛАТРа.")
            }
            LATR_DOWN_END -> {
                appendOneMessageToLog("Сработал нижний концевик ЛАТРа.")
            }
            LATR_BOTH_END -> {
                setCause("Сработали оба концевика ЛАТРа.")
            }
            LATR_TIME_ENDED -> {
                setCause("Время регулирования ЛАТРа превысило заданное.")
            }
            LATR_ZASTRYAL -> {
                setCause("Застревание ЛАТРа.")
            }
        }
    }

    private fun checkEndsVFDStatus() {
        when (statusEndsVFD) {
            OMIK_UP_END -> {
                appendOneMessageToLog("Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
                appendOneMessageToLog("Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
                appendOneMessageToLog("Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
                appendOneMessageToLog("FORWARD")
            }
            VFD_REVERSE -> {
                appendOneMessageToLog("REVERSE")
            }
        }
    }

    private fun setCurrentFrequencyObject(value: Short) {
        isDeltaReady50 = value.toInt() == 5000
        isDeltaReady0 = value.toInt() == 0
    }

    override fun setDialogStage(dialogStage: Stage) {
        this.dialogStage = dialogStage
    }

    override fun isCanceled(): Boolean {
        return isCanceled
    }
}
