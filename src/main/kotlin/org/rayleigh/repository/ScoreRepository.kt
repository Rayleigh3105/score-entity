package org.rayleigh.repository

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.rayleigh.entity.Group
import org.rayleigh.entity.Score

@ApplicationScoped
class ScoreRepository : PanacheRepository<Score>