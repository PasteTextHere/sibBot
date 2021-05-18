package sib.bot.model.jsonOpenRouteParcer

data class Query (

	val coordinates : List<List<Double>>,
	val profile : String,
	val format : String
)