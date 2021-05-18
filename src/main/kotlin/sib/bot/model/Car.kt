package sib.bot.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Cars")
data class Car(
        @Id
        @Column(name = "number", length = 100)
        val number: Int = 0,

        @Column(name = "fuelConsumption", length = 100)
        val fuelConsumption: Double = 0.0,

        @Column(name = "lastFuel", length = 100)
        var lastFuel: Double = 0.0,

        @Column(name = "odo", length = 100)
        var lastOdo: Int = 0
)
