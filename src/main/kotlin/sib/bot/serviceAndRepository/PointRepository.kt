package sib.bot.serviceAndRepository

import org.springframework.data.repository.CrudRepository
import sib.bot.model.Point

interface PointRepository: CrudRepository<Point, String> {
}