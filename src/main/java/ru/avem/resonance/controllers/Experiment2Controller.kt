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
import ru.avem.resonance.Constants.Time.MILLS_IN_SEC
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
import ru.avem.resonance.model.Experiment2Model
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Log
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment2Controller : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment2: TableView<Experiment2Model>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnIA: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnIC: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnILeak: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnFrequency: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var tableColumnResultExperiment2: TableColumn<Experiment2Model, String>
    @FXML
    lateinit var textAreaExperiment2Log: TextArea
    @FXML
    lateinit var lineChartExperiment2: LineChart<Number, Number>
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
    private var experiment2Model: Experiment2Model? = null
    private val experiment2Data = FXCollections.observableArrayList<Experiment2Model>()

    private var dialogStage: Stage? = null
    private var isCanceled: Boolean = false

    @Volatile
    private var isNeedToRefresh: Boolean = false
    @Volatile
    private var isExperimentRunning: Boolean = false
    @Volatile
    private var isExperimentEnded = true
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
    private var latrStatus: String = ""

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
    private var currentDot = XYChart.Series<Number, Number>()

    private var coef: Double = 102.0


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

        experiment2Model = mainModel.experiment2Model
        experiment2Data.add(experiment2Model)
        tableViewExperiment2.items = experiment2Data
        tableViewExperiment2.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageViu
        timeList = currentProtocol.timesViu

        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIA.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnIC.setCellValueFactory { cellData -> cellData.value.currentAProperty() }
        tableColumnILeak.setCellValueFactory { cellData -> cellData.value.currentLeakProperty() }
        tableColumnFrequency.setCellValueFactory { cellData -> cellData.value.frequencyProperty() }
        tableColumnResultExperiment2.setCellValueFactory { cellData -> cellData.value.resultProperty() }

        val createLoadDiagram = createLoadDiagram()
        lineChartExperiment2.data.addAll(createLoadDiagram)
    }

    private fun createLoadDiagram(): XYChart.Series<Number, Number> {
        val seriesTimesAndTorques = XYChart.Series<Number, Number>()

        var desperateDot = 0.0

        seriesTimesAndTorques.data.add(XYChart.Data(desperateDot, currentProtocol.voltageViu[0]))

        for (i in 0 until currentProtocol.timesViu.size) {
            seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViu[i], currentProtocol.voltageViu[i]))
            if (i != currentProtocol.timesViu.size - 1) {
                seriesTimesAndTorques.data.add(XYChart.Data(desperateDot + currentProtocol.timesViu[i], currentProtocol.voltageViu[i + 1]))
            }
            desperateDot += currentProtocol.timesViu[i]
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
        experiment2Model!!.clearProperties()
        isSchemeReady = false
        cause = ""

        Thread {
            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания")
                communicationModel.initExperimentDevices()
                sleep(4000)
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
                resetOmik()
            }

            if (isExperimentRunning) {
                communicationModel.onPRO3()
//                appendOneMessageToLog("Поднимаем напряжение на объекте испытания до $firstVoltageLatr")
//                communicationModel.startUpLATR(firstVoltageLatr, true)
//                waitingLatrCoarse(firstVoltageLatr)
//                sleep(2000)
//                fineLatrCoarse(firstVoltageLatr)
            }

            if (isExperimentRunning) {
                communicationModel.startUpLATR((voltageList[0] / coef).toFloat(), true)
            }

            var timeSum = 0.0

            if (isExperimentRunning && isDevicesResponding) {
                for (i in voltageList.indices) {
                    var timePassed = 0.0
                    if (isExperimentRunning && isDevicesResponding) {
                        appendOneMessageToLog("Началась регулировка")
                        communicationModel.startUpLATR((voltageList[i] / coef).toFloat(), false)
                        sleep(2000)
                        waitingLatrCoarse((voltageList[i] / coef).toFloat())
                        sleep(2000)
                        fineLatrCoarse((voltageList[i] / coef).toFloat())
                        appendOneMessageToLog("Регулировка окончена")
                    }
                    val time = timeList[i] * MILLS_IN_SEC

                    while (isExperimentRunning && timePassed < time) {
                        sleep(100)
                        timePassed += 100.0
                        drawDot(timeSum, timeSum + currentProtocol.timesViu[i], currentProtocol.voltageViu[i], timePassed / time)
                    }
                    timeSum += currentProtocol.timesViu[i]
                }
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
            communicationModel.startUpLATR(1f, true)
            sleep(5000)
            communicationModel.stopLATR()
            resetOmik()
            communicationModel.stopObject()
            isNeedToRefresh = false
            communicationModel.offAllKms() //разбираем все возможные схемы
            sleep(50)
            communicationModel.finalizeAllDevices() //прекращаем опрашивать устройства

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment2Model!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment2Model!!.result = "Неуспешно"
            } else {
                experiment2Model!!.result = "Успешно"
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

    private fun drawDot(x1: Double, x2: Double, y: Double, percent: Double) {
        Platform.runLater {
            lineChartExperiment2.data.removeAll(currentDot)
            currentDot = createCurrentDot(x1, x2, y, percent)
            lineChartExperiment2.data.addAll(currentDot)
        }
    }

    private fun createCurrentDot(x1: Double, x2: Double, y: Double, percent: Double): XYChart.Series<Number, Number> {
        val currentDot = XYChart.Series<Number, Number>()
        currentDot.data.add(XYChart.Data(x1 + (x2 - x1) * percent, y))
        return currentDot
    }

    private fun waitingLatrCoarse(voltage: Float) {
        appendOneMessageToLog("Грубая регулировка")
        var isLatrCoarseReady = false
        while (isExperimentRunning && isDevicesResponding && !isLatrCoarseReady) {
            if (measuringU > voltage * 0.80 && measuringU < voltage * 1.05) {
                isLatrCoarseReady = true
            }
            sleep(10)
        }
        communicationModel.stopLATR()
        appendOneMessageToLog("Грубая регулировка окончена")
    }

    private fun fineLatrCoarse(voltage: Float) {
        while ((measuringU <= voltage * 0.9 || measuringU >= voltage * 1.1) && isExperimentRunning) {
            appendOneMessageToLog("Точная регулировка")
            sleep(1500)
            if (measuringU <= voltage * 0.9) {
                appendMessageToLog("Accurate UP")
                communicationModel.startUpLATR(380f, false)
                sleep(1500)
                communicationModel.stopLATR()
            } else if (measuringU >= voltage * 1.1) {
                appendMessageToLog("Accurate DOWN")
                communicationModel.startUpLATR(1f, false)
                sleep(1500)
                communicationModel.stopLATR()
            }
        }
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
        }
        if (statusEndsVFD == OMIK_DOWN_END) {
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment2Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message))
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
                    experiment2Model!!.currentA = IA
                }
                ParmaT400Model.IB_PARAM -> {
                    measuringIB = value as Double
                    val IB = String.format("%.2f", measuringIB)
                    experiment2Model!!.currentB = IB
                }
                ParmaT400Model.IC_PARAM -> {
                    measuringIC = value as Double
                    val IC = String.format("%.2f", measuringIC)
                    experiment2Model!!.currentC = IC
                }
                ParmaT400Model.F_PARAM -> {
                    measuringF = value as Double
                    val FParma = String.format("%.2f", measuringF)
                    experiment2Model!!.frequency = FParma
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
                    experiment2Model!!.currentLeak = IAvem
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
//                    experiment2Model!!.voltage = UKiloAvem
//                }
//            }

            LATR_ID -> when (param) {
                LatrModel.RESPONDING_PARAM -> {
                    isLatrResponding = value as Boolean
                    Platform.runLater { deviceStateCircleLatr.fill = if (value) Color.LIME else Color.RED }
                }
                LatrModel.STATUS_PARAM -> {
                    latrStatus = Utils.toHexString(value as Byte)
                    checkLatrError()
                    checkLatrStatus()
                }
                LatrModel.U_PARAM -> {
                    measuringU = (value as Float)
                    val uLatr = String.format("%.2f", measuringU * coef)
                    experiment2Model!!.voltage = uLatr
                }
            }
        }
    }

    private fun checkLatrStatus() {
        when (latrStatus) {
            LATR_STARTED -> {
                Log.i("", "Выход ЛАТРа на заданное напряжение")
            }
            LATR_WAITING -> {
                Log.i("", "Выдерживаем заданное напряжение на ЛАТРе")
            }
            LATR_CONFIG -> {
                Log.i("", "Режим кофигурации ЛАТР")
            }
            LATR_STOP_RESET -> {
                Log.i("", "Стоп/Ресет ЛАТР")
            }
        }
    }

    private fun checkLatrError() {
        when (latrStatus) {
            LATR_UP_END -> {
                Log.i("", "Сработал верхний концевик ЛАТРа.")
            }
            LATR_DOWN_END -> {
                Log.i("", "Сработал нижний концевик ЛАТРа.")
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
                Log.i("", "Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
                Log.i("", "Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
                Log.i("", "Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
                Log.i("", "FORWARD")
            }
            VFD_REVERSE -> {
                Log.i("", "REVERSE")
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