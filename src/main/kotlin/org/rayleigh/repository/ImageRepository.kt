package org.rayleigh.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.rayleigh.entity.Image

@ApplicationScoped
class ImageRepository : PanacheRepository<Image> {


}