package org.rayleigh.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.rayleigh.entity.Area
import org.rayleigh.entity.Group
import org.rayleigh.entity.Item

@ApplicationScoped
class AreaRepository : PanacheRepository<Area> {

}