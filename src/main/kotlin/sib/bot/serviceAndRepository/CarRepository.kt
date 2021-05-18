package sib.bot.serviceAndRepository

import org.springframework.data.repository.CrudRepository
import sib.bot.model.Car

interface CarRepository:CrudRepository<Car, Int> {
}