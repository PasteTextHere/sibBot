package sib.bot.serviceAndRepository

import org.springframework.data.repository.CrudRepository
import sib.bot.model.Driver

interface DriverRepository : CrudRepository<Driver, String> {
}