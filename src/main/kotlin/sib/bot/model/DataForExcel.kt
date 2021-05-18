package sib.bot.model

import javax.persistence.*

data class DataForExcel(
    val date: Long,
    val carNumber: Int,
    val driver: String,
    val odoStart: Int,
    val odoFinish: Int,
    val odoTotal: Int,
    val fuelAdded: String,
    val fuelStart: String,
    val fuelFinish: String
)