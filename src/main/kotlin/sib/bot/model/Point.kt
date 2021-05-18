package sib.bot.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Points")
data class Point(
    @Id
    @Column(name = "name", length = 100)
    val name: String = "",

    @Column(name = "altitude", length = 100)
    val altitude: Double = 0.0,

    @Column(name = "longitude", length = 100)
    val longitude: Double = 0.0
) {
    override fun toString(): String {
        return "[$longitude, $altitude]"
    }
    @JvmName("getName1")
    private fun getName(): String {
        return "$name"
    }
}