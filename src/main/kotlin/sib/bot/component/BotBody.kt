package sib.bot.component

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Document
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import sib.bot.model.Car
import sib.bot.model.DataForExcel
import sib.bot.model.Driver
import sib.bot.model.Point
import sib.bot.serviceAndRepository.*
import java.io.File
import java.text.DecimalFormat


@Component
class BotBody(
    private val driverService: DriverService,
    private val pointService: PointService,
    private val carService: CarService,
    private val distanceService: DistanceService,
    private val excelService: ExcelService
) : TelegramLongPollingBot() {

    private val askMapReg = hashMapOf(
        Pair(0, "Введите фамилию"),
        Pair(1, "Введите имя"),
        Pair(2, "Введите номер авто")
    )
    private val askMapNormal = hashMapOf(
        Pair(0, "Введите конечный пробег"),
        Pair(1, "Введите заправленый бензин"),
        Pair(2, "Введите посещенный точки")
    )
    private var chatIdAndDriverData = mutableMapOf<String, MutableList<String>>()
    private var chatIdCount = mutableMapOf<String, Int>()
    private var chatIdAndOdo = mutableMapOf<String, Int>()
    private var chatIdAndFuel = mutableMapOf<String, Double>()
    private var chatIdAndDistance = mutableMapOf<String, MutableList<Int>>()
    private var chatIdAndPoints = mutableMapOf<String, MutableList<Point>>()
    private var userRegChatId = mutableMapOf<String, Int>()
    private var userAdmin = mutableMapOf<String, Boolean>()

    //для админских команд
    var nextWillBeFile = false
    var fileChanger = 0 //1-точки 2-автомобиль
    var deleter = 0 //1-водитель 2-автомобиль 3-точка
    private var messageText = ""

    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val chatId = update.message!!.chatId.toString()
            if (userAdmin[chatId] == true) {
                if (update.message.hasDocument() && nextWillBeFile) {
                    nextWillBeFile = false
                    when (fileChanger) {
                        1 -> {
                            fileChanger = 0
                            val gotPointsList = downloadDocument(getFilePath(getDocument(update)!!)!!)!!
                            val pointsList = mutableListOf<Point>()
                            gotPointsList.forEachLine {
                                try {
                                    pointsList.add(
                                        Point(
                                            it.split(" ")[0],
                                            it.split(" ")[1].toDouble(),
                                            it.split(" ")[2].toDouble()
                                        )
                                    )
                                } catch (e: NumberFormatException) {
                                    messageText = "$it неправильный формат"
                                    sendAnswer(chatId, messageText)
                                } catch (e: IndexOutOfBoundsException) {
                                }
                            }
                            messageText = "Успешно добавлено ${pointsList.size} точек"
                            pointService.addPoints(pointsList)
                            adminMenu(chatId, messageText)
                        } //добавление списка точек
                        2 -> {
                            fileChanger = 0
                            val gotCarsList = downloadDocument(getFilePath(getDocument(update)!!)!!)!!
                            val carsList = mutableListOf<Car>()
                            gotCarsList.forEachLine {
                                try {
                                    carsList.add(
                                        Car(
                                            it.split(" ")[0].toInt(),
                                            it.split(" ")[1].toDouble(),
                                            it.split(" ")[2].toDouble(),
                                            it.split(" ")[3].toInt()
                                        )
                                    )
                                } catch (e: NumberFormatException) {
                                    messageText = "$it неправильный формат данных"
                                    sendAnswer(chatId, messageText)
                                } catch (e: IndexOutOfBoundsException) {
                                    messageText = "$it неправильный формат ввода"
                                    sendAnswer(chatId, messageText)
                                }
                            }
                            messageText = "Успешно добавлено ${carsList.size} точек"
                            carService.addCars(carsList)
                            adminMenu(chatId, messageText)
                        } //добавление списка автомобилей
                    }
                }
                if (update.message.hasText()) {
                    if (deleter > 0) {
                        when (deleter) {
                            1 -> {
                                messageText = driverService.deleteDriver(update.message.text)
                                deleter = 0
                                adminMenu(chatId, messageText)
                            } //удалить водителя
                            2 -> {
                                try {
                                    messageText = carService.deleteCar(update.message.text.toInt())
                                } catch (e: NumberFormatException) {
                                    messageText = "Неправильный формат номера авто"
                                }
                                deleter = 0
                                adminMenu(chatId, messageText)

                            } //удалить автомобиль
                            3 -> {
                                messageText = pointService.deletePoint(update.message.text.uppercase())
                                deleter = 0
                                adminMenu(chatId, messageText)
                            } //удалить точку
                        }
                    } else
                        when (update.message.text) {
                            "Вернуться в режим пользователя" -> {
                                userAdmin[chatId] = false
                                sendRerunAnswer(chatId, "Теперь ты не админ")
                            }
                            "Инфо по машинам" -> {
                                messageText = carService.getAllCarsFullInfo().joinToString("\n")
                                adminMenu(chatId, messageText)
                            }
                            "Список водителей" -> {
                                messageText = driverService.getAllDrivers().joinToString("\n")
                                adminMenu(chatId, messageText)
                            }
                            "Список точек" -> {
                                messageText = pointService.getAllPoints().joinToString("\n")
                                adminMenu(chatId, messageText)
                            }
                            "Выгрузить Excel файл" -> {
                                sendDocument(chatId, File("ExcelFile.xls"))
                                adminMenu(chatId, "")
                            }
                            "Загрузить список точек" -> {
                                nextWillBeFile = true
                                messageText = "Отправь мне список точек в txt файле\n" +
                                        "В формате:\n" +
                                        "(название) (широта) (долгота)\n" +
                                        "пример:\n" +
                                        "MC091 55.701234 37.605085\n" +
                                        "MC109 55.688953 37.603884\n" +
                                        "PS: Координаты лучше брать с https://maps.openrouteservice.org"
                                sendAnswer(chatId, messageText)
                            }
                            "Добавить авто" -> {
                                nextWillBeFile = true
                                messageText = "Отправь мне список автомобилей в txt файле\n" +
                                        "В формате:\n" +
                                        "(номер) (расход) (остаток топлива) (пробег)\n" +
                                        "пример:\n" +
                                        "123 9.72 15.43 2385"
                                sendAnswer(chatId, messageText)
                            }
                            "Удалить водителя" -> {
                                messageText = "Введите фамилию водителя которого нужно удалить с учетом регистра:"
                                deleter = 1
                                sendAnswer(chatId, messageText)
                            }
                            "Удалить автомобиль" -> {
                                messageText = "Введите номер авто который нужно удалить:"
                                deleter = 2
                                sendAnswer(chatId, messageText)
                            }
                            "Удалить точку" -> {
                                messageText = "Введите название точки которую нужно удалить"
                                deleter = 3
                                sendAnswer(chatId, messageText)
                            }
                            else -> adminMenu(chatId, "Выбери команду")
                        }
                }
            } else {
                if (update.message.hasText()) {
                    if (chatIdCount[chatId] == null) {
                        chatIdCount[chatId] = 0
                        userRegChatId[chatId] = 4
                        chatIdAndDistance[chatId] = mutableListOf()
                        chatIdAndPoints[chatId] = mutableListOf()
                        chatIdAndFuel[chatId] = 0.0
                        chatIdAndOdo[chatId] = 0
                        } //инициализации при перезапуске
                    if ((update.message.text == "/start") && !driverService.driverExist(chatId)) {
                        userRegChatId[chatId] = 0
                        chatIdAndDriverData[chatId] = mutableListOf()

                    }
                    try {
                        when (update.message.text) {
                            "1" -> {
                                try {
                                    excelService.createExcel()
                                    sendAnswer(chatId, "Создан")
                                } catch (e: Exception) {
                                    sendRerunAnswer(chatId, "Не создан\n $e")
                                }
                            }
                            "3" -> {
                                sendDocument(chatId, File("ExcelFile.xls"))
                            }
                            "Рассчитать" -> {
                                messageText = try {
                                    createResult(chatId)
                                } catch (e: Exception) {
                                    throw Exception("v result")
                                }
                                chatIdAndPoints[chatId]?.clear()
                                chatIdAndDistance[chatId] = mutableListOf()
                                chatIdAndPoints[chatId] = mutableListOf()
                                chatIdAndFuel[chatId] = 0.0
                                chatIdAndOdo[chatId] = 0
                                chatIdCount[chatId] = 1
                                sendRerunAnswer(chatId, messageText)
                            }
                            "Заново" -> {
                                messageText = "${askMapNormal[0]}"
                                chatIdAndPoints[chatId]?.clear()
                                userRegChatId[chatId] = 4
                                chatIdAndDistance[chatId] = mutableListOf()
                                chatIdAndPoints[chatId] = mutableListOf()
                                chatIdAndFuel[chatId] = 0.0
                                chatIdAndOdo[chatId] = 0
                                chatIdCount[chatId] = 1
                                sendAnswer(chatId, messageText)
                            }
                            "Изменить номер авто" -> {
                                chatIdCount[chatId] = 1000
                                messageText = "Введите номер нового авто:\n" +
                                        carService.getAllCarNumbers().joinToString(separator = "\n")
                                sendAnswer(chatId, messageText)
                            }
                            "ADMIN" -> {
                                userAdmin[chatId] = true
                                messageText = "теперь ты админ"
                                adminMenu(chatId, messageText)
                            }
                            else -> {
                                if (!driverService.driverExist(chatId)) {
                                    when (userRegChatId[chatId]) {
                                        0 -> {
                                            messageText = "${askMapReg[0]}"
                                            userRegChatId[chatId] = 1
                                            sendAnswer(chatId, messageText)
                                        } //запрос фамилии
                                        1 -> {
                                            userRegChatId[chatId] = 2
                                            chatIdAndDriverData[chatId]?.add(0, update.message.text)
                                            messageText = "${askMapReg[1]}"
                                            sendAnswer(chatId, messageText)
                                        } //ввод фамилии, запрос имени
                                        2 -> {
                                            messageText = "${askMapReg[2]}"
                                            userRegChatId[chatId] = 3
                                            chatIdAndDriverData[chatId]?.add(1, update.message.text)
                                            sendAnswer(chatId, messageText)
                                        } //ввод имени, запрос номера авто
                                        3 -> try {
                                            if (carService.carExist(update.message.text.toInt())) {
                                                chatIdAndDriverData[chatId]?.add(2, update.message.text)
                                            } else throw Exception("vvod avto reg")
                                            userRegChatId[chatId] = 4
                                            chatIdAndDistance[chatId] = mutableListOf()
                                            chatIdAndPoints[chatId] = mutableListOf()
                                            userAdmin[chatId] = false
                                            chatIdAndFuel[chatId] = 0.0
                                            chatIdAndOdo[chatId] = 0
                                            chatIdCount[chatId] = 0
                                            driverService.addDriver(
                                                Driver(
                                                    chatId,
                                                    chatIdAndDriverData[chatId]!![0],
                                                    chatIdAndDriverData[chatId]!![1],
                                                    chatIdAndDriverData[chatId]!![2].toInt()
                                                )
                                            )
                                        } catch (e: Exception) {
                                            messageText = "Введите правильный номер авто из существующих:\n" +
                                                    carService.getAllCarNumbers().joinToString(separator = "\n")
                                            sendAnswer(chatId, messageText)
                                            messageText = "${askMapReg[2]}"
                                        } //ввод номера авто, создание водителя в бд
                                    }
                                }
                                if (driverService.driverExist(chatId)) {
                                    when (chatIdCount[chatId]) {
                                        1000 -> {
                                            messageText = "${askMapNormal[0]}"
                                            chatIdCount[chatId] = 1
                                            driverService.updDriverCar(chatId, update.message.text.toInt())
                                        } //ввод нового авто, запрос пробега
                                        0 -> {
                                            messageText = "${askMapNormal[0]}"
                                            chatIdCount[chatId] = 1
                                        } //запрос пробега
                                        1 -> {
                                            messageText = "${askMapNormal[1]}"
                                            try {
                                                chatIdAndOdo[chatId] = update.message.text.toInt()
                                                chatIdCount[chatId] = 2
                                            } catch (e: NumberFormatException) {
                                                chatIdCount[chatId] = 1
                                                messageText = "Введите нормальное число"
                                                sendAnswer(chatId, messageText)
                                                messageText = "${askMapNormal[0]}"
                                            }
                                        } //ввод пробега, запрос бензина
                                        2 -> {
                                            messageText = "${askMapNormal[2]}"
                                            chatIdCount[chatId] = 3
                                            try {
                                                chatIdAndFuel[chatId] = update.message.text.toDouble()
                                            } catch (e: Exception) {
                                                println(e)
                                                chatIdCount[chatId] = 2
                                                messageText = "Введите нормальное число в формате 12.34"
                                                sendAnswer(chatId, messageText)
                                                messageText = "${askMapNormal[1]}"
                                            }
                                        } //ввод бензина, запрос точки
                                        3 -> {
                                            messageText = "Еще точку:"
                                            try {
                                                if (pointService.pointExist(update.message.text.uppercase()))
                                                    chatIdAndPoints[chatId]?.add(pointService.getPoint(update.message.text.uppercase()))
                                                else throw Exception()
                                            } catch (e: Exception) {
                                                messageText = "Нет такой точки, либо неправильный формат"
                                                sendAnswer(chatId, messageText)
                                                messageText = "Точка:"
                                            }
                                        } //ввод точек
                                        else -> messageText = "error after reboot"
                                    }
                                    if (messageText == "Еще точку:")
                                        sendAnswerWithButtons(chatId, messageText)
                                    else {
                                        sendAnswer(chatId, messageText)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println(e)
                        sendRerunAnswer(chatId, "smth going wrong")
                    }
                } else {
                    sendAnswer(chatId, "Я понимаю только текст")
                    sendRerunAnswer(chatId, messageText)
                }
            }
        }
    }

    private fun sendRerunAnswer(chatId: String, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.parseMode = "Markdown"
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Заново")
            )
        )
        execute(responseMessage)
    }

    private fun sendAnswer(chatId: String, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.replyMarkup = ReplyKeyboardRemove(true)
        execute(responseMessage)
    }

    private fun sendAnswerWithButtons(chatId: String, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.parseMode = "Markdown"
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Рассчитать", "Заново", "Изменить номер авто")
            )
        )
        execute(responseMessage)
    }

    private fun adminMenu(chatId: String, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.parseMode = "Markdown"
        responseMessage.replyMarkup = getReplyMarkup(
            listOf(
                listOf("Инфо по машинам", "Список водителей", "Список точек"),
                listOf("Выгрузить Excel файл", "Загрузить список точек"/*, "Добавить авто"*/),
                listOf("Удалить водителя", "Удалить автомобиль", "Удалить точку"),
                listOf("Вернуться в режим пользователя")
            )
        )
        execute(responseMessage)
    }

    private fun getReplyMarkup(allButtons: List<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }

    private fun createResult(chatId: String): String {
        val car = driverService.getDriver(chatId).get().carNumber
        //расчет расхода
        val fuelCalc = (((chatIdAndOdo[chatId]!! - carService.getCarLastOdo(car)) *
                carService.getCarConsumption(car)) / 100)
        //расход фактический
        val fuelCalcFact = (if ((carService.getCarLastFuel(car) + chatIdAndFuel[chatId]!! - fuelCalc) < 0)
            carService.getCarLastFuel(car) + chatIdAndFuel[chatId]!!
        else
            fuelCalc)
        //остаток топлива
        val fuelResult = (if ((carService.getCarLastFuel(car) + chatIdAndFuel[chatId]!! - fuelCalc) < 0)
            0.0
        else
            carService.getCarLastFuel(car) - fuelCalc + chatIdAndFuel[chatId]!!)
        //экономия
        val fuelEconomy = (if ((carService.getCarLastFuel(car) + chatIdAndFuel[chatId]!! - fuelCalc) < 0)
            0.0 - (carService.getCarLastFuel(car) + chatIdAndFuel[chatId]!! - fuelCalc)
        else
            0.0)
        //строка расчетов
        val fuelCalculating = "${chatIdAndOdo[chatId]!! - carService.getCarLastOdo(car)} умножить на " +
                "${carService.getCarConsumption(car)} / 100 = " +
                DecimalFormat("##.##").format(fuelCalc)

        distanceService.getDistance(
            chatIdAndPoints[chatId]!!,
            chatIdAndDistance[chatId]!!,
            carService.getCarLastOdo(car)
        )
        //таблица точек с километражем
        val distanceAdded = mutableListOf<Int>()
        distanceAdded.add(chatIdAndDistance[chatId]!![0] + chatIdAndDistance[chatId]!![1])
        for (a in 0 until chatIdAndDistance[chatId]!!.size - 2) {
            distanceAdded.add(distanceAdded[a] + chatIdAndDistance[chatId]!![a + 2])
        }
        val pointsWithDistance = chatIdAndPoints[chatId]
            ?.zipWithNext { a, b -> "${Pair(a.name, b.name)}" }
            ?.zip(distanceAdded) { points, dist ->
                "$points ------- $dist"
            }

        val result = "Номер авто: $car\n" +
                "Начальный пробег: ${carService.getCarLastOdo(car)}\n" +
                "Конечный пробег: ${chatIdAndOdo[chatId]}\n" +
                "Заправленый бензин: ${DecimalFormat("##.##").format(chatIdAndFuel[chatId])}\n" +
                "Бензин до начала: ${DecimalFormat("##.##").format(carService.getCarLastFuel(car))}\n" +
                "Бензин в конце: ${DecimalFormat("##.##").format(fuelResult)}\n" +
                "Экономия: ${DecimalFormat("##.##").format(fuelEconomy)}\n" +
                pointsWithDistance?.joinToString(separator = "\n") + "\n" +
                "Расход рассчитаный: ${DecimalFormat("##.##").format(fuelCalc)}\n" +
                "Расход фактический: ${DecimalFormat("##.##").format(fuelCalcFact)}\n" +
                "Общий пробег: ${chatIdAndOdo[chatId]!! - carService.getCarLastOdo(car)}\n" +
                fuelCalculating
        val driver = driverService.getDriver(chatId).get()
        val data = DataForExcel(
            date = System.currentTimeMillis(),
            carNumber = car,
            driver = "${driver.lName} ${driver.name}",
            odoStart = carService.getCarLastOdo(car),
            odoFinish = chatIdAndOdo[chatId]!!,
            odoTotal = chatIdAndOdo[chatId]!! - carService.getCarLastOdo(car),
            fuelAdded = DecimalFormat("##.##").format(chatIdAndFuel[chatId]),
            fuelFinish = DecimalFormat("##.##").format(fuelResult),
            fuelStart = DecimalFormat("##.##").format(carService.getCarLastFuel(car))
        )
        excelService.editExcelFile(data)
        carService.setCarLastFuel(car, DecimalFormat("##.##").format(fuelResult).replace(',', '.').toDouble())
        carService.setCarLastOdo(car, chatIdAndOdo[chatId]!!)
        return result
    }

    private fun getDocument(update: Update): Document? {
        return if (update.hasMessage() && update.message.hasDocument()) {
            update.message.document
        } else null
    }

    private fun sendDocument(chatId: String, file: File) {
        execute(SendDocument(chatId, InputFile(file)))
    }

    private fun getFilePath(document: Document): String? {
        val getFile = GetFile(document.fileId)
        val file = try {
            execute(getFile)
        } catch (e: TelegramApiException) {
            println("get file path ошибка, $e")
            return null
        }
        return file.filePath
    }

    private fun downloadDocument(filePath: String): File? {
        return try {
            downloadFile(filePath)
        } catch (e: TelegramApiException) {
            println("download document error, $e")
            null
        }
    }
}