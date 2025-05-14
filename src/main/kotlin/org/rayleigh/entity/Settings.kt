package org.rayleigh.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "settings")
data class Settings(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    var endTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "price", length = 10000)
    var price: String = "",
) {
    constructor() : this(0, LocalDateTime.now())
}