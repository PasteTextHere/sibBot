package sib.bot.serviceAndRepository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import sib.bot.model.Point
import sib.bot.model.jsonOpenRouteParcer.OpenRouteAnswer
import kotlin.math.roundToInt

@Service
class DistanceService(
    private val pointService: PointService
) {
    @Value(("\${openRoute.token}"))
    private val openRouteToken = ""

    private val webClientOpenRoute: WebClient =
        WebClient.create("https://api.openrouteservice.org")


    fun getDistance(points: MutableList<Point>, segmentsList: MutableList<Int>, startOdo: Int) {
        points.add(0, pointService.getPoint("Office"))
        points.add(points.lastIndex + 1, pointService.getPoint("Office"))

        val body = BodyInserters.fromValue("{\"coordinates\":${points}}")
        val route = webClientOpenRoute
            .post()
            .uri("/v2/directions/driving-car")
            .header("Authorization", openRouteToken)
            .header("Accept", "application/json; charset=utf-8")
            .header("Content-Type", "application/json; charset=utf-8")
            .body(body)
            .retrieve()
            .bodyToMono(OpenRouteAnswer::class.java)
            .block()
        route!!.routes[0].segments.forEach{segmentsList.add((it.distance/1000).roundToInt())}
        segmentsList.add(0, startOdo)
    }
}