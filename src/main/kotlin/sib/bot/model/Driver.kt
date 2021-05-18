package sib.bot.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Drivers")
data class Driver(
        @Id
        @Column(name = "chatID", length = 100)
        val chatId: String = "",

        @Column(name = "lastName", length = 100)
        val lName: String = "",

        @Column(name = "name", length = 100)
        val name: String = "",

        @Column(name = "lastCar", length = 100)
        var carNumber: Int = 0
)