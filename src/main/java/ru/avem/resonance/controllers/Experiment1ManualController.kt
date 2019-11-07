package ru.avem.resonance.controllers

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.Stage
import ru.avem.resonance.Constants
import ru.avem.resonance.Constants.Ends.*
import ru.avem.resonance.Constants.Vfd.VFD_FORWARD
import ru.avem.resonance.Constants.Vfd.VFD_REVERSE
import ru.avem.resonance.Main
import ru.avem.resonance.communication.CommunicationModel
import ru.avem.resonance.communication.devices.DeviceController.*
import ru.avem.resonance.communication.devices.avem_voltmeter.AvemVoltmeterModel
import ru.avem.resonance.communication.devices.deltaC2000.DeltaCP2000Model
import ru.avem.resonance.communication.devices.latr.LatrModel
import ru.avem.resonance.communication.devices.pm130.PM130Model
import ru.avem.resonance.communication.devices.pr200.OwenPRModel
import ru.avem.resonance.communication.modbus.utils.Utils
import ru.avem.resonance.db.model.TestItem
import ru.avem.resonance.model.Experiment1ManualModel
import ru.avem.resonance.model.MainModel
import ru.avem.resonance.model.Point
import ru.avem.resonance.utils.Utils.sleep
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Experiment1ManualController : DeviceState(), ExperimentController {

    @FXML
    lateinit var tableViewExperiment1: TableView<Experiment1ManualModel>
    @FXML
    lateinit var tableColumnU: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnIB: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnUOI: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnIOI: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var tableColumnResultExperiment1: TableColumn<Experiment1ManualModel, String>
    @FXML
    lateinit var textAreaExperiment1Log: TextArea
    @FXML
    lateinit var lineChartExperiment1: LineChart<Number, Number>
    @FXML
    lateinit var textExperiment: Text
    @FXML
    lateinit var xAxis: NumberAxis
    @FXML
    lateinit var yAxis: NumberAxis
    @FXML
    lateinit var buttonCancelAll: Button
    @FXML
    lateinit var buttonSaveDot: Button
    @FXML
    lateinit var root: AnchorPane
    @FXML
    lateinit var gridPaneTimeTorque: GridPane
    @FXML
    lateinit var vBoxTime: VBox
    @FXML
    lateinit var vBoxVoltage: VBox
    @FXML
    lateinit var vBoxSpeed: VBox
    @FXML
    lateinit var anchorPaneTimeTorque: AnchorPane
    @FXML
    lateinit var scrollPaneTimeTorque: ScrollPane

    private lateinit var lastTriple: Triple<TextField, TextField, TextField>
    private val stackTriples: Stack<Triple<TextField, TextField, TextField>> = Stack()

    private val mainModel = MainModel.instance
    private val currentProtocol = mainModel.currentProtocol
    private val communicationModel = CommunicationModel.getInstance()
    private var experiment1ManualModel: Experiment1ManualModel? = null
    private val experiment1Data = FXCollections.observableArrayList<Experiment1ManualModel>()

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
    private var measuringULatr: Float = 0.0f
    @Volatile
    private var measuringIAvem: Float = 0.0f
    @Volatile
    private var measuringIA: Float = 0.0f
    @Volatile
    private var measuringIB: Float = 0.0f
    @Volatile
    private var measuringIC: Float = 0.0f
    @Volatile
    private var isControlRubilNeed: Boolean = false
    @Volatile
    private var isManualNeed: Boolean = false
    @Volatile
    private var isStartButtonOn: Boolean = false
    @Volatile
    private var measuringF: Float = 0.0f

    @Volatile
    private var ткзДоТрансформатора: Boolean = false
    @Volatile
    private var ткзОИ: Boolean = false
    @Volatile
    private var ткзПослеТрансформатора: Boolean = false
    @Volatile
    private var контрольДверей: Boolean = false
    @Volatile
    private var контрольПуска: Boolean = false
    @Volatile
    private var контрольРубильника: Boolean = false
    @Volatile
    private var ручнойРежимСПК: Boolean = false
    @Volatile
    private var переменное: Boolean = false
    @Volatile
    private var постоянное: Boolean = false
    @Volatile
    private var резонанс: Boolean = false
    @Volatile
    private var старт: Boolean = false
    @Volatile
    private var стоп: Boolean = false
    @Volatile
    private var стопИспытания: Boolean = false
    @Volatile
    private var подъемНапряжения: Boolean = false
    @Volatile
    private var уменьшениеНапряжения: Boolean = false
    @Volatile
    private var statusEndsVFD: Short = 0
    @Volatile
    private var statusVFD: Short = 0
    @Volatile
    private var modeOperating: Int = 0
    private var modeOperatingLast: Int = 0
    private var isModeOperatingSelected: Boolean = true

    private var coef: Double = 0.0

    private var voltageList: ArrayList<Double> = ArrayList()
    private var timeList: ArrayList<Double> = ArrayList()
    private var speedList: ArrayList<Double> = ArrayList()
    private var currentDot = XYChart.Series<Number, Number>()

    private val firstVoltage = 1500.0f

    private var currentTestItem: TestItem = mainModel.currentTestItem
    private var timePassed = 0.0
    private var time = 0.0
    private var timeSum = 0.0
    private var seriesTimesAndVoltage = XYChart.Series<Number, Number>()
    private var realTime = 0.0

    private var r: Random = Random()

    private var duty: Float = 0.0f
    private var pulse: Float = 0.0f

    private val isThereAreAccidents: Boolean
        get() {
            if (isCanceled) {
                isExperimentRunning = false
                isExperimentEnded = true
            }
            return !isCanceled
        }

    private val isDevicesResponding: Boolean
        //        get() = true
        get() = isOwenPRResponding && isAvemResponding && isDeltaResponding && isLatrResponding
                && isParmaResponding && isKiloAvemResponding

    private val points = ArrayList<Point>()

    @FXML
    private fun initialize() {
        if (Main.css == "white") {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css.css").toURI().toString()
        } else {
            root.stylesheets[0] = Main::class.java.getResource("styles/main_css_black.css").toURI().toString()
        }
        cause = ""
        experiment1ManualModel = mainModel.experiment1ManualModel
        experiment1Data.add(experiment1ManualModel)
        tableViewExperiment1.items = experiment1Data
        tableViewExperiment1.selectionModel = null
        communicationModel.addObserver(this)
        voltageList = currentProtocol.voltageResonance
        timeList = currentProtocol.timesResonance
        speedList = currentProtocol.speedResonance
        tableColumnU.setCellValueFactory { cellData -> cellData.value.voltageARNProperty() }
        tableColumnIB.setCellValueFactory { cellData -> cellData.value.currentBProperty() }
        tableColumnUOI.setCellValueFactory { cellData -> cellData.value.voltageProperty() }
        tableColumnIOI.setCellValueFactory { cellData -> cellData.value.currentOIProperty() }
        tableColumnResultExperiment1.setCellValueFactory { cellData -> cellData.value.resultProperty() }
        startExperiment()
    }

    private fun createLoadDiagram() {
//        Platform.runLater {
//            while (isExperimentRunning) {
//                sleep(1000)
//
//                Platform.runLater {
//                    measuringU += 0.01f
//                    realTime += 1
//                    seriesTimesAndVoltage.data.clear()
//                    seriesTimesAndVoltage.data.add(XYChart.Data(realTime, measuringU))
//                    lineChartExperiment1.data.add(seriesTimesAndVoltage)
//                }
//            }
//        }
    }

    private fun fillProtocolExperimentFields() {
        val currentProtocol = mainModel.currentProtocol
    }

    @FXML
    private fun handleNextExperiment() {
        fillProtocolExperimentFields()
        dialogStage!!.close()
    }

    @FXML
    private fun handleExperimentCancel() {
        isExperimentRunning = false
        dialogStage!!.close()
    }

    private fun startExperiment() {
        setCause("")

        ткзДоТрансформатора = false
        ткзОИ = false
        ткзПослеТрансформатора = false
        контрольДверей = false
        контрольПуска = false
        контрольРубильника = false
        ручнойРежимСПК = false
        переменное = false
        постоянное = false
        резонанс = false
        старт = false
        стоп = false
        стопИспытания = false
        подъемНапряжения = false
        уменьшениеНапряжения = false

        points.clear()
        isNeedToRefresh = true
        isNeedCheckLatrStatus = false
        isExperimentRunning = true
        isExperimentEnded = false
        buttonCancelAll.isDisable = true
        experiment1ManualModel!!.clearProperties()
        isControlRubilNeed = false
        lineChartExperiment1.data.clear()
        modeOperatingLast = 0
        modeOperating = 0
        isModeOperatingSelected = false
        isManualNeed = false

        Thread {

            if (isExperimentRunning) {
                appendOneMessageToLog("Визуально осматривайте трансфоматор на наличие потеков масла перед каждым опытом")
                communicationModel.initOwenPrController()
                communicationModel.resetResPR200()
                appendOneMessageToLog("Инициализация системы")
                communicationModel.initExperimentDevices()
            }

            while (!isDevicesResponding) {
                sleep(100)
            }

            if (isExperimentRunning) {
                communicationModel.setKiloAvemShowValue(Constants.Avem.VOLTAGE_RMS.ordinal)
                sleep(1000)
                communicationModel.resetLATR()
                communicationModel.приемКоманды_On()
            }

            while (modeOperating == 0 && isExperimentRunning) {
                appendOneMessageToLog("Выберите режим испытания")
            }

            if (isExperimentRunning) {
                communicationModel.разрешениеНаЗапуск_On()
                appendOneMessageToLog("ЖМИ ПУСК")
            }

            while (!контрольПуска && isExperimentRunning) {
                sleep(10)
            }

            if (isExperimentRunning) {
                modeOperatingLast = modeOperating
            }

            if (!контрольРубильника && isExperimentRunning) {
                sleep(1000)
                communicationModel.разрешениеНаЗапуск_Off()
                appendOneMessageToLog("Поднимите рубильник силового питания")
            }

            while (!контрольРубильника && isExperimentRunning) {
                sleep(10)
            }

            while (!контрольПуска && isExperimentRunning) {
                communicationModel.разрешениеНаЗапуск_On()
                sleep(100)
            }

            while (!контрольПуска && isExperimentRunning) {
                sleep(10)
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания")
                isModeOperatingSelected = true
                isControlRubilNeed = true
            }


            if (isExperimentRunning) {
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП")
                communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
                appendOneMessageToLog("Запускаем ЧП")
                resetOmik()
                if (постоянное) {
                    communicationModel.последовательнаяСхема_On()
                    communicationModel.авэм_On()
                } else {
                    communicationModel.параллельнаяСхема_On()
                }
                communicationModel.короткозамыкатель_On()
            }


            if (isExperimentRunning) {
                if (резонанс) {
                    communicationModel.приемКоманды_Off()
                    appendOneMessageToLog("Поднимаем напряжение на объекте испытания для поиска резонанса")
                    communicationModel.resetLATR()
                    putUpLatr(1200f, 200)
                    if (measuringULatr < measuringU * 0.5 && measuringULatr * 0.5 > measuringU) {
                        setCause("Коэфицент трансформации сильно отличается")
                    }
                    findResonance()
                }
            }

            timeSum = 0.0

            if (isExperimentRunning && isDevicesResponding) {
                isManualNeed = true
                communicationModel.приемКоманды_On()
                communicationModel.подсветкаТаймер_On()
                communicationModel.подсветкаНапряжения_On()
                createLoadDiagram()

                communicationModel.звук_On()
                sleep(3000)
                communicationModel.звук_Off()

                while (isExperimentRunning) {
                    sleep(100)
                }
            }

            isNeedToRefresh = false
            communicationModel.startUpLATRDown(0f, true)
            while (measuringU > 1300) {
                sleep(10)
            }
            communicationModel.stopLATR()
            resetOmik()
            var timeToSleep = 300
            while (isExperimentRunning && (timeToSleep-- > 0)) {
                sleep(10)
            }
            isControlRubilNeed = false

            communicationModel.offAllKms()

            if (контрольРубильника) {
                appendOneMessageToLog("Отключите рубильник")
                communicationModel.внимание_On()
                sleep(1000)
//                communicationModel.звук_On()
            }

            while (контрольРубильника) {
                sleep(100)
            }

            if (isExperimentRunning) {
                communicationModel.внимание_Off()
            }

            timeToSleep = 200
            while (isExperimentRunning && (timeToSleep-- > 0)) {
                sleep(10)
            }

            communicationModel.finalizeAllDevices()

            if (cause != "") {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause))
                experiment1ManualModel!!.result = "Неуспешно"
            } else if (!isDevicesResponding) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"))
                experiment1ManualModel!!.result = "Неуспешно"
            } else {
                experiment1ManualModel!!.result = "Успешно"
                appendMessageToLog("Испытание завершено успешно")
            }
            appendMessageToLog("\n------------------------------------------------\n")

            sleep(5000)

            isExperimentEnded = true
            isExperimentRunning = false
            Platform.runLater {
                buttonCancelAll.isDisable = false
            }
            startExperiment()
        }.start()
    }

    private fun findResonance() {
        appendOneMessageToLog("Идет поиск резонанса")
        if (statusVFD == VFD_REVERSE && isExperimentRunning) {
            communicationModel.changeRotation()
            sleep(2000)
        }
        communicationModel.startObject()
        sleep(3000)
        var highestU = measuringU
        var lowestI = measuringIC
        var step = 5
        while ((step-- > 0) && isExperimentRunning && isDevicesResponding) {
            if (measuringU > highestU) {
                highestU = measuringU
                step = 5
            }
            if (measuringIC < lowestI) {
                lowestI = measuringIC
                step = 5
            }
            sleep(500)
        }
        communicationModel.stopObject()
        sleep(3000)
        communicationModel.changeRotation()
        communicationModel.setObjectParams(25 * 100, 380 * 10, 25 * 100)
        communicationModel.startObject()
        while (measuringU * 1.05 < highestU && measuringIC * 0.95 > lowestI && isExperimentRunning) { //Из-за инерции
            if (statusEndsVFD == OMIK_DOWN_END) {
                setCause("Не удалось подобрать резонанс")
            }
            sleep(10)
        }
        communicationModel.stopObject()
        appendOneMessageToLog("Поиск завершен")
        sleep(1000)
    }

    private fun resetOmik() {
        communicationModel.setObjectParams(50 * 100, 380 * 10, 50 * 100)
        if (statusEndsVFD != OMIK_DOWN_END && isDevicesResponding) {
            appendOneMessageToLog("Возвращаем магнитопровод в исходное состояние")
            if (statusVFD != VFD_REVERSE && isDevicesResponding) {
                communicationModel.changeRotation()
            }
            communicationModel.startObject()
            var waitingTime = 30
            while (isExperimentRunning && isDevicesResponding && (waitingTime-- > 0)) {
                sleep(100)
            }
            while (statusEndsVFD != OMIK_DOWN_END && isDevicesResponding) {
                sleep(10)
                if (statusEndsVFD == OMIK_UP_END && isDevicesResponding) {
                    setCause("Омик в верхнем положенении, двигаясь вниз")
                    break
                }
            }
            communicationModel.stopObject()
        }
        if (statusEndsVFD == OMIK_DOWN_END) {
            appendOneMessageToLog("ОМИК в нижнем положении")
        }
        communicationModel.stopObject()
    }

    private fun putUpLatr(voltage: Float, difference: Int) {
        communicationModel.startUpLATRUp(7f, true)
        while (measuringU < voltage - 1000 && measuringU < voltage + 1000 && isExperimentRunning) {
            sleep(10)
        }
        waitingLatrCoarse(voltage)
    }

    private fun waitingLatrCoarse(voltage: Float) {
        while (isExperimentRunning && isDevicesResponding && (measuringU <= voltage - 300 || measuringU > voltage + 300)) {
            if (measuringU <= voltage - 300) {
                communicationModel.startUpLATRUp(440f, false)
            } else if (measuringU > voltage + 300) {
                communicationModel.startUpLATRDown(1f, false)
            }
        }
        communicationModel.stopLATR()
    }

    private fun appendMessageToLog(message: String) {
        Platform.runLater {
            textAreaExperiment1Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message))
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
                OwenPRModel.ТКЗ_ДО_ТРАНСФОРМАТОРА -> {
                    ткзДоТрансформатора = value as Boolean
                    if (ткзДоТрансформатора) {
                        communicationModel.offAllKms()
                        setCause("ткзДоТрансформатора")
                    }
                }
                OwenPRModel.ТКЗ_ОИ -> {
                    ткзОИ = value as Boolean
                    if (ткзОИ) {
                        communicationModel.offAllKms()
                        setCause("ткзОИ")
                    }
                }
                OwenPRModel.ТКЗ_ПОСЛЕ_ТРАНСФОРМАТОРА -> {
                    ткзПослеТрансформатора = value as Boolean
                    if (ткзПослеТрансформатора) {
                        communicationModel.offAllKms()
                        setCause("ткзПослеТрансформатора")
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ДВЕРЕЙ_ШСО -> {
                    контрольДверей = value as Boolean
                    if (контрольДверей) {
                        communicationModel.offAllKms()
                        setCause("контрольДверей")
                    }
                }
                OwenPRModel.КОНТРОЛЬ_ПУСКА -> {
                    контрольПуска = value as Boolean
                    if (!контрольПуска && isControlRubilNeed) {
                        communicationModel.offAllKms()
                        setCause("Сработала защита")
                    }
                }
                OwenPRModel.КОНТРОЛЬ_РУБИЛЬНИКА -> {
                    контрольРубильника = value as Boolean
                    if (!контрольРубильника && isControlRubilNeed) {
                        setCause("Во время испытания отключен рубильник силового питания")
                    }
                }
                OwenPRModel.РУЧНОЙ_РЕЖИМ_С_ПК -> {
                    ручнойРежимСПК = value as Boolean
                    if (!ручнойРежимСПК) {
                        setCause("Для продолжения переключите на Ручной режим с ПК")
                    }
                }
                OwenPRModel.ПЕРЕМЕННОЕ -> {
                    переменное = value as Boolean
                    if (переменное) {
                        textExperiment.text = "ВИУ переменным напряжением"
                        modeOperating = 1
                    }
                }
                OwenPRModel.ПЕРЕМЕННОЕ_С_РЕЗОНАНСОМ -> {
                    резонанс = value as Boolean
                    if (резонанс) {
                        textExperiment.text = "ВИУ резонансное переменным напряжением"
                        modeOperating = 2
                    }
                }
                OwenPRModel.ПОСТОЯННОЕ -> {
                    постоянное = value as Boolean
                    if (постоянное) {
                        textExperiment.text = "ВИУ постоянным напряжением"
                        modeOperating = 3
                    }
                    if (!переменное && !резонанс && !постоянное) {
                        modeOperating = 0
                        textExperiment.text = "ВНИМАНИЕ! ВЫБЕРИТЕ РЕЖИМ ИСПЫТАНИЯ!"
                        communicationModel.приемКоманды_On()
                    }
                    if (isModeOperatingSelected && modeOperatingLast != modeOperating) {
                        setCause("Во время испытания был изменен режим испытания")
                    }

                }
                OwenPRModel.СТАРТ_ТАЙМЕР -> {
                    старт = value as Boolean
                    if (старт) {
                        communicationModel.приемКоманды_Off()
                    }
                }
                OwenPRModel.СТОП_ТАЙМЕР -> {
                    стоп = value as Boolean
                    if (стоп) {
                        communicationModel.приемКоманды_On()
                    }
                }
                OwenPRModel.СТОП_ИСПЫТАНИЯ -> {
                    стопИспытания = value as Boolean
                    if (стопИспытания) {
                        isExperimentRunning = false
                    }
                }
                OwenPRModel.ПОДЪЕМ_НАПРЯЖЕНИЯ -> {
                    подъемНапряжения = value as Boolean
                    if (подъемНапряжения && !уменьшениеНапряжения && isManualNeed) {
                        communicationModel.startUpLATRWithRegulationSpeed(440f, false, 50f, 100f)
                    }
                    if (!подъемНапряжения && !уменьшениеНапряжения && isManualNeed) {
                        communicationModel.stopLATR()
                    }
                }
                OwenPRModel.УМЕНЬШЕНИЕ_НАПРЯЖЕНИЯ -> {
                    уменьшениеНапряжения = value as Boolean
                    if (уменьшениеНапряжения && !подъемНапряжения && isManualNeed) {
                        communicationModel.startUpLATRWithRegulationSpeed(1f, false, 50f, 100f)
                    }
                    if (!уменьшениеНапряжения && !подъемНапряжения && isManualNeed) {
                        communicationModel.stopLATR()
                    }
                }
            }


            PM130_ID -> when (param) {
                PM130Model.RESPONDING_PARAM -> {
                    isParmaResponding = value as Boolean
                    Platform.runLater { deviceStateCirclePM130.fill = if (value) Color.LIME else Color.RED }
                }
                PM130Model.I2_PARAM -> {
                    measuringIB = value as Float * 20
                    val IB = String.format("%.2f", measuringIB)
                    experiment1ManualModel!!.currentB = IB
                    if (measuringIB > 45) {
                        appendMessageToLog("Ток B превышает 45А")
                    }
                }
                PM130Model.I3_PARAM -> {
                    measuringIC = value as Float * 2
                    val IC = String.format("%.2f", measuringIC)
                    experiment1ManualModel!!.currentOI = IC
                    if (measuringIC > 45) {
                        appendMessageToLog("Ток C превышает 45А")
                    }
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
            }

            AVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleAvem.fill = if (value) Color.LIME else Color.RED }
                }
            }

            KILOAVEM_ID -> when (param) {
                AvemVoltmeterModel.RESPONDING_PARAM -> {
                    isKiloAvemResponding = value as Boolean
                    Platform.runLater { deviceStateCircleKiloAvem.fill = if (value) Color.LIME else Color.RED }
                }
                AvemVoltmeterModel.U_RMS_PARAM -> {
                    measuringU = (value as Float) * 1000
                    coef = (measuringU / (measuringULatr / 102)).toDouble()
                    val kiloAvemU = String.format("%.2f", measuringU)
                    experiment1ManualModel!!.voltage = kiloAvemU
                }
            }

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
                    measuringULatr = (value as Float) * 102
                    val uLatr = String.format("%.2f", measuringULatr / 102)
                    experiment1ManualModel!!.voltageARN = uLatr
                }
            }
        }
    }

    private fun checkLatrStatus() {
        when (latrStatus) {
            LATR_STARTED -> {
//                appendOneMessageToLog("Выход ЛАТРа на заданное напряжение")
            }
            LATR_WAITING -> {
                appendOneMessageToLog("Выдерживаем заданное напряжение на ЛАТРе")
            }
            LATR_CONFIG -> {
                appendOneMessageToLog("Режим кофигурации ЛАТР")
            }
            LATR_STOP_RESET -> {
//                appendOneMessageToLog("Стоп/Ресет ЛАТР")
            }
        }
    }

    private fun checkLatrError() {
        when (latrStatus) {
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
//                Log.d("", "Замкнут верхний концевик ОМИКа.")
            }
            OMIK_DOWN_END -> {
//                Log.d("", "Замкнут нижний концевик ОМИКа.")
            }
            OMIK_BOTH_END -> {
                setCause("Замкнуты оба концевика ОМИКа.")
            }
            OMIK_NOONE_END -> {
//                Log.d("", "Оба концевика ОМИКа не замкнуты")
            }
        }
    }

    private fun checkVFDStatus() {
        when (statusVFD) {
            VFD_FORWARD -> {
//                Log.d("", "FORWARD")
            }
            VFD_REVERSE -> {
//                Log.d("", "REVERSE")
            }
        }
    }

    override fun setDialogStage(dialogStage: Stage) {
        this.dialogStage = dialogStage
    }

    override fun isCanceled(): Boolean {
        return isCanceled
    }

    @FXML
    fun handleSaveDot() {

    }
}