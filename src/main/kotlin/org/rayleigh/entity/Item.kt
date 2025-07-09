package org.rayleigh.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "items")
class Item(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    var name: String = "",

    @Column(precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    var quantity: Int = 0,

    var color: String = "",

    @field:ManyToMany(fetch = FetchType.EAGER)
    @field:JoinTable(
        name = "item_areas",
        joinColumns = [JoinColumn(name = "item_id")],
        inverseJoinColumns = [JoinColumn(name = "area_id")]
    )
    var areas: MutableList<Area> = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "deposit_id")
    var deposit: Deposit? = null

)