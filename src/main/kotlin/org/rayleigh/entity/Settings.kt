package org.rayleigh.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "settings")
data class Settings(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    var endTime: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(0, LocalDateTime.now())
}