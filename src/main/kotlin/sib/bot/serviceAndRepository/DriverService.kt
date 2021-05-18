package sib.bot.serviceAndRepository

import org.springframework.stereotype.Service
import sib.bot.model.Driver

@Service
class DriverService(private val driverRepository: DriverRepository) {

    fun driverExist(chatId: String): Boolean {
        return driverRepository.existsById(chatId)
    }

    fun getDriver(chatId: String) = driverRepository.findById(chatId)

    fun updDriverCar(chatId: String, car: Int): Driver {
        val driver = driverRepository.findById(chatId).get()
        driverRepository.deleteById(chatId)
        driver.carNumber = car
        return driverRepository.save(driver)
    }

    fun addDriver(driver: Driver) = driverRepository.save(driver)

    fun getAllDrivers(): List<String> {
        return driverRepository.findAll().map { "${it.lName} ${it.name} последний раз ездил на ${it.carNumber}" }
    }

    fun deleteDriver(lastName: String): String {
        val deletedDriver = driverRepository.findAll().find { it.lName == lastName }
        if (deletedDriver is Driver){
            driverRepository.delete(deletedDriver)
            return "Водитель ${deletedDriver.lName} ${deletedDriver.name} удален"
        } else return "Такой водитель не найден"
    }
}