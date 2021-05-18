package sib.bot.serviceAndRepository

import org.springframework.stereotype.Service
import sib.bot.model.Point

@Service
class PointService(private val pointRepository: PointRepository) {

    fun pointExist(name: String): Boolean {
        return pointRepository.existsById(name)
    }

    fun getPoint(name: String): Point {
        return pointRepository.findById(name).get()
    }

    fun getAllPoints(): List<String> {
        return pointRepository.findAll().map { it.name }
    }

    fun deletePoint(point: String): String {
        val deletedPoint = pointRepository.findById(point)
        if (deletedPoint.isPresent) {
            pointRepository.delete(deletedPoint.get())
            return "Точка ${deletedPoint.get().name} удалена"
        } else return "Нет такой точки"
    }

    fun addPoints(pointsList: Iterable<Point>) {
        pointRepository.saveAll(pointsList)
    }
}