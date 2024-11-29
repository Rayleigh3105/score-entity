package org.rayleigh.entity

import jakarta.persistence.*

@Entity
@Table(name = "image")
class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    lateinit var publicId: String

    lateinit var imageUrl: String

}