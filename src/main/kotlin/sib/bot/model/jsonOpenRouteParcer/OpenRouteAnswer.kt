package sib.bot.model.jsonOpenRouteParcer

data class OpenRouteAnswer (

	val routes : List<Routes>,
	val bbox : List<Double>,
	val metadata : Metadata
)