package sib.bot.model.jsonOpenRouteParcer

data class Steps (

	val distance : Double,
	val duration : Double,
	val type : Int,
	val instruction : String,
	val name : String,
	val way_points : List<Int>
)