package sib.bot.model.jsonOpenRouteParcer

data class Routes (

	val summary : Summary,
	val segments : List<Segments>,
	val bbox : List<Double>,
	val geometry : String,
	val way_points : List<Int>
)