package org.rayleigh.entity

import jakarta.persistence.*

@Entity
@Table(name = "groups")
data class Group(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    var name: String = "",
    var imageUrl: String? = null,
    var totalScore: Int = 0


)