package org.rayleigh.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "groups")
class Group(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = 0,

    @OneToMany(mappedBy = "group", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    var scores: MutableList<Score> = mutableListOf(),

    var name: String = "",

    @OneToOne(orphanRemoval = false)
    var image: Image? = null,
)