package org.rayleigh.entity

import jakarta.persistence.*

@Entity
@Table(name = "items")
class Item(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    var name: String = "",

    @OneToOne(orphanRemoval = false)
    var image: Image? = null,

    var scoreValue: Int = 0
)