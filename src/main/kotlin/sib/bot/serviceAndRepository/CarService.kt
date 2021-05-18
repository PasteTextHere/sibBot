package sib.bot.serviceAndRepository

import org.springframework.stereotype.Service
import sib.bot.model.Car
import sib.bot.model.Driver

@Service
class CarService(private val carRepository: CarRepository) {

    fun carExist(carNumber: Int):Boolean {
        return carRepository.existsById(carNumber)
    }

    fun getCarConsumption(carNumber: Int): Double {
        return carRepository.findById(carNumber).get().fuelConsumption
    }

    fun getCarLastOdo(carNumber: Int): Int {
        return carRepository.findById(carNumber).get().lastOdo
    }

    fun getCarLastFuel(carNumber: Int): Double {
        return carRepository.findById(carNumber).get().lastFuel
    }

    fun setCarLastOdo(carNumber: Int, odo: Int): Car {
        val car = carRepository.findById(carNumber).get()
        carRepository.deleteById(carNumber)
        car.lastOdo = odo
        return carRepository.save(car)
    }

    fun setCarLastFuel(carNumber: Int, fuel: Double): Car {
        val car = carRepository.findById(carNumber).get()
        carRepository.deleteById(carNumber)
        car.lastFuel = fuel
        return carRepository.save(car)
    }

    fun getAllCarNumbers(): List<Int> {
        return carRepository.findAll().map { a -> a.number }
    }

    fun getAllCarsFullInfo(): List<String> {
        return carRepository.findAll().map { "${it.number} - ${it.fuelConsumption} - ${it.lastFuel} - ${it.lastOdo}"}
    }

    fun deleteCar(car: Int): String {
        val deletedCar = carRepository.findById(car).get()
        if (carExist(car)) {
            carRepository.delete(deletedCar)
            return "Автомобиль ${deletedCar.number} удален"
        } else return "Нет такого автомобиля"
    }

    fun addCars(carsList: Iterable<Car>) {
        carRepository.saveAll(carsList)
    }
}