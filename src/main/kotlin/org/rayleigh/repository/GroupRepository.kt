package org.rayleigh.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.rayleigh.entity.Group

@ApplicationScoped
class GroupRepository : PanacheRepository<Group>