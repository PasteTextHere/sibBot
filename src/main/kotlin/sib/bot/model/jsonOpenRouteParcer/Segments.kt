package sib.bot.model.jsonOpenRouteParcer

data class Segments (

	val distance : Double,
	val duration : Double,
	val steps : List<Steps>
)